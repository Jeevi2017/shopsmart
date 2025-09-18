package com.shopsmart.util;

import com.shopsmart.dto.ProductDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsvHelper {


    public static String[] HEADERS = { "name", "description", "price", "categoryId", "stockQuantity", "images" };

    public static List<ProductDTO> csvToProducts(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                     .setHeader(HEADERS)
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(fileReader)) {

            List<ProductDTO> products = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                try {
                    ProductDTO productDTO = new ProductDTO();
                    productDTO.setName(csvRecord.get("name"));
                    productDTO.setDescription(csvRecord.get("description"));
                    productDTO.setPrice(new BigDecimal(csvRecord.get("price")));
                    productDTO.setCategoryId(Long.parseLong(csvRecord.get("categoryId")));
                    productDTO.setStockQuantity(Long.parseLong(csvRecord.get("stockQuantity")));

                    String imagesString = csvRecord.get("images");
                    if (imagesString != null && !imagesString.trim().isEmpty()) {
                        productDTO.setImages(Arrays.asList(imagesString.split(",")).stream()
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList()));
                    } else {
                        productDTO.setImages(new ArrayList<>());
                    }

                    products.add(productDTO);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("CSV parsing error: Invalid number format for row " + csvRecord.getRecordNumber() + ". " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("CSV parsing error: Missing or invalid header in row " + csvRecord.getRecordNumber() + ". " + e.getMessage());
                }
            }
            return products;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public static boolean hasCsvFormat(MultipartFile file) {
        String type = "text/csv";
        return type.equals(file.getContentType()) || file.getOriginalFilename().endsWith(".csv");
    }
}
