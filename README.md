# Spring Boot Unit and Integration Test

<ul>
  <li>JUnit5</li>
  <li>Mockito</li>
  <li>Hamcrest</li>
</ul>

## Product Service Unit Test Examples

```java
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
```

```java
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
```

```java
    @Test
    void testGetById_ShouldThrowNotFoundException_WhenProductDoesNotExist() {
        //given
        Mockito.when(_productRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(NotFoundException.class, () -> _productService.GetById(""));
        Mockito.verify(_productRepository).findById(Mockito.any());
        Mockito.verifyNoInteractions(_productMapper);
    }
```

## Integration Test Examples

```java
    @Test
    void testUpdate_ShouldUpdateProductAndReturnProductDto_WhenValidRequestAndProductExist() throws Exception {

        Product newProduct = Product.builder().name("test").description("description").price(10.0).stockQuantity(1)
                .build();

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
```


```java
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
```

```java
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDelete_ShouldNotFoundExceptionWithAdminUser_WhenProductDoesNotExist() throws Exception {

        String id = "id1";

        mockMvc.perform(MockMvcRequestBuilders.delete(ENDPOINT + "{id}", id))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(String.format("Product (%s) not found.", id)));
    }
```
