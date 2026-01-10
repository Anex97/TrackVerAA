// panel.js - Funcionalidad para el panel principal

function actualizarDashboard() {
  // Simular actualización de datos (en producción, AJAX)
  const vehiculos = Math.floor(Math.random() * 20) + 10;
  const registros = Math.floor(Math.random() * 100) + 30;
  const posiciones = ['Monterrey, NL', 'Guadalajara, Jal', 'CDMX', 'Tijuana, BC'];
  const ultimaPos = posiciones[Math.floor(Math.random() * posiciones.length)];
  
  document.getElementById('numVehiculos').textContent = vehiculos + ' registrados';
  document.getElementById('ultimaPosicion').textContent = ultimaPos;
  document.getElementById('numRegistros').textContent = registros + ' totales';
}

// Actualizar cada 30 segundos
setInterval(actualizarDashboard, 30000);

// Actualizar al cargar
window.onload = actualizarDashboard;