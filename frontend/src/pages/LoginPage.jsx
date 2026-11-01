import { useState } from 'react'

export default function LoginPage({ onLogin, pending, error }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  function handleSubmit(event) {
    event.preventDefault()
    const submittedPassword = password
    setPassword('')
    onLogin(username.trim(), submittedPassword)
  }
  return (
    <main>
      <h1>HesapLab’a giriş</h1>
      <p className="intro">Yerel eğitim uygulaması · Demo kullanıcı hesabınızla giriş yapın.</p>
      {error && <p role="alert" className="field-error">{error}</p>}
      <form className="login-form" onSubmit={handleSubmit}>
        <div className="amount-field">
          <label htmlFor="login-username">Kullanıcı adı</label>
          <input id="login-username" autoComplete="username" value={username}
            onChange={(event) => setUsername(event.target.value)} disabled={pending} required />
        </div>
        <div className="amount-field">
          <label htmlFor="login-password">Parola</label>
          <input id="login-password" type="password" autoComplete="current-password" value={password}
            onChange={(event) => setPassword(event.target.value)} disabled={pending} required />
        </div>
        <button className="balance-toggle" type="submit" disabled={pending}>
          {pending ? 'Giriş yapılıyor…' : 'Giriş yap'}
        </button>
      </form>
    </main>
  )
}
