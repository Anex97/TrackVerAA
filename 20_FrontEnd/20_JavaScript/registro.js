// registro.js - Funcionalidad para la página de registro GPS

let mapa;
let marcador;

async function cargarVehiculosParaUsuario() {
  const usuarioJson = localStorage.getItem('usuario');
  const sel = document.getElementById('vehiculoSelect');
  const manual = document.getElementById('vehiculoManual');
  if (!sel) return;
  if (!usuarioJson) {
    sel.innerHTML = '<option value="">(Inicia sesión para ver vehículos)</option>';
    return;
  }
  const usuario = JSON.parse(usuarioJson);
  try {
    const resp = await fetch('/api/vehiculos?usuarioId=' + encodeURIComponent(usuario.id));
    if (!resp.ok) {
      sel.innerHTML = '<option value="">(Error cargando vehículos)</option>';
      return;
    }
    const list = await resp.json();
    if (!Array.isArray(list) || list.length === 0) {
      sel.innerHTML = '<option value="manual">No tiene vehículos (usar manual)</option>';
      sel.value = 'manual';
      manual.style.display = 'block';
      return;
    }
    sel.innerHTML = '<option value="">-- Selecciona vehículo --</option>' +
      list.map(v => `<option value="${v.id}">${(v.placas||'')} ${v.marca?'- '+v.marca:''}</option>`).join('') +
      '<option value="manual">Otro (manual)</option>';
    sel.addEventListener('change', () => {
      if (sel.value === 'manual') {
        manual.style.display = 'block';
        manual.focus();
      } else {
        manual.style.display = 'none';
      }
      actualizarVistaPrevia();
    });
  } catch (e) {
    console.error('Error cargando vehiculos', e);
    sel.innerHTML = '<option value="">(Error cargando vehículos)</option>';
  }
}

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
  const sel = document.getElementById('vehiculoSelect');
  const manual = document.getElementById('vehiculoManual');
  let veh = '-';
  if (sel) {
    if (sel.value === 'manual') veh = manual.value || '-';
    else if (sel.value) veh = sel.options[sel.selectedIndex]?.text || '-';
  }
  document.getElementById('previewVehiculo').textContent = veh;
  document.getElementById('previewLat').textContent = document.getElementById('latitud').value || '-';
  document.getElementById('previewLon').textContent = document.getElementById('longitud').value || '-';
  document.getElementById('previewDesc').textContent = document.getElementById('descripcion').value || '-';
  const velEl = document.getElementById('velocidad');
  document.getElementById('previewVel').textContent = (velEl && velEl.value) ? (velEl.value + ' km/h') : '-';
}

// Actualizar vista previa en tiempo real
document.addEventListener('DOMContentLoaded', () => {
  // wire change listeners
  const sel = document.getElementById('vehiculoSelect');
  const manual = document.getElementById('vehiculoManual');
  if (sel) sel.addEventListener('change', actualizarVistaPrevia);
  if (manual) manual.addEventListener('input', actualizarVistaPrevia);
  cargarVehiculosParaUsuario();
});
document.getElementById('latitud').addEventListener('input', actualizarVistaPrevia);
document.getElementById('longitud').addEventListener('input', actualizarVistaPrevia);
document.getElementById('descripcion').addEventListener('input', actualizarVistaPrevia);
document.getElementById('velocidad').addEventListener('input', actualizarVistaPrevia);

document.getElementById('registroForm').addEventListener('submit', function(event) {
  event.preventDefault();
  
  const sel = document.getElementById('vehiculoSelect');
  const manual = document.getElementById('vehiculoManual');
  let vehiculo = '';
  let vehiculoId = null;
  if (sel) {
    if (sel.value === 'manual') {
      vehiculo = manual.value.trim();
    } else {
      vehiculoId = sel.value ? parseInt(sel.value) : null;
      // show readable label if needed
      vehiculo = sel.options[sel.selectedIndex]?.text || '';
    }
  }
  const lat = parseFloat(document.getElementById('latitud').value);
  const lon = parseFloat(document.getElementById('longitud').value);
  const desc = document.getElementById('descripcion').value.trim();
  
  if (!vehiculo || isNaN(lat) || isNaN(lon) || !desc) {
    alert('Por favor, completa todos los campos correctamente.');
    return;
  }
  
  // Enviar manualmente la posición al servidor
  const usuarioJson = localStorage.getItem('usuario');
  if (!usuarioJson) {
    alert('Debes iniciar sesión para enviar posiciones.');
    window.location.href = 'Index.html';
    return;
  }
  const usuario = JSON.parse(usuarioJson);

  const submitBtn = document.querySelector('#registroForm button[type=submit]');
  if (submitBtn) submitBtn.disabled = true;
  (async () => {
    try {
      const body = new URLSearchParams();
      body.append('lat', String(lat));
      body.append('lon', String(lon));
      body.append('usuarioId', String(usuario.id));
      body.append('descripcion', desc);
      const velVal = document.getElementById('velocidad').value;
      if (velVal && !isNaN(parseFloat(velVal))) body.append('velocidad', String(parseFloat(velVal)));
      if (vehiculoId) body.append('vehiculoId', String(vehiculoId));
      else if (vehiculo) body.append('vehiculoPlacas', vehiculo);

      const resp = await fetch('/api/posiciones', { method: 'POST', body });
      if (resp.ok) {
        alert('Posición enviada correctamente');
        // limpiar formulario y marcador (manejar select/inputs existentes)
        const sel = document.getElementById('vehiculoSelect');
        const manual = document.getElementById('vehiculoManual');
        const legacy = document.getElementById('vehiculo');
        if (sel) sel.value = '';
        if (manual) manual.value = '';
        if (legacy) legacy.value = '';
        const latEl = document.getElementById('latitud');
        const lonEl = document.getElementById('longitud');
        const descEl = document.getElementById('descripcion');
        if (latEl) latEl.value = '';
        if (lonEl) lonEl.value = '';
        if (descEl) descEl.value = '';
        actualizarVistaPrevia();
        if (marcador) { try { mapa.removeLayer(marcador); } catch(e){} marcador = null; }
      } else {
        const txt = await resp.text().catch(()=>'');
        alert('Error enviando posición (HTTP ' + resp.status + '): ' + txt);
      }
    } catch (e) {
      console.error('Error enviando posición', e);
      alert('Error de conexión al enviar posición');
    } finally {
      if (submitBtn) submitBtn.disabled = false;
    }
  })();
});

window.onload = inicializarMapa;