package com.shopsmart.repository;

import com.shopsmart.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {

    // Custom query method for searching by name or description
    List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);

    // You can add more complex search methods here, for example:
    // List<ProductDocument> findByPriceBetween(BigDecimal min, BigDecimal max);
}