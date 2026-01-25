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
  const tipoSelect = document.getElementById('tipoAlerta');
  const fieldsContainer = document.getElementById('fieldsContainer');
  const alertForm = document.getElementById('alertForm');

  function renderFields(){
    const t = tipoSelect ? tipoSelect.value : 'geocerca';
    if(!fieldsContainer) return;
    fieldsContainer.innerHTML = '';
    if(t === 'geocerca'){
      fieldsContainer.innerHTML = `
        <label>Usuario ID (opcional):</label>
        <input id="gcUsuarioId" placeholder="3">
        <label>Nombre:</label>
        <input id="gcNombre" placeholder="Nombre de la geocerca">
        <label>Latitud:</label>
        <input id="gcLat" type="number" step="any" placeholder="19.0">
        <label>Longitud:</label>
        <input id="gcLon" type="number" step="any" placeholder="-99.0">
        <label>Radio (m):</label>
        <input id="gcRadio" type="number" step="any" placeholder="50">
      `;
    } else {
      fieldsContainer.innerHTML = `
        <label>Vehículo:</label>
        <select id="velVehiculoSelect"><option value="">Cargando...</option></select>
        <label>Velocidad máxima (km/h):</label>
        <input id="velMax" type="number" step="any" placeholder="50">
      `;
      cargarVehiculosEn('velVehiculoSelect');
    }
  }

  if (tipoSelect) tipoSelect.addEventListener('change', renderFields);

  if (alertForm) alertForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const tipo = tipoSelect ? tipoSelect.value : 'geocerca';
    if (tipo === 'geocerca') {
      // crear geocerca
      const nombre = document.getElementById('gcNombre').value;
      const usuarioId = document.getElementById('gcUsuarioId') ? document.getElementById('gcUsuarioId').value : null;
      const lat = parseFloat(document.getElementById('gcLat').value);
      const lon = parseFloat(document.getElementById('gcLon').value);
      const radio = parseFloat(document.getElementById('gcRadio').value);
      try {
        const resp = await fetch('/api/geocercas', { method: 'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({usuarioId: usuarioId||null, nombre, latitud: lat, longitud: lon, radio_m: radio}) });
        const j = await resp.json();
        alert(j.ok ? ('Geocerca creada id=' + j.id) : ('Error: ' + (j.message||JSON.stringify(j))));
      } catch (ex) { alert('Error conectando al crear geocerca'); }
      cargarGeocercas();
    } else {
      // asignar velocidad
      const vid = document.getElementById('velVehiculoSelect').value;
      const vel = parseFloat(document.getElementById('velMax').value);
      try {
        const resp = await fetch('/api/velocidades', { method: 'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({vehiculoId: vid, vel_max_kmh: vel}) });
        const j = await resp.json();
        alert(j.ok ? ('Velocidad asignada id=' + (j.id||'')) : ('Error: ' + (j.message||JSON.stringify(j))));
      } catch (ex) { alert('Error conectando al asignar velocidad'); }
      cargarVelocidades();
    }
  });

  // initial render
  renderFields();
  // load lists
  cargarVehiculosEn('asgVehiculoSelect');
  cargarVehiculosEn('velVehiculoSelect');
  cargarVehiculosEn('alertsVehSelect');
  cargarGeocercas();
  cargarVelocidades();
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

// cargar y mostrar geocercas configuradas para el usuario
async function cargarGeocercas() {
  const usuario = localStorage.getItem('usuario') ? JSON.parse(localStorage.getItem('usuario')).id : null;
  try {
    const resp = await fetch('/api/geocercas' + (usuario ? ('?usuarioId=' + usuario) : ''));
    if (!resp.ok) { const tb = document.querySelector('#tableGeocercas tbody'); if(tb) tb.innerHTML = '<tr><td colspan="6">Error al cargar</td></tr>'; return; }
    const list = await resp.json();
    const tbody = document.querySelector('#tableGeocercas tbody');
    if(!tbody) return;
    if (!Array.isArray(list) || list.length === 0) { tbody.innerHTML = '<tr><td colspan="6">No hay geocercas</td></tr>'; return; }
    tbody.innerHTML = list.map(g => `<tr><td>${g.id}</td><td>${g.nombre}</td><td>${g.latitud}</td><td>${g.longitud}</td><td>${g.radio_m}</td><td>${g.usuario_id||''}</td></tr>`).join('');
  } catch (e) { const tb = document.querySelector('#tableGeocercas tbody'); if(tb) tb.innerHTML = '<tr><td colspan="6">Error conectando</td></tr>'; }
}

// cargar y mostrar límites de velocidad configurados
async function cargarVelocidades() {
  const usuario = localStorage.getItem('usuario') ? JSON.parse(localStorage.getItem('usuario')).id : null;
  try {
    const resp = await fetch('/api/velocidades');
    const tbody = document.querySelector('#tableVelocidades tbody');
    if(!tbody) return;
    if (!resp.ok) { tbody.innerHTML = '<tr><td colspan="3">Error al cargar</td></tr>'; return; }
    const list = await resp.json();
    if (!Array.isArray(list) || list.length === 0) { tbody.innerHTML = '<tr><td colspan="3">No hay límites asignados</td></tr>'; return; }
    // opcional: show vehicle plates by fetching vehiculos list
    const vehs = await fetchVehiculos(usuario);
    const byId = {};
    vehs.forEach(v => byId[v.id] = v);
    tbody.innerHTML = list.map(v => `<tr><td>${v.id}</td><td>${byId[v.vehiculo_id] ? (byId[v.vehiculo_id].placas + ' ' + (byId[v.vehiculo_id].marca||'')) : 'id:'+v.vehiculo_id}</td><td>${Math.round(v.vel_max_kmh)}</td></tr>`).join('');
  } catch (e) { const tb = document.querySelector('#tableVelocidades tbody'); if(tb) tb.innerHTML = '<tr><td colspan="3">Error conectando</td></tr>'; }
}

// legacy helper: verAlertas remains available as-is