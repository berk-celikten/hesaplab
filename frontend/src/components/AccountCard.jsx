export default function AccountCard({ id, name, balance, showBalances, customerName }) {
  return (
    <article className="account-card">
      <p className="owner">{customerName}</p>
      <h2>{name}</h2>
      {id && <p>Demo hesap no: {id}</p>}
      <p className="balance-label">Kullanılabilir bakiye</p>
      <p className="balance">{showBalances ? balance : '••••••'}</p>
      <span className="currency">Türk lirası hesabı</span>
    </article>
  )
}
