import AccountCard from './AccountCard.jsx'
import { Link } from 'react-router'

export default function AccountList({ accounts, showBalances, customerName }) {
  return (
    <ul className="account-list" aria-label={`${customerName} hesapları`}>
      {accounts.map((account) => (
        <li key={account.id}>
          <AccountCard name={account.name} balance={account.balance} showBalances={showBalances} customerName={customerName} />
          <Link className="account-details-link" to={`/hesaplar/${account.id}`}
            aria-label={`${account.name} ayrıntılarını gör`}>
            Ayrıntıları gör
          </Link>
        </li>
      ))}
    </ul>
  )
}
