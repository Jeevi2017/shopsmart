package com.shopsmart.serviceImpl;

import com.shopsmart.document.ProductDocument;
import com.shopsmart.repository.ProductDocumentRepository;
import com.shopsmart.service.ProductSearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ElasticsearchProductSearchService implements ProductSearchService {

    private final ProductDocumentRepository productDocumentRepository;

    public ElasticsearchProductSearchService(ProductDocumentRepository productDocumentRepository) {
        this.productDocumentRepository = productDocumentRepository;
    }

    @Override
    public List<ProductDocument> searchProducts(String query) {
        return productDocumentRepository.findByNameContainingOrDescriptionContaining(query, query);
    }
}
