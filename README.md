# HesapLab
Bankacılık üzerinden yazılım geliştirmeyi öğrenmek için eğitim projesi.
Gerçek para, gerçek müşteri bilgisi veya banka bağlantısı içermez.

## Mevcut özellikler
- Vite + React + JavaScript ile mobil uyumlu hesap özeti.
- App, Header, AccountList ve AccountCard component'leri.
- Aynı demo müşterinin Vadesiz Hesap (10.000,00 TL) ve Birikim Hesabı (3.000,00 TL) kartları.
- Sabit örnek veriler props ve liste gösterimi ile aktarılır.

Henüz state, giriş, transfer, API, Oracle veya RabbitMQ yoktur.
Gelecek aşamalar docs/ROADMAP.md içindedir.

## Çalıştırma
Node 24.19.0 ve npm 11.17.0 ile kurulmuştur.
Resmî Vite gereksinimi: https://vite.dev/guide/ (Node 20.19+, 22.12+).
Repo kökünden:
```sh
cd frontend
npm ci
npm run dev
```
Terminalde gösterilen yerel adresi açın.

## Kontroller
frontend klasöründe:
```sh
npm run build
npm run lint
```
Build önizlemesi: `npm run preview`.
package-lock.json sürümlenir; bağımlılıklar ve derleme çıktıları sürümlenmez.
