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
  const [formErrors, setFormErrors] = useState({})

  useEffect(() => {
    let ignore = false
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
  const draftTarget = accounts.find((account) => account.id === transferDraft?.targetAccountId)
  const draftSourceCustomerName = draftSource ? user.name : ''
  const draftTargetCustomerName = draftTarget ? user.name : ''
  const normalizedSearchTerm = searchTerm.trim().toLocaleLowerCase('tr-TR')
  const visibleAccounts = customerAccounts.filter((account) =>
    account.name.toLocaleLowerCase('tr-TR').includes(normalizedSearchTerm),
  )

  function handleSearchChange(event) {
    setSearchTerm(event.target.value)
  }

  async function handleReloadAccounts(shouldFail = false) {
    if (isLoadingAccounts) return
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
    setFormErrors({})
    setSourceAccountId(event.target.value)
    if (event.target.value === targetAccountId) {
      setTargetAccountId('')
    }
    setTransferDraft(null)
  }

  function handleTargetAccountChange(event) {
    setFormErrors({})
    setTargetAccountId(event.target.value)
    setTransferDraft(null)
  }

  function handleAmountChange(event) {
    setFormErrors({})
    setAmountText(event.target.value)
    setTransferDraft(null)
  }

  function handleDraftSubmit(event) {
    event.preventDefault()
    const errors = {}
    if (sourceAccountId === '') {
      errors.sourceAccount = 'Lütfen kaynak hesap seçin.'
    }
    if (targetAccountId === '') {
      errors.targetAccount = 'Lütfen hedef hesap seçin.'
    }
    const trimmedAmount = amountText.trim()
    const amountError = 'Sıfırdan büyük, en fazla iki ondalık basamaklı bir tutar girin.'
    if (trimmedAmount === '') {
      errors.amount = 'Lütfen tutar girin.'
    } else if (!/^\d+([,.]\d{1,2})?$/.test(trimmedAmount)) {
      errors.amount = amountError
    } else {
      const amount = Number(trimmedAmount.replace(',', '.'))
      if (!Number.isFinite(amount) || amount <= 0) {
        errors.amount = amountError
      }
    }
    setFormErrors(errors)
    if (Object.keys(errors).length > 0) {
      setTransferDraft(null)
      return
    }
    setTransferDraft({ sourceAccountId, targetAccountId, amountText })
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
            isInitialLoading ? <p role="status">Hesaplar yükleniyor…</p> : isInitialError ? null :
            <AccountDetailsPage userId={user.id} onUnauthorized={onUnauthorized}
              customerName={selectedCustomer?.name ?? ''} showBalances={showBalances} />
          } />
          <Route path="/transfer" element={
            isInitialLoading ? <p role="status">Hesaplar yükleniyor…</p> : isInitialError ? null :
            <TransferPage sourceAccountId={sourceAccountId} targetAccountId={targetAccountId}
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
