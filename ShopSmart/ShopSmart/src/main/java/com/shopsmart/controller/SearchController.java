package com.shopsmart.controller;

import com.shopsmart.document.ProductDocument;
import com.shopsmart.service.ProductSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ProductSearchService productSearchService;

    public SearchController(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @GetMapping("/products")
    public List<ProductDocument> searchProducts(@RequestParam(name = "q") String query) {
        return productSearchService.searchProducts(query);
    }
}