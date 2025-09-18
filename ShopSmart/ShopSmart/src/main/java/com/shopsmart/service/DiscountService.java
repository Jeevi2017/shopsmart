package com.shopsmart.service;

import java.math.BigDecimal;
import java.util.List;

import com.shopsmart.dto.DiscountDTO;

public interface DiscountService {

	DiscountDTO createDiscount(DiscountDTO discountDTO);
    DiscountDTO getDiscountById(Long id);
    DiscountDTO getDiscountByCode(String code);
    List<DiscountDTO> getAllDiscounts();
    DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO);
    void deleteDiscount(Long id);

    boolean isValidDiscount(String code, BigDecimal currentAmount);
	List<DiscountDTO> getAvailableCouponsForCustomer(Long customerId);
}
