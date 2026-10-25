import Header from './components/Header.jsx'
import AccountList from './components/AccountList.jsx'

const accounts = [
  { id: 'checking', name: 'Vadesiz Hesap', balance: '10.000,00 TL' },
  { id: 'savings', name: 'Birikim Hesabı', balance: '3.000,00 TL' },
]

export default function App() {
  return (
    <>
      <Header />
      <main>
        <p className="eyebrow">HESAP ÖZETİ</p>
        <h1>Hesaplarım</h1>
        <p className="intro">Demo Müşteri 01 · İki hesabınız, tek bir yerde.</p>
        <AccountList accounts={accounts} />
        <p className="notice">Eğitim amaçlı demo · Bakiyeler örnektir, gerçek para içermez.</p>
      </main>
    </>
  )
}
