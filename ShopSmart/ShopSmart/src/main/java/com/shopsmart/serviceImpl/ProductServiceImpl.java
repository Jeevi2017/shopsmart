package com.shopsmart.serviceImpl;

import com.shopsmart.document.ProductDocument;
import com.shopsmart.dto.BulkUploadResultDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.entity.Category;
import com.shopsmart.entity.Product;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.CategoryRepository;
import com.shopsmart.repository.ProductDocumentRepository;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.service.ProductService;
import com.shopsmart.util.CsvHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapProductToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Id", id));
        return mapProductToDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = mapDTOToProduct(productDTO);
        Product savedProduct = productRepository.save(product);

        // Business Logic: Index the new product in Elasticsearch
        productDocumentRepository.save(mapProductToDocument(savedProduct));

        return mapProductToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "Id", id));

        // Update core product fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setImages(productDTO.getImages());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());

        // Update category relationship
        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "Id", productDTO.getCategoryId()));
            existingProduct.setCategory(category);
        } else {
            throw new IllegalArgumentException("Category ID is required for product update.");
        }

        Product updatedProduct = productRepository.save(existingProduct);

        // Business Logic: Update the corresponding document in Elasticsearch
        productDocumentRepository.save(mapProductToDocument(updatedProduct));
        
        return mapProductToDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "Id", categoryId));

        return productRepository.findByCategory(category).stream().map(this::mapProductToDTO)
                .collect(Collectors.toList());
    }

    // 🔍 New Business Logic: Implement Elasticsearch for searching products
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String searchTerm) {
        List<ProductDocument> productDocuments = productDocumentRepository
                .findByNameContainingOrDescriptionContaining(searchTerm, searchTerm);
        
        return productDocuments.stream()
                .map(this::mapDocumentToDTO)
                .collect(Collectors.toList());
    }

    // New Business Logic: Filter products by price range
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return products.stream().map(this::mapProductToDTO).collect(Collectors.toList());
    }

    // Implementation for interface method with double parameters
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByPriceRange(double minPrice, double maxPrice) {
        return getProductsByPriceRange(BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice));
    }

    // New Business Logic: Filter products by stock availability
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsInStock() {
        List<Product> products = productRepository.findByStockQuantityGreaterThan(0);
        return products.stream().map(this::mapProductToDTO).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", "Id", id));
        productRepository.deleteById(id);

        // Business Logic: Delete the product from Elasticsearch as well
        productDocumentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BulkUploadResultDTO uploadProductsFromCsv(MultipartFile file) {
        int totalProcessed = 0;
        int addedCount = 0;
        int skippedCount = 0;
        StringBuilder messageBuilder = new StringBuilder();

        try {
            List<ProductDTO> productDTOs = CsvHelper.csvToProducts(file.getInputStream());
            totalProcessed = productDTOs.size();

            for (ProductDTO productDTO : productDTOs) {
                Category category = null;
                if (productDTO.getCategoryId() != null) {
                    category = categoryRepository.findById(productDTO.getCategoryId()).orElse(null);
                }

                if (category == null) {
                    skippedCount++;
                    messageBuilder.append("Skipped '").append(productDTO.getName())
                            .append("' (Category ID ")
                            .append(productDTO.getCategoryId() != null ? productDTO.getCategoryId() : "null")
                            .append(" not found). ");
                    continue;
                }

                boolean exists = productRepository.findByNameIgnoreCaseAndCategoryId(productDTO.getName(), productDTO.getCategoryId()).isPresent();

                if (exists) {
                    skippedCount++;
                    messageBuilder.append("Skipped '").append(productDTO.getName())
                            .append("' (Duplicate found in category '").append(category.getName()).append("'). ");
                } else {
                    Product product = mapDTOToProduct(productDTO);
                    product.setCategory(category);
                    Product savedProduct = productRepository.save(product);
                    addedCount++;
                    
                    // Business Logic: Index the newly uploaded product in Elasticsearch
                    productDocumentRepository.save(mapProductToDocument(savedProduct));
                }
            }
            String finalMessage = String.format("CSV upload complete. %d products processed: %d added, %d skipped.", totalProcessed, addedCount, skippedCount);
            if (messageBuilder.length() > 0) {
                finalMessage += " Details: " + messageBuilder.toString();
            }
            return new BulkUploadResultDTO(totalProcessed, addedCount, skippedCount, finalMessage);

        } catch (IOException e) {
            return new BulkUploadResultDTO(totalProcessed, addedCount, skippedCount, "Failed to read CSV file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace for debugging
            return new BulkUploadResultDTO(totalProcessed, addedCount, skippedCount, "Failed to process CSV file: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public List<ProductDTO> createMultipleProducts(List<ProductDTO> productDTOs) {
        List<Product> products = productDTOs.stream().map(this::mapDTOToProduct).collect(Collectors.toList());
        List<Product> savedProducts = productRepository.saveAll(products);

        // Business Logic: Index all newly created products in Elasticsearch
        List<ProductDocument> productDocuments = savedProducts.stream()
            .map(this::mapProductToDocument)
            .collect(Collectors.toList());
        productDocumentRepository.saveAll(productDocuments);

        return savedProducts.stream().map(this::mapProductToDTO).collect(Collectors.toList());
    }

    private ProductDTO mapProductToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setImages(product.getImages());
        productDTO.setPrice(product.getPrice());
        productDTO.setStockQuantity(product.getStockQuantity());

        if (product.getCategory() != null) {
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setCategoryName(product.getCategory().getName());
        }
        return productDTO;
    }

    private Product mapDTOToProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setId(productDTO.getId());
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setImages(productDTO.getImages());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "Id", productDTO.getCategoryId()));
            product.setCategory(category);
        } else {
            throw new IllegalArgumentException("Category ID is required for product creation/update.");
        }
        return product;
    }

    private ProductDocument mapProductToDocument(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        if (product.getCategory() != null) {
            document.setCategoryId(product.getCategory().getId());
            document.setCategoryName(product.getCategory().getName());
        }
        return document;
    }

    private ProductDTO mapDocumentToDTO(ProductDocument document) {
        ProductDTO dto = new ProductDTO();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setDescription(document.getDescription());
        dto.setCategoryId(document.getCategoryId());
        dto.setCategoryName(document.getCategoryName());
        // Note: price, images, and stock are not always in the search index
        // so you might need to fetch the full product from the database
        // for detailed views.
        return dto;
    }

}