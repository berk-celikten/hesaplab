export default function TransferPage({
  externalTarget, handleExternalTarget, recipientInput, handleRecipientInput, verifiedRecipient, recipientLoading, recipientError, verifyRecipient,
  isSending, transferMessage, handleTransfer,
  sourceAccountId,
  targetAccountId,
  amountText,
  formErrors,
  sourceAccount,
  customerAccounts,
  targetAccounts,
  handleSourceAccountChange,
  handleTargetAccountChange,
  handleAmountChange,
  handleDraftSubmit,
  transferDraft,
  draftSource,
  draftTarget,
  draftSourceCustomerName,
  draftTargetCustomerName,
}) {
  return (
    <>
      <h1 className="page-title">Transfer taslağı</h1>
      <fieldset disabled={isSending} style={{ border: 0, padding: 0, minWidth: 0 }}>
      <form className="transfer-form" onSubmit={handleDraftSubmit} aria-label="Transfer taslak formu">
        <div className="source-account">
          <label htmlFor="source-account">Kaynak hesap</label>
          <select id="source-account" value={sourceAccountId} onChange={handleSourceAccountChange}
            aria-invalid={Boolean(formErrors.sourceAccount)} aria-describedby={formErrors.sourceAccount ? 'source-account-error' : undefined}>
            <option value="">Hesap seçin</option>
            {customerAccounts.map((account) => (
              <option key={account.id} value={account.id}>{account.name}</option>
            ))}
          </select>
          {formErrors.sourceAccount && <p className="field-error" id="source-account-error">{formErrors.sourceAccount}</p>}
          <p>{sourceAccount ? `Seçilen kaynak hesap: ${sourceAccount.name}` : 'Henüz hesap seçilmedi.'}</p>
        </div>
        <label><input type="checkbox" checked={externalTarget} onChange={handleExternalTarget} /> Demo hesap no ile alıcı doğrula</label>
        {externalTarget ? <section aria-label="Alıcı doğrulama">
          <div className="amount-field"><label htmlFor="recipient-number">Alıcı demo hesap no</label><input id="recipient-number" inputMode="numeric" value={recipientInput} onChange={handleRecipientInput} /></div>
          <button className="balance-toggle" type="button" disabled={recipientLoading} onClick={verifyRecipient}>Alıcıyı doğrula</button>
          {recipientLoading && <p role="status">Alıcı doğrulanıyor…</p>}
          {recipientError && <p role="alert">{recipientError}</p>}
          {verifiedRecipient && <p>Doğrulanan alıcı: {verifiedRecipient.customerName} · {verifiedRecipient.name} · Demo hesap no: {verifiedRecipient.id} · {verifiedRecipient.currency}</p>}
          {formErrors.targetAccount && <p className="field-error">{formErrors.targetAccount}</p>}
        </section> : (
        <div className="source-account">
          <label htmlFor="target-account">Hedef hesap</label>
          <select id="target-account" value={targetAccountId} onChange={handleTargetAccountChange}
            aria-invalid={Boolean(formErrors.targetAccount)} aria-describedby={formErrors.targetAccount ? 'target-account-error' : undefined}>
            <option value="">Hedef hesap seçin</option>
            {targetAccounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.customerName} · {account.name}
              </option>
            ))}
          </select>
          {formErrors.targetAccount && <p className="field-error" id="target-account-error">{formErrors.targetAccount}</p>}
        </div>
        )}
        <div className="amount-field">
          <label htmlFor="amount-text">Tutar (TL)</label>
          <input id="amount-text" type="text" inputMode="decimal" value={amountText} onChange={handleAmountChange}
            aria-invalid={Boolean(formErrors.amount)} aria-describedby={formErrors.amount ? 'amount-error' : undefined} />
          {formErrors.amount && <p className="field-error" id="amount-error">{formErrors.amount}</p>}
          <p>{amountText.trim() === '' ? 'Henüz tutar girilmedi.' : `Girilen tutar: ${amountText} TL`}</p>
        </div>
        <button className="balance-toggle" type="submit">Taslağı göster</button>
      </form>
      </fieldset>
      {transferMessage && <p role="status">{transferMessage}</p>}
      {isSending && <p role="status">Transfer işleniyor…</p>}
      {transferDraft && (
        <section className="transfer-draft" aria-label="Transfer taslağı">
          <h2>Transfer taslağı</h2>
          <p>Kaynak: {draftSource ? `${draftSourceCustomerName} · ${draftSource.name}` : 'Seçilmedi'}</p>
          <p>Hedef: {draftTarget ? `${draftTargetCustomerName} · ${draftTarget.name}` : 'Seçilmedi'}</p>
          <p>Tutar: {transferDraft.amountText.trim() === '' ? 'Girilmedi' : `${transferDraft.amountText} TL`}</p>
          <button className="balance-toggle" type="button" disabled={isSending} onClick={() => handleTransfer()}>Transferi gerçekleştir</button>
        </section>
      )}
    </>
  )
}
