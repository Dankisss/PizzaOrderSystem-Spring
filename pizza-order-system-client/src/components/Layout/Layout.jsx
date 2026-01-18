import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { AppBar, Toolbar, Typography, Button, Box, Container } from '@mui/material';
import { logout } from '../../redux/slices/authSlice';

const Layout = ({ children }) => {
  const { role, isAuthenticated } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Pizza Express
          </Typography>

          <Box>
            {isAuthenticated ? (
              <>
                <Button color="inherit" component={Link} to="/orders">History</Button>

                {role === 'EMPLOYEE' && (
                  <>
                    <Button color="inherit" component={Link} to="/orders/process">Process Orders</Button>
                    <Button color="inherit" component={Link} to="/products/new">Add Product</Button>
                  </>
                )}

                {role === 'CUSTOMER' && (
                  <Button color="inherit" component={Link} to="/orders/new">New Order</Button>
                )}

                <Button color="error" onClick={handleLogout} sx={{ ml: 2 }}>Logout</Button>
              </>
            ) : (
              <>
                <Button color="inherit" component={Link} to="/login">Login</Button>
                <Button color="inherit" component={Link} to="/sign-up">Sign up</Button>
              </>
            )}
          </Box>
        </Toolbar>
      </AppBar>

      <Container sx={{ mt: 4 }}>
        {children}
      </Container>
    </>
  );
};

export default Layout;