import AccountList from '../components/AccountList.jsx'

export default function AccountsPage({
  selectedCustomer,
  customerAccounts,
  searchTerm,
  handleSearchChange,
  visibleAccounts,
  showBalances,
  isLoadingAccounts,
  handleReloadAccounts,
  isInitialError,
}) {
  return (
    <>
      <p className="eyebrow">HESAP ÖZETİ</p>
      <h1>Hesaplarım</h1>
      <p className="intro">{selectedCustomer.name}{!isLoadingAccounts && !isInitialError && ` · ${customerAccounts.length} hesap, tek bir yerde.`}</p>
      <button className="balance-toggle" type="button" onClick={() => handleReloadAccounts(false)} disabled={isLoadingAccounts}>
        Hesapları yeniden yükle
      </button>
      <button className="balance-toggle demo-error-button" type="button" onClick={() => handleReloadAccounts(true)} disabled={isLoadingAccounts}>
        Demo hatasını dene
      </button>
      <div className="account-search">
        <label htmlFor="account-search">Hesap adı ara</label>
        <input id="account-search" type="text" value={searchTerm} onChange={handleSearchChange} />
        {!isLoadingAccounts && !isInitialError && <p role="status">Gösterilen hesap: {visibleAccounts.length}</p>}
      </div>
      {isLoadingAccounts ? (
        <p role="status">Hesaplar yükleniyor…</p>
      ) : isInitialError ? null : customerAccounts.length === 0 ? (
        <p>Seçili müşterinin henüz hesabı yok.</p>
      ) : visibleAccounts.length === 0 ? (
        <p>Aramanıza uygun hesap bulunamadı.</p>
      ) : (
        <AccountList accounts={visibleAccounts} showBalances={showBalances} customerName={selectedCustomer.name} />
      )}
    </>
  )
}
