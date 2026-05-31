# Kawan Kasir POS

Kawan Kasir adalah starter sistem Point of Sale online-only untuk toko/UMKM. Repo ini berisi backend Spring Boot Kotlin, aplikasi Android kasir Kotlin, draft web admin, dan skema database MySQL.

## Struktur

```text
BE-KawanKAsir/                 # Backend API Spring Boot Kotlin
KawanKasirFE/                  # Android POS Kotlin
web-admin/admin-dashboard/     # Draft web admin React + TypeScript + Vite
backend/springboot-api/        # Alias dokumentasi backend sesuai target struktur
android/kawan-kasir-android/   # Alias dokumentasi Android sesuai target struktur
database/                      # schema.sql, seed.sql, README database
```

## Default akun development

- Developer super access: `lukimia` / `lukimia`
- Super Admin: `admin` / `password123`
- Kasir: `kasir` / `password123`

## Menjalankan backend cepat

```bash
cd BE-KawanKAsir
./gradlew bootRun
```

Backend development memakai H2 in-memory secara default. Untuk MySQL, lihat `database/README.md`.

## Endpoint utama

- Auth: `/api/auth/login`, `/api/auth/register`, `/api/auth/me`, `/api/auth/refresh-token`, `/api/auth/change-password`
- Master: `/api/outlets`, `/api/users`, `/api/roles`
- Produk & stok: `/api/categories`, `/api/products`, `/api/stock-movements`
- POS: `/api/customers`, `/api/shifts/open`, `/api/sales`, `/api/sales/{id}/receipt`
- Laporan: `/api/reports/dashboard`, `/api/reports/sales`, `/api/reports/stock`
- Swagger UI: `/swagger-ui/index.html`

## Alur transaksi POS

1. Android login ke `/api/auth/login` dan menyimpan access token.
2. Kasir membuka shift melalui `/api/shifts/open`.
3. Android mencari produk via `/api/products/search` atau `/api/products/barcode/{barcode}`.
4. Checkout dikirim ke `/api/sales`.
5. Backend membuat nomor invoice `KK-{OUTLET_CODE}-{YYYYMMDD}-{RUNNING_NUMBER}` dan mengurangi stok.
6. Android mengambil preview struk dari `/api/sales/{id}/receipt`.
7. Kasir menutup shift melalui `/api/shifts/{id}/close`.

## Role

Role awal: `DEVELOPER`, `SUPER_ADMIN`, `OWNER`, `ADMIN`, `MANAGER`, `CASHIER`, `INVENTORY_STAFF`, dan `FINANCE`. Role `DEVELOPER` adalah super access untuk akun Lukimia. Permission disimpan sebagai data pada backend starter agar mudah dipindah ke tabel `roles`, `permissions`, dan `role_permissions` di MySQL.

## Smoke test backend

Setelah backend berjalan, jalankan:

```bash
BASE_URL=http://localhost:8080 ./scripts/backend-smoke-test.sh
```

Lihat `docs/IMPLEMENTATION_STATUS.md` untuk status implementasi dan batas produksi.
