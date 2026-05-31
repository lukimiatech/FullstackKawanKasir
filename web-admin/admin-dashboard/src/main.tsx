import React, { FormEvent, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const modules = [
  ['User & Role', 'Kelola admin, kasir, role, dan permission fleksibel.', '#039CD5'],
  ['Produk & Stok', 'Kategori, SKU, barcode, stok masuk/keluar, opname.', '#078546'],
  ['Transaksi', 'Pantau invoice, void approval, dan riwayat kasir.', '#FC8200'],
  ['Laporan', 'Ringkasan omzet, stok menipis, export PDF/Excel.', '#075D92'],
];

type RegisterForm = {
  storeName: string;
  ownerName: string;
  email: string;
  username: string;
  phone: string;
  password: string;
};

function App() {
  const [form, setForm] = useState<RegisterForm>({
    storeName: '',
    ownerName: '',
    email: '',
    username: '',
    phone: '',
    password: '',
  });
  const [status, setStatus] = useState('Daftar untuk membuat outlet dan akun OWNER baru.');
  const [loading, setLoading] = useState(false);

  const update = (key: keyof RegisterForm) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setForm((current) => ({ ...current, [key]: event.target.value }));
  };

  const register = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setStatus('Mengirim pendaftaran ke backend...');
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const payload = await response.json();
      if (!response.ok) throw new Error(payload?.message ?? 'Pendaftaran gagal');
      setStatus(`Pendaftaran berhasil. Akun ${payload.data.user.username} sudah aktif sebagai ${payload.data.user.role}.`);
      localStorage.setItem('kk_access_token', payload.data.accessToken);
    } catch (error) {
      setStatus(error instanceof Error ? error.message : 'Pendaftaran gagal diproses.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="shell">
      <aside className="sidebar">
        <div className="brand-badge">KK</div>
        <h1>Kawan Kasir</h1>
        <p>Admin Dashboard</p>
        <nav>
          <a>Dashboard</a>
          <a>Pendaftaran</a>
          <a>Outlet</a>
          <a>Produk</a>
          <a>Inventori</a>
          <a>Transaksi</a>
          <a>Laporan</a>
        </nav>
        <div className="developer-card">
          <span>Akun developer</span>
          <strong>lukimia</strong>
          <small>Role DEVELOPER • super access</small>
        </div>
      </aside>
      <section className="content">
        <div className="hero">
          <div>
            <p className="eyebrow">Online-only POS platform</p>
            <h2>Kelola operasional toko dari satu dashboard.</h2>
            <p>Backend siap melayani web admin dan Android kasir melalui REST API JWT.</p>
          </div>
          <button>Login Admin</button>
        </div>

        <section className="register-panel" aria-labelledby="register-title">
          <div className="register-copy">
            <p className="eyebrow">Pendaftaran akun</p>
            <h2 id="register-title">Buat outlet dan akun owner baru.</h2>
            <p>
              Form ini memanggil endpoint <code>/api/auth/register</code>, membuat outlet,
              pengaturan struk awal, dan akun role OWNER agar toko bisa langsung dikonfigurasi.
            </p>
            <div className="status-box">{status}</div>
          </div>
          <form className="register-form" onSubmit={register}>
            <label>
              Nama toko/outlet
              <input required value={form.storeName} onChange={update('storeName')} placeholder="Contoh: Toko Sumber Rejeki" />
            </label>
            <label>
              Nama pemilik
              <input required value={form.ownerName} onChange={update('ownerName')} placeholder="Nama owner" />
            </label>
            <label>
              Email
              <input required type="email" value={form.email} onChange={update('email')} placeholder="owner@toko.com" />
            </label>
            <label>
              Username
              <input required value={form.username} onChange={update('username')} placeholder="username login" />
            </label>
            <label>
              Nomor HP
              <input value={form.phone} onChange={update('phone')} placeholder="08xxxxxxxxxx" />
            </label>
            <label>
              Password
              <input required minLength={6} type="password" value={form.password} onChange={update('password')} placeholder="Minimal 6 karakter" />
            </label>
            <button disabled={loading}>{loading ? 'Memproses...' : 'Daftar Sekarang'}</button>
          </form>
        </section>

        <div className="stats">
          <article><span>Omzet Hari Ini</span><strong>Rp 8,75 jt</strong></article>
          <article><span>Transaksi</span><strong>128</strong></article>
          <article><span>Stok Menipis</span><strong>12</strong></article>
        </div>
        <div className="grid">
          {modules.map(([title, desc, color]) => (
            <article className="module" key={title} style={{ borderTopColor: color }}>
              <h3>{title}</h3>
              <p>{desc}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')!).render(<App />);
