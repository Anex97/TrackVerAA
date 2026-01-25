(function(){
  const tableBody = document.querySelector('#usersTable tbody');
  const btnAdd = document.getElementById('btnAddUser');
  const newUserForm = document.getElementById('newUserForm');
  const newUserMsg = document.getElementById('newUserMsg');
  const modal = document.getElementById('userModal');
  const form = document.getElementById('userForm');
  const btnCancel = document.getElementById('btnCancel');
  const modalTitle = document.getElementById('modalTitle');

  function fetchUsers(){
    fetch('/api/usuarios').then(r=>r.json()).then(renderUsers).catch(e=>{
      console.error(e);
      tableBody.innerHTML = '<tr><td colspan="5">Error cargando usuarios</td></tr>';
    });
  }

  function renderUsers(list){
    tableBody.innerHTML = '';
    if (!Array.isArray(list) || list.length===0) {
      tableBody.innerHTML = '<tr><td colspan="5">No hay usuarios registrados</td></tr>';
      return;
    }
    list.forEach(u => {
      const tr = document.createElement('tr');
      tr.innerHTML = `<td>${u.id}</td><td>${escapeHtml(u.nombre)}</td><td>${escapeHtml(u.correo)}</td><td>${u.nivelAcceso}</td>
        <td class="action-cell">
          <button class="btn" data-action="edit" data-id="${u.id}">Editar</button>
          <button class="btn danger" data-action="delete" data-id="${u.id}">Eliminar</button>
        </td>`;
      tableBody.appendChild(tr);
    });
  }

  function escapeHtml(s){ if (!s) return ''; return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }

  tableBody.addEventListener('click', e=>{
    const btn = e.target.closest('button');
    if (!btn) return;
    const action = btn.getAttribute('data-action');
    const id = btn.getAttribute('data-id');
    if (action==='edit') openEdit(id);
    if (action==='delete') doDelete(id);
  });

  if (btnAdd) btnAdd.addEventListener('click', ()=>{ openCreate(); });
  if (newUserForm) {
    newUserForm.addEventListener('submit', e=>{
      e.preventDefault();
      const fd = new FormData(newUserForm);
      const body = new URLSearchParams();
      body.append('nombre', fd.get('nombre'));
      body.append('correo', fd.get('correo'));
      body.append('nivelAcceso', fd.get('nivelAcceso'));
      body.append('contrasena', fd.get('contrasena'));
      newUserMsg.textContent = 'Creando...';
      fetch('/api/usuarios', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: body.toString()})
        .then(r=>r.json()).then(resp=>{
          if (resp.ok) {
            newUserMsg.style.color='#0a6'; newUserMsg.textContent = 'Usuario creado correctamente';
            newUserForm.reset(); fetchUsers();
          } else {
            newUserMsg.style.color='#c00'; newUserMsg.textContent = resp.message || 'Error creando usuario';
          }
        }).catch(err=>{ newUserMsg.style.color='#c00'; newUserMsg.textContent = 'Error: '+err; });
    });
  }
  btnCancel.addEventListener('click', closeModal);

  form.addEventListener('submit', e=>{
    e.preventDefault();
    const fd = new FormData(form);
    const id = fd.get('id');
    const body = new URLSearchParams();
    body.append('nombre', fd.get('nombre'));
    body.append('correo', fd.get('correo'));
    body.append('nivelAcceso', fd.get('nivelAcceso'));
    const pass = fd.get('nuevaContrasena');
    if (pass) body.append('nuevaContrasena', pass);
    if (id) body.append('id', id);

    const url = id ? '/api/usuarios/update' : '/api/usuarios';
    fetch(url, {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:body.toString()})
      .then(r=>r.json()).then(resp=>{
        if (resp.ok) {
          closeModal(); fetchUsers();
        } else {
          alert('Error: ' + (resp.message || JSON.stringify(resp)));
        }
      }).catch(err=>{ alert('Error: ' + err); });
  });

  function openEdit(id){
    // fetch users then populate
    fetch('/api/usuarios').then(r=>r.json()).then(list=>{
      const u = list.find(x=>x.id==id);
      if (!u) return alert('Usuario no encontrado');
      document.getElementById('userId').value = u.id;
      document.getElementById('userNombre').value = u.nombre||'';
      document.getElementById('userCorreo').value = u.correo||'';
      document.getElementById('userNivel').value = u.nivelAcceso||0;
      document.getElementById('userPass').value = '';
      modalTitle.textContent = 'Editar usuario #' + u.id;
      openModal();
    });
  }

  function openCreate(){
    document.getElementById('userId').value = '';
    document.getElementById('userNombre').value = '';
    document.getElementById('userCorreo').value = '';
    document.getElementById('userNivel').value = '0';
    document.getElementById('userPass').value = '';
    modalTitle.textContent = 'Crear nuevo usuario';
    openModal();
  }

  function doDelete(id){
    if (!confirm('¿Eliminar usuario #' + id + ' ?')) return;
    const body = new URLSearchParams(); body.append('id', id);
    fetch('/api/usuarios/delete', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:body.toString()})
      .then(r=>r.json()).then(resp=>{
        if (resp.ok) fetchUsers(); else alert('No se pudo eliminar');
      }).catch(err=>alert('Error: ' + err));
  }

  function openModal(){ modal.setAttribute('aria-hidden','false'); modal.style.display='flex'; }
  function closeModal(){ modal.setAttribute('aria-hidden','true'); modal.style.display='none'; }

  // init
  fetchUsers();
})();