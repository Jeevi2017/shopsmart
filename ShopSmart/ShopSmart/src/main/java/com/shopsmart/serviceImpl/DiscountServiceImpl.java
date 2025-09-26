package com.shopsmart.serviceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shopsmart.dto.DiscountDTO;
import com.shopsmart.entity.Discount;
import com.shopsmart.entity.Discount.DiscountType;
import com.shopsmart.exception.ResourceNotFoundException;
import com.shopsmart.repository.DiscountRepository;
import com.shopsmart.service.DiscountService;

import jakarta.transaction.Transactional;

@Service
public class DiscountServiceImpl implements DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    // ModelMapper is not injected here, but used in mapDTOToDiscount/mapDiscountToDTO.
    // If you are using ModelMapper for automatic DTO-Entity mapping, ensure it's configured as a bean.
    // private ModelMapper modelMapper; // Uncomment and @Autowired if directly injecting

    @Override
    @Transactional
    public DiscountDTO createDiscount(DiscountDTO discountDTO) {
        if (discountRepository.findByCode(discountDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Discount code '" + discountDTO.getCode() + "' already exists.");
        }
        Discount discount = mapDTOToDiscount(discountDTO);
        discount.setUsedCount(0);
        Discount savedDiscount = discountRepository.save(discount);
        return mapDiscountToDTO(savedDiscount);
    }

    @Override
    public DiscountDTO getDiscountById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", id));
        return mapDiscountToDTO(discount);
    }

    @Override
    public DiscountDTO getDiscountByCode(String code) {
        Discount discount = discountRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "code", code));
        return mapDiscountToDTO(discount);
    }

    @Override
    public List<DiscountDTO> getAllDiscounts() {
        return discountRepository.findAll().stream().map(this::mapDiscountToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO) {
        Discount existingDiscount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", id));

        if (!existingDiscount.getCode().equals(discountDTO.getCode())
                && discountRepository.findByCode(discountDTO.getCode()).isPresent()) {
            throw new IllegalArgumentException("Discount code '" + discountDTO.getCode() + "' already exists.");
        }

        existingDiscount.setCode(discountDTO.getCode());
        existingDiscount.setType(DiscountType.valueOf(discountDTO.getType()));
        existingDiscount.setValue(discountDTO.getValue());
        existingDiscount.setMinOrderAmount(discountDTO.getMinOrderAmount());
        
        // Use Instant directly from the DTO
        existingDiscount.setStartDate(discountDTO.getStartDate());
        existingDiscount.setEndDate(discountDTO.getEndDate());

        existingDiscount.setUsageLimit(discountDTO.getUsageLimit());
        existingDiscount.setActive(discountDTO.isActive());

        Discount updatedDiscount = discountRepository.save(existingDiscount);
        return mapDiscountToDTO(updatedDiscount);
    }

    @Override
    @Transactional
    public void deleteDiscount(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", id));
        discountRepository.delete(discount);
    }

    @Override
    public boolean isValidDiscount(String code, BigDecimal currentAmount) {
        return discountRepository.findByCode(code).map(discount -> {
            Instant now = Instant.now();
            if (!discount.isActive()) {
                System.out.println("DEBUG: Discount " + code + " is not active.");
                return false;
            }
            // Directly compare Instant from entity
            if (now.isBefore(discount.getStartDate()) || now.isAfter(discount.getEndDate())) {
                System.out.println("DEBUG: Discount " + code + " is outside its valid date range.");
                return false;
            }
            if (discount.getUsageLimit() != null && discount.getUsedCount() >= discount.getUsageLimit()) {
                System.out.println("DEBUG: Discount " + code + " has reached its usage limit.");
                return false;
            }
            if (discount.getMinOrderAmount() != null && currentAmount.compareTo(discount.getMinOrderAmount()) < 0) {
                System.out.println("DEBUG: Discount " + code + " requires a minimum order amount of "
                        + discount.getMinOrderAmount() + ".");
                return false;
            }
            return true;
        }).orElse(false);
    }

    // Helper method to map DTO to Entity
    private Discount mapDTOToDiscount(DiscountDTO discountDTO) {
        Discount discount = new Discount();
        if (discountDTO.getId() != null) {
            discount.setId(discountDTO.getId());
        }
        discount.setCode(discountDTO.getCode());
        discount.setType(DiscountType.valueOf(discountDTO.getType()));
        discount.setValue(discountDTO.getValue());
        discount.setMinOrderAmount(discountDTO.getMinOrderAmount());
        
        // Set Instant directly from DTO
        discount.setStartDate(discountDTO.getStartDate());
        discount.setEndDate(discountDTO.getEndDate());

        discount.setUsageLimit(discountDTO.getUsageLimit());
        discount.setActive(discountDTO.isActive());
        discount.setUsedCount(discountDTO.getUsedCount() != null ? discountDTO.getUsedCount() : 0);
        return discount;
    }

    // Helper method to map Entity to DTO
    private DiscountDTO mapDiscountToDTO(Discount discount) {
        DiscountDTO discountDTO = new DiscountDTO();
        discountDTO.setId(discount.getId());
        discountDTO.setCode(discount.getCode());
        discountDTO.setType(discount.getType().name());
        discountDTO.setValue(discount.getValue());
        discountDTO.setMinOrderAmount(discount.getMinOrderAmount());
        
        // Set Instant directly from Entity
        discountDTO.setStartDate(discount.getStartDate());
        discountDTO.setEndDate(discount.getEndDate());

        discountDTO.setUsageLimit(discount.getUsageLimit());
        discountDTO.setUsedCount(discount.getUsedCount());
        discountDTO.setActive(discount.isActive());
        return discountDTO;
    }

    
    @Override
    public List<DiscountDTO> getAvailableCouponsForCustomer(Long customerId) {
        // OPTIMIZATION: For better performance, consider adding a custom query to your DiscountRepository
        // to fetch only active and non-expired discounts directly from the DB.
        // Example: List<Discount> findByActiveTrueAndEndDateGreaterThanEqual(Instant instant);
        List<Discount> allActiveDiscounts = discountRepository.findByActiveTrue(); // Assumes this method exists in DiscountRepository

        Instant now = Instant.now();

        // Filter discounts to ensure they are not expired
        List<Discount> availableDiscounts = allActiveDiscounts.stream()
                .filter(discount -> {
                    // Check if the discount's end date is after or equal to the current time.
                    return discount.getEndDate().isAfter(now) || discount.getEndDate().equals(now);
                })
                // FUTURE ENHANCEMENT: Add more filtering logic here if needed, e.g.:
                // - Check against customer-specific usage limits (if you track per-user usage)
                // - Filter based on product/category applicability (if your discounts are not cart-wide)
                // - Filter based on customer segments/groups
                .collect(Collectors.toList());

        // Map the filtered entities to DTOs
        return availableDiscounts.stream()
                .map(this::mapDiscountToDTO) // Using the existing helper method
                .collect(Collectors.toList());
    }
}
