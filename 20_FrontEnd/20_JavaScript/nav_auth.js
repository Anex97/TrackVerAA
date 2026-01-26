(function(){
  // If there's a nav menu, add Admin link only for users with nivelAcceso === 2
  function ensureNavAdmin(){
    try{
      const usuario = (() => { try { return JSON.parse(localStorage.getItem('usuario')||'null'); } catch(e){ return null; }})();
      const menus = document.querySelectorAll('ul.nav-menu');
      menus.forEach(ul => {
        // if there's already an admin link, remove it (we'll re-add only when allowed)
        const existing = ul.querySelector('#navAdmin');
        if (existing) existing.remove();
        // Only add the admin link when the user is present and nivelAcceso === 2
        if (usuario && Number(usuario.nivelAcceso) === 2) {
          const li = document.createElement('li');
          li.id = 'navAdmin';
          const a = document.createElement('a');
          a.href = 'admin_users.html';
          a.textContent = 'Admin Usuarios';
          li.appendChild(a);
          // if there's a Logout/Cerrar Sesión item, insert admin BEFORE it (swap places)
          const logoutLi = Array.from(ul.querySelectorAll('li')).find(x => {
            const txt = (x.textContent||'').trim().toLowerCase();
            return txt.includes('cerrar') && txt.includes('sesión') || txt.includes('cerrar sesion');
          });
          if (logoutLi) {
            ul.insertBefore(li, logoutLi);
          } else {
            ul.appendChild(li);
          }
        }
      });
      // If current page is admin_users.html and user is not admin, redirect away
      const path = window.location.pathname || '';
      const page = path.substring(path.lastIndexOf('/')+1).toLowerCase();
      if (page === 'admin_users.html') {
        if (!(usuario && Number(usuario.nivelAcceso) === 2)) {
          // redirect to panel (safe fallback)
          window.location.href = 'Panel.html';
        }
      }
    }catch(e){ console.error('nav_auth error', e); }
  }

  // run on load and when storage changes (login/logout in other tab)
  window.addEventListener('load', ensureNavAdmin);
  window.addEventListener('storage', ensureNavAdmin);
})();
