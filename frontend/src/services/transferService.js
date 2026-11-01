import { request } from './api.js'
export async function sendTransfer(draft) {
  const csrf = await request('/api/auth/csrf')
  return request('/api/transfers', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', [csrf.headerName]: csrf.token },
    body: JSON.stringify({ sourceAccountId: draft.sourceAccountId, targetAccountId: draft.targetAccountId,
      amount: draft.amountText.trim().replace(',', '.') }),
  })
}
