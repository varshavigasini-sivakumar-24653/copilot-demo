import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { getExpenseSummary } from '../api/client'

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

export default function Dashboard() {
  const [summary, setSummary] = useState(null)
  const [month, setMonth] = useState(() => {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  })
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')

  useEffect(() => {
    let cancelled = false
    getExpenseSummary(month).then(data => {
      if (cancelled) return
      if (data && typeof data.savings !== 'undefined') setSummary(data)
      else setErr('Failed to load summary')
      setLoading(false)
    }).catch(() => {
      if (!cancelled) { setErr('Failed to load'); setLoading(false) }
    })
    return () => { cancelled = true }
  }, [month])

  const [y, m] = month.split('-').map(Number)
  const prevMonth = () => {
    if (m === 1) setMonth(`${y - 1}-12`)
    else setMonth(`${y}-${String(m - 1).padStart(2, '0')}`)
  }
  const nextMonth = () => {
    if (m === 12) setMonth(`${y + 1}-01`)
    else setMonth(`${y}-${String(m + 1).padStart(2, '0')}`)
  }

  return (
    <>
      <div className="month-nav">
        <button type="button" className="btn btn-secondary" onClick={prevMonth}>←</button>
        <span>{MONTHS[m - 1]} {y}</span>
        <button type="button" className="btn btn-secondary" onClick={nextMonth}>→</button>
      </div>
      {loading && <p>Loading...</p>}
      {err && <p className="error">{err}</p>}
      {summary && !loading && (
        <div className="cards">
          <div className="card">
            <h3>Savings</h3>
            <div className="amount">₹{Number(summary.savings).toLocaleString()}</div>
          </div>
          <div className="card">
            <h3>Loans</h3>
            <div className="amount">₹{Number(summary.loans).toLocaleString()}</div>
          </div>
          <div className="card">
            <h3>Expenses</h3>
            <div className="amount">₹{Number(summary.expenses).toLocaleString()}</div>
          </div>
        </div>
      )}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <Link to="/app/expenses/new" className="btn btn-primary">Add expense</Link>
        <Link to="/app/expenses" className="btn btn-secondary">Expense list</Link>
      </div>
    </>
  )
}
