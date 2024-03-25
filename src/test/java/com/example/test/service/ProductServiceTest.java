package com.example.test.service;

import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.dto.UpdateProductDto;
import com.example.test.exceptions.NotFoundException;
import com.example.test.mapper.ProductMapper;
import com.example.test.model.Product;
import com.example.test.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


class ProductServiceTest {

    //    @InjectMocks
    private ProductService _productService;

    //    @MockBean
    private ProductRepository _productRepository;

    //    @MockBean
    private ProductMapper _productMapper;

    @BeforeEach
    void setUp() {
        _productRepository = Mockito.mock(ProductRepository.class);
        _productMapper = Mockito.mock(ProductMapper.class);

        _productService = new ProductService(_productRepository, _productMapper);
    }


    @Test
    @DisplayName("test GetAll Should Return List ProductDto")
    void testGetAll_ShouldReturnListProductDto() {

        //given
        List<Product> productList = Arrays.asList(
                Product.builder().id("id1").name("test").description("description").price(10.0).stockQuantity(1).build(),
                Product.builder().id("id2").name("test2").description("description2").price(10.0).stockQuantity(1).build(),
                Product.builder().id("id3").name("test3").description("description3").price(10.0).stockQuantity(1).build()
        );

        List<ProductDto> expected = Arrays.asList(
                new ProductDto("id1", "test", "description", 10.0, 1),
                new ProductDto("id2", "test2", "description2", 10.0, 1),
                new ProductDto("id3", "test3", "description3", 10.0, 1)
        );


        Mockito.when(_productRepository.findAll()).thenReturn(productList);

//        for (int i = 0; i < productList.size(); i++) {
//            Mockito.when(_productMapper.MapToProductDto(productList.get(i))).thenReturn(expected.get(i));
//        }

//        Mockito.when(_productMapper.MapToProductDto(productList.get(0))).thenReturn(expected.get(0));
//        Mockito.when(_productMapper.MapToProductDto(productList.get(1))).thenReturn(expected.get(1));
//        Mockito.when(_productMapper.MapToProductDto(productList.get(2))).thenReturn(expected.get(2));

        Mockito.when(_productMapper.MapToProductDto(Mockito.any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStockQuantity());
                });

        //when
        List<ProductDto> result = _productService.GetAll();

        //then
        Mockito.verify(_productRepository).findAll();
        Mockito.verify(_productMapper, Mockito.times(productList.size())).MapToProductDto(Mockito.any(Product.class));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void testGetById_ShouldReturnProductDto_WhenProductExist() {

        //given
        Product product = Product.builder()
                .id("id1").name("test").description("description").price(10.0).stockQuantity(1).build();

        ProductDto expected = new ProductDto(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), product.getStockQuantity());

        Mockito.when(_productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Mockito.when(_productMapper.MapToProductDto(product)).thenReturn(expected);

        //when
        ProductDto result = _productService.GetById(product.getId());

        //then
        Assertions.assertEquals(expected, result);
        Mockito.verify(_productRepository).findById(product.getId());
        Mockito.verify(_productMapper).MapToProductDto(product);
    }

    @Test
    void testGetById_ShouldThrowNotFoundException_WhenProductDoesNotExist() {
        //given
        Mockito.when(_productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(NotFoundException.class, () -> _productService.GetById(""));
        Mockito.verify(_productRepository).findById(Mockito.any());
        Mockito.verifyNoInteractions(_productMapper);
    }

    @Test
    void testUpdate_ShouldUpdateProductAndReturnProductDto_WhenValidRequestAndProductExist() {

        //given
        String id = "id1";

        Product product = Product.builder()
                .id("id1").name("test").description("description").price(10.0).stockQuantity(1).build();

        UpdateProductDto updateProductDto = new UpdateProductDto
                (id, "updateName", "updateDesc", 15, 100);

        Product updatedProduct = Product.builder()
                .id("id1").name(updateProductDto.name())
                .description(updateProductDto.description()).price(updateProductDto.price())
                .stockQuantity(updateProductDto.stockQuantity()).build();

        ProductDto expected = new ProductDto(updateProductDto.id(), updateProductDto.name(),
                updateProductDto.description(), updateProductDto.price(), updateProductDto.stockQuantity());


        Mockito.when(_productRepository.findById(id)).thenReturn(Optional.of(product));
        Mockito.when(_productRepository.save(updatedProduct)).thenReturn(updatedProduct);
        Mockito.when(_productMapper.MapToProductDto(updatedProduct)).thenReturn(expected);

        //when
        ProductDto result = _productService.Update(updateProductDto);

        //then
        Assertions.assertEquals(expected, result);
        Mockito.verify(_productRepository).findById(id);
        Mockito.verify(_productRepository).save(product);
    }

    @Test
    void testUpdate_ShouldThrowNotFoundException_WhenProductDoesNotExist() {

        //given
        UpdateProductDto updateProductDto = new UpdateProductDto
                ("", "updateName", "updateDesc", 15, 100);

        Mockito.when(_productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(NotFoundException.class, () -> _productService.Update(updateProductDto));
        Mockito.verify(_productRepository).findById(Mockito.any());
        Mockito.verify(_productRepository, Mockito.never()).save(Mockito.any());
//        Mockito.verifyNoMoreInteractions(_productRepository);
        Mockito.verifyNoInteractions(_productMapper);
    }

    @Test
    void testAdd_ShouldReturnProductDto_WhenValidRequest() {

        //given
        CreateProductDto createProductDto = new CreateProductDto(
                "test", "desc", 10, 1);

        Product product = Product.builder().id("id1").name(createProductDto.name()).description(createProductDto.description())
                .price(createProductDto.price()).stockQuantity(createProductDto.stockQuantity()).build();

        ProductDto expect = new ProductDto(product.getId(), product.getName(), product.getDescription()
                , product.getPrice(), product.getStockQuantity());

        Mockito.when(_productRepository.save(product)).thenReturn(product);
        Mockito.when(_productMapper.MapToProduct(createProductDto)).thenReturn(product);
        Mockito.when(_productMapper.MapToProductDto(product)).thenReturn(expect);

        //when
        ProductDto result = _productService.Add(createProductDto);

        //then
        Assertions.assertEquals(expect, result);
        Mockito.verify(_productRepository).save(product);
        Mockito.verify(_productMapper).MapToProductDto(product);
        Mockito.verify(_productMapper).MapToProduct(createProductDto);
    }

//    @ParameterizedTest(name = "name={1},description={2}")
    @ParameterizedTest()
    @CsvSource(value = {"name,desc","name2,desc2"})
    void testAdds_ShouldReturnProductDto_WhenValidRequest(String name,String description) {

        //given
        CreateProductDto createProductDto = new CreateProductDto(
                name, description, 10, 1);

        Product product = Product.builder().id("id1").name(createProductDto.name()).description(createProductDto.description())
                .price(createProductDto.price()).stockQuantity(createProductDto.stockQuantity()).build();

        ProductDto expect = new ProductDto(product.getId(), product.getName(), product.getDescription()
                , product.getPrice(), product.getStockQuantity());

        Mockito.when(_productRepository.save(product)).thenReturn(product);
        Mockito.when(_productMapper.MapToProduct(createProductDto)).thenReturn(product);
        Mockito.when(_productMapper.MapToProductDto(product)).thenReturn(expect);

        //when
        ProductDto result = _productService.Add(createProductDto);

        //then
        Assertions.assertEquals(expect, result);
        Mockito.verify(_productRepository).save(product);
        Mockito.verify(_productMapper).MapToProductDto(product);
        Mockito.verify(_productMapper).MapToProduct(createProductDto);
    }

    @Test
    void testDelete_ShouldDeleteProduct_WhenProductExist() {

        //given
        Product product = new Product();

        Mockito.when(_productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        Mockito.doNothing().when(_productRepository).delete(product);

        //when
        _productService.Delete(product.getId());

        //then
        Mockito.verify(_productRepository).findById(product.getId());
        Mockito.verify(_productRepository).delete(product);
    }

    @Test
    void testDelete_ShouldDeleteProduct_WhenProductDoesNotExist() {

        //given
        Mockito.when(_productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(NotFoundException.class, () -> _productService.Delete(""));
        Mockito.verify(_productRepository).findById(Mockito.any());
    }

}