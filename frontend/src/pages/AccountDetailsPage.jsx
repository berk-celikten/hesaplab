import AccountMovements from '../components/AccountMovements.jsx'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import AccountCard from '../components/AccountCard.jsx'
import { getAccount } from '../services/accountService.js'

export default function AccountDetailsPage({ userId, onUnauthorized, customerName, showBalances }) {
  const { accountId } = useParams()
  const [result, setResult] = useState({ id: '', account: null, error: '' })
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let ignore = false
    getAccount(accountId).then((account) => {
      if (ignore) return
      if (account.customerId !== userId) { onUnauthorized(); return }
      setResult({ id: accountId, account, error: '' })
    }).catch((error) => {
      if (ignore) return
      if (error.status === 401) { onUnauthorized(); return }
      setResult({ id: accountId, account: null, error: error.message })
    })
    return () => { ignore = true }
  }, [accountId, userId, onUnauthorized, attempt])
  const loading = result.id !== accountId
  const account = result.account

  return (
    <>
      <h1 className="page-title">Hesap ayrıntısı</h1>
      {loading ? <p role="status">Hesap yükleniyor…</p> : account ? (
        <><AccountCard id={account.id} name={account.name} balance={account.balance}
          customerName={customerName} showBalances={showBalances} />
        <AccountMovements key={`${userId}:${accountId}`} accountId={accountId} userId={userId} showBalances={showBalances} onUnauthorized={onUnauthorized} /></>
      ) : (
        <div>
          <p role="alert">{result.error}</p>
          <button className="balance-toggle" type="button" onClick={() => {
            setResult({ id: '', account: null, error: '' })
            setAttempt((value) => value + 1)
          }}>Tekrar dene</button>
        </div>
      )}
      <Link className="account-details-link" to="/hesaplar">Hesaplarım’a dön</Link>
    </>
  )
}
