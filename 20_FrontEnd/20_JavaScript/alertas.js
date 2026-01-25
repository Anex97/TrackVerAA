// alertas.js - Funcionalidad para la página de alertas
// Simple helpers for the alertas UI
async function fetchVehiculos(usuarioId) {
  try {
    const resp = await fetch('/api/vehiculos' + (usuarioId ? ('?usuarioId=' + encodeURIComponent(usuarioId)) : ''));
    if (!resp.ok) return [];
    return await resp.json();
  } catch (e) { console.error(e); return []; }
}

async function cargarVehiculosEn(selectId, usuarioId) {
  const sel = document.getElementById(selectId);
  if (!sel) return;
  sel.innerHTML = '<option value="">Cargando...</option>';
  const usuario = usuarioId || (localStorage.getItem('usuario') ? JSON.parse(localStorage.getItem('usuario')).id : null);
  const list = await fetchVehiculos(usuario);
  if (!Array.isArray(list) || list.length === 0) {
    sel.innerHTML = '<option value="">(No hay vehículos)</option>';
    return;
  }
  sel.innerHTML = list.map(v => `<option value="${v.id}">${(v.placas||'')} ${v.marca?'- '+v.marca:''}</option>`).join('');
}

document.addEventListener('DOMContentLoaded', () => {
  // wire buttons and forms
  const formCrear = document.getElementById('formCrearGeocerca');
  const btnCargar = document.getElementById('btnCargarVehiculos');
  const btnCargar2 = document.getElementById('btnCargarVehiculos2');
  const btnVer = document.getElementById('btnVerAlertas');
  const btnRef = document.getElementById('btnRefrescar');

  cargarVehiculosEn('asgVehiculoSelect');
  cargarVehiculosEn('velVehiculoSelect');
  cargarVehiculosEn('alertsVehSelect');

  if (btnCargar) btnCargar.addEventListener('click', () => cargarVehiculosEn('asgVehiculoSelect'));
  if (btnCargar2) btnCargar2.addEventListener('click', () => cargarVehiculosEn('velVehiculoSelect'));
  if (btnVer) btnVer.addEventListener('click', verAlertas);
  if (btnRef) btnRef.addEventListener('click', () => { cargarVehiculosEn('alertsVehSelect'); document.getElementById('alertsList').innerHTML=''; });

  if (formCrear) formCrear.addEventListener('submit', async (e) => {
    e.preventDefault();
    const nombre = document.getElementById('gcNombre').value;
    const usuarioId = document.getElementById('gcUsuarioId').value;
    const lat = document.getElementById('gcLat').value;
    const lon = document.getElementById('gcLon').value;
    const radio = document.getElementById('gcRadio').value;
    const body = new URLSearchParams();
    if (nombre) body.append('nombre', nombre);
    if (usuarioId) body.append('usuarioId', usuarioId);
    body.append('lat', lat);
    body.append('lon', lon);
    body.append('radio_m', radio);
    try {
      const resp = await fetch('/api/geocercas', { method: 'POST', body });
      const j = await resp.json();
      if (j.ok) {
        document.getElementById('gcCrearResult').textContent = 'Geocerca creada, id=' + j.id;
      } else {
        document.getElementById('gcCrearResult').textContent = 'Error: ' + (j.message||JSON.stringify(j));
      }
    } catch (ex) { document.getElementById('gcCrearResult').textContent = 'Error conectando'; }
  });

  const formAsg = document.getElementById('formAsignarGeocerca');
  if (formAsg) formAsg.addEventListener('submit', async (e) => {
    e.preventDefault();
    const gid = document.getElementById('asgGeocercaId').value;
    const vid = document.getElementById('asgVehiculoSelect').value;
    const body = new URLSearchParams(); body.append('geocercaId', gid); body.append('vehiculoId', vid);
    try {
      const resp = await fetch('/api/geocercas/asignar', { method: 'POST', body });
      const j = await resp.json();
      if (j.ok) document.getElementById('asgResult').textContent = 'Asignación guardada'; else document.getElementById('asgResult').textContent = 'Error: '+(j.message||JSON.stringify(j));
    } catch (ex) { document.getElementById('asgResult').textContent = 'Error conectando'; }
  });

  const formVel = document.getElementById('formVelocidad');
  if (formVel) formVel.addEventListener('submit', async (e) => {
    e.preventDefault();
    const vid = document.getElementById('velVehiculoSelect').value;
    const vel = document.getElementById('velMax').value;
    const body = new URLSearchParams(); body.append('vehiculoId', vid); body.append('vel_max_kmh', vel);
    try {
      const resp = await fetch('/api/velocidades', { method: 'POST', body });
      const j = await resp.json();
      if (j.ok) document.getElementById('velResult').textContent = 'Límite asignado, id=' + (j.id||''); else document.getElementById('velResult').textContent = 'Error: '+(j.message||JSON.stringify(j));
    } catch (ex) { document.getElementById('velResult').textContent = 'Error conectando'; }
  });
});

async function verAlertas() {
  const sel = document.getElementById('alertsVehSelect');
  const vid = sel ? sel.value : null;
  if (!vid) { document.getElementById('alertsList').innerText = 'Selecciona un vehículo'; return; }
  try {
    const resp = await fetch('/api/alertas?vehiculoId=' + encodeURIComponent(vid));
    if (!resp.ok) { document.getElementById('alertsList').innerText = 'Error al consultar'; return; }
    const list = await resp.json();
    if (!Array.isArray(list) || list.length === 0) { document.getElementById('alertsList').innerText = 'No hay alertas'; return; }
    const html = list.map(a => `<div style="border:1px solid #ddd;padding:8px;margin-bottom:6px;border-radius:6px;"><strong>${a.tipo}</strong> - ${a.descripcion}<br><small>${a.fecha} - ${a.estado}</small></div>`).join('');
    document.getElementById('alertsList').innerHTML = html;
  } catch (e) { document.getElementById('alertsList').innerText = 'Error conectando'; }
}