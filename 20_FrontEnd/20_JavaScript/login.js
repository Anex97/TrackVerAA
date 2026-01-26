// login.js - small UI effects for animated login
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  const card = document.querySelector('.login-card');
  const btn = form.querySelector('.btn-login');
  const user = document.getElementById('usuario');
  const pass = document.getElementById('password');

  // enable float-label behavior when autofill or prefilled
  Array.from(form.querySelectorAll('input')).forEach(inp => {
    // mark filled state for float labels
    const update = () => { if (inp.value && inp.value.length>0) inp.classList.add('filled'); else inp.classList.remove('filled'); };
    update();
    inp.addEventListener('input', update);
    // also on blur to catch autofill
    inp.addEventListener('blur', update);
  });

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    btn.disabled = true;
    btn.classList.add('loading');
    // small visual delay to show shimmer
    await new Promise(r => setTimeout(r, 450));
    // Basic credential check against backend (if available)
    try {
      const fd = new URLSearchParams();
      // Backend expects form fields 'correo' and 'contrasena'
      fd.append('correo', user.value);
      fd.append('contrasena', pass.value);
      const resp = await fetch('/api/login', { method: 'POST', body: fd }).catch(()=>null);
      const msgEl = document.getElementById('loginMessage');
      if (resp) {
        if (resp.ok) {
          const j = await resp.json().catch(()=>null);
          if (j && j.ok) {
            const usuario = { id: j.id, nombre: j.nombre, nivelAcceso: j.nivelAcceso };
            localStorage.setItem('usuario', JSON.stringify(usuario));
            // show success message then redirect
            if (msgEl) { msgEl.textContent = 'Acceso correcto. Bienvenido ' + (j.nombre||''); msgEl.className = 'login-message visible success'; }
            setTimeout(() => { window.location.href = 'Panel.html'; }, 900);
            return;
          }
        } else if (resp.status === 401) {
          if (msgEl) { msgEl.textContent = 'Credenciales inválidas'; msgEl.className = 'login-message visible error'; }
          // shake
          card.classList.remove('shake'); void card.offsetWidth; card.classList.add('shake'); setTimeout(() => card.classList.remove('shake'), 700);
          return;
        } else {
          // try parse message
          let bodyText = '';
          try { bodyText = await resp.text(); } catch(e){}
          if (msgEl) { msgEl.textContent = 'Error del servidor: ' + (bodyText || resp.status); msgEl.className = 'login-message visible error'; }
          return;
        }
      }
      // fallback: simple client-side success when user/password both 'admin' (dev convenience)
      if (user.value === 'admin' && pass.value === 'admin') {
        localStorage.setItem('usuario', JSON.stringify({ id:1, nombre:'Admin' }));
        const msgEl = document.getElementById('loginMessage');
        if (msgEl) { msgEl.textContent = 'Acceso correcto. Bienvenido Admin'; msgEl.className = 'login-message visible success'; }
        setTimeout(() => { window.location.href = 'Panel.html'; }, 600);
        return;
      }
      // if we get here, auth failed (no response)
      const msgEl2 = document.getElementById('loginMessage');
      if (msgEl2) { msgEl2.textContent = 'Error de conexión'; msgEl2.className = 'login-message visible error'; }
      card.classList.remove('shake'); void card.offsetWidth; card.classList.add('shake'); setTimeout(() => card.classList.remove('shake'), 700);
    } catch (err) {
      console.warn('Login error', err);
      card.classList.add('shake');
      setTimeout(() => card.classList.remove('shake'), 700);
    } finally {
      btn.disabled = false;
      btn.classList.remove('loading');
    }
  });
});
