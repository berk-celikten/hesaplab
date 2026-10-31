import { useEffect, useState } from 'react'
import Header from './components/Header.jsx'
import { Link, Navigate, Route, Routes } from 'react-router'
import AccountsPage from './pages/AccountsPage.jsx'
import TransferPage from './pages/TransferPage.jsx'
import AccountDetailsPage from './pages/AccountDetailsPage.jsx'
import { getDemoAccounts } from './services/accountService.js'

const customers = [
  { id: 'customer-01', name: 'Demo Müşteri 01' },
  { id: 'customer-02', name: 'Demo Müşteri 02' },
]

export default function App() {
  const [accounts, setAccounts] = useState([])
  const [isLoadingAccounts, setIsLoadingAccounts] = useState(true)
  const [accountsError, setAccountsError] = useState('')
  const [showBalances, setShowBalances] = useState(true)
  const [selectedCustomerId, setSelectedCustomerId] = useState('customer-01')
  const [searchTerm, setSearchTerm] = useState('')
  const [sourceAccountId, setSourceAccountId] = useState('')
  const [amountText, setAmountText] = useState('')
  const [targetAccountId, setTargetAccountId] = useState('')
  const [transferDraft, setTransferDraft] = useState(null)
  const [formErrors, setFormErrors] = useState({})

  useEffect(() => {
    let ignore = false

    async function loadInitialAccounts() {
      setAccountsError('')
      try {
        const loadedAccounts = await getDemoAccounts()
        if (!ignore) {
          setAccounts(loadedAccounts)
        }
      } catch (error) {
        if (!ignore) {
          setAccountsError(error.message)
        }
      } finally {
        if (!ignore) {
          setIsLoadingAccounts(false)
        }
      }
    }

    loadInitialAccounts()
    return () => {
      ignore = true
    }
  }, [])

  const isInitialLoading = isLoadingAccounts && accounts.length === 0
  const isInitialError = accountsError !== '' && accounts.length === 0

  const selectedCustomer = customers.find((customer) => customer.id === selectedCustomerId)
  const customerAccounts = accounts.filter((account) => account.customerId === selectedCustomerId)
  const sourceAccount = customerAccounts.find((account) => account.id === sourceAccountId)
  const targetAccounts = accounts.filter((account) => account.id !== sourceAccountId).map((account) => ({
    ...account,
    customerName: customers.find((customer) => customer.id === account.customerId).name,
  }))
  const draftSource = accounts.find((account) => account.id === transferDraft?.sourceAccountId)
  const draftTarget = accounts.find((account) => account.id === transferDraft?.targetAccountId)
  const draftSourceCustomerName = customers.find((customer) => customer.id === draftSource?.customerId)?.name
  const draftTargetCustomerName = customers.find((customer) => customer.id === draftTarget?.customerId)?.name
  const normalizedSearchTerm = searchTerm.trim().toLocaleLowerCase('tr-TR')
  const visibleAccounts = customerAccounts.filter((account) =>
    account.name.toLocaleLowerCase('tr-TR').includes(normalizedSearchTerm),
  )

  function handleSearchChange(event) {
    setSearchTerm(event.target.value)
  }

  async function handleReloadAccounts(shouldFail = false) {
    if (isLoadingAccounts) return
    setAccountsError('')
    setIsLoadingAccounts(true)
    try {
      const loadedAccounts = await getDemoAccounts(shouldFail)
      setAccounts(loadedAccounts)
    } catch (error) {
      setAccountsError(error.message)
    } finally {
      setIsLoadingAccounts(false)
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

  function handleCustomerChange(customerId) {
    if (customerId !== selectedCustomerId) {
      setSelectedCustomerId(customerId)
      setSourceAccountId('')
      setAmountText('')
      setTargetAccountId('')
      setTransferDraft(null)
      setFormErrors({})
    }
  }

  function toggleBalances() {
    setShowBalances((currentValue) => !currentValue)
  }

  return (
    <>
      <Header />
      <main>
        <div className="customer-selection" role="group" aria-label="Demo müşteri seçimi">
          <p className="selection-label">Demo müşteri seçimi</p>
          <div className="customer-buttons">
            {customers.map((customer) => (
              <button
                key={customer.id}
                className="customer-button"
                type="button"
                aria-pressed={selectedCustomerId === customer.id}
                onClick={() => handleCustomerChange(customer.id)}
              >
                {customer.name}
              </button>
            ))}
          </div>
        </div>
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
            <AccountDetailsPage customerAccounts={customerAccounts}
              customerName={selectedCustomer.name} showBalances={showBalances} />
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
