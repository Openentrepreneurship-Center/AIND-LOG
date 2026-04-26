package com.backend.domain.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.cart.entity.Cart;
import com.backend.domain.cart.exception.CartEmptyException;
import com.backend.domain.cart.service.CartService;
import com.backend.domain.remotearea.repository.RemoteAreaZipCodeRepository;
import com.backend.domain.order.dto.internal.CreateOrderCommand;
import com.backend.domain.order.dto.internal.ShippingAddress;
import com.backend.domain.order.dto.mapper.OrderItemMapper;
import com.backend.domain.order.dto.mapper.OrderMapper;
import com.backend.domain.order.dto.mapper.OrderResponseMapper;
import com.backend.domain.order.dto.mapper.ShippingAddressMapper;
import com.backend.domain.order.dto.request.CreateOrderRequest;
import com.backend.domain.order.dto.response.OrderListResponse;
import com.backend.domain.order.dto.response.OrderResponse;
import com.backend.domain.order.entity.Order;
import com.backend.domain.order.entity.OrderItem;
import com.backend.domain.order.entity.OrderStatus;
import com.backend.domain.order.exception.OrderNotCancellableException;
import com.backend.domain.order.exception.OrderNotFoundException;
import com.backend.domain.order.repository.OrderRepository;
import com.backend.domain.customproduct.repository.CustomProductRepository;
import com.backend.domain.product.exception.InsufficientStockException;
import com.backend.domain.product.repository.ProductRepository;
import com.backend.domain.sitesetting.entity.SiteSetting;
import com.backend.domain.sitesetting.repository.SiteSettingRepository;
import com.backend.global.common.ItemType;

import lombok.RequiredArgsConstructor;
import com.backend.global.util.DateTimeUtil;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomProductRepository customProductRepository;
    private final CartService cartService;
    private final SiteSettingRepository siteSettingRepository;
    private final RemoteAreaZipCodeRepository remoteAreaZipCodeRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderResponseMapper orderResponseMapper;
    private final ShippingAddressMapper shippingAddressMapper;

    @Transactional
    public OrderResponse createOrderFromCart(String userId, CreateOrderRequest request) {
        Cart cart = cartService.getCartEntity(userId);

        if (cart.isEmpty()) {
            throw new CartEmptyException();
        }

        ShippingAddress shippingAddress = shippingAddressMapper.toInternal(request.shippingAddress());

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(orderItemMapper::toEntity)
                .toList();

        BigDecimal totalAmount = cart.getTotalAmount();
        BigDecimal shippingFee = calculateShippingFee(totalAmount, shippingAddress.zipCode());
        String orderNumber = generateOrderNumber();

        CreateOrderCommand command = new CreateOrderCommand(
                orderNumber,
                cart.getUser(),
                orderItems,
                totalAmount,
                shippingFee,
                shippingAddress
        );

        Order order = orderMapper.toEntity(command);
        Order savedOrder = orderRepository.saveAndFlush(order);

        return orderResponseMapper.toResponse(savedOrder);
    }

    @Transactional
    public void completeOrderPayment(Order order) {
        order.confirm();
        cartService.clearCart(order.getUser().getId());
    }

    public Order getOrderEntity(String orderId) {
        return orderRepository.getByIdOrThrow(orderId);
    }

    public OrderResponse getOrder(String userId, String orderId) {
        Order order = orderRepository.getByIdOrThrow(orderId);
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException();
        }
        return orderResponseMapper.toResponse(order);
    }

    public OrderListResponse getUserOrders(String userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdAndStatusNot(userId, OrderStatus.PENDING, pageable);
        return orderResponseMapper.toListResponse(orders);
    }

    public OrderListResponse getUserOrdersByStatus(String userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        return orderResponseMapper.toListResponse(orders);
    }

    @Transactional
    public OrderResponse cancelOrder(String userId, String orderId, String reason) {
        Order order = orderRepository.getByIdOrThrow(orderId);

        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException();
        }

        if (order.isPending()) {
            order.delete();
            return orderResponseMapper.toResponse(order);
        }

        if (!order.isCancellable()) {
            throw new OrderNotCancellableException();
        }

        restoreProductStock(order);
        order.cancel(reason);

        return orderResponseMapper.toResponse(order);
    }

    @Transactional
    public void softDeleteOrder(Order order) {
        order.delete();
    }

    @Transactional
    public void cancelOrderByDelivery(Order order) {
        restoreProductStock(order);
        order.cancel("배송 취소");
    }

    @Transactional
    public void validateAndDecreaseStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getItemType() == ItemType.PRODUCT) {
                int updated = productRepository.decreaseStockAtomically(
                        item.getItemId(), item.getQuantity());
                if (updated == 0) {
                    throw new InsufficientStockException();
                }
            } else if (item.getItemType() == ItemType.CUSTOM_PRODUCT) {
                int updated = customProductRepository.decreaseStockAtomically(
                        item.getItemId(), item.getQuantity());
                if (updated == 0) {
                    throw new InsufficientStockException();
                }
            }
        }
    }

    @Transactional
    public void restoreProductStock(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getItemType() == ItemType.PRODUCT) {
                productRepository.increaseStockAtomically(item.getItemId(), item.getQuantity());
            } else if (item.getItemType() == ItemType.CUSTOM_PRODUCT) {
                customProductRepository.increaseStockAtomically(item.getItemId(), item.getQuantity());
            }
        }
    }

    private BigDecimal calculateShippingFee(BigDecimal totalAmount, String zipCode) {
        SiteSetting siteSetting = siteSettingRepository.findFirstOrThrow();
        BigDecimal threshold = BigDecimal.valueOf(siteSetting.getFreeShippingThreshold());
        BigDecimal fee = BigDecimal.valueOf(siteSetting.getShippingFee());

        BigDecimal baseFee = totalAmount.compareTo(threshold) >= 0 ? BigDecimal.ZERO : fee;
        BigDecimal extraFee = remoteAreaZipCodeRepository.existsByZipCode(zipCode)
                ? BigDecimal.valueOf(siteSetting.getExtraShippingFee())
                : BigDecimal.ZERO;

        return baseFee.add(extraFee);
    }

    private String generateOrderNumber() {
        String datePrefix = DateTimeUtil.today().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "ORD-" + datePrefix;

        for (int attempt = 0; attempt < 5; attempt++) {
            int nextSequence = orderRepository.findMaxOrderNumberByPrefix(prefix)
                    .map(maxOrderNumber -> {
                        String sequencePart = maxOrderNumber.substring(maxOrderNumber.lastIndexOf('-') + 1);
                        return Integer.parseInt(sequencePart) + 1;
                    })
                    .orElse(1);

            String orderNumber = String.format("%s-%04d", prefix, nextSequence + attempt);
            if (!orderRepository.existsByOrderNumberIncludingDeleted(orderNumber)) {
                return orderNumber;
            }
        }

        java.security.SecureRandom random = new java.security.SecureRandom();
        return String.format("%s-%04d", prefix, random.nextInt(10000));
    }
}
