import React, { useState } from 'react';
import { 
    Container, Paper, Typography, TextField, Button, 
    Box, MenuItem, Link, Alert, InputAdornment, IconButton 
} from '@mui/material';
import { Visibility, VisibilityOff, PersonAdd } from '@mui/icons-material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import api from '../../axios/axiosConfig';

const SignUpPage = () => {
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        userRole: 'CUSTOMER'
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            await api.post('/api/v1/users/sign-up', formData);
            
            navigate('/login', { state: { message: 'Registration successful! Please login.' } });
        } catch (err) {
            const serverMsg = err.response?.data?.message || 'Registration failed. Please check your details.';
            setError(serverMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="xs" sx={{ mt: 8 }}>
            <Paper elevation={6} sx={{ p: 4, borderRadius: 2 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
                    <PersonAdd color="primary" sx={{ fontSize: 40, mb: 1 }} />
                    <Typography variant="h5" fontWeight="bold">Create Account</Typography>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                <form onSubmit={handleSubmit}>
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
                        label="Email Address"
                        name="email"
                        type="email"
                        margin="normal"
                        required
                        value={formData.email}
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
                        helperText="Minimum 8 characters"
                    />

                    <TextField
                        select
                        fullWidth
                        label="I am a..."
                        name="userRole"
                        margin="normal"
                        value={formData.userRole}
                        onChange={handleChange}
                    >
                        <MenuItem value="CUSTOMER">Customer</MenuItem>
                        <MenuItem value="EMPLOYEE">Employee</MenuItem>
                    </TextField>

                    <Button
                        fullWidth
                        type="submit"
                        variant="contained"
                        size="large"
                        disabled={loading}
                        sx={{ mt: 3, py: 1.5, fontWeight: 'bold' }}
                    >
                        {loading ? 'Registering...' : 'Sign Up'}
                    </Button>

                    <Box sx={{ mt: 2, textAlign: 'center' }}>
                        <Typography variant="body2">
                            Already have an account?{' '}
                            <Link component={RouterLink} to="/login" underline="hover">
                                Log In
                            </Link>
                        </Typography>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default SignUpPage;