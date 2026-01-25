// vehiculo.js - lógica para la página de agregar vehículo (envía al backend)

function actualizarVistaPrevia() {
  document.getElementById('previewMarca').textContent = document.getElementById('marca').value || '-';
  document.getElementById('previewModelo').textContent = document.getElementById('modelo').value || '-';
  document.getElementById('previewPlacas').textContent = document.getElementById('placas').value || '-';
  document.getElementById('previewAnio').textContent = document.getElementById('anio').value || '-';
}

async function initVehiculoPage() {
  const marcaEl = document.getElementById('marca');
  const modeloEl = document.getElementById('modelo');
  const placasEl = document.getElementById('placas');
  const anioEl = document.getElementById('anio');

  marcaEl.addEventListener('input', actualizarVistaPrevia);
  modeloEl.addEventListener('input', actualizarVistaPrevia);
  placasEl.addEventListener('input', actualizarVistaPrevia);
  anioEl.addEventListener('input', actualizarVistaPrevia);

  // Prefill from query params (redirect desde Registro.html)
  try {
    const params = new URLSearchParams(window.location.search);
    const p = params.get('placas') || params.get('vehiculo');
    if (p) {
      placasEl.value = p;
    }
    // If lat/lon/desc present, store in localStorage for later use
    const lat = params.get('lat');
    const lon = params.get('lon');
    const desc = params.get('desc');
    if (lat && lon) {
      localStorage.setItem('pendingGPS', JSON.stringify({ placas: p || '', lat, lon, desc: desc || '' }));
    }
    actualizarVistaPrevia();
  } catch (e) {
    console.warn('No se pudieron leer parámetros de query:', e);
  }

  const form = document.getElementById('vehiculoForm');
  form.addEventListener('submit', async function(event) {
    event.preventDefault();
    const marca = marcaEl.value.trim();
    const modelo = modeloEl.value.trim();
    const placas = placasEl.value.trim();
    const anio = anioEl.value.trim();

    if (!marca || !placas || !anio) {
      alert('Marca, placas y año son obligatorios');
      return;
    }

    const usuarioJson = localStorage.getItem('usuario');
    if (!usuarioJson) {
      alert('No estás autenticado.');
      window.location.href = 'Index.html';
      return;
    }
    const usuario = JSON.parse(usuarioJson);

    const submitBtn = form.querySelector('button[type=submit]');
    submitBtn.disabled = true;
    try {
      const body = new URLSearchParams();
      body.append('marca', marca);
      body.append('modelo', modelo);
      body.append('placas', placas);
      body.append('anio', anio);
      body.append('usuarioId', String(usuario.id));

      const resp = await fetch('/api/vehiculos', { method: 'POST', body });
      let data = null;
      try { data = await resp.json(); } catch (e) { }

      if (resp.ok && data && data.ok) {
        alert('Vehículo creado correctamente');
        // limpiar formulario y recargar lista
        marcaEl.value = '';
        modeloEl.value = '';
        placasEl.value = '';
        anioEl.value = '';
        actualizarVistaPrevia();
        await loadVehiculos();
        submitBtn.disabled = false;
        return;
      }

      if (resp.status === 409 || (data && data.error === 'duplicate_placas')) {
        alert('Error: placas ya registradas');
      } else if (data && data.message) {
        alert('Error: ' + data.message);
      } else {
        alert('Error creando vehículo');
      }
    } catch (err) {
      console.error(err);
      alert('Error de conexión al crear vehículo');
    } finally {
      submitBtn.disabled = false;
    }
  });
}

window.addEventListener('DOMContentLoaded', initVehiculoPage);

// Cargar y renderizar vehículos del usuario
async function loadVehiculos() {
  const tbody = document.getElementById('vehiculosTbody');
  if (!tbody) return;
  const u = localStorage.getItem('usuario');
  if (!u) {
    tbody.innerHTML = '<tr><td colspan="5">Debes iniciar sesión.</td></tr>';
    return;
  }
  const usuario = JSON.parse(u);
    try {
    const resp = await fetch('/api/vehiculos?usuarioId=' + encodeURIComponent(usuario.id));
    if (!resp.ok) {
      const txt = await resp.text().catch(() => '');
      console.error('GET /api/vehiculos failed', resp.status, txt);
      tbody.innerHTML = `<tr><td colspan="5">Error al obtener vehículos (HTTP ${resp.status}): ${escapeHtml(txt || '')}</td></tr>`;
      return;
    }
    const list = await resp.json();
    if (!Array.isArray(list) || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5">No hay vehículos registrados.</td></tr>';
      return;
    }
    tbody.innerHTML = '';
    for (const v of list) {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.placas||'')}</td>` +
           `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.marca||'')}</td>` +
           `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.modelo||'')}</td>` +
           `<td style="padding:8px;border-bottom:1px solid #eee;">${v.anio||''}</td>` +
           `<td style="padding:8px;border-bottom:1px solid #eee;text-align:right;"><button type="button" data-delete-id="${v.id}" style="margin-left:8px;color:#c00;">Borrar</button></td>`;
      tbody.appendChild(tr);
    }
  } catch (e) {
    console.error('Error cargando vehículos', e);
    tbody.innerHTML = '<tr><td colspan="5">Error al obtener vehículos.</td></tr>';
  }
}

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

// Cargar vehículos al entrar
// --- Sorting + filtering state and render pipeline ---
let _vehiculosCache = [];
let _sortKey = null; // e.g. 'anio'
let _sortDir = 1; // 1 asc, -1 desc

function applyFiltersAndRender() {
  try {
    const text = (document.getElementById('vehFilterText') || {}).value || '';
    const year = (document.getElementById('vehFilterYear') || {}).value || '';
    let list = _vehiculosCache.slice();
    if (text) {
      const t = text.toLowerCase();
      list = list.filter(v => (v.placas||'').toLowerCase().includes(t) || (v.marca||'').toLowerCase().includes(t) || (v.modelo||'').toLowerCase().includes(t));
    }
    if (year) {
      list = list.filter(v => String(v.anio) === String(year));
    }
    if (_sortKey) {
      list.sort((a,b) => {
        const av = a[_sortKey] == null ? '' : a[_sortKey];
        const bv = b[_sortKey] == null ? '' : b[_sortKey];
        if (av === bv) return 0;
        if (typeof av === 'number' || typeof bv === 'number') {
          return (_sortDir) * ((Number(av) || 0) - (Number(bv) || 0));
        }
        return (_sortDir) * (String(av).localeCompare(String(bv)));
      });
    }
    _renderListToTable(list);
  } catch (err) {
    console.error('applyFiltersAndRender error', err);
    // don't clear table if filtering/rendering fails
  }
}

function _renderListToTable(list) {
  const tbody = document.getElementById('vehiculosTbody');
  if (!tbody) return;
  // build rows first to avoid intermediate empty state
  if (!Array.isArray(list) || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5">No hay vehículos que coincidan.</td></tr>';
    return;
  }
  console.log('_renderListToTable: rendering', list.length, 'rows');
  const frag = document.createDocumentFragment();
  for (const v of list) {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.placas||'')}</td>` +
         `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.marca||'')}</td>` +
         `<td style="padding:8px;border-bottom:1px solid #eee;">${escapeHtml(v.modelo||'')}</td>` +
         `<td style="padding:8px;border-bottom:1px solid #eee;">${v.anio||''}</td>` +
         `<td style="padding:8px;border-bottom:1px solid #eee;text-align:right;"><button type="button" data-delete-id="${v.id}" style="margin-left:8px;color:#c00;">Borrar</button></td>`;
    frag.appendChild(tr);
  }
  // commit fragment
  tbody.innerHTML = '';
  tbody.appendChild(frag);
}

  // --- Delete modal handling ---
  function openDeleteModal(id, placas) {
    const modal = document.getElementById('deleteModal');
    if (!modal) return;
    console.log('openDeleteModal', id, placas);
    modal.dataset.deleteId = id;
    const placasEl = document.getElementById('deleteModalPlacas');
    const pwdEl = document.getElementById('deleteModalPassword');
    const errEl = document.getElementById('deleteModalError');
    if (placasEl) placasEl.textContent = placas || '';
    if (pwdEl) pwdEl.value = '';
    if (errEl) errEl.textContent = '';
    modal.style.display = 'flex';
    setTimeout(() => { if (pwdEl) pwdEl.focus(); }, 50);
  }

  function closeDeleteModal() {
    const modal = document.getElementById('deleteModal');
    if (!modal) return;
    modal.style.display = 'none';
    modal.dataset.deleteId = '';
  }

  // wire modal buttons (idempotent)
  window.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('deleteModal');
    if (!modal) return;
    const confirmBtn = document.getElementById('deleteModalConfirm');
    const cancelBtn = document.getElementById('deleteModalCancel');
    const pwdEl = document.getElementById('deleteModalPassword');
    const errEl = document.getElementById('deleteModalError');

    cancelBtn?.addEventListener('click', () => { closeDeleteModal(); });

    confirmBtn?.addEventListener('click', async () => {
      const id = modal.dataset.deleteId;
      const pwd = pwdEl ? pwdEl.value : '';
      errEl.textContent = '';
      if (!pwd) { errEl.textContent = 'Ingrese la contraseña'; return; }
      const u = localStorage.getItem('usuario');
      if (!u) { alert('No autenticado'); closeDeleteModal(); return; }
      const usuario = JSON.parse(u);
      confirmBtn.disabled = true;
      try {
        const body = new URLSearchParams();
        body.append('id', id);
        body.append('usuarioId', String(usuario.id));
        body.append('password', pwd);
        const resp = await fetch('/api/vehiculos/delete', { method: 'POST', body });
        let data = null;
        try { data = await resp.json(); } catch (e) { }
        if (resp.ok && data && data.ok) {
          alert('Vehículo eliminado');
          closeDeleteModal();
          await loadVehiculos();
          return;
        }
        if (resp.status === 401) {
          errEl.textContent = 'Contraseña incorrecta';
        } else if (resp.status === 403) {
          errEl.textContent = 'No autorizado para eliminar este vehículo';
        } else if (data && data.message) {
          errEl.textContent = data.message;
        } else {
          errEl.textContent = 'No se pudo eliminar el vehículo';
        }
      } catch (e) {
        console.error('Error borrando vehículo', e);
        errEl.textContent = 'Error de conexión';
      } finally {
        confirmBtn.disabled = false;
      }
    });

    // keyboard support: Enter to confirm, Esc to cancel
    pwdEl?.addEventListener('keydown', (ev) => {
      if (ev.key === 'Enter') { ev.preventDefault(); confirmBtn.click(); }
      if (ev.key === 'Escape') { ev.preventDefault(); closeDeleteModal(); }
    });
  });

function setupTableInteractions() {
  // Sorting by clicking the header
  const ths = document.querySelectorAll('#vehiculosTable thead th[data-sort]');
  ths.forEach(th => {
    th.addEventListener('click', () => {
      const key = th.getAttribute('data-sort');
      if (_sortKey === key) _sortDir = -_sortDir; else { _sortKey = key; _sortDir = 1; }
      // visual indicator (simple)
      ths.forEach(x => x.textContent = x.textContent.replace(/\s*[↕↑↓]$/,''));
      const arrow = _sortDir === 1 ? ' ↑' : ' ↓';
      th.textContent = th.textContent.replace(/\s*[↕↑↓]$/,'') + arrow;
      applyFiltersAndRender();
    });
  });

  // Filters
  const txt = document.getElementById('vehFilterText');
  const sel = document.getElementById('vehFilterYear');
  const clear = document.getElementById('vehClearFilters');
  if (txt) txt.addEventListener('input', () => applyFiltersAndRender());
  if (sel) sel.addEventListener('change', () => applyFiltersAndRender());
  if (clear) clear.addEventListener('click', () => { if (txt) txt.value=''; if (sel) sel.value=''; applyFiltersAndRender(); });
}

// Replace previous loadVehiculos success rendering to cache list and populate filters
async function loadVehiculos() {
  const tbody = document.getElementById('vehiculosTbody');
  if (!tbody) return;
  const u = localStorage.getItem('usuario');
  if (!u) {
    tbody.innerHTML = '<tr><td colspan="5">Debes iniciar sesión.</td></tr>';
    return;
  }
  const usuario = JSON.parse(u);
  try {
    const resp = await fetch('/api/vehiculos?usuarioId=' + encodeURIComponent(usuario.id));
    if (!resp.ok) {
      const txt = await resp.text().catch(() => '');
      console.error('GET /api/vehiculos failed', resp.status, txt);
      tbody.innerHTML = `<tr><td colspan="5">Error al obtener vehículos (HTTP ${resp.status}): ${escapeHtml(txt || '')}</td></tr>`;
      return;
    }
    const list = await resp.json();
    _vehiculosCache = Array.isArray(list) ? list : [];

    // populate year filter options
    const yearSel = document.getElementById('vehFilterYear');
    if (yearSel) {
      const years = Array.from(new Set(_vehiculosCache.map(v => v.anio).filter(y=>y))).sort((a,b)=>b-a);
      yearSel.innerHTML = '<option value="">Todos los años</option>' + years.map(y=>`<option value="${y}">${y}</option>`).join('');
    }

    // initialize interactions once
    setupTableInteractions();
    applyFiltersAndRender();
  } catch (e) {
    console.error('Error cargando vehículos', e);
    tbody.innerHTML = '<tr><td colspan="5">Error al obtener vehículos.</td></tr>';
  }
}

// Cargar vehículos al entrar
window.addEventListener('DOMContentLoaded', () => { setTimeout(loadVehiculos, 50); });

// Event delegation for table actions to avoid per-button binding bugs
window.addEventListener('DOMContentLoaded', () => {
  const tbody = document.getElementById('vehiculosTbody');
  if (!tbody) return;
  tbody.addEventListener('click', (ev) => {
    try {
      console.log('tbody click event target=', ev.target);
      const target = ev.target;
      if (!(target instanceof Element)) return;
      if (target.matches('button[data-delete-id]')) {
        ev.preventDefault();
        ev.stopPropagation();
        ev.stopImmediatePropagation();
        const id = target.getAttribute('data-delete-id');
        const placas = target.closest('tr').querySelector('td')?.textContent || '';
        console.log('delegated delete click', id, placas);
        openDeleteModal(id, placas.trim());
        return;
      }
    } catch (e) {
      console.error('delegated handler error', e);
    }
  });
});

// Debugging aids: capture all clicks and observe tbody mutations
window.addEventListener('DOMContentLoaded', () => {
  // capture phase click logger
  document.addEventListener('click', (ev) => {
    try {
      console.log('CAPTURE click:', ev.target, 'classList=', ev.target.classList?.toString());
    } catch (e) { console.error('capture click log error', e); }
  }, true);

  // observe tbody for unexpected clears
  const tbody = document.getElementById('vehiculosTbody');
  if (!tbody) return;
  const mo = new MutationObserver((mutations) => {
    for (const m of mutations) {
      try {
        console.warn('tbody mutation', m.type, m);
        console.warn('tbody now childCount=', tbody.childElementCount, 'innerHTML length=', tbody.innerHTML.length);
        console.warn(new Error('stack').stack);
      } catch (e) { console.error('mutation observer error', e); }
    }
  });
  mo.observe(tbody, { childList: true, subtree: true, characterData: true });
});

// Prevent accidental form submits triggered by buttons outside of form scope
window.addEventListener('DOMContentLoaded', () => {
  document.addEventListener('submit', (e) => {
    console.warn('Unexpected submit prevented for', e.target);
    e.preventDefault();
  }, true);
});