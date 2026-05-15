import api from './client'

export const rideApi = {
  // Rides
  create:           (data) => api.post('/api/rides', data).then(r => r.data),
  update:           (id, data) => api.put(`/api/rides/${id}`, data).then(r => r.data),
  cancel:           (id) => api.put(`/api/rides/${id}/cancel`).then(r => r.data),
  getById:          (id) => api.get(`/api/rides/${id}`).then(r => r.data),
  getByDriver:      (driverId) => api.get(`/api/rides/driver/${driverId}`).then(r => r.data),
  search:           (source, destination, date) => api.get('/api/rides/search', {
                       params: { source, destination, date }
                    }).then(r => r.data),
  getAllAvailable:  () => api.get('/api/rides/available').then(r => r.data),

  // Bookings
  book:             (data) => api.post('/api/rides/bookings', data).then(r => r.data),
  cancelBooking:    (id) => api.put(`/api/rides/bookings/${id}/cancel`).then(r => r.data),
  getBooking:       (id) => api.get(`/api/rides/bookings/${id}`).then(r => r.data),
  getBookingHistory:(userId) => api.get(`/api/rides/bookings/user/${userId}`).then(r => r.data),

  // Ratings
  rate:             (data) => api.post('/api/rides/ratings', data).then(r => r.data),
  getRatings:       (userId) => api.get(`/api/rides/ratings/user/${userId}`).then(r => r.data),
  getAverageRating: (userId) => api.get(`/api/rides/ratings/user/${userId}/average`).then(r => r.data),
}
