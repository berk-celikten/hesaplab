import { useEffect, useRef, useState } from 'react'
import { request } from '../services/api.js'
export default function AdminChanges({ users, accounts, pending, onBusy, onChanged, onUnauthorized }) {
  const [username,setUsername]=useState('')
  const [name,setName]=useState('')
  const [password,setPassword]=useState('')
  const [customerId,setCustomerId]=useState('')
  const [customerName,setCustomerName]=useState('')
  const [accountId,setAccountId]=useState('')
  const [accountName,setAccountName]=useState('')
  const [message,setMessage]=useState('')
  const alive=useRef(false)
  const guard=useRef(false)
  useEffect(()=>{alive.current=true;return()=>{alive.current=false}},[])
  async function save(path,method,body,success) {
    if(guard.current || pending)return
    guard.current=true;onBusy(true);setMessage('')
    try {
      const csrf=await request('/api/auth/csrf')
      if(!alive.current)return
      await request(path,{method,headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},body:JSON.stringify(body)})
      if(!alive.current)return
      setMessage(success)
      try{await onChanged()}catch(error){if(alive.current){if(error.status===401)onUnauthorized();else setMessage(`${success} Ancak listeler yenilenemedi; listeleri yeniden yükleyin.`)}}
    }catch(error){if(alive.current){if(error.status===401)onUnauthorized();else setMessage(error.status===0 || error.status>=500?'Sonuç doğrulanamadı. Tekrar göndermeden önce listeleri kontrol edin.':error.message)}}
    finally{if(alive.current){guard.current=false;onBusy(false)}}
  }
  function create(event) {
    event.preventDefault()
    const submitted=password
    setPassword('')
    if(!/^[a-z0-9_]{3,50}$/.test(username.trim()) || !name.trim() || [...name.trim()].length>100 || [...submitted].length<15 || new TextEncoder().encode(submitted).length>72){setMessage('Kullanıcı adı 3–50 küçük harf/rakam/alt çizgi; ad 1–100 karakter; parola en az 15 karakter ve en fazla 72 UTF-8 bayt olmalı.');return}
    save('/api/admin/users','POST',{username:username.trim(),name:name.trim(),password:submitted},'Müşteri oluşturuldu.')
  }
  function rename(event,type,id,value) {
    event.preventDefault()
    if(!id || !value.trim() || [...value.trim()].length>100){setMessage('Kayıt seçin ve 1–100 karakter ad girin.');return}
    save(`/api/admin/${type}/${encodeURIComponent(id)}`,'PATCH',{name:value.trim()},'Ad güncellendi.')
  }
  return <section aria-label="Müşteri ve ad yönetimi">
    <h2>Müşteri oluştur</h2>
    <form className="login-form" onSubmit={create}><fieldset disabled={pending}>
      <div className="amount-field"><label htmlFor="new-username">Yeni kullanıcı adı</label><input id="new-username" value={username} onChange={e=>setUsername(e.target.value)} autoComplete="off" /></div>
      <div className="amount-field"><label htmlFor="new-name">Görünen ad</label><input id="new-name" value={name} onChange={e=>setName(e.target.value)} /></div>
      <div className="amount-field"><label htmlFor="new-password">Yeni müşteri parolası</label><input id="new-password" type="password" autoComplete="new-password" value={password} onChange={e=>setPassword(e.target.value)} /></div>
      <p>Parola: en az15 karakter, en fazla72 UTF-8 bayt. Rol CUSTOMER; hesap otomatik açılmaz.</p>
      <button className="balance-toggle" type="submit">Müşteri oluştur</button>
    </fieldset></form>
    <h2>Müşteri adını düzenle</h2>
    <form className="login-form" onSubmit={e=>rename(e,'users',customerId,customerName)}><fieldset disabled={pending}>
      <div className="source-account"><label htmlFor="rename-customer">Düzenlenecek müşteri</label><select id="rename-customer" value={customerId} onChange={e=>{setCustomerId(e.target.value);setCustomerName(users.find(u=>u.id===e.target.value)?.name || '')}}><option value="">Müşteri seçin</option>{users.filter(u=>u.role==='CUSTOMER').map(u=><option key={u.id} value={u.id}>{u.name} · {u.username}</option>)}</select></div>
      <div className="amount-field"><label htmlFor="customer-new-name">Yeni görünen ad</label><input id="customer-new-name" value={customerName} onChange={e=>setCustomerName(e.target.value)} /></div>
      <button className="balance-toggle" type="submit">Müşteri adını kaydet</button>
    </fieldset></form>
    <h2>Hesap adını düzenle</h2>
    <form className="login-form" onSubmit={e=>rename(e,'accounts',accountId,accountName)}><fieldset disabled={pending}>
      <div className="source-account"><label htmlFor="rename-account">Düzenlenecek hesap</label><select id="rename-account" value={accountId} onChange={e=>{setAccountId(e.target.value);setAccountName(accounts.find(a=>a.id===e.target.value)?.name || '')}}><option value="">Hesap seçin</option>{accounts.map(a=><option key={a.id} value={a.id}>{a.ownerName} · {a.name}</option>)}</select></div>
      <div className="amount-field"><label htmlFor="account-new-name">Yeni hesap adı</label><input id="account-new-name" value={accountName} onChange={e=>setAccountName(e.target.value)} /></div>
      <button className="balance-toggle" type="submit">Hesap adını kaydet</button>
    </fieldset></form>
    {message && <p role="status">{message}</p>}
  </section>
}
