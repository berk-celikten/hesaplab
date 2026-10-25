import AccountCard from './AccountCard.jsx'

export default function AccountList({ accounts }) {
  return (
    <ul className="account-list" aria-label="Demo Müşteri 01 hesapları">
      {accounts.map((account) => (
        <li key={account.id}>
          <AccountCard name={account.name} balance={account.balance} />
        </li>
      ))}
    </ul>
  )
}
