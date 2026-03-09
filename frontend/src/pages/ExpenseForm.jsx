import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getExpense, createExpense, updateExpense } from '../api/client'
import { CATEGORIES, isLoanCategory } from '../constants/categories'

export default function ExpenseForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const [category, setCategory] = useState('DAILY')
  const [amount, setAmount] = useState('')
  const [currency, setCurrency] = useState('INR')
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [note, setNote] = useState('')
  const [loanName, setLoanName] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!isEdit) return
    getExpense(id).then(row => {
      if (row && row.id) {
        setCategory(row.category)
        setAmount(String(row.amount))
        setCurrency(row.currency || 'INR')
        setDate(row.date || date)
        setNote(row.note || '')
        setLoanName(row.loanName || '')
      }
    })
  }, [id, isEdit])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const amt = parseFloat(amount)
    if (isNaN(amt) || amt <= 0) {
      setError('Amount is required and must be positive')
      return
    }
    if (!date) {
      setError('Date is required')
      return
    }
    setLoading(true)
    try {
      const payload = { category, amount: amt, currency, date, note }
      if (isLoanCategory(category)) payload.loanName = loanName || undefined
      if (isEdit) {
        await updateExpense(id, payload)
      } else {
        await createExpense(payload)
      }
      navigate('/app/expenses')
    } catch (e) {
      setError(e?.message || 'Failed to save')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <h2>{isEdit ? 'Edit expense' : 'Add expense'}</h2>
      <form onSubmit={handleSubmit} style={{ maxWidth: 400 }}>
        <div className="form-group">
          <label>Category *</label>
          <select value={category} onChange={e => setCategory(e.target.value)} required>
            {CATEGORIES.map(c => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>
        </div>
        <div className="form-group">
          <label>Amount *</label>
          <input type="number" step="0.01" min="0" value={amount} onChange={e => setAmount(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Currency</label>
          <select value={currency} onChange={e => setCurrency(e.target.value)}>
            <option value="INR">INR</option>
            <option value="USD">USD</option>
          </select>
        </div>
        <div className="form-group">
          <label>Date *</label>
          <input type="date" value={date} onChange={e => setDate(e.target.value)} required />
        </div>
        <div className="form-group">
          <label>Note</label>
          <textarea value={note} onChange={e => setNote(e.target.value)} />
        </div>
        {isLoanCategory(category) && (
          <div className="form-group">
            <label>Loan name</label>
            <input type="text" value={loanName} onChange={e => setLoanName(e.target.value)} placeholder="e.g. Bank X" />
          </div>
        )}
        {error && <p className="error">{error}</p>}
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Saving...' : 'Save'}</button>
          <button type="button" className="btn btn-secondary" onClick={() => navigate('/app/expenses')}>Cancel</button>
        </div>
      </form>
    </>
  )
}
