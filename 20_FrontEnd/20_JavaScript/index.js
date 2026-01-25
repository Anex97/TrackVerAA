// index.js - Funcionalidad para la página de login

document.getElementById('loginForm').addEventListener('submit', async function(event) {
  event.preventDefault();

  const usuario = document.getElementById('usuario').value.trim();
  const password = document.getElementById('password').value;

  if (!usuario || !password) {
    alert('Por favor, ingresa usuario y contraseña.');
    return;
  }

  try {
    const params = new URLSearchParams();
    // El backend espera los campos 'correo' y 'contrasena'
    params.append('correo', usuario);
    params.append('contrasena', password);

    const resp = await fetch('/api/login', {
      method: 'POST',
      body: params,
    });

    if (resp.ok) {
      const data = await resp.json();
      // Guardar usuario en localStorage para uso en panel
      localStorage.setItem('usuario', JSON.stringify({ id: data.id, nombre: data.nombre, nivelAcceso: data.nivelAcceso }));
      alert('Login exitoso. Bienvenido ' + (data.nombre || ''));
      window.location.href = '/10_HTML/Panel.html';
    } else if (resp.status === 401) {
      alert('Usuario o contraseña incorrectos.');
    } else {
      alert('Error en el servidor. Código: ' + resp.status);
    }
  } catch (err) {
    console.error('Error al conectar con /api/login', err);
    alert('No se pudo conectar con el servidor.');
  }
});