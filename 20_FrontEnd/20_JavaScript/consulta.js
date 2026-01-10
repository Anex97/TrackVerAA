// consulta.js - Funcionalidad para la página de consulta de registros

let mapa;
let marcadores = [];

function inicializarMapa() {
  mapa = L.map('mapa').setView([25.6866142, -100.3161139], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(mapa);

  // Agregar marcadores para todas las filas
  const filas = document.querySelectorAll("#tablaRegistros tbody tr");
  filas.forEach(fila => {
    const lat = parseFloat(fila.cells[2].textContent);
    const lon = parseFloat(fila.cells[3].textContent);
    const vehiculo = fila.cells[1].textContent;
    const estado = fila.cells[4].textContent;
    const fecha = fila.cells[5].textContent;

    const marcador = L.marker([lat, lon]).addTo(mapa)
      .bindPopup(`<b>${vehiculo}</b><br>Estado: ${estado}<br>Fecha: ${fecha}`);

    marcadores.push(marcador);

    // Hacer que al hacer clic en la fila, el mapa se centre en el marcador
    fila.onclick = () => {
      mapa.setView([lat, lon], 16);
      marcador.openPopup();
    };
  });

  // Ajustar el zoom para mostrar todos los marcadores
  if (marcadores.length > 0) {
    const grupo = new L.featureGroup(marcadores);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function mostrarMapa(lat, lon) {
  // Esta función ya no es necesaria, pero la dejamos por compatibilidad
  mapa.setView([lat, lon], 16);
}

function filtrarTabla() {
  const vehiculo = document.getElementById("filtroVehiculo").value.toLowerCase();
  const lat = document.getElementById("filtroLat").value;
  const lon = document.getElementById("filtroLon").value;
  const estado = document.getElementById("filtroEstado").value.toLowerCase();
  const fechaInicio = document.getElementById("filtroFechaInicio").value;
  const fechaFin = document.getElementById("filtroFechaFin").value;

  const filas = document.querySelectorAll("#tablaRegistros tbody tr");
  filas.forEach(fila => {
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

    fila.style.display = visible ? "" : "none";

    // Mostrar/ocultar marcador correspondiente
    const index = Array.from(filas).indexOf(fila);
    if (marcadores[index]) {
      if (visible) {
        mapa.addLayer(marcadores[index]);
      } else {
        mapa.removeLayer(marcadores[index]);
      }
    }
  });

  // Reajustar el mapa después del filtro
  const marcadoresVisibles = marcadores.filter((_, i) => filas[i].style.display !== 'none');
  if (marcadoresVisibles.length > 0) {
    const grupo = new L.featureGroup(marcadoresVisibles);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function resetTabla() {
  document.querySelectorAll("#tablaRegistros tbody tr").forEach(fila => fila.style.display = "");
  document.querySelectorAll(".filtros input").forEach(input => input.value = "");

  // Mostrar todos los marcadores
  marcadores.forEach(marcador => mapa.addLayer(marcador));

  // Reajustar el mapa
  if (marcadores.length > 0) {
    const grupo = new L.featureGroup(marcadores);
    mapa.fitBounds(grupo.getBounds().pad(0.1));
  }
}

function descargarCSV() {
  const filas = document.querySelectorAll("#tablaRegistros tbody tr");
  let csv = "ID,Vehículo,Latitud,Longitud,Estado,Fecha\n";

  filas.forEach(fila => {
    if (fila.style.display !== "none") {
      const celdas = fila.querySelectorAll("td");
      const filaCSV = Array.from(celdas).map(celda => `"${celda.textContent}"`).join(",");
      csv += filaCSV + "\n";
    }
  });

  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const link = document.createElement("a");
  const url = URL.createObjectURL(blob);
  link.setAttribute("href", url);
  link.setAttribute("download", "registros_gps.csv");
  link.style.visibility = "hidden";
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

// Inicializar el mapa cuando se carga la página
window.onload = inicializarMapa;