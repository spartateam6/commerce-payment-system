const api = (() => {
  const getToken = () => localStorage.getItem('accessToken');

  const buildHeaders = () => {
    const h = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) h['Authorization'] = `Bearer ${token}`;
    return h;
  };

  const handleResponse = async (res) => {
    const json = await res.json();
    if (json.code !== 'SUCCESS') {
      const err = new Error(json.message || '오류가 발생했습니다.');
      err.code = json.code;
      err.status = res.status;
      throw err;
    }
    return json.data;
  };

  return {
    get(url) {
      return fetch(url, { headers: buildHeaders() }).then(handleResponse);
    },
    post(url, body) {
      return fetch(url, {
        method: 'POST',
        headers: buildHeaders(),
        body: JSON.stringify(body),
      }).then(handleResponse);
    },
    patch(url, body) {
      return fetch(url, {
        method: 'PATCH',
        headers: buildHeaders(),
        body: JSON.stringify(body),
      }).then(handleResponse);
    },
    delete(url) {
      return fetch(url, { method: 'DELETE', headers: buildHeaders() }).then(res => {
        if (res.status === 204) return null;
        return handleResponse(res);
      });
    },
  };
})();