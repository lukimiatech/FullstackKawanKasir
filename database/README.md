# Database Kawan Kasir

Database produksi memakai MySQL. File `schema.sql` membuat tabel utama POS: outlet, role, permission, user, kategori, produk, stok, customer, shift, transaksi, item transaksi, dan pengaturan struk.

## Cara pakai

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

Untuk menjalankan backend dengan MySQL:

```bash
export DB_URL='jdbc:mysql://localhost:3306/kawan_kasir?useSSL=false&serverTimezone=Asia/Jakarta'
export DB_USERNAME='root'
export DB_PASSWORD='password_mysql'
export DB_DRIVER='com.mysql.cj.jdbc.Driver'
cd BE-KawanKAsir && ./gradlew bootRun
```

Default backend development tetap memakai H2 in-memory supaya bisa langsung jalan tanpa setup MySQL.

## Akun demo

Backend development in-memory menyediakan akun developer super access `lukimia` / `lukimia`. Jika memakai seed MySQL, ganti placeholder `password_hash` dengan BCrypt hash sebelum digunakan untuk login produksi.
