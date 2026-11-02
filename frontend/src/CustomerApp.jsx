import { request } from './services/api.js'
import TransferReceipt from './components/TransferReceipt.jsx'
import { sendTransfer } from './services/transferService.js'
import { useEffect, useRef, useState } from 'react'

import { Link, Navigate, Route, Routes } from 'react-router'
import AccountsPage from './pages/AccountsPage.jsx'
import TransferPage from './pages/TransferPage.jsx'
import AccountDetailsPage from './pages/AccountDetailsPage.jsx'
import { getAccounts } from './services/accountService.js'

export default function CustomerApp({ user, onUnauthorized }) {
  const requestVersion = useRef({ version: 0 })
  const [accounts, setAccounts] = useState([])
  const [isLoadingAccounts, setIsLoadingAccounts] = useState(true)
  const [accountsError, setAccountsError] = useState('')
  const [showBalances, setShowBalances] = useState(true)

  const [searchTerm, setSearchTerm] = useState('')
  const [sourceAccountId, setSourceAccountId] = useState('')
  const [amountText, setAmountText] = useState('')
  const [targetAccountId, setTargetAccountId] = useState('')
  const [transferDraft, setTransferDraft] = useState(null)
  const [isSending, setIsSending] = useState(false)
  const [transferMessage, setTransferMessage] = useState('')
  const [receiptTransferId, setReceiptTransferId] = useState(null)
  const sendGuard = useRef(false)
  const [retryTransfer, setRetryTransfer] = useState(null)
  const [externalTarget, setExternalTarget] = useState(false)
  const [recipientInput, setRecipientInput] = useState('')
  const [verifiedRecipient, setVerifiedRecipient] = useState(null)
  const [recipientLoading, setRecipientLoading] = useState(false)
  const [recipientError, setRecipientError] = useState('')
  const recipientVersion = useRef({ version: 0 })
  const [formErrors, setFormErrors] = useState({})

  useEffect(() => {
    let ignore = false
    const recipientRequests = recipientVersion.current
    const requests = requestVersion.current
    const version = ++requests.version

    async function loadInitialAccounts() {
      setAccountsError('')
      try {
        const loadedAccounts = await getAccounts()
        if (!ignore && version === requestVersion.current.version) {
          if (loadedAccounts.some((account) => account.customerId !== user.id)) {
            onUnauthorized()
            return
          }
          setAccounts(loadedAccounts)
        }
      } catch (error) {
        if (!ignore && version === requestVersion.current.version) {
          if (error.status === 401) { onUnauthorized(); return }
          setAccountsError(error.message)
        }
      } finally {
        if (!ignore && version === requestVersion.current.version) {
          setIsLoadingAccounts(false)
        }
      }
    }

    loadInitialAccounts()
    return () => {
      ignore = true
      recipientRequests.version++
      requests.version++
    }
  }, [onUnauthorized, user.id])

  const isInitialLoading = isLoadingAccounts && accounts.length === 0
  const isInitialError = accountsError !== '' && accounts.length === 0

  const selectedCustomer = user
  const customerAccounts = accounts
  const sourceAccount = customerAccounts.find((account) => account.id === sourceAccountId)
  const targetAccounts = accounts.filter((account) => account.id !== sourceAccountId).map((account) => ({
    ...account,
    customerName: user.name,
  }))
  const draftSource = accounts.find((account) => account.id === transferDraft?.sourceAccountId)
  const draftTarget = transferDraft?.recipient || accounts.find((account) => account.id === transferDraft?.targetAccountId)
  const draftSourceCustomerName = draftSource ? user.name : ''
  const draftTargetCustomerName = draftTarget?.customerName || (draftTarget ? user.name : '')
  const normalizedSearchTerm = searchTerm.trim().toLocaleLowerCase('tr-TR')
  const visibleAccounts = customerAccounts.filter((account) =>
    account.name.toLocaleLowerCase('tr-TR').includes(normalizedSearchTerm),
  )

  function invalidateRecipient() {
    recipientVersion.current.version++
    setVerifiedRecipient(null)
    setTargetAccountId('')
    setRecipientError('')
    setRecipientLoading(false)
    setTransferDraft(null)
  }
  function handleRecipientInput(event) {
    if (sendGuard.current) return
    setRecipientInput(event.target.value)
    invalidateRecipient()
  }
  function handleExternalTarget(event) {
    if (sendGuard.current) return
    setExternalTarget(event.target.checked)
    invalidateRecipient()
  }
  async function verifyRecipient() {
    if (sendGuard.current) return
    const version = ++recipientVersion.current.version
    setVerifiedRecipient(null); setTargetAccountId(''); setTransferDraft(null)
    setRecipientLoading(true); setRecipientError('')
    try {
      const recipient = await request(`/api/recipients/${encodeURIComponent(recipientInput.trim())}`)
      if (version !== recipientVersion.current.version) return
      if (recipient.id === sourceAccountId) { setRecipientError('Kaynak ve hedef farklı olmalı.'); return }
      setVerifiedRecipient(recipient); setTargetAccountId(recipient.id)
    } catch (error) {
      if (version !== recipientVersion.current.version) return
      if (error.status === 401) { onUnauthorized(); return }
      setRecipientError(error.message)
    } finally { if (version === recipientVersion.current.version) setRecipientLoading(false) }
  }
  function handleSearchChange(event) {
    setSearchTerm(event.target.value)
  }

  async function handleReloadAccounts(shouldFail = false) {
    if (isLoadingAccounts || sendGuard.current) return
    const requests = requestVersion.current
    const version = ++requests.version
    setAccountsError('')
    setIsLoadingAccounts(true)
    try {
      const loadedAccounts = await getAccounts(shouldFail)
      if (version !== requestVersion.current.version) return
      if (loadedAccounts.some((account) => account.customerId !== user.id)) { onUnauthorized(); return }
      setAccounts(loadedAccounts)
    } catch (error) {
      if (version !== requestVersion.current.version) return
      if (error.status === 401) { onUnauthorized(); return }
      setAccountsError(error.message)
    } finally {
      if (version === requestVersion.current.version) setIsLoadingAccounts(false)
    }
  }

  function handleSourceAccountChange(event) {
    if (sendGuard.current) return
    setFormErrors({})
    if (externalTarget) invalidateRecipient()
    setSourceAccountId(event.target.value)
    if (event.target.value === targetAccountId) {
      setTargetAccountId('')
    }
    setTransferDraft(null)
  }

  function handleTargetAccountChange(event) {
    if (sendGuard.current) return
    setFormErrors({})
    setTargetAccountId(event.target.value)
    setTransferDraft(null)
  }

  function handleAmountChange(event) {
    if (sendGuard.current) return
    setFormErrors({})
    setAmountText(event.target.value)
    setTransferDraft(null)
  }

  function handleDraftSubmit(event) {
    event.preventDefault()
    if (sendGuard.current) return
    setTransferMessage('')
    const errors = {}
    if (sourceAccountId === '') {
      errors.sourceAccount = 'Lütfen kaynak hesap seçin.'
    }
    if (targetAccountId === '' || (externalTarget && !verifiedRecipient)) {
      errors.targetAccount = 'Lütfen hedef hesap seçin.'
    }
    const trimmedAmount = amountText.trim()
    const amountError = 'Sıfırdan büyük, en fazla iki ondalık basamaklı bir tutar girin.'
    if (trimmedAmount === '') {
      errors.amount = 'Lütfen tutar girin.'
    } else if (!/^\d+([,.]\d{1,2})?$/.test(trimmedAmount)) {
      errors.amount = amountError
    } else {
      if (!/[1-9]/.test(trimmedAmount)) errors.amount = amountError
    }
    setFormErrors(errors)
    if (Object.keys(errors).length > 0) {
      setTransferDraft(null)
      return
    }
    if (retryTransfer) return
    setTransferDraft({ sourceAccountId, targetAccountId, amountText, recipient: externalTarget ? verifiedRecipient : null, idempotencyKey: crypto.randomUUID() })
  }

  async function handleTransfer(retry = false) {
    const draft = retry ? retryTransfer : transferDraft
    if (!draft || sendGuard.current) return
    sendGuard.current = true
    setIsSending(true)
    setTransferMessage('')
    const version = ++requestVersion.current.version // Önceki GET sonuçları artık kullanılamaz.
    setIsLoadingAccounts(false)
    setRetryTransfer(draft)
    setTransferDraft(null) // Sonucu belirsiz isteği de aynı taslakla tekrar göndermeyin.
    try {
      const completed = await sendTransfer(draft)
      if (version !== requestVersion.current.version) return
      setRetryTransfer(null)
      setReceiptTransferId(completed.transferId)
      setTransferMessage('Transfer tamamlandı.')
      setAccountsError('')
      try {
        const updated = await getAccounts()
        if (version !== requestVersion.current.version) return
        setAccounts(updated)
      } catch (error) {
        if (version !== requestVersion.current.version) return
        if (error.status === 401) { onUnauthorized(); return }
        setAccountsError('Transfer tamamlandı ancak bakiyeler yenilenemedi. Hesapları yeniden yükleyin.')
      }
    } catch (error) {
      if (version !== requestVersion.current.version) return
      if (error.status === 401) { onUnauthorized(); return }
      if (error.status >= 400 && error.status < 500 && error.status !== 409) setRetryTransfer(null)
      setTransferMessage(error.status === 0 || error.status >= 500
        ? 'İşlemin sonucu doğrulanamadı. Tekrar göndermeden önce hesapları kontrol edin.' : error.message)
    } finally {
      if (version === requestVersion.current.version) { setIsSending(false); sendGuard.current = false }
    }
  }
  function toggleBalances() {
    setShowBalances((currentValue) => !currentValue)
  }

  return (
    <>

      <main>
        <button className="balance-toggle" type="button" onClick={toggleBalances}>
          {showBalances ? 'Bakiyeleri gizle' : 'Bakiyeleri göster'}
        </button>
        {accountsError && (
          <section className="accounts-error" aria-label="Hesap yükleme hatası">
            <p role="alert">{accountsError}</p>
            {accounts.length > 0 && <p>Önceden yüklenen hesap verileri gösteriliyor.</p>}
            <button className="balance-toggle" type="button" disabled={isLoadingAccounts}
              onClick={() => handleReloadAccounts(false)}>Tekrar dene</button>
          </section>
        )}
        {retryTransfer && !isSending && <section aria-label="Bekleyen transfer isteği">
          <p>Bekleyen istek: {retryTransfer.amountText} TL. Kaynak {retryTransfer.sourceAccountId}, hedef {retryTransfer.targetAccountId}. Aynı içerik ve anahtar korunuyor.</p>
          <button className="balance-toggle" type="button" onClick={() => handleTransfer(true)}>Aynı isteği tekrar dene</button>
          <p>Yeni taslaktan önce bu isteğin sonucunu doğrulayın. Sayfa yenilemeyin.</p>
        </section>}
        {receiptTransferId && <TransferReceipt key={`${user.id}:${receiptTransferId}`} transferId={receiptTransferId} userId={user.id} onUnauthorized={onUnauthorized} showBalances={showBalances} initiallyOpen />}
        <Routes>
          <Route path="/" element={<Navigate to="/hesaplar" replace />} />
          <Route path="/hesaplar" element={
            <AccountsPage selectedCustomer={selectedCustomer} customerAccounts={customerAccounts}
              searchTerm={searchTerm} handleSearchChange={handleSearchChange}
              visibleAccounts={visibleAccounts} showBalances={showBalances}
              isLoadingAccounts={isLoadingAccounts} handleReloadAccounts={handleReloadAccounts}
              isInitialError={isInitialError} />
          } />
          <Route path="/hesaplar/:accountId" element={
            (isInitialLoading || isSending) ? <p role="status">Hesaplar yükleniyor…</p> : isInitialError ? null :
            <AccountDetailsPage userId={user.id} onUnauthorized={onUnauthorized}
              customerName={selectedCustomer?.name ?? ''} showBalances={showBalances} />
          } />
          <Route path="/transfer" element={
            isInitialLoading ? <p role="status">Hesaplar yükleniyor…</p> : isInitialError ? null :
            <TransferPage externalTarget={externalTarget} handleExternalTarget={handleExternalTarget} recipientInput={recipientInput} handleRecipientInput={handleRecipientInput} verifiedRecipient={verifiedRecipient} recipientLoading={recipientLoading} recipientError={recipientError} verifyRecipient={verifyRecipient} isSending={isSending} transferMessage={transferMessage} handleTransfer={handleTransfer} sourceAccountId={sourceAccountId} targetAccountId={targetAccountId}
              amountText={amountText} formErrors={formErrors} sourceAccount={sourceAccount}
              customerAccounts={customerAccounts} targetAccounts={targetAccounts}
              handleSourceAccountChange={handleSourceAccountChange} handleTargetAccountChange={handleTargetAccountChange}
              handleAmountChange={handleAmountChange} handleDraftSubmit={handleDraftSubmit}
              transferDraft={transferDraft} draftSource={draftSource} draftTarget={draftTarget}
              draftSourceCustomerName={draftSourceCustomerName} draftTargetCustomerName={draftTargetCustomerName} />
          } />
          <Route path="*" element={
            <section>
              <h1>Sayfa bulunamadı</h1>
              <p>Bu adreste bir sayfa yok.</p>
              <Link to="/hesaplar">Hesaplarım'a dön</Link>
            </section>
          } />
        </Routes>
        <p className="notice">Eğitim amaçlı demo · Bakiyeler örnektir, gerçek para içermez.</p>
      </main>
    </>
  )
}
