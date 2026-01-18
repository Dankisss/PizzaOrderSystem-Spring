import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { 
    Container, Paper, Typography, TextField, Button, 
    Box, Link, Alert, InputAdornment, IconButton 
} from '@mui/material';
import { Visibility, VisibilityOff, LockOpen } from '@mui/icons-material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { setCredentials } from '../../redux/slices/authSlice';
import api from '../../axios/axiosConfig';

export const LoginPage = () => {
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await api.post('/api/v1/users/login', formData);

            console.log(response);
            const { role } = response.data;

            dispatch(setCredentials(response.data));

            if (role === 'ROLE_EMPLOYEE' || role === 'EMPLOYEE') {
                navigate('/orders/process');
            } else {
                navigate('/orders');
            }

        } catch (err) {
            const serverMsg = err.response?.data?.message || 'Invalid username or password';
            setError(serverMsg);
            console.error("Login Error:", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="xs" sx={{ mt: 8 }}>
            <Paper elevation={6} sx={{ p: 4, borderRadius: 2 }}>
                {/* Header Icon & Title */}
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                    <LockOpen color="primary" sx={{ fontSize: 40, mb: 1 }} />
                    <Typography variant="h5" fontWeight="bold">Pizza System Login</Typography>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                <form onSubmit={handleLogin}>
                    <TextField
                        fullWidth
                        label="Username"
                        name="username"
                        margin="normal"
                        required
                        value={formData.username}
                        onChange={handleChange}
                    />

                    <TextField
                        fullWidth
                        label="Password"
                        name="password"
                        type={showPassword ? 'text' : 'password'}
                        margin="normal"
                        required
                        value={formData.password}
                        onChange={handleChange}
                    />

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        size="large"
                        disabled={loading}
                        sx={{ mt: 3, py: 1.5, fontWeight: 'bold' }}
                    >
                        {loading ? 'Logging in...' : 'Login'}
                    </Button>

                    <Box sx={{ mt: 3, textAlign: 'center' }}>
                        <Typography variant="body2">
                            Don't have an account?{' '}
                            <Link component={RouterLink} to="/sign-up" underline="hover">
                                Sign Up
                            </Link>
                        </Typography>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};