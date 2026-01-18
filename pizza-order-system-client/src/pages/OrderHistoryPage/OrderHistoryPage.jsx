import { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Chip,
    Button,
    CircularProgress,
    Alert
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../axios/axiosConfig';
import { useSelector } from 'react-redux';

const getStatusChipColor = (status) => {
    switch (status) {
        case 'PROCESSING':
            return 'primary';
        case 'COMPLETED':
            return 'success';
        case 'NEW':
            return 'warning';
        case 'CANCELLED':
            return 'error';
        default:
            return 'default';
    }
};

const OrderHistoryPage = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();
    const { token, userId } = useSelector(state => state.auth)

    console.log(token, userId)
    useEffect(() => {
        const fetchOrders = async (filters = {}) => {
            console.log("API Call: Fetching all orders with token:", token);
            const queryString = new URLSearchParams(filters).toString();
            try {
                const response = await api.get(`/api/v1/orders?${queryString}`);

                return response.data;
            } catch (error) {
                console.error("An error occured: ", error)
            }
        };

        const loadOrders = async () => {
            try {
                const data = await fetchOrders({ userId: userId });

                console.log(data);
                setOrders(data);
            } catch (err) {
                setError('Failed to fetch order history.');
            } finally {
                setLoading(false);
            }
        };

        loadOrders();
    }, []);

    const handleViewDetails = (orderId) => {
        navigate(`/orders/${orderId}`);
    };

    if (loading) {
        return <Container sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Container>;
    }

    if (error) {
        return <Container><Alert severity="error">{error}</Alert></Container>;
    }

    return (
        <Container maxWidth="md" sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>
                My Orders
            </Typography>
            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 650 }} aria-label="order history table">
                    <TableHead>
                        <TableRow>
                            <TableCell>Order #</TableCell>
                            <TableCell>Date</TableCell>
                            <TableCell>Status</TableCell>
                            <TableCell align="right">Total</TableCell>
                            <TableCell align="center">Actions</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {orders.map((order, index) => (
                            <TableRow key={order.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                                <TableCell component="th" scope="row">
                                    #{index + 1}
                                </TableCell>
                                <TableCell>{order.orderDate}</TableCell>
                                <TableCell>
                                    <Chip label={order.status} color={getStatusChipColor(order.status)} size="small" />
                                </TableCell>
                                <TableCell align="center">
                                    <Button variant="outlined" size="small" onClick={() => handleViewDetails(order.id)}>
                                        View Details
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );

}

export default OrderHistoryPage