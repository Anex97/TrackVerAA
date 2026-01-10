// registro.js - Funcionalidad para la página de registro GPS

let mapa;
let marcador;

function inicializarMapa() {
  mapa = L.map('mapa').setView([25.6866, -100.3161], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(mapa);

  // Al hacer clic en el mapa, colocar marcador y actualizar campos
  mapa.on('click', function(e) {
    const lat = e.latlng.lat.toFixed(6);
    const lon = e.latlng.lng.toFixed(6);

    document.getElementById('latitud').value = lat;
    document.getElementById('longitud').value = lon;

    actualizarVistaPrevia();

    if (marcador) {
      mapa.removeLayer(marcador);
    }
    marcador = L.marker([lat, lon]).addTo(mapa);
  });
}

function actualizarVistaPrevia() {
  document.getElementById('previewVehiculo').textContent = document.getElementById('vehiculo').value || '-';
  document.getElementById('previewLat').textContent = document.getElementById('latitud').value || '-';
  document.getElementById('previewLon').textContent = document.getElementById('longitud').value || '-';
  document.getElementById('previewDesc').textContent = document.getElementById('descripcion').value || '-';
}

// Actualizar vista previa en tiempo real
document.getElementById('vehiculo').addEventListener('input', actualizarVistaPrevia);
document.getElementById('latitud').addEventListener('input', actualizarVistaPrevia);
document.getElementById('longitud').addEventListener('input', actualizarVistaPrevia);
document.getElementById('descripcion').addEventListener('input', actualizarVistaPrevia);

document.getElementById('registroForm').addEventListener('submit', function(event) {
  event.preventDefault();
  
  const vehiculo = document.getElementById('vehiculo').value.trim();
  const lat = parseFloat(document.getElementById('latitud').value);
  const lon = parseFloat(document.getElementById('longitud').value);
  const desc = document.getElementById('descripcion').value.trim();
  
  if (!vehiculo || isNaN(lat) || isNaN(lon) || !desc) {
    alert('Por favor, completa todos los campos correctamente.');
    return;
  }
  
  // Simular guardado (en producción, AJAX)
  alert('Registro guardado exitosamente.');
  // Limpiar formulario
  this.reset();
  actualizarVistaPrevia();
  if (marcador) {
    mapa.removeLayer(marcador);
    marcador = null;
  }
});

window.onload = inicializarMapa;