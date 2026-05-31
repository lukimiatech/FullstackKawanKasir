USE kawan_kasir;
INSERT INTO outlets (id, name, code, address, phone, email) VALUES ('00000000-0000-0000-0000-000000000001', 'Kawan Kasir Demo Store', 'STORE01', 'Jakarta', '081234567890', 'store@kawankasir.local');
INSERT INTO roles (id, code, name) VALUES
('10000000-0000-0000-0000-000000000000','DEVELOPER','DEVELOPER'),
('10000000-0000-0000-0000-000000000001','SUPER_ADMIN','SUPER ADMIN'),('10000000-0000-0000-0000-000000000002','OWNER','OWNER'),('10000000-0000-0000-0000-000000000003','ADMIN','ADMIN'),('10000000-0000-0000-0000-000000000004','MANAGER','MANAGER'),('10000000-0000-0000-0000-000000000005','CASHIER','CASHIER'),('10000000-0000-0000-0000-000000000006','INVENTORY_STAFF','INVENTORY STAFF'),('10000000-0000-0000-0000-000000000007','FINANCE','FINANCE');
-- Password demo runtime backend in-memory: lukimia untuk user developer dan password123 untuk admin/kasir.
-- Untuk import MySQL produksi, ganti password_hash placeholder dengan BCrypt hash baru sebelum dipakai login.
INSERT INTO users (id, outlet_id, name, email, username, phone, password_hash, role_id) VALUES
('20000000-0000-0000-0000-000000000000','00000000-0000-0000-0000-000000000001','Lukimia Developer','developer@lukimia.tech','lukimia','080000000000','$2a$10$replace-with-bcrypt-hash','10000000-0000-0000-0000-000000000000'),
('20000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','Super Admin','admin@kawankasir.local','admin','081111111111','$2a$10$replace-with-bcrypt-hash','10000000-0000-0000-0000-000000000001');
INSERT INTO categories (id, outlet_id, name, description) VALUES ('30000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','Minuman','Produk minuman favorit');
INSERT INTO products (id, outlet_id, category_id, sku, barcode, name, unit, purchase_price, selling_price, stock, min_stock) VALUES
('40000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','DRK-TEA-001','899000000001','Es Teh Manis','cup',2500,5000,100,10),
('40000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000001','30000000-0000-0000-0000-000000000001','DRK-COF-001','899000000002','Kopi Susu','cup',6000,12000,80,10);
INSERT INTO receipt_settings (id, outlet_id, store_name, header_note, footer_note) VALUES ('50000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','Kawan Kasir Demo Store','Kawan Kasir - POS modern untuk UMKM','Terima kasih sudah berbelanja di Kawan Kasir');
