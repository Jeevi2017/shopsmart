package com.shopsmart.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "products")
@Setting(shards = 1, replicas = 0)
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, name = "name", analyzer = "english", searchAnalyzer = "english")
    private String name;

    @Field(type = FieldType.Text, name = "description", analyzer = "english", searchAnalyzer = "english")
    private String description;

    @Field(type = FieldType.Double, name = "price")
    private BigDecimal price;

    @Field(type = FieldType.Keyword, name = "categoryName")
    private String categoryName;

    @Field(type = FieldType.Long, name = "categoryId")
    private Long categoryId;

    @Field(type = FieldType.Integer, name = "stockQuantity")
    private Integer stockQuantity;

    @Field(type = FieldType.Keyword, name = "tags")
    private List<String> tags;

    // Constructors, Getters, and Setters
    public ProductDocument() {}

    public ProductDocument(Long id, String name, String description, BigDecimal price, String categoryName, Long categoryId, Integer stockQuantity, List<String> tags) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.categoryName = categoryName;
        this.categoryId = categoryId;
        this.stockQuantity = stockQuantity;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "ProductDocument{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", categoryName='" + categoryName + '\'' +
                ", categoryId=" + categoryId +
                ", stockQuantity=" + stockQuantity +
                ", tags=" + tags +
                '}';
    }
}