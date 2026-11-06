package edu.testing.quickcart.service;
import java.math.BigDecimal;
public interface PaymentGateway {
    boolean authorize(String customerId, BigDecimal amount);
}
