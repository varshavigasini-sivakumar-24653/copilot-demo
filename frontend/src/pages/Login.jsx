import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { googleLogin } from '../api/client'

const GOOGLE_CLIENT_ID = (import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim()

export default function Login() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [googleReady, setGoogleReady] = useState(false)
  const divRef = useRef(null)

  useEffect(() => {
    if (user) {
      navigate('/app/dashboard', { replace: true })
      return
    }
  }, [user, navigate])

  // Wait for Google script to load (it's async in index.html)
  useEffect(() => {
    if (window.google?.accounts?.id) {
      setGoogleReady(true)
      return
    }
    const id = setInterval(() => {
      if (window.google?.accounts?.id) {
        setGoogleReady(true)
        clearInterval(id)
      }
    }, 200)
    return () => clearInterval(id)
  }, [])

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID || !googleReady || !divRef.current) return
    try {
      window.google.accounts.id.initialize({
        client_id: GOOGLE_CLIENT_ID,
        callback: handleCredentialResponse,
      })
      window.google.accounts.id.renderButton(divRef.current, {
        type: 'standard',
        theme: 'filled_blue',
        size: 'large',
        text: 'continue_with',
      })
    } catch (e) {
      setError('Google Sign-In failed to load. Set VITE_GOOGLE_CLIENT_ID.')
    }
  }, [GOOGLE_CLIENT_ID, googleReady])

  async function handleCredentialResponse(response) {
    if (!response?.credential) return
    setLoading(true)
    setError('')
    try {
      const data = await googleLogin(response.credential)
      if (data?.token && data?.user) {
        login(data.token, data.user)
        navigate('/app/dashboard', { replace: true })
      } else {
        setError(data?.error || 'Login failed')
      }
    } catch (e) {
      setError('Login failed. Is the API running?')
    } finally {
      setLoading(false)
    }
  }

  const showSetupHint = !GOOGLE_CLIENT_ID || GOOGLE_CLIENT_ID.includes('your-client-id')

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>Expense Tracker</h1>
        {error && <p className="error">{error}</p>}
        {loading && <p>Signing in...</p>}
        {!loading && (
          <>
            <div ref={divRef} id="google-signin-button" style={{ minHeight: 44, display: 'flex', justifyContent: 'center' }} />
            {showSetupHint && (
              <p className="login-setup-hint">
                To see the button: add <code>VITE_GOOGLE_CLIENT_ID=your-oauth-client-id</code> to <code>frontend/.env</code> and restart <code>npm run dev</code>.
              </p>
            )}
          </>
        )}
        <p>Sign in with your Gmail account. Your data stays private.</p>
      </div>
    </div>
  )
}
