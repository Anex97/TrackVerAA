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
  // Update greeting on the panel: show 'Admin' for admin users or the user's name
  try {
    const gEl = document.getElementById('greetingName');
    if (gEl) {
      if (usuario && usuario.nivelAcceso && Number(usuario.nivelAcceso) === 2) {
        gEl.textContent = 'Admin';
      } else {
        gEl.textContent = usuario.nombre || '-';
      }
    }
  } catch (e) { /* ignore DOM errors */ }

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

// (Se elimina la sección de alertas recientes; se usa solo la tabla completa abajo)

// --- Tabla completa de alertas y acciones ---
async function cargarTodasAlertas() {
  const u = localStorage.getItem('usuario');
  if (!u) return;
  const usuario = JSON.parse(u);
  try {
    const vResp = await fetch('/api/vehiculos?usuarioId=' + encodeURIComponent(usuario.id));
    let vehiculos = [];
    if (vResp.ok) vehiculos = await vResp.json();
    const vehIds = new Set((vehiculos || []).map(v => Number(v.id)));

    const aResp = await fetch('/api/alertas');
    if (!aResp.ok) { console.warn('No se pudieron cargar alertas', aResp.status); return; }
    let alertas = await aResp.json(); if (!Array.isArray(alertas)) alertas = [];

    // filtrar alertas del usuario
    const userAlerts = alertas.filter(a => vehIds.has(Number(a.vehiculo_id)));
    const tbody = document.querySelector('#tableTodasAlertas tbody');
    if (!tbody) return;
    if (userAlerts.length === 0) { tbody.innerHTML = '<tr><td colspan="8">No hay alertas</td></tr>'; return; }
    // map veh id -> label
    const vehMap = {};
    vehiculos.forEach(v => vehMap[v.id] = (v.modelo? v.modelo+' - ':'') + (v.placas||('id:'+v.id)));
    tbody.innerHTML = userAlerts.map(a => {
      const correo = (a.correo_enviado && Number(a.correo_enviado)) ? 'Sí' : 'No';
      const sms = (a.sms_enviado && Number(a.sms_enviado)) ? 'Sí' : 'No';
      const vehLabel = vehMap[a.vehiculo_id] || ('id:'+a.vehiculo_id);
      // use external SVG files for icons (button wraps img for accessibility)
      const iconCorreo = `<button class="icon-btn icon-correo" data-id="${a.id}" title="Marcar correo" aria-label="Marcar correo">`+
             `<img src="/img/envelope-check.svg" class="icon-img" alt="correo"/>`+
             `</button>`;
      const iconSms = `<button class="icon-btn icon-sms" data-id="${a.id}" title="Marcar SMS" aria-label="Marcar SMS">`+
              `<img src="/img/sms-check.svg" class="icon-img" alt="sms"/>`+
              `</button>`;

      return `<tr data-id="${a.id}"><td>${a.id}</td><td>${vehLabel}</td><td>${a.tipo||''}</td><td>${a.descripcion||''}</td><td>${a.fecha||''}</td><td class="col-correo">${correo}</td><td class="col-sms">${sms}</td><td class="action-cell">${iconCorreo} ${iconSms} <button class="btn-dismiss icon-dismiss" data-id="${a.id}" aria-label="Desestimar">×</button></td></tr>`;
    }).join('');

    // attach handlers
    // icon button handlers: update only the affected row on success
    document.querySelectorAll('.icon-correo').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      const btn = e.currentTarget;
      btn.disabled = true; btn.classList.add('loading');
      try {
        const resp = await fetch('/api/alertas/mark?id=' + encodeURIComponent(id) + '&correo=1', { method: 'POST' });
        if (resp.ok) {
          // update row cell
          const tr = document.querySelector('tr[data-id="' + id + '"]');
          if (tr) {
            const cell = tr.querySelector('.col-correo'); if (cell) cell.textContent = 'Sí';
            btn.classList.add('done');
          }
          btn.disabled = true;
          return;
        }
        alert('Error marcando correo');
      } catch (ex) { alert('Error conectando'); }
      btn.disabled = false; btn.classList.remove('loading');
    }));

    document.querySelectorAll('.icon-sms').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      const btn = e.currentTarget;
      btn.disabled = true; btn.classList.add('loading');
      try {
        const resp = await fetch('/api/alertas/mark?id=' + encodeURIComponent(id) + '&sms=1', { method: 'POST' });
        if (resp.ok) {
          const tr = document.querySelector('tr[data-id="' + id + '"]');
          if (tr) {
            const cell = tr.querySelector('.col-sms'); if (cell) cell.textContent = 'Sí';
            btn.classList.add('done');
          }
          btn.disabled = true;
          return;
        }
        alert('Error marcando SMS');
      } catch (ex) { alert('Error conectando'); }
      btn.disabled = false; btn.classList.remove('loading');
    }));
    document.querySelectorAll('.btn-dismiss').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      if (!confirm('Desestimar alerta id=' + id + '?')) return;
      try {
        const resp = await fetch('/api/alertas/dismiss?id=' + encodeURIComponent(id), { method: 'POST' });
        if (resp.ok) { await cargarTodasAlertas(); return; }
        alert('Error descartando alerta');
      } catch (ex) { alert('Error conectando'); }
    }));

  } catch (e) { console.error('Error cargando todas alertas', e); }
}

document.addEventListener('DOMContentLoaded', () => { cargarTodasAlertas(); });
setInterval(cargarTodasAlertas, 30000);

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