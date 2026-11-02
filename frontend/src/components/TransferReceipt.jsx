import { useEffect, useRef, useState } from 'react'
import { request } from '../services/api.js'

export default function TransferReceipt({ transferId, userId, onUnauthorized, showBalances, initiallyOpen = false }) {
  const [open, setOpen] = useState(initiallyOpen)
  const [result, setResult] = useState(null)
  const [attempt, setAttempt] = useState(0)
  const [busy, setBusy] = useState(false)
  const version = useRef({ value: 0 })
  const guard = useRef(false)
  const path = `/api/transfers/${encodeURIComponent(transferId)}/receipt`

  useEffect(() => {
    const requests = version.current
    const current = ++requests.value
    if (!open) return () => { requests.value++ }
    request(path).then((receipt) => {
      if (current === version.current.value) setResult({ receipt })
    }).catch((error) => {
      if (current !== version.current.value) return
      if (error.status === 401) { onUnauthorized(); return }
      setResult({ error: error.message })
    })
    return () => { requests.value++ }
  }, [path, userId, onUnauthorized, attempt, open])

  async function requestAgain() {
    if (guard.current) return
    guard.current = true
    setBusy(true)
    const current = ++version.current.value
    try {
      const csrf = await request('/api/auth/csrf')
      if (current !== version.current.value) return
      const receipt = await request(path, {
        method: 'POST',
        headers: { [csrf.headerName]: csrf.token },
      })
      if (current === version.current.value) setResult({ receipt })
    } catch (error) {
      if (current !== version.current.value) return
      if (error.status === 401) { onUnauthorized(); return }
      setResult({ error: error.status === 0 ? 'Dekont isteğinin sonucu doğrulanamadı. Durumu yenileyin; transfer tekrarlanmadı.' : error.message })
    } finally {
      if (current === version.current.value) { setBusy(false); guard.current = false }
    }
  }

  return (
    <section className="transfer-receipt" aria-label={`Transfer ${transferId} dekontu`}>
      <h3>Metin dekontu · Transfer {transferId}</h3>
      {!open ? <button className="balance-toggle" type="button" onClick={() => setOpen(true)}>Dekontu görüntüle</button> : <>
        {!result ? <p role="status">Dekont durumu yükleniyor…</p> : result.error ? <p role="alert">{result.error}</p> : <>
          <p role="status">{result.receipt.status === 'READY' ? 'Dekont hazır.' : result.receipt.status === 'FAILED' ? 'Transfer tamamlandı; dekontta ayrı bir sorun var.' : 'Transfer tamamlandı; dekont hazırlanıyor.'}</p>
          {result.receipt.errorMessage && <p role="alert">{result.receipt.errorMessage}</p>}
          {result.receipt.status === 'READY' && (showBalances
            ? <pre className="receipt-content">{result.receipt.content}</pre>
            : <p>Dekont içeriği gizli. Görmek için bakiyeleri gösterin.</p>)}
          {result.receipt.status !== 'READY' && <button className="balance-toggle" type="button" disabled={busy} onClick={requestAgain}>Dekontu tekrar iste</button>}
        </>}
        <button className="balance-toggle" type="button" disabled={busy} onClick={() => { setResult(null); setAttempt((n) => n + 1) }}>Dekont durumunu yenile</button>
        {busy && <p role="status">Yalnız dekont isteniyor; yeni transfer yapılmıyor…</p>}
      </>}
    </section>
  )
}
