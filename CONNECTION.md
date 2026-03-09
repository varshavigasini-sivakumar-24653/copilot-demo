# React and Java connection

- **Development:** The React app (Vite) runs on **http://localhost:3000**. The Vite dev server proxies all `/api` requests to the Java backend at **http://localhost:7000**. So the browser talks only to port 3000; no CORS issues.
- **Backend:** Java (Javalin) runs on port **7000** and has CORS enabled for all origins so it can also be called from other hosts if needed.
- **Env:** In `frontend/.env`, leave `VITE_API_URL` empty for dev (proxy used). For production, set `VITE_API_URL` to your backend URL (e.g. `https://your-api.onrender.com`).

To run both:
1. Start MySQL and run `backend/src/main/resources/schema.sql`.
2. Start Java: `cd backend && mvn exec:java -Dexec.mainClass="com.expense.tracker.Main"`.
3. Start React: `cd frontend && npm install && npm run dev`.
4. Open http://localhost:3000.
