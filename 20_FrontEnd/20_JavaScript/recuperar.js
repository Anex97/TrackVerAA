// recuperar.js - Funcionalidad para la página de recuperación de contraseña

document.getElementById('recuperarForm').addEventListener('submit', function(event) {
  event.preventDefault();
  
  const correo = document.getElementById('correo').value.trim();
  
  if (!correo || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
    alert('Por favor, ingresa un correo electrónico válido.');
    return;
  }
  
  // Simular envío
  alert('Enlace de recuperación enviado a ' + correo);
  this.reset();
});