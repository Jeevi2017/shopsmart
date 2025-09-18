package com.shopsmart.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopsmart.dto.OrderItemDTO;
import com.shopsmart.dto.ProductDTO;
import com.shopsmart.entity.OrderItem;
import com.shopsmart.entity.Product;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.OrderItemRepository;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.service.OrderItemService;

@Service
public class OrderItemServiceImpl implements OrderItemService {

	@Autowired
	private OrderItemRepository orderItemRepository;

	@Autowired
	private ProductRepository productRepository;

	@Override
	@Transactional
	public OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO) {
		OrderItem orderItem = new OrderItem();

		ProductDTO productDto = orderItemDTO.getProductDetails();
		if (productDto != null && productDto.getId() != null) {
			Product product = productRepository.findById(productDto.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Product", "Id", productDto.getId()));
			orderItem.setProduct(product);

			orderItem.setPrice(product.getPrice());
		} else {
			throw new IllegalArgumentException("Product ID is required for an order item.");
		}

		orderItem.setQuantity(orderItemDTO.getQuantity());

		OrderItem savedOrderItem = orderItemRepository.save(orderItem);

		return convertToDTO(savedOrderItem);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrderItemDTO> getAllOrderItems() {
		return orderItemRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	private OrderItemDTO convertToDTO(OrderItem orderItem) {
		OrderItemDTO dto = new OrderItemDTO();
		dto.setId(orderItem.getId());
		dto.setQuantity(orderItem.getQuantity());
		dto.setPrice(orderItem.getPrice());

		if (orderItem.getProduct() != null) {
			ProductDTO productDto = new ProductDTO();
			productDto.setId(orderItem.getProduct().getId());
			productDto.setName(orderItem.getProduct().getName());
			productDto.setPrice(orderItem.getProduct().getPrice());
			productDto.setStockQuantity(orderItem.getProduct().getStockQuantity());
			productDto.setDescription(orderItem.getProduct().getDescription());
			productDto.setImages(orderItem.getProduct().getImages());
            if (orderItem.getProduct().getCategory() != null) {
                productDto.setCategoryId(orderItem.getProduct().getCategory().getId());
                productDto.setCategoryName(orderItem.getProduct().getCategory().getName());
            }

			dto.setProductDetails(productDto);
		}
		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public OrderItemDTO getOrderItemById(Long id) {
		OrderItem orderItem = orderItemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("OrderItem", "Id", id));
		return convertToDTO(orderItem);
	}

}