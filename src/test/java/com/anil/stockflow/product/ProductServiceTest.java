
package com.anil.stockflow.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository);
    }

    @Test
    void getProductByIdShouldReturnProductWhenFound() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");

        when(productRepository.findById(1L))
                .thenReturn(java.util.Optional.of(product));

        Product result = productService.getProductById(1L);

        assertEquals("Wireless Mouse", result.getName());
        verify(productRepository).findById(1L);
    }
    @Test
    void getProductByIdShouldThrowExceptionWhenNotFound() {
    when(productRepository.findById(999L)).thenReturn(java.util.Optional.empty());
    assertThrows(
            ProductNotFoundException.class,() -> productService.getProductById(999L));
    }
    @Test
void createProductShouldSaveProduct() {
    Product product = new Product();
    product.setName("USB-C Hub");
    product.setPrice(new BigDecimal("1799.00"));
    product.setQuantity(20);

    when(productRepository.save(any(Product.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    Product result = productService.createProduct(product);

    assertEquals("USB-C Hub", result.getName());
    assertEquals(new BigDecimal("1799.00"), result.getPrice());
    assertEquals(20, result.getQuantity());

    verify(productRepository).save(product);
}

@Test
void updateProductShouldUpdateExistingProduct() {
    Product existingProduct = new Product();
    existingProduct.setId(1L);
    existingProduct.setName("Old Mouse");
    existingProduct.setPrice(new BigDecimal("500.00"));
    existingProduct.setQuantity(5);

    Product updatedProduct = new Product();
    updatedProduct.setName("Wireless Mouse");
    updatedProduct.setPrice(new BigDecimal("799.00"));
    updatedProduct.setQuantity(25);

    when(productRepository.findById(1L))
            .thenReturn(java.util.Optional.of(existingProduct));

    when(productRepository.save(existingProduct))
            .thenReturn(existingProduct);

    Product result = productService.updateProduct(1L, updatedProduct);

    assertEquals("Wireless Mouse", result.getName());
    assertEquals(new BigDecimal("799.00"), result.getPrice());
    assertEquals(25, result.getQuantity());

    verify(productRepository).findById(1L);
    verify(productRepository).save(existingProduct);
}

@Test
void deleteProductShouldDeleteExistingProduct() {
    Product product = new Product();
    product.setId(1L);
    product.setName("Wireless Mouse");

    when(productRepository.findById(1L))
            .thenReturn(java.util.Optional.of(product));

    productService.deleteProduct(1L);

    verify(productRepository).findById(1L);
    verify(productRepository).delete(product);
}

@Test
void getAllProductsShouldReturnAllProductsWhenSearchIsBlank() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> productPage =
            new PageImpl<>(List.of(new Product()));

    when(productRepository.findAll(pageable)).thenReturn(productPage);

    Page<Product> result = productService.getAllProducts(" ", pageable);

    assertEquals(productPage, result);
    verify(productRepository).findAll(pageable);
}

@Test
void getAllProductsShouldSearchByNameWhenSearchIsProvided() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> productPage =
            new PageImpl<>(List.of(new Product()));

    when(productRepository.findByNameContainingIgnoreCase(
            "Laptop", pageable)).thenReturn(productPage);

    Page<Product> result =
            productService.getAllProducts("Laptop", pageable);

    assertEquals(productPage, result);
    verify(productRepository)
            .findByNameContainingIgnoreCase("Laptop", pageable);
}

@Test
void updateProductShouldThrowExceptionWhenProductDoesNotExist() {
    Long id = 999L;
    Product updatedProduct = new Product();

    when(productRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
            ProductNotFoundException.class,
            () -> productService.updateProduct(id, updatedProduct));

    verify(productRepository, never()).save(any(Product.class));
}

@Test
void deleteProductShouldThrowExceptionWhenProductDoesNotExist() {
    Long id = 999L;

    when(productRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(
            ProductNotFoundException.class,
            () -> productService.deleteProduct(id));

    verify(productRepository, never()).delete(any(Product.class));
}

@Test
void getAllProductsShouldReturnAllProductsWhenSearchIsNull() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> productPage =
            new PageImpl<>(List.of(new Product()));

    when(productRepository.findAll(pageable)).thenReturn(productPage);

    Page<Product> result = productService.getAllProducts(null, pageable);

    assertEquals(productPage, result);
    verify(productRepository).findAll(pageable);
    verify(productRepository, never())
            .findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
}


}
