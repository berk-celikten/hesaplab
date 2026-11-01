export default function AdminPage({ user }) {
  return (
    <section>
      <h1>Yönetici başlangıç ekranı</h1>
      <p className="intro">Hoş geldiniz, {user.name}.</p>
      <p>Bu aşamada yönetim işlemleri eklenmedi. Müşteri hesapları ve transfer ekranı yöneticiye açık değildir.</p>
    </section>
  )
}
