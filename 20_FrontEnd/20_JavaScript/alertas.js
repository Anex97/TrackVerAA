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

// Muestra modal para pedir contraseña (y opcionalmente usuarioId si no hay sesión)
// Devuelve { usuarioId, password } o null si el usuario cancela
function showPasswordModal() {
  return new Promise((resolve) => {
    // crear overlay
    const modal = document.createElement('div'); modal.className = 'modal';
    const content = document.createElement('div'); content.className = 'modal-content';
    const title = document.createElement('h3'); title.innerText = 'Confirmar eliminación';
    const info = document.createElement('p'); info.innerText = 'Por favor ingresa tu contraseña para confirmar la acción.';
    const form = document.createElement('div');
    form.className = 'modal-form';

    // si no hay usuario en session pedir id
    const stored = localStorage.getItem('usuario');
    let usuarioId = stored ? JSON.parse(stored).id : null;
    let userInput = null;
    if (!usuarioId) {
      const lbl = document.createElement('label'); lbl.innerText = 'Usuario ID:';
      userInput = document.createElement('input'); userInput.type = 'number'; userInput.placeholder = 'Tu id de usuario';
      form.appendChild(lbl); form.appendChild(userInput);
    }

    const lbl2 = document.createElement('label'); lbl2.innerText = 'Contraseña:';
    const passInput = document.createElement('input'); passInput.type = 'password'; passInput.placeholder = 'Contraseña';
    form.appendChild(lbl2); form.appendChild(passInput);

    const actions = document.createElement('div'); actions.className = 'modal-actions';
    const btnCancel = document.createElement('button'); btnCancel.innerText = 'Cancelar'; btnCancel.className = 'btn-cancel';
    const btnOk = document.createElement('button'); btnOk.innerText = 'Confirmar'; btnOk.className = 'btn-confirm';
    actions.appendChild(btnCancel); actions.appendChild(btnOk);

    content.appendChild(title); content.appendChild(info); content.appendChild(form); content.appendChild(actions);
    modal.appendChild(content); document.body.appendChild(modal);

    passInput.focus();

    function cleanup() { modal.remove(); }
    btnCancel.addEventListener('click', () => { cleanup(); resolve(null); });
    btnOk.addEventListener('click', () => {
      const pwd = passInput.value || '';
      let uid = usuarioId;
      if (!uid && userInput) uid = parseInt(userInput.value || '0');
      if (!uid || !pwd) { alert('Se requiere usuario y contraseña'); return; }
      cleanup(); resolve({ usuarioId: uid, password: pwd });
    });
    // allow Enter key
    passInput.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') btnOk.click(); });
    if (userInput) userInput.addEventListener('keydown', (ev) => { if (ev.key === 'Enter') passInput.focus(); });
  });
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
        <label>Vehículo (opcional):</label>
        <select id="gcVehiculoSelect"><option value="">Cargando...</option></select>
        <label>Nombre:</label>
        <input id="gcNombre" placeholder="Nombre de la geocerca">
        <label>Latitud:</label>
        <input id="gcLat" type="number" step="any" placeholder="19.0">
        <label>Longitud:</label>
        <input id="gcLon" type="number" step="any" placeholder="-99.0">
        <label>Radio (m):</label>
        <input id="gcRadio" type="number" step="any" placeholder="50">
      `;
      // Cargar lista de vehículos para asignación directa
      cargarVehiculosEn('gcVehiculoSelect');
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
      const vehSel = document.getElementById('gcVehiculoSelect');
      const vehId = vehSel ? vehSel.value : null;
      const lat = parseFloat(document.getElementById('gcLat').value);
      const lon = parseFloat(document.getElementById('gcLon').value);
      const radio = parseFloat(document.getElementById('gcRadio').value);
      try {
        // Enviar parámetros en querystring para compatibilidad con el backend
        const qs = new URLSearchParams();
        if (nombre) qs.set('nombre', nombre);
        if (!isNaN(lat)) qs.set('lat', String(lat));
        if (!isNaN(lon)) qs.set('lon', String(lon));
        if (!isNaN(radio)) qs.set('radio_m', String(radio));
        // Nota: mantenemos soporte histórico para usuarioId si existe en localStorage
        const usuarioId = localStorage.getItem('usuario') ? JSON.parse(localStorage.getItem('usuario')).id : null;
        if (usuarioId) qs.set('usuarioId', String(usuarioId));
        const resp = await fetch('/api/geocercas?' + qs.toString(), { method: 'POST' });
        const j = await resp.json();
        if (j.ok) {
          alert('Geocerca creada id=' + j.id);
          // Si el usuario seleccionó un vehículo, asignar la geocerca a dicho vehículo
          if (vehId) {
            try {
              await fetch('/api/geocercas/asignar?geocercaId=' + j.id + '&vehiculoId=' + encodeURIComponent(vehId), { method: 'POST' });
            } catch (ex2) { console.warn('No se pudo asignar geocerca al vehículo', ex2); }
          }
        } else {
          alert('Error: ' + (j.message||JSON.stringify(j)));
        }
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
    // Primero intentamos con filtro por usuario (si existe)
    let url = '/api/geocercas' + (usuario ? ('?usuarioId=' + usuario) : '');
    let resp = await fetch(url);
    if (!resp.ok) { const tb = document.querySelector('#tableGeocercas tbody'); if(tb) tb.innerHTML = '<tr><td colspan="7">Error al cargar</td></tr>'; return; }
    let list = await resp.json();
    // Si la consulta filtrada devolvió vacío y había un usuario, reintentar sin filtro global
    if (usuario && Array.isArray(list) && list.length === 0) {
      try {
        resp = await fetch('/api/geocercas');
        if (resp.ok) list = await resp.json();
      } catch (ex) {
        console.warn('Reintento sin filtro falló', ex);
      }
    }
    const tbody = document.querySelector('#tableGeocercas tbody');
    if(!tbody) return;
    if (!Array.isArray(list) || list.length === 0) { tbody.innerHTML = '<tr><td colspan="7">No hay geocercas</td></tr>'; return; }
    // Obtener asignaciones y vehículos para mostrar etiqueta de vehículo/placas
    let asigns = [];
    try {
      const r2 = await fetch('/api/geocercas/asignaciones');
      if (r2.ok) asigns = await r2.json();
    } catch (ex) { console.warn('No se pudo cargar asignaciones', ex); }
    let vehs = [];
    try {
      const r3 = await fetch('/api/vehiculos');
      if (r3.ok) vehs = await r3.json();
    } catch (ex) { console.warn('No se pudo cargar vehículos', ex); }
    const vehById = {};
    if (Array.isArray(vehs)) vehs.forEach(v => vehById[v.id] = v);
    // agrupar asignaciones por geocerca
    const asignByGeo = {};
    if (Array.isArray(asigns)) asigns.forEach(a => {
      if (!asignByGeo[a.geocerca_id]) asignByGeo[a.geocerca_id] = [];
      asignByGeo[a.geocerca_id].push(a.vehiculo_id);
    });

    // Filtrar geocercas para mostrar solo las relacionadas con el usuario (por propietario o por vehículo asignado)
    let filteredList = list;
    if (usuario) {
      filteredList = list.filter(g => {
        if (g.usuario_id && Number(g.usuario_id) === Number(usuario)) return true;
        const assigned = asignByGeo[g.id] || [];
        for (const vid of assigned) {
          const v = vehById[vid];
          if (v && v.usuarioId && Number(v.usuarioId) === Number(usuario)) return true;
        }
        return false;
      });
    } else {
      // si no hay usuario logueado no mostrar geocercas
      filteredList = [];
    }

    tbody.innerHTML = filteredList.map(g => {
      const assigned = asignByGeo[g.id] || [];
      const vehLabels = assigned.map(id => {
        const v = vehById[id];
        return v ? ((v.modelo?v.modelo+' - ':'') + (v.placas||'')) : ('id:'+id);
      }).join(', ');
      const ownerLabel = vehLabels || (g.usuario_id ? ('usuario:'+g.usuario_id) : '');
      return `<tr data-id="${g.id}"><td>${g.id}</td><td>${g.nombre}</td><td>${g.latitud}</td><td>${g.longitud}</td><td>${g.radio_m}</td><td>${ownerLabel}</td><td class="action-cell"><button class="btn-delete-vel" data-id="${g.id}" title="Eliminar">&times;</button></td></tr>`;
    }).join('');
    // attach delete handlers for geocercas (with password confirmation)
    document.querySelectorAll('#tableGeocercas .btn-delete-vel').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      // pedir contraseña (y usuario si no está logueado)
      const cred = await showPasswordModal();
      if (!cred) return;
      try {
        const url = '/api/geocercas/delete?id=' + encodeURIComponent(id) + '&usuarioId=' + encodeURIComponent(cred.usuarioId) + '&password=' + encodeURIComponent(cred.password);
        const resp = await fetch(url, { method: 'POST' });
        if (resp.ok) {
          const j = await resp.json();
          if (j.ok) {
            const row = document.querySelector(`#tableGeocercas tr[data-id=\"${id}\"]`);
            if (row) row.remove();
            return;
          }
          alert('Error: ' + (j.message||JSON.stringify(j)));
          return;
        }
        const txt = await resp.text();
        alert('Error: ' + resp.status + ' ' + txt);
      } catch (ex) { alert('Error conectando al eliminar geocerca'); }
    }));
  } catch (e) { const tb = document.querySelector('#tableGeocercas tbody'); if(tb) tb.innerHTML = '<tr><td colspan="7">Error conectando</td></tr>'; }
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
    // obtener lista completa de vehículos (sin filtrar por usuario) para mostrar modelo/placas
    const vehs = await fetchVehiculos();
    const byId = {};
    vehs.forEach(v => byId[v.id] = v);
    tbody.innerHTML = list.map(v => {
      const veh = byId[v.vehiculo_id];
      const vehLabel = veh ? ((veh.modelo?veh.modelo+' - ':'') + (veh.placas||'')) : ('id:'+v.vehiculo_id);
      return `<tr data-id="${v.id}"><td>${v.id}</td><td>${vehLabel}</td><td>${Math.round(v.vel_max_kmh)}</td><td class="action-cell"><button class="btn-delete-vel" data-id="${v.id}" title="Eliminar">&times;</button></td></tr>`;
    }).join('');
    // attach handlers (require password confirmation)
    document.querySelectorAll('.btn-delete-vel').forEach(b => b.addEventListener('click', async (e) => {
      const id = e.currentTarget.getAttribute('data-id');
      const cred = await showPasswordModal();
      if (!cred) return;
      try {
        const url = '/api/velocidades/delete?id=' + encodeURIComponent(id) + '&usuarioId=' + encodeURIComponent(cred.usuarioId) + '&password=' + encodeURIComponent(cred.password);
        const resp = await fetch(url, { method: 'POST' });
        if (resp.ok) {
          const j = await resp.json();
          if (j.ok) {
            const row = document.querySelector(`#tableVelocidades tr[data-id=\"${id}\"]`);
            if (row) row.remove();
            return;
          }
          alert('Error: ' + (j.message||JSON.stringify(j)));
          return;
        }
        const txt = await resp.text();
        alert('Error: ' + resp.status + ' ' + txt);
      } catch (ex) { alert('Error conectando al eliminar'); }
    }));
  } catch (e) { const tb = document.querySelector('#tableVelocidades tbody'); if(tb) tb.innerHTML = '<tr><td colspan="3">Error conectando</td></tr>'; }
}

// Make table headers sortable by clicking on <th class="sortable"> elements
function makeTableSortable(tableId) {
  const table = document.getElementById(tableId);
  if (!table) return;
  const tbody = table.tBodies[0];
  const headers = table.querySelectorAll('th.sortable');
  headers.forEach((th, index) => {
    th.addEventListener('click', () => {
      // determine current sort state
      const currentlyAsc = th.classList.contains('sorted-asc');
      headers.forEach(h => h.classList.remove('sorted-asc','sorted-desc'));
      th.classList.add(currentlyAsc ? 'sorted-desc' : 'sorted-asc');
      const rows = Array.from(tbody.querySelectorAll('tr'));
      const colIndex = Array.from(th.parentNode.children).indexOf(th);
      rows.sort((a,b) => {
        const aText = a.children[colIndex].innerText.trim();
        const bText = b.children[colIndex].innerText.trim();
        const aNum = parseFloat(aText.replace(/[^0-9.-]/g,''));
        const bNum = parseFloat(bText.replace(/[^0-9.-]/g,''));
        if (!isNaN(aNum) && !isNaN(bNum)) return (currentlyAsc ? -1 : 1) * (aNum - bNum);
        return (currentlyAsc ? -1 : 1) * aText.localeCompare(bText, undefined, {numeric:true});
      });
      // re-append
      rows.forEach(r => tbody.appendChild(r));
    });
  });
}

// attach sortable behavior after content loads
document.addEventListener('DOMContentLoaded', () => {
  // initialize sortables (they will work after first render too)
  makeTableSortable('tableGeocercas');
  makeTableSortable('tableVelocidades');
});

// legacy helper: verAlertas remains available as-is