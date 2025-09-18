package com.shopsmart.service;

import java.util.List;
import com.shopsmart.dto.OrderItemDTO;

public interface OrderItemService {

	OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO);

	List getAllOrderItems();

	OrderItemDTO getOrderItemById(Long id);



}
