import { Box, Button, CircularProgress, Container, FormControl, InputLabel, MenuItem, Paper, Select, TextField, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { toast } from "react-toastify";
import api from "../../axios/axiosConfig";

const createProduct = async (productData, photoFile) => {
    const formData = new FormData();

    formData.append('request', new Blob([JSON.stringify(productData)], {
        type: 'application/json'
    }));

    if (photoFile) {
        formData.append('photo', photoFile);
    }

    try {
        const response = await api.post(`/api/v1/products`, formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        });
        
        console.log(response.data);
        return await response.data;
    } catch (error) {
        console.error("Failed to create product:", error);
        toast.error("An error occured");
    }

};

const CreateProductPage = () => {
    const [product, setProduct] = useState({ name: '', description: '', price: '', stock: '', category: '', size: '' });
    const [photoFile, setPhotoFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setProduct(prev => ({ ...prev, [name]: value }));
    };

    useEffect(() => {
        setProduct(prev => ({ ...prev, size: '' }));
    }, [product.category]);


    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setPhotoFile(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!product.name || !product.price || !product.stock || !product.category) {
            toast.error('Name, Category, Price, and Stock are required fields.');
            return;
        }
        if ((product.category === 'PIZZA' || product.category === 'DRINK') && !product.size) {
            toast.error('Size is required for Pizzas and Drinks.');
            return;
        }

        setSubmitting(true);
        try {

            const productToSend = { ...product };
            if (product.category === 'SAUCE') {
                delete productToSend.size;
            }
            
            const response = await createProduct(productToSend, photoFile);

            console.log(response);
            toast.success('Product created successfully!');
        } catch (error) {
            toast.error(error.message || 'Failed to create product.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ mt: 4 }}>
            <Typography variant="h4" gutterBottom>Create New Product</Typography>
            <Paper sx={{ p: 3 }}>
                <Box component="form" onSubmit={handleSubmit} noValidate>
                    <TextField name="name" label="Product Name" value={product.name} onChange={handleChange} fullWidth required margin="normal" />
                    
                    <FormControl fullWidth required margin="normal">
                        <InputLabel id="category-select-label">Category</InputLabel>
                        <Select
                            labelId="category-select-label"
                            name="category"
                            value={product.category}
                            label="Category"
                            onChange={handleChange}
                        >
                            <MenuItem value={"PIZZA"}>Pizza</MenuItem>
                            <MenuItem value={"DRINK"}>Drink</MenuItem>
                            <MenuItem value={"SAUCE"}>Sauce</MenuItem>
                        </Select>
                    </FormControl>

                    {(product.category === 'PIZZA' || product.category === 'DRINK') && (
                         <FormControl fullWidth required margin="normal">
                            <InputLabel id="size-select-label">Size</InputLabel>
                            <Select
                                labelId="size-select-label"
                                name="size"
                                value={product.size}
                                label="Size"
                                onChange={handleChange}
                            >
                                {product.category === 'PIZZA' && [
                                    <MenuItem key="small" value={"SMALL"}>Small</MenuItem>,
                                    <MenuItem key="medium" value={"MEDIUM"}>Medium</MenuItem>,
                                    <MenuItem key="large" value={"LARGE"}>Large</MenuItem>
                                ]}
                                {product.category === 'DRINK' && [
                                    <MenuItem key="330" value={"_330ML"}>330ml</MenuItem>,
                                    <MenuItem key="500" value={"_500ML"}>500ml</MenuItem>
                                ]}
                            </Select>
                        </FormControl>
                    )}

                    <TextField name="description" label="Description" value={product.description} onChange={handleChange} fullWidth multiline rows={4} margin="normal" />
                    <TextField name="price" label="Price" type="number" value={product.price} onChange={handleChange} fullWidth required margin="normal" />
                    <TextField name="stock" label="Stock" type="number" value={product.stock} onChange={handleChange} fullWidth required margin="normal" />
                    
                    <Button variant="contained" component="label" startIcon={<CloudUploadIcon />} sx={{ mt: 2 }}>
                        Upload Photo
                        <input type="file" hidden accept="image/*" onChange={handleFileChange} />
                    </Button>
                    
                    {previewUrl && (
                        <Box sx={{ mt: 2, textAlign: 'center' }}>
                            <Typography variant="subtitle2">Image Preview:</Typography>
                            <img src={previewUrl} alt="Preview" style={{ width: '100%', maxWidth: '200px', marginTop: '8px', borderRadius: '4px' }} />
                        </Box>
                    )}

                    <Button type="submit" variant="contained" size="large" fullWidth sx={{ mt: 3 }} disabled={submitting}>
                        {submitting ? <CircularProgress size={24} /> : 'Create Product'}
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
}

export default CreateProductPage;