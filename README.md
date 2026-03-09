# Expense Tracker

Daily/monthly expense tracker with Gmail login, loans (Personal/Office), savings, and categories. Plain Java backend + React frontend.

> **This project is used as a hands-on playground for the [GitHub Copilot Workshop](Workshop.md)** — covering Ask, Plan, Agent mode, Copilot Instructions, custom Agents, and Skills.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 11, Javalin 6.x, raw JDBC (no ORM), MySQL 8+ |
| Frontend | React 18, Vite, plain CSS, JSX |
| Auth | Google Identity Services + JWT (JJWT) |
| Database | MySQL 8+ with HikariCP connection pool |
| Build | Maven (backend), npm/Vite (frontend) |

## Prerequisites

- Java 11+
- Node.js 18+
- MySQL 8+

## Backend

1. **Start MySQL** (if not running):
   - macOS: `brew services start mysql`
   - Linux: `sudo systemctl start mysql`
2. Create database and run schema (from the `ExpenseTracker` folder):
   ```bash
   mysql -u root -p < backend/src/main/resources/schema.sql
   ```
   Or: `chmod +x scripts/setup-mysql.sh && ./scripts/setup-mysql.sh`
3. Database connection is hardcoded in `Database.java` (defaults: `localhost:3306`, user `root`, no password).
   - `JWT_SECRET` – min 32 chars (env var, or a default dev key is used)
   - `PORT` – default **7001**
4. Run:
   ```bash
   cd backend && mvn compile exec:java -Dexec.mainClass="com.expense.tracker.Main"
   ```
   Or: `mvn package` then `java -jar target/backend-1.0.0.jar`

## Frontend

1. Install and run:
   ```bash
   cd frontend && npm install && npm run dev
   ```
2. Set Google OAuth Client ID (for Sign in with Google):
   - Create a project in [Google Cloud Console](https://console.cloud.google.com/), enable Google+ API / Identity, create OAuth 2.0 Client ID (Web).
   - Create `frontend/.env`:
     ```
     VITE_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
     ```
   - Add `http://localhost:3000` to authorized JavaScript origins.
3. Open http://localhost:3000. The dev server proxies `/api` to the backend (port **7001**).

## API Endpoints

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `POST` | `/api/auth/google` | No | Exchange Google ID token for JWT |
| `GET` | `/api/me` | JWT | Get current user profile |
| `GET` | `/api/expenses` | JWT | List expenses (filters: `from`, `to`, `category`) |
| `GET` | `/api/expenses/summary` | JWT | Monthly summary (savings/loans/expenses) |
| `GET` | `/api/expenses/{id}` | JWT | Get single expense |
| `POST` | `/api/expenses` | JWT | Create expense |
| `PUT` | `/api/expenses/{id}` | JWT | Update expense |
| `DELETE` | `/api/expenses/{id}` | JWT | Delete expense |
| `GET` | `/api/admin/users` | JWT+ADMIN | List all users |
| `GET` | `/api/admin/logins` | JWT+ADMIN | Login audit trail (paginated) |

## Project Structure

```
backend/src/main/java/com/expense/tracker/
├── Main.java              — Javalin server, routes, JWT middleware
├── handler/               — Request handlers (controllers)
├── dao/                   — Data access (raw JDBC)
├── model/                 — POJOs (User, ExpenseEntry, LoginEvent)
├── dto/                   — Request DTOs
└── util/                  — Database, JWT, Google token verification

frontend/src/
├── pages/                 — Login, Dashboard, ExpenseList, ExpenseForm, Admin
├── auth/                  — AuthContext (Google login + JWT storage)
├── api/                   — Fetch wrapper with JWT auth header
├── components/            — Layout with nav bar
└── constants/             — Expense categories
```

## First User

The first user who signs in with Google is assigned the **ADMIN** role and can open the Admin page to see all users and login history. Other users are **USER** and only see their own expenses.

## Copilot Workshop

See [Workshop.md](Workshop.md) for the full workshop plan with 6 hands-on modules:

1. **Copilot Instructions** — Set up project conventions
2. **Ask Mode** — Explore the codebase conversationally
3. **Plan Mode** — Plan a CSV export feature
4. **Agent Mode** — Add validation + fix a real bug
5. **Custom Agents** — Build a code reviewer agent
6. **Skills** — Build an API docs generator skill
