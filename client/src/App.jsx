import { Routes, Route } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import ProtectedRoute from './components/ProtectedRoute'

import Landing from './pages/Landing'
import Login from './pages/Login'
import Register from './pages/Register'
import Rides from './pages/Rides'
import RideDetail from './pages/RideDetail'
import Host from './pages/Host'
import Bookings from './pages/Bookings'
import Profile from './pages/Profile'
import NotFound from './pages/NotFound'

export default function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route path="/rides" element={
            <ProtectedRoute><Rides /></ProtectedRoute>
          } />
          <Route path="/rides/:id" element={
            <ProtectedRoute><RideDetail /></ProtectedRoute>
          } />
          <Route path="/host" element={
            <ProtectedRoute requireRole="DRIVER"><Host /></ProtectedRoute>
          } />
          <Route path="/bookings" element={
            <ProtectedRoute><Bookings /></ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute><Profile /></ProtectedRoute>
          } />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </>
  )
}
