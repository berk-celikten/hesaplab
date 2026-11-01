import { useCallback, useEffect, useRef, useState } from 'react'
import { Navigate, Route, Routes, useNavigate } from 'react-router'
import Header from './components/Header.jsx'
import CustomerApp from './CustomerApp.jsx'
import LoginPage from './pages/LoginPage.jsx'
import AdminPage from './pages/AdminPage.jsx'
import { getCurrentUser, login, logout } from './services/authService.js'

export default function App() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [checkingSession, setCheckingSession] = useState(true)
  const [pending, setPending] = useState(false)
  const [authError, setAuthError] = useState('')
  const requestVersion = useRef({ version: 0 })

  useEffect(() => {
    const requests = requestVersion.current
    const version = ++requests.version
    let ignore = false
    getCurrentUser().then((currentUser) => {
      if (!ignore && version === requestVersion.current.version) setUser(currentUser)
    }).catch((error) => {
      if (!ignore && version === requestVersion.current.version && error.status !== 401) setAuthError(error.message)
    }).finally(() => {
      if (!ignore && version === requestVersion.current.version) setCheckingSession(false)
    })
    return () => { ignore = true; requests.version++ }
  }, [])

  const handleUnauthorized = useCallback(() => {
    requestVersion.current.version++
    setUser(null)
    setPending(false)
    setCheckingSession(false)
    setAuthError('Oturumunuz sona erdi. Lütfen yeniden giriş yapın.')
  }, [])

  async function handleLogin(username, password) {
    const requests = requestVersion.current
    const version = ++requests.version
    setPending(true)
    setAuthError('')
    try {
      const currentUser = await login(username, password)
      if (version === requestVersion.current.version) {
        setUser(currentUser)
        navigate(currentUser.role === 'ADMIN' ? '/admin' : '/hesaplar', { replace: true })
      }
    } catch (error) {
      if (version === requestVersion.current.version) setAuthError(error.message)
    } finally {
      if (version === requestVersion.current.version) setPending(false)
    }
  }

  async function handleLogout() {
    const requests = requestVersion.current
    const version = ++requests.version
    // CustomerApp hemen unmount olur: bütün hesap/form/taslak state'i ve eski istekler bırakılır.
    setUser(null)
    setPending(true)
    setCheckingSession(true)
    setAuthError('')
    try {
      await logout()
    } catch {
      if (version === requestVersion.current.version) setAuthError('Sunucuda çıkış doğrulanamadı. Bağlantıyı kontrol edin.')
    } finally {
      if (version === requestVersion.current.version) { setPending(false); setCheckingSession(false) }
    }
  }

  return (
    <>
      <Header user={user} onLogout={handleLogout} pending={pending} />
      {checkingSession ? <main><p role="status">Oturum kontrol ediliyor…</p></main>
        : !user ? <LoginPage onLogin={handleLogin} pending={pending} error={authError} />
          : user.role === 'ADMIN' ? <main><Routes>
            <Route path="/admin" element={<AdminPage user={user} />} />
            <Route path="*" element={<Navigate to="/admin" replace />} />
          </Routes></main>
            : <CustomerApp key={user.id} user={user} onUnauthorized={handleUnauthorized} />}
    </>
  )
}
