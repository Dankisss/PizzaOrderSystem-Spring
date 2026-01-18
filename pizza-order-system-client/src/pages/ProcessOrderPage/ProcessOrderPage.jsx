import { Alert, Button, Chip, CircularProgress, Container, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import api from "../../axios/axiosConfig";
import { useSelector } from "react-redux";

const processOrder = async (orderId, employeeId) => {
    const requestDto = { employeeId: employeeId };

    try {
        const response = await api.patch(`/api/v1/orders/${orderId}`, requestDto);
        
        console.log(response.data);

        return response.data; 
    } catch (error) {
        const message = error.response?.data?.message || error.message;
        throw new Error("Error calculating the coordinates of the order");
    }
};

/**
 * Fetches all orders from the backend for the admin view.
 */
const fetchAllOrders = async (filters = {}) => {
    try {
        const queryString = new URLSearchParams(filters).toString();
        const response = await api.get(`/api/v1/orders?${queryString}`);

        return response.data;
    } catch (error) {
        console.error("Error fetching all orders:", error);
        throw error;
    }
};

// =================================================================================
// HELPER COMPONENT
// =================================================================================

const getStatusChipColor = (status) => {
    switch (status) {
        case 'NEW': return 'info';
        case 'PROCESSING': return 'secondary';
        case 'PENDING': return 'warning';
        case 'CANCELLED': return 'error';
        default: return 'default';
    }
};

// =================================================================================
// MAIN PAGE COMPONENT
// =================================================================================

const ProcessOrderPage = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [processingId, setProcessingId] = useState(null);
    const [error, setError] = useState(null);

    // State for the results modal
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalContent, setModalContent] = useState({ distance: null, time: null });

    const { userId } = useSelector(state => state.auth);

    const loadOrders = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchAllOrders({status: "NEW"});
            if (Array.isArray(data)) {
                setOrders(data);
            } else {
                setOrders([]);
                throw new Error("Received invalid data for orders.");
            }
        } catch (err) {
            setError(err.message || 'Failed to fetch orders.');
            toast.error(err.message || 'Failed to fetch orders.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadOrders();
    }, []);

    const handleProcessOrder = async (orderId) => {
        setProcessingId(orderId);
        // In a real app, this ID would come from the authenticated user's state.

        try {
            const result = await processOrder(orderId, userId);
            const updatedOrder = result.order;
            
            toast.success(`Order #${orderId} is now being processed!`);
            
            setModalContent({
                distance: Number(result.distance).toFixed(2) + ' km',
                time: Number(result.time).toFixed(2) + ' h',
            });
            setIsModalOpen(true);

            setOrders(prevOrders =>
                prevOrders.map(order =>
                    order.id === orderId ? updatedOrder : order
                )
            );
        } catch (err) {
            toast.error(err.message || `Failed to process order #${orderId}`);
        } finally {
            setProcessingId(null);
        }
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
    };

    if (loading) return <Container sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Container>;
    if (error) return <Container><Alert severity="error">{error}</Alert></Container>;

    return (
        <>
            <Container maxWidth="lg" sx={{ mt: 4 }}>
                <Typography variant="h4" gutterBottom>Process Orders (Admin)</Typography>
                <TableContainer component={Paper}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>Order ID</TableCell>
                                <TableCell>User ID</TableCell>
                                <TableCell>Date</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell align="center">Actions</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {orders.map((order) => (
                                <TableRow key={order.id} hover>
                                    <TableCell>#{order.id}</TableCell>
                                    <TableCell>{order.userId}</TableCell>
                                    <TableCell>{order.orderDate}</TableCell>
                                    <TableCell>
                                        <Chip label={order.status} color={getStatusChipColor(order.status)} size="small" />
                                    </TableCell>
                                    {/* <TableCell align="right">${order.total?.toFixed(2)}</TableCell> */}
                                    <TableCell align="center">
                                        {order.status === 'NEW' ? (
                                            <Button
                                                variant="contained"
                                                size="small"
                                                onClick={() => handleProcessOrder(order.id)}
                                                disabled={processingId === order.id}
                                            >
                                                {processingId === order.id ? <CircularProgress size={20} color="inherit" /> : 'Process'}
                                            </Button>
                                        ) : (
                                            <Typography variant="caption" color="text.secondary">
                                                -
                                            </Typography>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Container>

            <Dialog open={isModalOpen} onClose={handleCloseModal}>
                <DialogTitle>Order Processing Details</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        The order has been successfully assigned.
                    </DialogContentText>
                    <Typography variant="h6" sx={{ mt: 2 }}>
                        Distance: {modalContent.distance}
                    </Typography>
                    <Typography variant="h6">
                        Estimated Time: {modalContent.time}
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseModal} autoFocus>
                        Close
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
}

export default ProcessOrderPage;


