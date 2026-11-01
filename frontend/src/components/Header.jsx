import { Link, NavLink } from 'react-router'

export default function Header({ user, onLogout, pending }) {
  return (
    <header className="header">
      <Link className="brand" to={user?.role === 'ADMIN' ? '/admin' : '/hesaplar'}>HesapLab<span>.</span></Link>
      {user?.role === 'CUSTOMER' && <nav className="main-nav" aria-label="Ana gezinme">
        <NavLink to="/hesaplar">Hesaplarım</NavLink>
        <NavLink to="/transfer">Transfer</NavLink>
      </nav>}
      <div className="session-controls">
        <span className="badge">{user ? user.name : 'Demo bankacılık'}</span>
        {user && <button className="session-logout" type="button" onClick={onLogout} disabled={pending}>Çıkış yap</button>}
      </div>
    </header>
  )
}
