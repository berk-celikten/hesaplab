import { useEffect, useState } from 'react'
import { request } from '../services/api.js'

const money = new Intl.NumberFormat('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const date = new Intl.DateTimeFormat('tr-TR', { dateStyle: 'short', timeStyle: 'short' })
export default function AccountMovements({ accountId, userId, showBalances, onUnauthorized }) {
  const [result, setResult] = useState(null)
  const [attempt, setAttempt] = useState(0)
  useEffect(() => {
    let ignore = false
    request(`/api/accounts/${encodeURIComponent(accountId)}/movements`).then((items) => {
      if (!ignore) setResult({ items })
    }).catch((error) => {
      if (ignore) return
      if (error.status === 401) { onUnauthorized(); return }
      setResult({ error: error.message })
    })
    return () => { ignore = true }
  }, [accountId, userId, onUnauthorized, attempt])
  function amount(value) { return showBalances ? `${money.format(Number(value))} TL` : '••••••' }
  return (
    <section aria-label="Son 50 hareket">
      <h2>Son 50 hareket</h2>
      {!result ? <p role="status">Hareketler yükleniyor…</p> : result.error ? <>
        <p role="alert">{result.error}</p>
        <button className="balance-toggle" type="button" onClick={() => { setResult(null); setAttempt((n) => n + 1) }}>Hareketleri tekrar dene</button>
      </> : result.items.length === 0 ? <p>Bu hesapta henüz hareket yok.</p> : <ol className="movement-list">
        {result.items.map((item) => <li key={item.id}>
          <time dateTime={item.createdAt}>{date.format(new Date(item.createdAt))}</time>
          <p>{item.direction === 'CREDIT' ? 'Giriş' : 'Çıkış'} · {amount(item.amount)}</p>
          <p>İşlem sonrası bakiye: {amount(item.balanceAfter)}</p>
          <p>{item.description || 'Açıklama yok.'}</p>
        </li>)}
      </ol>}
    </section>
  )
}
