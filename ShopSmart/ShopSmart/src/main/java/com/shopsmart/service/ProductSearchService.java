package com.shopsmart.service;

import com.shopsmart.document.ProductDocument;
import java.util.List;

public interface ProductSearchService {
    List<ProductDocument> searchProducts(String query);
}