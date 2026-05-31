# Backend API Kawan Kasir

Backend memakai Spring Boot, Kotlin, Spring Security, BCrypt, JWT HMAC, Validation, Spring Data JPA dependency, Flyway dependency, H2 development, dan MySQL runtime driver.

## Run development

```bash
./gradlew bootRun
```

## Run dengan MySQL

```bash
mysql -u root -p < ../database/schema.sql
mysql -u root -p < ../database/seed.sql
export DB_URL='jdbc:mysql://localhost:3306/kawan_kasir?useSSL=false&serverTimezone=Asia/Jakarta'
export DB_USERNAME='root'
export DB_PASSWORD='password_mysql'
export DB_DRIVER='com.mysql.cj.jdbc.Driver'
./gradlew bootRun
```

## Auth

Akun default development:

- Developer super access: `lukimia` / `lukimia`
- Super Admin: `admin` / `password123`
- Kasir: `kasir` / `password123`


```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"lukimia","password":"lukimia"}'
```

Gunakan header `Authorization: Bearer <accessToken>` untuk endpoint lain. Pendaftaran akun web tersedia di `POST /api/auth/register` dan membuat outlet + akun OWNER baru.

## Catatan implementasi

Saat ini API memakai in-memory store agar seluruh endpoint POS core bisa langsung dijalankan dan dites tanpa database lokal. File `database/schema.sql` sudah menyiapkan struktur MySQL untuk tahap persistence JPA berikutnya.
