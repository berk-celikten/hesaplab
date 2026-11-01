export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

export async function request(path, options = {}) {
  let response
  try {
    response = await fetch(path, { ...options, credentials: 'same-origin', cache: 'no-store' })
  } catch {
    throw new ApiError('Sunucuya ulaşılamıyor. Bağlantıyı kontrol edip tekrar deneyin.', 0)
  }
  const data = await response.json().catch(() => null)
  if (!response.ok) {
    throw new ApiError(data?.message ?? 'İstek tamamlanamadı. Lütfen tekrar deneyin.', response.status)
  }
  return data
}
