import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getExpenses, deleteExpense } from '../api/client'
import { getCategoryLabel } from '../constants/categories'

export default function ExpenseList() {
  const [list, setList] = useState([])
  const [loading, setLoading] = useState(true)
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [category, setCategory] = useState('')
  const [err, setErr] = useState('')

  function load() {
    setLoading(true)
    const params = {}
    if (from) params.from = from
    if (to) params.to = to
    if (category) params.category = category
    getExpenses(params).then(data => {
      setList(Array.isArray(data) ? data : [])
      setLoading(false)
    }).catch(() => {
      setErr('Failed to load expenses')
      setLoading(false)
    })
  }

  useEffect(() => {
    load()
  }, [from, to, category])

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expense?')) return
    await deleteExpense(id)
    load()
  }

  return (
    <>
      <h2>Expenses</h2>
      <div className="filters">
        <input type="date" value={from} onChange={e => setFrom(e.target.value)} placeholder="From" />
        <input type="date" value={to} onChange={e => setTo(e.target.value)} placeholder="To" />
        <select value={category} onChange={e => setCategory(e.target.value)}>
          <option value="">All categories</option>
          <option value="LOAN_PERSONAL">Personal Loan</option>
          <option value="LOAN_OFFICE">Office Loan</option>
          <option value="SAVINGS">Savings</option>
          <option value="DAILY">Daily</option>
          <option value="HOME">Home</option>
          <option value="COSMETICS">Cosmetics</option>
          <option value="TRIP">Trip</option>
        </select>
      </div>
      <p>
        <Link to="/app/expenses/new" className="btn btn-primary">Add expense</Link>
      </p>
      {err && <p className="error">{err}</p>}
      {loading && <p>Loading...</p>}
      {!loading && (
        <div className="table-wrap">
          {list.length === 0 ? (
            <p className="empty">No expenses in this period. Add one above.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Category</th>
                  <th>Amount</th>
                  <th>Note</th>
                  <th>Loan name</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {list.map(row => (
                  <tr key={row.id}>
                    <td>{row.date}</td>
                    <td>{getCategoryLabel(row.category)}</td>
                    <td>₹{Number(row.amount).toLocaleString()}</td>
                    <td>{row.note || '—'}</td>
                    <td>{row.loanName || '—'}</td>
                    <td>
                      <Link to={'/app/expenses/' + row.id + '/edit'} className="btn btn-secondary btn-sm">Edit</Link>
                      <button type="button" className="btn btn-danger btn-sm" onClick={() => handleDelete(row.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </>
  )
}
