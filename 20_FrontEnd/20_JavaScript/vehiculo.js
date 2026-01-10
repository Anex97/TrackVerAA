// vehiculo.js - Funcionalidad para la página de agregar vehículo

function actualizarVistaPrevia() {
  document.getElementById('previewMarca').textContent = document.getElementById('marca').value || '-';
  document.getElementById('previewModelo').textContent = document.getElementById('modelo').value || '-';
  document.getElementById('previewPlacas').textContent = document.getElementById('placas').value || '-';
  document.getElementById('previewAnio').textContent = document.getElementById('anio').value || '-';
}

// Actualizar vista previa en tiempo real
document.getElementById('marca').addEventListener('input', actualizarVistaPrevia);
document.getElementById('modelo').addEventListener('input', actualizarVistaPrevia);
document.getElementById('placas').addEventListener('input', actualizarVistaPrevia);
document.getElementById('anio').addEventListener('input', actualizarVistaPrevia);

document.getElementById('vehiculoForm').addEventListener('submit', function(event) {
  event.preventDefault();
  
  const marca = document.getElementById('marca').value.trim();
  const modelo = document.getElementById('modelo').value.trim();
  const placas = document.getElementById('placas').value.trim();
  const anio = parseInt(document.getElementById('anio').value);
  
  if (!marca || !modelo || !placas || isNaN(anio) || anio < 1900 || anio > 2030) {
    alert('Por favor, completa todos los campos correctamente.');
    return;
  }
  
  // Simular guardado
  alert('Vehículo agregado exitosamente.');
  this.reset();
  actualizarVistaPrevia();
});