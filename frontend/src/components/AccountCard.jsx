export default function AccountCard({ name, balance }) {
  return (
    <article className="account-card">
      <p className="owner">Demo Müşteri 01</p>
      <h2>{name}</h2>
      <p className="balance-label">Kullanılabilir bakiye</p>
      <p className="balance">{balance}</p>
      <span className="currency">Türk lirası hesabı</span>
    </article>
  )
}
