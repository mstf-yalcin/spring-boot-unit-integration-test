package com.example.test.controller;

import com.example.test.dto.CreateProductDto;
import com.example.test.dto.ProductDto;
import com.example.test.dto.UpdateProductDto;
import com.example.test.mapper.ProductMapper;
import com.example.test.model.Product;
import com.example.test.repository.ProductRepository;
import com.example.test.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ProductMapper _productMapper;
    @Autowired
    private ProductRepository _productRepository;
    @Autowired
    private ProductService _productService;

    @Autowired
    private ObjectMapper _objectMapper;

    private final String ENDPOINT = "/api/v1/products/";


//    @InjectMocks
//    ProductController _productController;
//
//    @BeforeEach
//    void setup() {
//
//        _productController = Mockito.mock(ProductController.class);
//        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(_productService)).build();
//    }


    @AfterEach
    void tearDown() {
        _productRepository.deleteAll();
    }

    @Test
    void testGetAll_ShouldReturnListProductDto_WhenValidRequest() throws Exception {
        //given
        List<Product> productList = Arrays.asList(
                Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build(),
                Product.builder().name("test2").description("description2").price(10.0).stockQuantity(1).build(),
                Product.builder().name("test3").description("description3").price(10.0).stockQuantity(1).build()
        );

        List<ProductDto> expected = Arrays.asList(
                new ProductDto("", "test", "description", 10.0, 1),
                new ProductDto("", "test2", "description2", 10.0, 1),
                new ProductDto("", "test3", "description3", 10.0, 1)
        );

        _productRepository.saveAll(productList);

        //then
        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$[0].name").value(expected.get(0).name()))
                .andExpect(jsonPath("$[1].description").value(expected.get(1).description()))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(status().isOk());

        List<ProductDto> serviceResult = _productService.GetAll();

        Assertions.assertAll(
                () -> assertEquals(_productRepository.findAll().stream().toList().size(), 3),
                () -> assertEquals(serviceResult.get(0).name(), expected.get(0).name()),
                () -> assertEquals(serviceResult.get(0).description(), expected.get(0).description()),
                () -> assertEquals(serviceResult.get(0).stockQuantity(), expected.get(0).stockQuantity()));
    }


    @Test
    void testUpdate_ShouldUpdateProductAndReturnProductDto_WhenValidRequestAndProductExist() throws Exception {

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();

        _productRepository.save(newProduct);

        UpdateProductDto updateProductDto = new UpdateProductDto(newProduct.getId(),
                "update-test", "update-description", 150, 10);


        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(updateProductDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updateProductDto.id()))
                .andExpect(jsonPath("$.name").value(updateProductDto.name()))
                .andExpect(jsonPath("$.description").value(updateProductDto.description()));
    }

    @Test
    void testUpdate_ShouldThrowNotFoundException_WhenValidRequestAndProductDoesNotExist() throws Exception {

        UpdateProductDto updateProductDto = new UpdateProductDto("id1",
                "update-test", "update-description", 150, 10);


        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(updateProductDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(String.format("Product (%s) not found.", updateProductDto.id())));
    }

    @Test
    void testUpdate_ShouldThrowMethodArgumentNotValidException_WhenInValidRequest() throws Exception {

        UpdateProductDto updateProductDto = new UpdateProductDto("id1",
                "", "", 150, 10);

        String[] expectedMessages = {
                "name: must not be blank",
                "description: must not be blank"
        };

        mockMvc.perform(MockMvcRequestBuilders.put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(updateProductDto)))
                .andDo(print())
                .andExpect(jsonPath("$", Matchers.containsInAnyOrder(expectedMessages)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void testGetById_ShouldReturnProductDto_WhenProductExist() throws Exception {

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();
        _productRepository.save(newProduct);

        ProductDto productDto = _productMapper.MapToProductDto(newProduct);

        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "{id}", newProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(productDto.name()));
    }

    @Test
    void testGetById_ShouldReturnProductDto_WhenProductDoesNotExist() throws Exception {

        String id = "id1";

        mockMvc.perform(MockMvcRequestBuilders.get(ENDPOINT + "{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(String.format("Product (%s) not found.", id)));
    }

    @Test
    void testCreate_ShouldCreateProductAndReturnProductDto_WhenValidRequest() throws Exception {

        CreateProductDto createProductDto = new CreateProductDto("test", "description", 10, 1);

        Product newProduct = _productMapper.MapToProduct(createProductDto);

        _productRepository.save(newProduct);

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(newProduct)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createProductDto.name()));
    }


    @Test
    void testCreate_ShouldThrowMethodArgumentNotValidException_WhenInvalidRequest() throws Exception {
        CreateProductDto createProductDto = new CreateProductDto("", "", -1, -150);

        List<String> expectedMessages = List.of(
                "stockQuantity: must be greater than or equal to 1",
                "price: must be greater than or equal to 0.1",
                "name: must not be blank",
                "description: must not be blank"
        );

        mockMvc.perform(MockMvcRequestBuilders.post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(_objectMapper.writeValueAsString(createProductDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$", Matchers.containsInAnyOrder(expectedMessages.toArray())));


//                .andExpect(jsonPath("$").value("stockQuantity: must be greater than or equal to 1"))
//                .andExpect(jsonPath("$[1]").value("price: must be greater than or equal to 0.1"))
//                .andExpect(jsonPath("$[2]").value("name: must not be blank"))
//                .andExpect(jsonPath("$[3]").value("description: must not be blank"));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete_ShouldDeleteProductWithAdminUser_WhenProductExist() throws Exception {
        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();
        _productRepository.save(newProduct);
        String id = newProduct.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete_ShouldNotFoundExceptionWithAdminUser_WhenProductDoesNotExist() throws Exception {

        String id = "id1";

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(String.format("Product (%s) not found.", id)));
    }


    @Test
    void testDelete_ShouldReturnStatusUnauthorizedWithUnAuthorizeUser() throws Exception {

        String id = "id1";

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser()
    void testDelete_ShouldReturnStatusForbiddenWithAuthorizeUser() throws Exception {

        String id = "id1";

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    void testDelete_ShouldDeleteProductWithValueSource_WhenProductExist(String role) throws Exception {

        String username = "testUser";
        String password = "testPassword";

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();
        _productRepository.save(newProduct);
        String id = newProduct.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER"})
    void testDelete_ShouldDeleteProductWithRoleUser_WhenProductExist(String role) throws Exception {

        String username = "testUser";
        String password = "testPassword";

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();
        _productRepository.save(newProduct);
        String id = newProduct.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }


    @ParameterizedTest
    @MethodSource("provideRoles")
    void testDelete_ShouldDeleteProductWithMethodSource_WhenProductExist(String role) throws Exception {

        String username = "testUser";
        String password = "testPassword";

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1).build();
        _productRepository.save(newProduct);
        String id = newProduct.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    static List<String> provideRoles() {
        return List.of("MANAGER", "ADMIN");
    }

}