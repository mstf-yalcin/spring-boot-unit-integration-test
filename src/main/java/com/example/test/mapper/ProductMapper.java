package com.example.test.mapper;


import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto MapToProductDto(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity());
    }

    public Product MapToProduct(CreateProductDto productDto) {
        Product product = new Product();

        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setPrice(productDto.price());
        product.setStockQuantity(productDto.stockQuantity());

        return product;
    }

}
