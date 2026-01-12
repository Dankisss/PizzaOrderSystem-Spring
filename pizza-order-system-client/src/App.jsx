import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
import OrderHistoryPage from './pages/OrderHistoryPage/OrderHistoryPage'
import Layout from './components/Layout/Layout'
import { Navigate, Route, Routes } from 'react-router-dom'
import OrderDetailPage from './pages/OrderDetailPage/OrderDetailPage'
import CreateOrderPage from './pages/CreateOrderPage/CreateOrderPage'
import CreateProductPage from './pages/CreateProductPage/CreateProductPage'
import ProcessOrderPage from './pages/ProcessOrderPage/ProcessOrderPage'
import { LoginPage } from './pages/LoginPage/LoginPage'
import ProtectedRoute from './components/ProtectedRoute/ProtectedRoute'

function App() {

  return (
    <>
      <Layout>
        <Routes>
          <Route path='/login' element={<LoginPage />} />

          <Route path="/products/new" element={
            <ProtectedRoute allowedRoles={['EMPLOYEE']}>
              <CreateProductPage />
            </ProtectedRoute>
          } />

          <Route path="/orders/process" element={
            <ProtectedRoute allowedRoles={['EMPLOYEE']}>
              <ProcessOrderPage />
            </ProtectedRoute>
          } />

          <Route path="/orders/new" element={
            <ProtectedRoute allowedRoles={['CUSTOMER']}>
              <CreateOrderPage />
            </ProtectedRoute>
          } />
          <Route path="/orders/:orderId" element={
            <ProtectedRoute allowedRoles={['CUSTOMER', 'EMPLOYEE']}>
              <OrderDetailPage />
            </ProtectedRoute>
          } />

          <Route path="/orders" element={
            <ProtectedRoute allowedRoles={['CUSTOMER', 'EMPLOYEE']}>
              <OrderHistoryPage />
            </ProtectedRoute>
          } />

          {/* <Route path="*" element={<Navigate to="/login" replace />} /> */}
        </Routes>
      </Layout>

    </>


  );
}

export default App
