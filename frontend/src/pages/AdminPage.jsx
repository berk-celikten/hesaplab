import AdminChanges from '../components/AdminChanges.jsx'
import { useEffect, useRef, useState } from 'react'
import { request } from '../services/api.js'
const money = new Intl.NumberFormat('tr-TR', {minimumFractionDigits:2, maximumFractionDigits:2})
export default function AdminPage({ user, onUnauthorized }) {
  const [data,setData]=useState(null)
  const [error,setError]=useState('')
  const [attempt,setAttempt]=useState(0)
  const [ownerId,setOwnerId]=useState('')
  const [name,setName]=useState('')
  const [pending,setPending]=useState(false)
  const [message,setMessage]=useState('')
  const alive=useRef(false)
  const sending=useRef(false)
  useEffect(()=>{
    let ignore=false
    alive.current=true
    Promise.all([request('/api/admin/users'),request('/api/admin/accounts')]).then(([users,accounts])=>{
      if(!ignore){setData({users,accounts});setError('')}
    }).catch(e=>{if(!ignore){if(e.status===401)onUnauthorized();else setError(e.message)}})
    return ()=>{ignore=true;alive.current=false}
  },[user.id,onUnauthorized,attempt])
  async function submit(event){
    event.preventDefault()
    if(sending.current)return
    setMessage('')
    if(!ownerId || !name.trim() || [...name.trim()].length>100){setMessage('Müşteri seçin ve 1–100 karakter hesap adı girin.');return}
    sending.current=true;setPending(true)
    try{
      const csrf=await request('/api/auth/csrf')
      if(!alive.current)return
      await request('/api/admin/accounts',{method:'POST',headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},body:JSON.stringify({ownerId,name:name.trim()})})
      if(!alive.current)return
      setMessage('Hesap açıldı.');setName('')
      try{
        const accounts=await request('/api/admin/accounts')
        if(alive.current){setData(current=>({...current,accounts}));setError('')}
      }catch(e){if(alive.current){if(e.status===401)onUnauthorized();else setError('Hesap açıldı ancak liste yenilenemedi. Listeyi tekrar yükleyin.')}}
    }catch(e){if(alive.current){if(e.status===401)onUnauthorized();else setMessage(e.status===0 || e.status>=500?'Sonuç doğrulanamadı. Tekrar göndermeden önce hesap listesini kontrol edin.':e.message)}}
    finally{if(alive.current){setPending(false);sending.current=false}}
  }
  return <section>
    <h1>Yönetim</h1><p>Hoş geldiniz, {user.name}.</p>
    {error && <p role="alert">{error}</p>}
    <button className="balance-toggle" type="button" disabled={pending} onClick={()=>{setData(null);setError('');setAttempt(n=>n+1)}}>Listeleri yeniden yükle</button>
    {!data ? !error && <p role="status">Listeler yükleniyor…</p> : <>
      <AdminChanges users={data.users} accounts={data.accounts} pending={pending} onBusy={setPending} onUnauthorized={onUnauthorized} onChanged={async()=>{
        const [users,accounts]=await Promise.all([request('/api/admin/users'),request('/api/admin/accounts')])
        if(alive.current){setData({users,accounts});setError('')}
      }} />
      <h2>Kullanıcılar</h2>
      {data.users.length===0?<p>Kullanıcı bulunamadı.</p>:<ul className="movement-list">{data.users.map(u=><li key={u.id}>{u.name} · {u.username} · {u.role} · Kimlik: {u.id}</li>)}</ul>}
      <h2>Hesap aç</h2>
      <form className="login-form" onSubmit={submit}>
        <fieldset disabled={pending} style={{border:0,padding:0,minWidth:0}}>
          <div className="source-account"><label htmlFor="admin-owner">Müşteri</label>
            <select id="admin-owner" value={ownerId} onChange={e=>setOwnerId(e.target.value)}><option value="">Müşteri seçin</option>{data.users.filter(u=>u.role==='CUSTOMER').map(u=><option key={u.id} value={u.id}>{u.name} · {u.username}</option>)}</select>
          </div>
          <div className="amount-field"><label htmlFor="admin-name">Hesap adı</label><input id="admin-name" value={name} onChange={e=>setName(e.target.value)} /></div>
          <p>Başlangıç bakiyesi: 0,00 TL · Para birimi: TRY</p>
          <button className="balance-toggle" type="submit">{pending?'Hesap açılıyor…':'Hesap aç'}</button>
        </fieldset>
      </form>
      <h2>Hesaplar</h2>
      {data.accounts.length===0?<p>Hesap bulunamadı.</p>:<ul className="movement-list">{data.accounts.map(a=><li key={a.id}>{a.name} · {a.ownerName} (sahip: {a.ownerId}) · {money.format(Number(a.balance))} {a.currency} · Hesap: {a.id}</li>)}</ul>}
    </>}
    {message && <p role="status">{message}</p>}
  </section>
}
