package com.example.test.controller;


import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.dto.UpdateProductDto;
import com.example.test.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products/")
public class ProductController {
    private final ProductService _productService;

    public ProductController(ProductService productService) {
        _productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> GetAll() {
        return ResponseEntity.ok(_productService.GetAll());
    }

    @PutMapping
    public ResponseEntity<ProductDto> Update(@RequestBody @Valid UpdateProductDto updateProductDto) {
        return ResponseEntity.ok(_productService.Update(updateProductDto));
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductDto> GetById(@PathVariable String id) {
        return ResponseEntity.ok(_productService.GetById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> Create(@RequestBody @Valid CreateProductDto createProductDto) {
        return ResponseEntity.created(URI.create("/api/v1/category")).body(_productService.Add(createProductDto));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> Delete(@PathVariable String id) {
        _productService.Delete(id);
        return ResponseEntity.noContent().build();
    }
}
