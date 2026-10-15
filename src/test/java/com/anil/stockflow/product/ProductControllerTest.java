
package com.anil.stockflow.product;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.anil.stockflow.config.JwtAuthenticationFilter;
import com.anil.stockflow.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.anil.stockflow.product.ProductNotFoundException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(
    controllers = ProductController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

   

    @Test
    void getAllProductsShouldReturnProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");
        product.setPrice(new BigDecimal("799.00"));
        product.setQuantity(25);

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(productService.getAllProducts(
                isNull(), any(Pageable.class)))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Wireless Mouse"))
                .andExpect(jsonPath("$.content[0].quantity")
                        .value(25))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
@Test
void getAllProductsShouldReturnBadRequestForNegativePage()
        throws Exception {

    mockMvc.perform(get("/api/products")
                    .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                    .value("Page must be 0 or greater, and size must be between 1 and 100"));
}

@Test
void getProductByIdShouldReturnNotFoundWhenProductDoesNotExist()
        throws Exception {

    when(productService.getProductById(999L))
            .thenThrow(new ProductNotFoundException(999L));

    mockMvc.perform(get("/api/products/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error")
                    .value("Product not found with id: 999"));
}

@Test
void createProductShouldReturnCreatedStatus() throws Exception {
    Product savedProduct = new Product();
    savedProduct.setId(10L);
    savedProduct.setName("Keyboard");
    savedProduct.setPrice(new BigDecimal("1500.00"));
    savedProduct.setQuantity(10);

    when(productService.createProduct(any(Product.class)))
            .thenReturn(savedProduct);

    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Keyboard",
                                "price": 1500.00,
                                "quantity": 10
                            }
                            """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Keyboard"))
            .andExpect(jsonPath("$.quantity").value(10));
}

@Test
void createProductShouldReturnBadRequestForInvalidData() throws Exception {
    mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "",
                                "price": -10,
                                "quantity": -5
                            }
                            """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.price").exists())
            .andExpect(jsonPath("$.quantity").exists());
}

@Test
void updateProductShouldReturnUpdatedProduct() throws Exception {
    Product updatedProduct = new Product();
    updatedProduct.setId(1L);
    updatedProduct.setName("Updated Mouse");
    updatedProduct.setPrice(new BigDecimal("999.00"));
    updatedProduct.setQuantity(30);

    when(productService.updateProduct(
            org.mockito.ArgumentMatchers.eq(1L),
            any(Product.class)))
            .thenReturn(updatedProduct);

    mockMvc.perform(put("/api/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Updated Mouse",
                                "price": 999.00,
                                "quantity": 30
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Updated Mouse"))
            .andExpect(jsonPath("$.price").value(999.00))
            .andExpect(jsonPath("$.quantity").value(30));
}

@Test
void deleteProductShouldReturnNoContent() throws Exception {
    mockMvc.perform(
                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .delete("/api/products/1"))
            .andExpect(status().isNoContent());

    verify(productService).deleteProduct(1L);
}

@Test
void getAllProductsShouldReturnBadRequestForInvalidPageType()
        throws Exception {

    mockMvc.perform(get("/api/products")
                    .param("page", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error")
                    .value("Invalid value for parameter: page"));
}

@Test
void getAllProductsShouldReturnBadRequestWhenPageSizeIsZero() throws Exception {
    mockMvc.perform(get("/api/products")
                    .param("size", "0"))
            .andExpect(status().isBadRequest());
}

@Test
void getAllProductsShouldReturnBadRequestWhenPageSizeExceedsLimit() throws Exception {
    mockMvc.perform(get("/api/products")
                    .param("size", "101"))
            .andExpect(status().isBadRequest());
}

}
