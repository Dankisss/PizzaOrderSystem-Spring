import React from "react";
import LocalPizzaIcon from '@mui/icons-material/LocalPizza';
import LocalBarIcon from '@mui/icons-material/LocalBar';
import OpacityIcon from '@mui/icons-material/Opacity';
import ImageIcon from '@mui/icons-material/Image';

const ProductCategoryIcon = ({ category }) => {
    switch(category) {
        case 'PIZZA': return <LocalPizzaIcon />;
        case 'DRINK': return <LocalBarIcon />;
        case 'SAUCE': return <OpacityIcon />;
        default: return <ImageIcon />;
    }
};

export default ProductCategoryIcon;