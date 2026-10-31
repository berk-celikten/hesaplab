import { Link, useParams } from 'react-router'
import AccountCard from '../components/AccountCard.jsx'

export default function AccountDetailsPage({ customerAccounts, customerName, showBalances }) {
  const { accountId } = useParams()
  const account = customerAccounts.find((account) => account.id === accountId)

  return (
    <>
      <h1 className="page-title">Hesap ayrıntısı</h1>
      {account ? (
        <AccountCard name={account.name} balance={account.balance}
          customerName={customerName} showBalances={showBalances} />
      ) : (
        <p>Hesap bulunamadı veya seçili müşteriye ait değil.</p>
      )}
      <Link className="account-details-link" to="/hesaplar">Hesaplarım’a dön</Link>
    </>
  )
}
