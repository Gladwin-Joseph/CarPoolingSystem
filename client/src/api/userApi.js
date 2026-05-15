import api from './client'

export const userApi = {
  register: (data) => api.post('/api/users/register', data).then(r => r.data),
  login:    (email, password) => api.post('/api/users/login', { email, password }).then(r => r.data),
  getById:  (id) => api.get(`/api/users/${id}`).then(r => r.data),
  update:   (id, data) => api.put(`/api/users/${id}`, data).then(r => r.data),
  verify:   (userId, userType) => api.get(`/api/users/verify/${userId}`, { params: { userType } }).then(r => r.data),
}
