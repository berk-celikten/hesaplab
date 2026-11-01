import { request } from './api.js'

export function getCurrentUser() { return request('/api/auth/me') }

async function postWithCsrf(path, body) {
  // Her POST için oturumun tokenı alınır; kalıcı depoya yazılmaz.
  const csrf = await request('/api/auth/csrf')
  const result = await request(path, {
    method: 'POST',
    headers: { [csrf.headerName]: csrf.token, 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })
  // Login/logout önceki tokenı kaldırır; yeni oturum tokenını yeniden oluştur.
  await request('/api/auth/csrf')
  return result
}

export function login(username, password) {
  return postWithCsrf('/api/auth/login', new URLSearchParams({ username, password }))
}

export function logout() { return postWithCsrf('/api/auth/logout', new URLSearchParams()) }
