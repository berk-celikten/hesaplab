export const demoAccounts = [
  { id: 'checking', customerId: 'customer-01', name: 'Vadesiz Hesap', balance: '10.000,00 TL' },
  { id: 'savings', customerId: 'customer-01', name: 'Birikim Hesabı', balance: '3.500,00 TL' },
  { id: 'checking-02', customerId: 'customer-02', name: 'Vadesiz Hesap', balance: '2.000,00 TL' },
]

export function getDemoAccounts(shouldFail = false) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (shouldFail) {
        reject(new Error('Demo hesaplar yüklenemedi. Lütfen tekrar deneyin.'))
        return
      }
      resolve(demoAccounts.map((account) => ({ ...account })))
    }, 1000)
  })
}
