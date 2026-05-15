import api from './client'

export const paymentApi = {
  process:     (data) => api.post('/api/payments', data).then(r => r.data),
  refund:      (id) => api.put(`/api/payments/${id}/refund`).then(r => r.data),
  getById:     (id) => api.get(`/api/payments/${id}`).then(r => r.data),
  getByBooking:(bookingId) => api.get(`/api/payments/booking/${bookingId}`).then(r => r.data),
  getByUser:   (userId) => api.get(`/api/payments/user/${userId}`).then(r => r.data),
}
