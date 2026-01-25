// panel.js - Funcionalidad para el panel principal

async function actualizarDashboard() {
  // Obtener usuario desde localStorage
  const u = localStorage.getItem('usuario');
  if (!u) {
    // No hay usuario, volver al login
    window.location.href = 'Index.html';
    return;
  }
  const usuario = JSON.parse(u);

  try {
    // Vehículos (por usuario)
    const vResp = await fetch('/api/vehiculos/count?usuarioId=' + encodeURIComponent(usuario.id));
    const vData = await vResp.json();
    document.getElementById('numVehiculos').textContent = (vData.count || 0) + ' registrados';

    // Última posición del usuario
    console.log('Obteniendo última posición para usuario', usuario.id);
    const ultimaResp = await fetch('/api/posiciones/ultima?usuarioId=' + encodeURIComponent(usuario.id));
    if (ultimaResp.ok) {
      const ultima = await ultimaResp.json();
      console.log('Última posición (raw):', ultima);
      if (ultima && ultima.fechaHora) {
        const lat = Number(ultima.latitud);
        const lon = Number(ultima.longitud);
        const latText = Number.isFinite(lat) ? lat.toFixed(6) : ultima.latitud;
        const lonText = Number.isFinite(lon) ? lon.toFixed(6) : ultima.longitud;
        let vehText = '';
        if (ultima.vehiculoPlacas) {
          vehText = ` - ${ultima.vehiculoMarca || ''} (${ultima.vehiculoPlacas})`;
        }
        document.getElementById('ultimaPosicion').textContent = `${latText}, ${lonText} (${ultima.fechaHora})${vehText}`;
      } else {
        document.getElementById('ultimaPosicion').textContent = 'Sin posiciones registradas';
      }
    } else {
      console.error('Error HTTP al solicitar última posición', ultimaResp.status);
      document.getElementById('ultimaPosicion').textContent = 'Error al obtener última posición';
    }

    // Lista de posiciones (registros): solo contamos cuántas posiciones tiene el usuario
    const regsResp = await fetch('/api/posiciones?usuarioId=' + encodeURIComponent(usuario.id));
    if (regsResp.ok) {
      const regs = await regsResp.json();
      console.log('Registros obtenidos:', regs.length);
      document.getElementById('numRegistros').textContent = (regs.length || 0) + ' totales';
    } else {
      console.error('Error HTTP al solicitar registros', regsResp.status);
      document.getElementById('numRegistros').textContent = 'Error al obtener registros';
    }
  } catch (err) {
    console.error('Error actualizando dashboard', err);
  }
}

// Actualizar cada 30 segundos
setInterval(actualizarDashboard, 30000);

// Actualizar al cargar
document.addEventListener('DOMContentLoaded', actualizarDashboard);

// Ejecutar una vez inmediatamente por si el evento ya ocurrió
actualizarDashboard();

// --- Modal agregar vehículo ---
function showAddVehModal() {
  document.getElementById('modalAddVeh').style.display = 'block';
}
function hideAddVehModal() {
  document.getElementById('modalAddVeh').style.display = 'none';
}

document.addEventListener('DOMContentLoaded', () => {
  const btn = document.getElementById('btnAddVeh');
  if (btn) btn.addEventListener('click', () => { window.location.href = 'vehiculo.html'; });
  const cancel = document.getElementById('vehCancel');
  if (cancel) cancel.addEventListener('click', hideAddVehModal);
  const save = document.getElementById('vehSave');
  if (save) save.addEventListener('click', async () => {
    const u = localStorage.getItem('usuario');
    if (!u) { window.location.href = 'Index.html'; return; }
    const usuario = JSON.parse(u);
    const marca = document.getElementById('vehMarca').value.trim();
    const modelo = document.getElementById('vehModelo').value.trim();
    const placas = document.getElementById('vehPlacas').value.trim();
    const anio = document.getElementById('vehAnio').value;
    if (!marca || !placas || !anio) {
      alert('Marca, placas y año son requeridos');
      return;
    }
    try {
      const saveBtn = document.getElementById('vehSave');
      saveBtn.disabled = true;
      const fd = new URLSearchParams();
      fd.append('marca', marca);
      fd.append('modelo', modelo);
      fd.append('placas', placas);
      fd.append('anio', anio);
      fd.append('usuarioId', String(usuario.id));
      const resp = await fetch('/api/vehiculos', { method: 'POST', body: fd });
      const data = await resp.json().catch(() => null);
      if (resp.ok && data && data.ok) {
        hideAddVehModal();
        // limpiar campos
        document.getElementById('vehMarca').value = '';
        document.getElementById('vehModelo').value = '';
        document.getElementById('vehPlacas').value = '';
        // refrescar dashboard
        await actualizarDashboard();
        alert('Vehículo agregado correctamente');
        saveBtn.disabled = false;
        return;
      }
      // Manejo de errores conocidos
      if (resp.status === 409 || (data && data.error === 'duplicate_placas')) {
        alert('Error: las placas ya están registradas. Usa otras placas.');
      } else if (data && data.message) {
        alert('Error al agregar vehículo: ' + data.message);
      } else {
        alert('Error al agregar vehículo. Intenta nuevamente.');
      }
      saveBtn.disabled = false;
    } catch (e) {
      console.error(e);
      alert('Error al agregar vehículo (no se pudo conectar)');
      const saveBtn = document.getElementById('vehSave'); if (saveBtn) saveBtn.disabled = false;
    }
  });
});