import React, { useEffect, useId, useState } from 'react';
import {
    Container, Typography, Grid, Card, CardContent,
    Button, TextField, Box, Divider, Paper, IconButton, Alert, Snackbar
} from '@mui/material';
import { Add, Remove, ShoppingCartCheckout, ShoppingBasket } from '@mui/icons-material';
import api from '../../axios/axiosConfig';
import { useSelector } from 'react-redux';

const CreateOrderPage = () => {
    const [products, setProducts] = useState([]);
    const [cart, setCart] = useState({});
    const [address, setAddress] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    const { userId } = useSelector(state => state.auth);

    useEffect(() => {
        const fetchProducts = async () => {
            try {
                const response = await api.get('/api/v1/products', { params: {} });
                setProducts(response.data);
            } catch (err) {
                console.error("Failed to fetch products", err);
                setMessage({ type: 'error', text: 'Could not load products.' });
            }
        };
        fetchProducts();
    }, []);

    const updateQuantity = (id, delta) => {
        setCart(prev => {
            const currentQty = prev[id] || 0;
            const newQty = Math.max(0, currentQty + delta);
            return { ...prev, [id]: newQty };
        });
    };

    const calculateTotal = () => {
        return products.reduce((sum, product) => {
            return sum + (product.price * (cart[product.id] || 0));
        }, 0);
    };

    const handlePlaceOrder = async () => {
        if (!address.trim()) {
            setMessage({ type: 'error', text: 'Please provide a delivery address.' });
            return;
        }

        const items = Object.keys(cart)
            .filter(id => cart[id] > 0)
            .map(id => ({
                productId: parseInt(id),
                quantity: cart[id]
            }));

        if (items.length === 0) {
            setMessage({ type: 'error', text: 'Your cart is empty!' });
            return;
        }

        setLoading(true);
        try {
            const orderRequest = { address, items, userId };
            await api.post('api/v1/orders', orderRequest);

            setMessage({ type: 'success', text: 'Order placed successfully!' });
            setCart({});
            setAddress('');
        } catch (err) {
            const errorMsg = err.response?.data?.message || 'Server error';
            setMessage({ type: 'error', text: 'Order failed: ' + errorMsg });
        } finally {
            setLoading(false);
        }
    };

    const selectedItemsCount = Object.keys(cart).filter(id => cart[id] > 0).length;

    return (
        <Container sx={{ py: 4 }} maxWidth="lg">
            <Typography variant="h4" gutterBottom fontWeight="bold">
                Create New Order
            </Typography>

            <Grid container spacing={4}>
                {/* Left Side: Product Grid */}
                <Grid item xs={12} md={8}>
                    {products.length === 0 ? (
                        <Box sx={{ textAlign: 'center', mt: 4 }}>
                            <Typography color="text.secondary">No products available at the moment.</Typography>
                        </Box>
                    ) : (
                        <Grid container spacing={2}>
                            {products.map((product) => (
                                <Grid item xs={12} sm={6} key={product.id}>
                                    <Card 
                                        elevation={cart[product.id] > 0 ? 8 : 2}
                                        sx={{ 
                                            display: 'flex', 
                                            flexDirection: 'column', 
                                            height: '100%',
                                            border: cart[product.id] > 0 ? '2px solid #1976d2' : 'none',
                                            transition: '0.3s'
                                        }}
                                    >
                                        <CardContent sx={{ flexGrow: 1 }}>
                                            <Typography variant="h6" gutterBottom>{product.name}</Typography>
                                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                                {product.description}
                                            </Typography>
                                            <Typography variant="h6" color="primary">
                                                ${product.price.toFixed(2)}
                                            </Typography>
                                        </CardContent>
                                        <Divider />
                                        <Box sx={{ p: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                            <IconButton 
                                                color="primary" 
                                                onClick={() => updateQuantity(product.id, -1)} 
                                                disabled={!cart[product.id]}
                                            >
                                                <Remove />
                                            </IconButton>
                                            <Typography sx={{ mx: 2, fontWeight: 'bold', minWidth: '20px', textAlign: 'center' }}>
                                                {cart[product.id] || 0}
                                            </Typography>
                                            <IconButton 
                                                color="primary" 
                                                onClick={() => updateQuantity(product.id, 1)}
                                            >
                                                <Add />
                                            </IconButton>
                                        </Box>
                                    </Card>
                                </Grid>
                            ))}
                        </Grid>
                    )}
                </Grid>

                {/* Right Side: Order Summary Sidebar */}
                <Grid item xs={12} md={4}>
                    <Paper elevation={4} sx={{ p: 3, position: 'sticky', top: 24, borderRadius: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            <ShoppingBasket sx={{ mr: 1, color: 'primary.main' }} />
                            <Typography variant="h6">Order Summary</Typography>
                        </Box>
                        
                        <Divider sx={{ mb: 2 }} />

                        {/* List of selected items */}
                        <Box sx={{ mb: 3, maxHeight: '250px', overflowY: 'auto', pr: 1 }}>
                            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                                Cart Items
                            </Typography>
                            {selectedItemsCount > 0 ? (
                                Object.keys(cart).map(id => {
                                    if (cart[id] === 0) return null;
                                    const product = products.find(p => p.id === parseInt(id));
                                    return (
                                        <Box key={id} sx={{ display: 'flex', justifyContent: 'space-between', py: 0.5 }}>
                                            <Typography variant="body2" sx={{ maxWidth: '70%' }}>
                                                {product?.name}
                                            </Typography>
                                            <Typography variant="body2" fontWeight="bold">
                                                x {cart[id]}
                                            </Typography>
                                        </Box>
                                    );
                                })
                            ) : (
                                <Typography variant="body2" color="text.disabled" sx={{ fontStyle: 'italic' }}>
                                    No items selected yet.
                                </Typography>
                            )}
                        </Box>

                        <Divider sx={{ mb: 2 }} />

                        <TextField
                            fullWidth
                            label="Delivery Address"
                            placeholder="Enter your full address"
                            multiline
                            rows={3}
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            sx={{ mb: 3 }}
                        />

                        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Typography variant="subtitle1">Total Price:</Typography>
                            <Typography variant="h5" color="primary.main" fontWeight="bold">
                                ${calculateTotal().toFixed(2)}
                            </Typography>
                        </Box>

                        <Button
                            fullWidth
                            variant="contained"
                            size="large"
                            startIcon={<ShoppingCartCheckout />}
                            onClick={handlePlaceOrder}
                            disabled={loading || selectedItemsCount === 0}
                            sx={{ py: 1.5, borderRadius: 2 }}
                        >
                            {loading ? 'Processing Order...' : 'Place Order Now'}
                        </Button>
                    </Paper>
                </Grid>
            </Grid>

            {/* Notification Feedback */}
            <Snackbar
                open={!!message.text}
                autoHideDuration={5000}
                onClose={() => setMessage({ ...message, text: '' })}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            >
                <Alert 
                    onClose={() => setMessage({ ...message, text: '' })} 
                    severity={message.type || 'info'} 
                    variant="filled"
                    sx={{ width: '100%' }}
                >
                    {message.text}
                </Alert>
            </Snackbar>
        </Container>
    );
};

export default CreateOrderPage;