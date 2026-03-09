package com.expense.tracker.handler;

import com.google.gson.Gson;
import com.expense.tracker.dao.ExpenseDao;
import com.expense.tracker.model.ExpenseEntry;
import com.expense.tracker.model.User;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseHandler {
    private static final Gson gson = new Gson();
    private final ExpenseDao expenseDao = new ExpenseDao();

    public void list(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        String fromStr = ctx.queryParam("from");
        String toStr = ctx.queryParam("to");
        String category = ctx.queryParam("category");
        LocalDate from = fromStr != null && !fromStr.isBlank() ? LocalDate.parse(fromStr) : null;
        LocalDate to = toStr != null && !toStr.isBlank() ? LocalDate.parse(toStr) : null;
        List<ExpenseEntry> list = expenseDao.findByUserAndFilters(user.getId(), from, to, category);
        ctx.json(list.stream().map(this::entryResponse).collect(Collectors.toList()));
    }

    public void getOne(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        String id = ctx.pathParam("id");
        var opt = expenseDao.findByIdAndUser(id, user.getId());
        if (opt.isEmpty()) {
            ctx.status(404).json(Map.of("error", "Expense not found"));
            return;
        }
        ctx.json(entryResponse(opt.get()));
    }

    public void create(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            if (body == null) body = new HashMap<>();
            ExpenseEntry e = new ExpenseEntry();
            e.setUserId(user.getId());
            e.setCategory(required(body, "category"));
            e.setAmount(new BigDecimal(required(body, "amount").toString()));
            e.setCurrency(body.containsKey("currency") && body.get("currency") != null ? body.get("currency").toString() : "INR");
            e.setEntryDate(LocalDate.parse(required(body, "date").toString()));
            e.setNote(body.containsKey("note") && body.get("note") != null ? body.get("note").toString() : null);
            e.setLoanName(body.containsKey("loanName") && body.get("loanName") != null ? body.get("loanName").toString() : null);
            ExpenseEntry created = expenseDao.insert(e);
            ctx.status(201).json(entryResponse(created));
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(Map.of("error", ex.getMessage()));
        }
    }

    public void update(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            if (body == null) body = new HashMap<>();
            String category = required(body, "category");
            BigDecimal amount = new BigDecimal(required(body, "amount").toString());
            String currency = body.containsKey("currency") && body.get("currency") != null ? body.get("currency").toString() : "INR";
            LocalDate entryDate = LocalDate.parse(required(body, "date").toString());
            String note = body.containsKey("note") && body.get("note") != null ? body.get("note").toString() : null;
            String loanName = body.containsKey("loanName") && body.get("loanName") != null ? body.get("loanName").toString() : null;
            var updated = expenseDao.update(id, user.getId(), category, amount, currency, entryDate, note, loanName);
            if (updated.isEmpty()) {
                ctx.status(404).json(Map.of("error", "Expense not found"));
                return;
            }
            ctx.json(entryResponse(updated.get()));
        } catch (IllegalArgumentException ex) {
            ctx.status(400).json(Map.of("error", ex.getMessage()));
        }
    }

    public void delete(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        String id = ctx.pathParam("id");
        if (!expenseDao.delete(id, user.getId())) {
            ctx.status(404).json(Map.of("error", "Expense not found"));
            return;
        }
        ctx.status(204);
    }

    public void summary(Context ctx) {
        User user = ctx.attribute("user");
        if (user == null) {
            ctx.status(401).json(Map.of("error", "Unauthorized"));
            return;
        }
        String month = ctx.queryParam("month"); // yyyy-MM
        LocalDate from;
        LocalDate to;
        if (month != null && !month.isBlank()) {
            from = LocalDate.parse(month + "-01");
            to = from.withDayOfMonth(from.lengthOfMonth());
        } else {
            from = LocalDate.now().withDayOfMonth(1);
            to = LocalDate.now();
        }
        BigDecimal savings = expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.SAVINGS, from, to);
        BigDecimal loans = expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.LOAN_PERSONAL, from, to)
                .add(expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.LOAN_OFFICE, from, to));
        BigDecimal expenses = expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.DAILY, from, to)
                .add(expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.HOME, from, to))
                .add(expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.COSMETICS, from, to))
                .add(expenseDao.sumByUserAndCategoryAndDateRange(user.getId(), ExpenseEntry.TRIP, from, to));
        Map<String, Object> m = new HashMap<>();
        m.put("savings", savings);
        m.put("loans", loans);
        m.put("expenses", expenses);
        m.put("from", from.toString());
        m.put("to", to.toString());
        ctx.json(m);
    }

    private String required(Map<String, Object> body, String key) {
        if (!body.containsKey(key) || body.get(key) == null || body.get(key).toString().isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return body.get(key).toString();
    }

    private Map<String, Object> entryResponse(ExpenseEntry e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("category", e.getCategory());
        m.put("amount", e.getAmount());
        m.put("currency", e.getCurrency());
        m.put("date", e.getEntryDate() != null ? e.getEntryDate().toString() : null);
        m.put("note", e.getNote());
        m.put("loanName", e.getLoanName());
        m.put("createdAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        return m;
    }
}
