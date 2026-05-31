# Web Admin Kawan Kasir

Dashboard admin berada di `admin-dashboard` dan memakai React + TypeScript + Vite. Web admin ditujukan untuk role SUPER_ADMIN, OWNER, ADMIN, MANAGER, FINANCE, dan INVENTORY_STAFF; kasir utama tetap melalui Android.

## Fitur awal

- Dashboard admin statis.
- Halaman/form pendaftaran akun yang memanggil `POST /api/auth/register`.
- Info akun developer super access: `lukimia` / `lukimia`.

## Run

```bash
cd web-admin/admin-dashboard
npm install
npm run dev
```

Vite mem-proxy `/api` ke `http://localhost:8080`.
