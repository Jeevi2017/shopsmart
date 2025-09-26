// package com.shopsmart.controller;

// import com.shopsmart.service.OrderService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/orders")
// public class OrderReportController {

//     @Autowired
//     private OrderService orderService;

//     @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CUSTOMER') ")
//     @GetMapping("/{orderId}/report/pdf")
//     public ResponseEntity<byte[]> generateOrderReportPdf(@PathVariable Long orderId) {
//         try {
//             // Corrected method call to match the updated OrderService interface
//             byte[] pdfBytes = orderService.generateOrderReportPdf(orderId);

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_PDF);
//             headers.setContentDispositionFormData("attachment", "order_report_" + orderId + ".pdf");
//             headers.setContentLength(pdfBytes.length);

//             return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
//         } catch (Exception e) {
//             // Log the exception details for debugging
//             e.printStackTrace();
//             return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//         }
//     }
// }
