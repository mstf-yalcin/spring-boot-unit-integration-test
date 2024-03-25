package com.example.test.mapper;

import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper _productMapper = new ProductMapper();


    @Test
    void testMapToProductDto_ShouldReturnProductDto_WhenValidRequest() {
        Product product = Product.builder().id("id1")
                .name("name").description("desc").price(10).stockQuantity(1).build();

        ProductDto expect = new ProductDto(product.getId(),
                product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity());

        ProductDto result = _productMapper.MapToProductDto(product);

        assertEquals(expect, result);
    }

    @Test
    void testMapMapToProduct_ShouldReturnProduct_WhenValidRequest() {

        Product expect = Product.builder()
                .name("name").description("desc").price(10).stockQuantity(1).build();

        CreateProductDto CreateProductDto = new CreateProductDto(expect.getName(),
                expect.getDescription(), expect.getPrice(), expect.getStockQuantity());

        Product result = _productMapper.MapToProduct(CreateProductDto);

        assertEquals(expect, result);
    }


}