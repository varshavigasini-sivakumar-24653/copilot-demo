import { Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Link } from 'react-router-dom'

export default function Layout() {
  const { user, logout } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  return (
    <div className="app">
      <nav className="topbar">
        <div>
          <Link to="/app/dashboard">Expense Tracker</Link>
          <Link to="/app/dashboard">Dashboard</Link>
          <Link to="/app/expenses">Expenses</Link>
          {isAdmin && <Link to="/app/admin">Admin</Link>}
        </div>
        <div className="user">
          {user?.pictureUrl && <img src={user.pictureUrl} alt="" />}
          <span>{user?.name || user?.email}</span>
          <button type="button" className="btn btn-secondary btn-sm" onClick={logout}>Logout</button>
        </div>
      </nav>
      <Outlet />
    </div>
  )
}
