# Status Implementasi Kawan Kasir

Dokumen ini menjelaskan batas MVP yang sedang dikejar agar sistem bergerak dari starter menuju siap pakai.

## Sudah tersedia

- Backend Spring Boot Kotlin dengan JWT, BCrypt, CORS, response wrapper, exception handler, role/permission seed, dan method security.
- Akun developer super access: `lukimia` / `lukimia` dengan role `DEVELOPER`.
- Endpoint pendaftaran `POST /api/auth/register` untuk membuat outlet, akun OWNER, receipt setting awal, dan token login.
- Endpoint POS core: outlet, user, role, category, product, stock movement, customer, shift, sale, receipt preview, dashboard/report.
- Web admin scaffold dengan dashboard dan form pendaftaran akun yang memanggil backend.
- Android UI kasir, API client awal, dan abstraction printer thermal untuk Bluetooth/USB/network printer tahap berikutnya.
- Schema dan seed MySQL awal.
- Script smoke test backend di `scripts/backend-smoke-test.sh`.

## Batas yang masih harus diselesaikan untuk produksi 100%

- Mengganti in-memory store menjadi entity/repository JPA penuh dengan migration Flyway.
- Menambahkan refresh token storage/rotation dan logout token invalidation.
- Menambahkan audit log permanen untuk perubahan data penting.
- Menyelesaikan CRUD web admin real untuk semua modul, termasuk table, filter, pagination, export PDF/Excel.
- Menyelesaikan Android POS real: login, shift, cart, checkout, receipt preview, transaction history, close shift, dan printer integration.
- Menambahkan automated tests lengkap: backend service/controller/security tests, web tests, dan Android tests.

## Cara smoke test backend

Jalankan backend lebih dulu, lalu:

```bash
BASE_URL=http://localhost:8080 ./scripts/backend-smoke-test.sh
```
