package com.shopsmart.service;

import com.shopsmart.document.ProductDocument;
import com.shopsmart.entity.Product;
import com.shopsmart.event.ProductCreatedEvent;
import com.shopsmart.event.ProductDeletedEvent;
import com.shopsmart.event.ProductUpdatedEvent;
import com.shopsmart.repository.ProductDocumentRepository;
import com.shopsmart.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductIndexingService {

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;

    public ProductIndexingService(ProductRepository productRepository, ProductDocumentRepository productDocumentRepository) {
        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
    }

    /**
     * Maps a JPA Product entity to an Elasticsearch ProductDocument.
     */
    private ProductDocument toProductDocument(Product product) {
        if (product == null) {
            return null;
        }
        ProductDocument document = new ProductDocument();
        document.setId(product.getId());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setPrice(product.getPrice());
        if (product.getCategory() != null) {
            document.setCategoryName(product.getCategory().getName());
        }
        return document;
    }

    /**
     * Initial data indexing on application startup.
     */
    @PostConstruct
    public void indexAllProducts() {
        System.out.println("Indexing all products into Elasticsearch...");
        List<Product> allProducts = productRepository.findAll();
        List<ProductDocument> documents = allProducts.stream()
            .map(this::toProductDocument)
            .collect(Collectors.toList());
        productDocumentRepository.saveAll(documents);
        System.out.println("Product indexing complete. Total indexed: " + documents.size());
    }

    /**
     * Listens for a ProductCreatedEvent and indexes the new product.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductCreated(ProductCreatedEvent event) {
        indexProduct(event.getProduct());
    }

    /**
     * Listens for a ProductUpdatedEvent and updates the product in the index.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        indexProduct(event.getProduct());
    }

    /**
     * Listens for a ProductDeletedEvent and removes the product from the index.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductDeleted(ProductDeletedEvent event) {
        productDocumentRepository.deleteById(event.getProductId());
    }

    /**
     * Helper method to save/update a product in Elasticsearch.
     */
    private void indexProduct(Product product) {
        ProductDocument document = toProductDocument(product);
        if (document != null) {
            productDocumentRepository.save(document);
        }
    }
}