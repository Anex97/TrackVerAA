// index.js - Funcionalidad para la página de login

document.getElementById('loginForm').addEventListener('submit', function(event) {
  event.preventDefault();
  
  const usuario = document.getElementById('usuario').value.trim();
  const password = document.getElementById('password').value;
  
  if (!usuario || !password) {
    alert('Por favor, ingresa usuario y contraseña.');
    return;
  }
  
  // Simular login (en producción, usar AJAX al backend)
  if (usuario === 'admin' && password === '123') {
    alert('Login exitoso. Redirigiendo al panel...');
    window.location.href = 'Panel.html';
  } else {
    alert('Usuario o contraseña incorrectos.');
  }
});