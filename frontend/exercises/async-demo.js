function getDemoMessage() {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve('Demo bilgi hazır.')
    }, 3000)
  })
}

async function runDemo() {
  console.log('1. Demo bilgi isteniyor.')
  const message = await getDemoMessage()
  console.log(`3. Sonuç: ${message}`)
}

runDemo()
console.log('2. Beklerken bu satır çalışıyor.')
