function getDemoMessage() {
  return new Promise((_resolve, reject) => {
    setTimeout(() => {
      reject(new Error('Demo bilgi alınamadı.'))
    }, 1000)
  })
}

async function runDemo() {
  console.log('1. Demo bilgi isteniyor.')
  try {
    const message = await getDemoMessage()
    console.log(`3. Sonuç: ${message}`)
  } catch (error) {
    console.log(`3. Hata: ${error.message}`)
  } finally {
    console.log('4. Bekleme sona erdi.')
  }
}

runDemo()
console.log('2. Beklerken bu satır çalışıyor.')
