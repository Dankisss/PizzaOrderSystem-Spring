import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Container, Paper, Typography, Grid, Chip, Divider, 
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Box, Button, CircularProgress, Alert
} from '@mui/material';
import { ArrowBack, ReceiptLong, LocalShipping, AccessTime } from '@mui/icons-material';
import api from '../../axios/axiosConfig'; 

const OrderDetailPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const response = await api.get(`/api/v1/orders/${orderId}`);
        setOrder(response.data);
      } catch (err) {
        setError('Failed to load order details.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric', 
      hour: '2-digit', minute: '2-digit'
    });
  };

  const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
      case 'COMPLETED': return 'success';
      case 'PENDING': return 'warning';
      case 'CANCELLED': return 'error';
      case 'PROCESSING': return 'info';
      default: return 'default';
    }
  };

  const calculateTotal = () => {
    if (!order?.items) return 0;
    return order.items.reduce((sum, item) => sum + (item.priceAtOrderTime * item.quantity), 0);
  };

  if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
  if (error) return <Container sx={{ mt: 5 }}><Alert severity="error">{error}</Alert></Container>;
  if (!order) return <Container sx={{ mt: 5 }}><Alert severity="info">Order not found.</Alert></Container>;

  return (
    <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
      <Button startIcon={<ArrowBack />} onClick={() => navigate(-1)} sx={{ mb: 2 }}>
        Back to Orders
      </Button>

      <Paper elevation={3} sx={{ p: 4 }}>
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h4" component="h1" gutterBottom>
              Order #{order.id}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Placed on {formatDate(order.createdAt)}
            </Typography>
          </Box>
          <Chip 
            label={order.status} 
            color={getStatusColor(order.status)} 
            variant="filled" 
            sx={{ fontSize: '1rem', px: 1 }}
          />
        </Box>

        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
              <LocalShipping color="action" sx={{ mr: 1 }} />
              <Typography variant="h6">Shipping Address</Typography>
            </Box>
            <Typography variant="body1" sx={{ ml: 4 }}>
              {order.address}
            </Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
              <AccessTime color="action" sx={{ mr: 1 }} />
              <Typography variant="h6">Timeline</Typography>
            </Box>
            <Box sx={{ ml: 4 }}>
              <Typography variant="body2">
                <strong>Created:</strong> {formatDate(order.createdAt)}
              </Typography>
              {order.updatedAt && (
                <Typography variant="body2">
                  <strong>Last Update:</strong> {formatDate(order.updatedAt)}
                </Typography>
              )}
            </Box>
          </Grid>
        </Grid>

        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
          <ReceiptLong sx={{ mr: 1 }} /> Order Summary
        </Typography>
        
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead sx={{ bgcolor: 'grey.100' }}>
              <TableRow>
                <TableCell><strong>Product</strong></TableCell>
                <TableCell align="center"><strong>Quantity</strong></TableCell>
                <TableCell align="right"><strong>Unit Price</strong></TableCell>
                <TableCell align="right"><strong>Total</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {order.items.map((item, index) => (
                <TableRow key={index}>
                  <TableCell>{item.productName || `Product #${item.productId}`}</TableCell>
                  <TableCell align="center">{item.quantity}</TableCell>
                  <TableCell align="right">${item.priceAtOrderTime?.toFixed(2)}</TableCell>
                  <TableCell align="right">
                    <strong>${(item.priceAtOrderTime * item.quantity).toFixed(2)}</strong>
                  </TableCell>
                </TableRow>
              ))}
              
              <TableRow>
                <TableCell colSpan={2} />
                <TableCell align="right" sx={{ typography: 'subtitle1' }}>
                  <strong>Grand Total:</strong>
                </TableCell>
                <TableCell align="right" sx={{ typography: 'h6', color: 'primary.main' }}>
                  ${calculateTotal().toFixed(2)}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>

      </Paper>
    </Container>
  );
};

export default OrderDetailPage;