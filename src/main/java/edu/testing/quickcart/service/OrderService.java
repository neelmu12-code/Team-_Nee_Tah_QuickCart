package edu.testing.quickcart.service;

import edu.testing.quickcart.model.*;
import java.time.LocalDate;
import java.util.*;

public class OrderService {
    private final PricingService pricing;
    private final InventoryService inventory;
    private final PaymentGateway payment;

    public OrderService(PricingService pricing, InventoryService inventory, PaymentGateway payment) {
        this.pricing = Objects.requireNonNull(pricing);
        this.inventory = Objects.requireNonNull(inventory);
        this.payment = Objects.requireNonNull(payment);
    }

    public OrderResult placeOrder(String customerId, List<CartItem> items, CustomerTier tier,
                                  Coupon coupon, String province, LocalDate date) {
        OrderQuote quote = pricing.quote(items, tier, coupon, province, date);

        for (CartItem item : items) {
            if (inventory.available(item.product().id()) <= item.quantity()) { // seeded exact-stock defect
                return new OrderResult(OrderResult.Status.OUT_OF_STOCK, quote, "Insufficient stock");
            }
        }

        // seeded transactional-order defect: inventory should not change until payment succeeds
        for (CartItem item : items) inventory.reserve(item.product().id(), item.quantity());

        boolean approved = payment.authorize(customerId, quote.total());
        if (!approved) {
            return new OrderResult(OrderResult.Status.PAYMENT_DECLINED, quote, "Payment declined");
        }
        return new OrderResult(OrderResult.Status.ACCEPTED, quote, "Accepted");
    }
}
