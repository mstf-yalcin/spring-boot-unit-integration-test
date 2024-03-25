package com.example.test.service;


import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.dto.UpdateProductDto;
import com.example.test.exceptions.NotFoundException;
import com.example.test.mapper.ProductMapper;
import com.example.test.model.Product;
import com.example.test.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository _productRepository;
    private final ProductMapper _mapper;

    public ProductService(ProductRepository productRepository, ProductMapper mapper) {
        _productRepository = productRepository;
        _mapper = mapper;
    }

    public List<ProductDto> GetAll() {
        List<Product> listProducts = _productRepository.findAll();
        List<ProductDto> listProductDto = listProducts.stream().map(x -> _mapper.MapToProductDto(x)).collect(Collectors.toList());
        return listProductDto;
    }

    public ProductDto GetById(String Id) {
        Product product = FindById(Id);
        ProductDto produtDto = _mapper.MapToProductDto(product);
        return produtDto;
    }

    public ProductDto Update(UpdateProductDto updateProductDto) {
        Product product = FindById(updateProductDto.id());

        product.setName(updateProductDto.name());
        product.setDescription(updateProductDto.description());
        product.setPrice(updateProductDto.price());
        product.setStockQuantity(updateProductDto.stockQuantity());

        _productRepository.save(product);

        ProductDto productDto = _mapper.MapToProductDto(product);
        return productDto;
    }

    public ProductDto Add(CreateProductDto productDto) {
        Product product = _productRepository.save(_mapper.MapToProduct(productDto));
        return _mapper.MapToProductDto(product);

    }

    public void Delete(String Id) {
        Product product = FindById(Id);
        _productRepository.delete(product);
    }

    private Product FindById(String Id) {
        return _productRepository.findById(Id).orElseThrow(() -> new NotFoundException("Product (" + Id + ") not found."));
    }


}
