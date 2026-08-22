const Auth = {
  getToken: () => localStorage.getItem('accessToken'),
  setToken: (token) => localStorage.setItem('accessToken', token),
  clear: () => localStorage.removeItem('accessToken'),
  isLoggedIn: () => !!localStorage.getItem('accessToken'),

  requireAuth() {
    if (!this.isLoggedIn()) {
      location.href = '/demo/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
      return false;
    }
    return true;
  },

  logout() {
    this.clear();
    location.href = '/demo/login.html';
  },
};
