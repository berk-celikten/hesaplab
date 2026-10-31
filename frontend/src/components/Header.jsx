import { Link, NavLink } from 'react-router'

export default function Header() {
  return (
    <header className="header">
      <Link className="brand" to="/hesaplar">HesapLab<span>.</span></Link>
      <nav className="main-nav" aria-label="Ana gezinme">
        <NavLink to="/hesaplar">Hesaplarım</NavLink>
        <NavLink to="/transfer">Transfer</NavLink>
      </nav>
      <span className="badge">Demo bankacılık</span>
    </header>
  )
}
