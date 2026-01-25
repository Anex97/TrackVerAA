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