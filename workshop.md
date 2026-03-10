# GitHub Copilot Workshop — Ask, Plan, Agent, Instructions, Agents & Skills

**Project**: ExpenseTracker

---

## Prerequisites

- VS Code with GitHub Copilot extension (Chat enabled)
- Access to GitHub Copilot (Individual, Business, or Enterprise)
- Project cloned locally
- No database or external services needed for the workshop tasks

---

## Quick Reference

| Module | Topic             | Time   | Key Takeaway                                    |
| ------ | ----------------- | ------ | ----------------------------------------------- |
| 1      | **Ask Mode**      | 10 min | Explore & understand code conversationally      |
| 2      | **Plan Mode**     | 10 min | Research-first planning before implementation   |
| 3      | **Agent Mode**    | 10 min | Autonomous multi-file implementation            |
| 4      | **Instructions**  | 10 min | Project-wide convention enforcement             |
| 5      | **Custom Agents** | 15 min | Domain-specific persistent expertise            |
| 6      | **Skills**        | 10 min | Reusable prompt templates for consistent output |

---

## Project Overview

ExpenseTracker is a full-stack app with:

- **Backend**: Java 11, Javalin, raw JDBC (MySQL), JWT auth, Google OAuth
- **Frontend**: React 18, Vite, plain CSS, Google Identity Services
- **Features**: Expense CRUD, monthly dashboard summary, admin panel, login audit trail
- **Architecture**: Handler → DAO → Model (no DI framework, no ORM)

All workshop tasks are **code-only** — no external services, no new dependencies, no database required. Participants work with the existing source code.

---

## Module 1 — Ask Mode

### Task: Explore & Understand the Codebase

**Objective**: Use Copilot Ask mode to onboard onto an unfamiliar codebase and discover issues.

**Steps**:

1. Open the project in VS Code with GitHub Copilot enabled.
2. Open **Copilot Chat** and set it to **Ask mode**.
3. Ask Copilot to explain the authentication flow. Use `#file` or `add context` references to scope the question:

   ```
   #file:AuthHandler.java #file:GoogleTokenVerifier.java #file:JwtUtil.java #file:AuthContext.jsx
   Explain the complete authentication flow — from Google sign-in to JWT validation on API requests.
   ```

4. Ask follow-up questions:

   - _"What happens when a JWT expires?"_
   - _"Why does the first user get ADMIN role automatically?"_

5. Ask about a code quality issue:

   ```
   #file:Database.java #file:README.md
   The README documents JDBC_URL, DB_USER, DB_PASSWORD env vars.
   Does Database.java actually read these environment variables?
   ```

6. Try a workspace-wide question:

   ```
   @workspace List all API endpoints in this project with their HTTP methods and purpose.
   ```

### Learning Outcome

- Navigate unfamiliar codebases using `#file` and `@workspace`
- Discover bugs and inconsistencies through conversational exploration
- Understand cross-file flows without reading every line

---

## Module 2 — Plan Mode

### Task: Plan an Expense Export Feature

**Objective**: Use Plan mode to research the codebase and produce a structured implementation plan.

**Steps**:

1. Switch Copilot Chat to **Plan mode**.
2. Enter this prompt:

   ```
   Plan adding a CSV export feature for expenses. If any doubts, confirm with me.
   - Add a new backend endpoint GET /api/expenses/export that returns expenses as CSV text
   - Add an "Export CSV" button on the ExpenseList page that downloads the file
   - Use only plain Java string formatting for CSV generation (no libraries)
   - Use only the browser's built-in Blob/download APIs on the frontend
   ```

3. Observe how Copilot:

   - Reads `ExpenseHandler.java` and `ExpenseDao.java` to understand existing patterns
   - Reads `ExpenseList.jsx` to understand the current page layout
   - Reads `Main.java` to see how routes are registered
   - Proposes a step-by-step plan with specific file changes

4. Ask a follow-up to refine the plan:

   - _"Should the export endpoint reuse the same filters (date range, category) as the list endpoint?"_

5. Review the final plan — notice it includes exact file paths, method names, and a logical order.

### Learning Outcome

- Plan mode researches the codebase autonomously before proposing
- Plans iterate based on your feedback
- You get a structured, executable plan before writing a single line of code

---

## Module 3 — Agent Mode

### Task: Add Input Validation & Fix a Real Bug

**Objective**: Use Agent mode to autonomously implement multi-file code changes.

**Steps**:

1. Switch Copilot Chat to **Agent mode**.
2. Enter this prompt:

   ```
   Add input validation to the expense creation and update endpoints in ExpenseHandler.java.

   Validate:
   - amount must be positive and not null
   - category must be one of: LOAN_PERSONAL, LOAN_OFFICE, SAVINGS, DAILY, HOME, COSMETICS, TRIP
   - entry_date must not be null and must not be in the future
   - note must be max 500 characters if provided
   - loan_name is required when category starts with "LOAN_"

   Return 400 status with JSON: { "error": "<message>" }
   Use only existing project dependencies. No new libraries.
   ```

3. **Observe** how Copilot:

   - Opens and edits `ExpenseHandler.java`
   - Applies validation to both `create` and `update` methods
   - May extract a shared validation helper method
   - Maintains the existing code style and patterns

4. Review the changes — accept or reject individual edits.

### Learning Outcome

- Agent mode makes autonomous multi-file edits
- It follows existing code patterns and conventions
- You review and accept/reject — you stay in control
- Real bugs make great Agent mode tasks

---

## Module 4 — Copilot Instructions

### Task: Configure Project-Level Copilot Instructions

**Objective**: Create `.github/copilot-instructions.md` to enforce project conventions across all Copilot interactions.

**Steps**:

1. Create the file `.github/copilot-instructions.md` at the project root.
2. Add the following content:

   ```markdown
   # Copilot Instructions for ExpenseTracker

   ## Tech Stack

   - Backend: Java 11, Javalin 6.x, raw JDBC with PreparedStatements, MySQL 8+
   - Frontend: React 18, Vite, plain CSS, JSX (NOT TypeScript)
   - Auth: Google Identity Services + JWT (JJWT library)

   ## Conventions

   - Do NOT suggest Hibernate, JPA, Spring, or any ORM
   - Do NOT suggest Tailwind, CSS-in-JS, or UI component libraries
   - Do NOT add new dependencies unless explicitly asked
   - All database IDs are UUIDs (VARCHAR 36)
   - Use Database.getConnection() for DB access
   - Follow the existing pattern: Handler → DAO → Model
   - Use Gson for JSON serialization
   - Frontend uses plain fetch via src/api/client.js — do NOT suggest Axios

   ## Code Style

   - Java: 4-space indentation, no wildcard imports
   - JSX: 2-space indentation, functional components with hooks
   - SQL: UPPERCASE keywords, lowercase column/table names
   ```

3. **Test the instructions** — switch to Agent mode and prompt:

   ```
   Add a utility method in ExpenseDao that returns the total expense amount for a given user and month. Then use it in ExpenseHandler to add a "total" field in the /api/expenses response.
   ```

4. **Verify** Copilot follows the instructions:
   - Uses raw JDBC with `PreparedStatement` (not Hibernate)
   - Uses `Database.getConnection()`
   - Returns JSON with `ctx.json()` using Maps
   - No new dependencies added

### Learning Outcome

- Instructions apply to ALL Copilot interactions project-wide
- No need to repeat context in every prompt
- Team conventions are enforced automatically

---

## Module 5 — Build a Custom Agent

### Task: Create a Code Reviewer Agent

**Objective**: Build a custom Copilot agent that acts as a code reviewer with project-specific knowledge.

**Steps**:

1. Create the file `.github/agents/code-reviewer.agent.md`.
2. Add the following content:

   ```markdown
   ---
   name: Code Reviewer
   description: Reviews code for bugs, security issues, and best practices
   user-invokable: true
   ---

   You are a senior code reviewer for the ExpenseTracker Java + React application.

   ## Your Role

   - Review code for bugs, security issues, and bad practices
   - Check adherence to project conventions
   - Suggest improvements with specific code references

   ## Project Knowledge

   - Backend uses Javalin (NOT Spring) with raw JDBC and PreparedStatements
   - Frontend is React with plain CSS (no TypeScript, no UI libraries)
   - Auth is Google OAuth → JWT stored in localStorage
   - All IDs are UUIDs, all DB access via Database.getConnection()
   - DAOs use PreparedStatements (safe from SQL injection)

   ## Review Checklist

   - SQL injection risks (any string concatenation in queries?)
   - Missing null checks on user input
   - Missing authorization checks (does the endpoint verify user ownership?)
   - Error handling (are exceptions swallowed or properly handled?)
   - Resource leaks (are DB connections, ResultSets closed properly?)
   - Frontend: XSS risks, missing error states, missing loading states
   ```

3. **Test the agent** in Copilot Chat:

   ```
   @code-reviewer(In the modes drop down, select "Code Reviewer" Agent)
   Review ExpenseHandler.java for security issues,
   missing validations, and code quality problems.
   ```

4. Try another prompt:

   ```
    @code-reviewer(In the modes drop down, select "Code Reviewer" Agent)
    Review the frontend auth flow in AuthContext.jsx
   and client.js. Are there any security concerns with how tokens
   are stored and transmitted?
   ```

5. Observe how the agent provides project-aware reviews, not generic advice.

### Learning Outcome

- Custom agents carry persistent domain expertise
- Invoked by anyone on the team with `@agent-name`
- Provides project-specific advice, not generic suggestions

---

## Module 6 — Build a Skill

### Task: Create an API Documentation Generator Skill

**Objective**: Build a reusable skill that generates markdown API documentation from any Handler class.

**Steps**:

1. Create the file `.github/skills/api-docs-generator/SKILL.md`.
2. Add the following content:

   ```markdown
   ---

   name: api-docs-generator
   description: Generates REST API documentation for the ExpenseTracker backend. Use this skill when asked to document endpoints, generate API reference, create OpenAPI-style docs, or produce request/response examples for Javalin route handlers.
   argument-hint: 'handler file or endpoint path, optionally followed by output format (markdown or openapi)'
   ```

---

# API Docs Generator

Generates clear, structured REST API documentation for the ExpenseTracker backend by reading Javalin route handlers and their corresponding DAOs.

## When to Use

- User asks to document an endpoint or handler
- User wants API reference for a feature (auth, expenses, admin)
- User needs request/response examples for a route
- User wants to understand what a handler accepts/returns

## Project Context

- **Framework:** Javalin 6.1.3 — routes are registered in `Main.java`
- **Handlers:** `backend/src/main/java/com/expense/tracker/handler/`
  - `AuthHandler.java` — Google login, JWT issuance
  - `ExpenseHandler.java` — CRUD for expenses
  - `AdminHandler.java` — admin-only operations
- **Models:** `backend/src/main/java/com/expense/tracker/model/`
- **DTOs:** `backend/src/main/java/com/expense/tracker/dto/`
- **Auth:** JWT passed as `Authorization: Bearer <token>` header; user identity extracted server-side via `JwtUtil`
- **IDs:** All resource IDs are UUIDs (VARCHAR 36)
- **Errors:** Returned as `{ "error": "message" }` with appropriate HTTP status

## Step-by-Step Procedure

1. **Identify the target** — determine which handler file(s) or route(s) to document from the user's request.
2. **Read `Main.java`** to find the route path and HTTP method for the handler method.
3. **Read the handler** to extract:
   - Path parameters (`ctx.pathParam(...)`)
   - Query parameters (`ctx.queryParam(...)`)
   - Request body shape (`ctx.bodyAs(...)` or `gson.fromJson(...)`)
   - Response body shape (what is passed to `ctx.json(...)`)
   - HTTP status codes returned
   - Auth requirements (is a JWT check present?)
4. **Read the DAO** referenced by the handler to understand DB constraints (required fields, types).
5. **Read the Model/DTO** classes for field names and types.
6. **Generate documentation** in the format below.

## Output Format

Produce a Markdown document with one section per endpoint:

```
## <HTTP METHOD> <path>

**Description:** One-sentence summary of what this endpoint does.

**Auth required:** Yes / No
**Role required:** admin / user / none

### Path Parameters
| Name | Type   | Description |
|------|--------|-------------|
| id   | UUID   | Expense ID  |

### Query Parameters
| Name | Type   | Required | Description |
|------|--------|----------|-------------|

### Request Body
\`\`\`json
{
  "field": "type — description"
}
\`\`\`

### Response

**Success `<status>`**
\`\`\`json
{
  "field": "example value"
}
\`\`\`

**Error Responses**
| Status | Body                        | Condition          |
|--------|-----------------------------|--------------------|
| 401    | `{ "error": "Unauthorized" }` | Missing/invalid JWT |
| 404    | `{ "error": "Not found" }`  | Resource not found |
```

## Rules

- Use **actual field names** from the model/DTO — never invent field names
- Always note if an endpoint requires a JWT (`Authorization: Bearer <token>`)
- For admin endpoints, note `Role required: admin`
- Show both success and all documented error responses
- If a field is a UUID, note its type as `string (UUID)`
- Amounts are stored as `DECIMAL` — represent as `number` in JSON examples
- Dates use ISO 8601 format (`YYYY-MM-DD`)
- Do **not** suggest adding Swagger/OpenAPI libraries — documentation is Markdown only

## Example

For `ExpenseHandler.java` `createExpense` mapped to `POST /api/expenses`:

```
## POST /api/expenses

**Description:** Creates a new expense entry for the authenticated user.

**Auth required:** Yes

### Request Body
\`\`\`json
{
  "amount": 42.50,
  "category": "Food",
  "description": "Lunch",
  "date": "2026-03-10"
}
\`\`\`

### Response

**Success `201`**
\`\`\`json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "...",
  "amount": 42.50,
  "category": "Food",
  "description": "Lunch",
  "date": "2026-03-10"
}
\`\`\`

**Error Responses**
| Status | Body | Condition |
|--------|------|-----------|
| 401    | `{ "error": "Unauthorized" }` | Missing or invalid JWT |
| 400    | `{ "error": "Invalid request" }` | Missing required fields |
```

```


3. **Test the skill** in Agent mode: (Use /api-docs-generator slash command)

```

Generate API documentation for ExpenseHandler.java. Save it as docs/expense-api.md

```

4. Review the generated doc — check that:
- All endpoints from `ExpenseHandler` are documented
- Request/response fields match the actual code
- Example JSON responses look realistic

5. **Try it on another handler**:

```

Generate API documentation for AuthHandler.java. Save it as docs/auth-api.md

```

6. Compare both docs — the format and style should be identical because the skill enforces consistency.

### Learning Outcome
- Skills encode repeatable tasks as reusable prompt templates
- They produce consistent output regardless of which handler is targeted
- Any team member can generate up-to-date API docs in seconds


```
