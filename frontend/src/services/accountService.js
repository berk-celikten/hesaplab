import { request } from './api.js'

const balanceFormatter = new Intl.NumberFormat('tr-TR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

function formatAccount(account) {
  return {
    ...account,
    balance: `${balanceFormatter.format(Number(account.balance))} ${account.currency === 'TRY' ? 'TL' : account.currency}`,
  }
}

export async function getAccounts(shouldFail = false) {
  if (shouldFail) {
    throw new Error('Hata simülasyonu: Yeniden yükleme başarısız. Tekrar dene ile gerçek API çağrılır.')
  }
  return (await request('/api/accounts')).map(formatAccount)
}

export async function getAccount(id) {
  return formatAccount(await request(`/api/accounts/${encodeURIComponent(id)}`))
}
