// alertas.js - Funcionalidad para la página de alertas

function filtrarAlertas() {
  const vehiculo = document.getElementById('filtroVehiculo').value.toLowerCase();
  const tipo = document.getElementById('filtroTipo').value.toLowerCase();
  const fechaInicio = document.getElementById('fechaInicio').value;
  const fechaFin = document.getElementById('fechaFin').value;

  const filas = document.querySelectorAll('.consulta-tabla tbody tr');
  filas.forEach(fila => {
    const veh = fila.cells[1].textContent.toLowerCase();
    const tipoAlerta = fila.cells[2].textContent.toLowerCase();
    const fecha = fila.cells[4].textContent;

    let visible = true;
    if (vehiculo && !veh.includes(vehiculo)) visible = false;
    if (tipo && !tipoAlerta.includes(tipo)) visible = false;
    if (fechaInicio && new Date(fecha) < new Date(fechaInicio)) visible = false;
    if (fechaFin && new Date(fecha) > new Date(fechaFin)) visible = false;

    fila.style.display = visible ? '' : 'none';
  });
}

function mostrarTodas() {
  document.querySelectorAll('.consulta-tabla tbody tr').forEach(fila => fila.style.display = '');
  document.querySelectorAll('.filtros input').forEach(input => input.value = '');
}