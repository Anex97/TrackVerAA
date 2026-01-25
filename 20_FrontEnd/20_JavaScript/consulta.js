// consulta.js - cargar posiciones del servidor, renderear tabla y mapa

let mapa;
let marcadores = [];
let registros = [];
let vehiculosMap = {};
let sortColumn = null;
let sortAsc = true;

function inicializarMapa() {
  mapa = L.map('mapa').setView([25.6866142, -100.3161139], 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(mapa);
}

async function cargarVehiculos(usuarioId) {
  try {
    const res = await fetch(`/api/vehiculos?usuarioId=${usuarioId}`);
    if (!res.ok) return;
    const lista = await res.json();
    vehiculosMap = {};
    lista.forEach(v => { vehiculosMap[v.id] = v; });
  } catch (e) {
    console.warn('No se pudieron cargar vehículos', e);
  }
}

async function cargarRegistros() {
  const usuarioJson = localStorage.getItem('usuario');
  if (!usuarioJson) {
    document.getElementById('tablaRegistros').style.display = 'none';
    document.getElementById('mapa').style.display = 'none';
    document.getElementById('msgNoLogin').textContent = 'No hay usuario logueado. Inicia sesión.';
    return;
  }
  const usuario = JSON.parse(usuarioJson);
  const uid = usuario.id;
  await cargarVehiculos(uid);
  const res = await fetch(`/api/posiciones?usuarioId=${uid}`);
  if (!res.ok) {
    console.error('Error al obtener posiciones', res.status);
    return;
  }
  registros = await res.json();
  renderTablaYMarcadores(registros);
  attachSortHandlers();
}

function renderTablaYMarcadores(lista) {
  const tbody = document.querySelector('#tablaRegistros tbody');
  tbody.innerHTML = '';
  // limpiar marcadores previos
  marcadores.forEach(m => mapa.removeLayer(m));
  marcadores = [];

  lista.forEach((r, index) => {
    const tr = document.createElement('tr');
    const placas = (r.vehiculoId && vehiculosMap[r.vehiculoId]) ? vehiculosMap[r.vehiculoId].placas : '';
    const estado = r.estado || r.descripcion || '';
    const fecha = r.fechaHora || '';

    tr.innerHTML = `<td>${r.id}</td><td>${placas}</td><td>${r.latitud}</td><td>${r.longitud}</td><td>${estado}</td><td>${fecha}</td>`;
    tbody.appendChild(tr);

    const lat = parseFloat(r.latitud);
    const lon = parseFloat(r.longitud);
    if (!isNaN(lat) && !isNaN(lon)) {
      const marcador = L.marker([lat, lon]).addTo(mapa)
        .bindPopup(`<b>${placas || 'Sin vehículo'}</b><br>${estado}<br>${fecha}`);
      marcadores.push(marcador);

      tr.addEventListener('click', () => {
        mapa.setView([lat, lon], 16);
        marcador.openPopup();
      });
      // Si no tenemos estado, intentar geocodificar inversamente (con retardo para evitar ráfagas)
      if ((!r.estado || r.estado === '') && (!r.descripcion || r.descripcion === '')) {
        setTimeout(() => reverseGeocodeAndFill(r, tr, marcador), index * 600);
      }
    }
  });

  // ajustar vista para mostrar todos
  if (marcadores.length > 0) {
    const grupo = new L.featureGroup(marcadores);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function attachSortHandlers() {
  const headers = document.querySelectorAll('#tablaRegistros thead th[data-sort]');
  headers.forEach(th => {
    th.onclick = () => {
      const col = th.getAttribute('data-sort');
      sortRegistros(col);
    };
  });
}

function sortRegistros(col) {
  if (sortColumn === col) sortAsc = !sortAsc; else { sortColumn = col; sortAsc = true; }
  registros.sort((a, b) => {
    const val = (rec, c) => {
      if (c === 'id') return Number(rec.id || 0);
      if (c === 'vehiculo') return (rec.vehiculoId && vehiculosMap[rec.vehiculoId]) ? (vehiculosMap[rec.vehiculoId].placas||'').toLowerCase() : '';
      if (c === 'latitud') return Number(rec.latitud || 0);
      if (c === 'longitud') return Number(rec.longitud || 0);
      if (c === 'estado') return (rec.estado || rec.descripcion || '').toLowerCase();
      if (c === 'fecha') return new Date(rec.fechaHora || 0).getTime();
      return '';
    };
    const va = val(a, col);
    const vb = val(b, col);
    if (va < vb) return sortAsc ? -1 : 1;
    if (va > vb) return sortAsc ? 1 : -1;
    return 0;
  });
  renderTablaYMarcadores(registros);
}

async function reverseGeocodeAndFill(rec, tr, marcador) {
  const lat = rec.latitud;
  const lon = rec.longitud;
  if (!lat || !lon) return;
  try {
    const url = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}&accept-language=es`;
    const r = await fetch(url);
    if (!r.ok) return;
    const data = await r.json();
    const addr = data.address || {};
    const estado = addr.state || addr.county || addr.region || addr.state_district || '';
    if (estado) {
      rec.estado = estado;
      // actualizar celda y popup
      if (tr && tr.cells && tr.cells[4]) tr.cells[4].textContent = estado;
      if (marcador && marcador.getPopup) {
        const contenido = marcador.getPopup().getContent();
        const placas = (rec.vehiculoId && vehiculosMap[rec.vehiculoId]) ? vehiculosMap[rec.vehiculoId].placas : 'Sin vehículo';
        marcador.setPopupContent(`<b>${placas}</b><br>${estado}<br>${rec.fechaHora || ''}`);
      }
    }
  } catch (e) {
    // silencioso; la geocodificación inversa es solo un fallback
    console.debug('reverse geocode failed', e);
  }
}

function filtrarTabla() {
  const vehiculo = document.getElementById('filtroVehiculo').value.toLowerCase();
  const lat = document.getElementById('filtroLat').value;
  const lon = document.getElementById('filtroLon').value;
  const estado = document.getElementById('filtroEstado').value.toLowerCase();
  const fechaInicio = document.getElementById('filtroFechaInicio').value;
  const fechaFin = document.getElementById('filtroFechaFin').value;

  const filas = Array.from(document.querySelectorAll('#tablaRegistros tbody tr'));
  filas.forEach((fila, i) => {
    const veh = fila.cells[1].textContent.toLowerCase();
    const latVal = fila.cells[2].textContent;
    const lonVal = fila.cells[3].textContent;
    const estadoVal = fila.cells[4].textContent.toLowerCase();
    const fechaVal = fila.cells[5].textContent;

    let visible = true;
    if (vehiculo && !veh.includes(vehiculo)) visible = false;
    if (lat && latVal !== lat) visible = false;
    if (lon && lonVal !== lon) visible = false;
    if (estado && !estadoVal.includes(estado)) visible = false;
    if (fechaInicio && new Date(fechaVal) < new Date(fechaInicio)) visible = false;
    if (fechaFin && new Date(fechaVal) > new Date(fechaFin)) visible = false;

    fila.style.display = visible ? '' : 'none';
    const marcador = marcadores[i];
    if (marcador) {
      if (visible) mapa.addLayer(marcador); else mapa.removeLayer(marcador);
    }
  });

  const visibles = marcadores.filter((_, i) => document.querySelectorAll('#tablaRegistros tbody tr')[i].style.display !== 'none');
  if (visibles.length > 0) {
    const grupo = new L.featureGroup(visibles);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function resetTabla() {
  document.querySelectorAll('#tablaRegistros tbody tr').forEach(f => f.style.display = '');
  document.querySelectorAll('.filtros input').forEach(i => i.value = '');
  if (marcadores.length > 0) {
    const grupo = new L.featureGroup(marcadores);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function descargarCSV() {
  const filas = document.querySelectorAll('#tablaRegistros tbody tr');
  let csv = 'ID,Vehículo,Latitud,Longitud,Estado,Fecha\n';
  filas.forEach(fila => {
    if (fila.style.display !== 'none') {
      const celdas = fila.querySelectorAll('td');
      const filaCSV = Array.from(celdas).map(c => '"' + c.textContent + '"').join(',');
      csv += filaCSV + '\n';
    }
  });
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = 'registros_gps.csv';
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// Inicializar
window.addEventListener('load', () => {
  inicializarMapa();
  cargarRegistros();
});