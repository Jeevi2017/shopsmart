package com.shopsmart.service;

import com.shopsmart.dto.BulkUploadResultDTO;
import com.shopsmart.dto.ProductDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    // Core CRUD Operations
    ProductDTO getProductById(Long id); 
    List<ProductDTO> getAllProducts();
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(Long id, ProductDTO productDTO); 
    void deleteProduct(Long id); 

    // Category and Filter Operations
    List<ProductDTO> getProductsByCategoryId(Long categoryId);
    List<ProductDTO> getProductsByPriceRange(double minPrice, double maxPrice);
    List<ProductDTO> getProductsInStock();

    // Search Operations (using Elasticsearch)
    List<ProductDTO> searchProducts(String searchTerm);
    
    // Bulk Operations
    List<ProductDTO> createMultipleProducts(List<ProductDTO> productDTOs);
    BulkUploadResultDTO uploadProductsFromCsv(MultipartFile file);
    
}