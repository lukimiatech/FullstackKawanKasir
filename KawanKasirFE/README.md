# Android Kawan Kasir

Aplikasi Android Kotlin untuk kasir/staff toko. UI awal menggunakan warna logo Kawan Kasir, logo aplikasi, dan logo developer.

## Run

Buka folder `KawanKasirFE` di Android Studio, pilih device/emulator, lalu run `app`.

## Build CLI

```bash
./gradlew :app:assembleDebug
```

## Fitur UI awal

- Header brand Kawan Kasir.
- Dashboard kasir dan status shift.
- Shortcut buka/tutup shift, POS cart/checkout, cari produk/barcode, riwayat/struk.
- Preview keranjang dan total pembayaran.

Integrasi jaringan berikutnya dapat diarahkan ke backend endpoint `/api/auth/login`, `/api/shifts/open`, `/api/products/search`, `/api/sales`, dan `/api/sales/{id}/receipt`.

## Integrasi API dan printer

Kode awal `KawanKasirApiClient` sudah disiapkan untuk login, buka shift, cari produk, checkout, dan receipt preview ke backend. `PrinterService` menjadi abstraction printer thermal agar Bluetooth/USB/network printer bisa dipasang pada tahap integrasi hardware.
