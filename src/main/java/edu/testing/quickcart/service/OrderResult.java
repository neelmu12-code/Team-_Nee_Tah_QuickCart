package edu.testing.quickcart.service;
import edu.testing.quickcart.model.OrderQuote;
public record OrderResult(Status status, OrderQuote quote, String message) {
    public enum Status { ACCEPTED, OUT_OF_STOCK, PAYMENT_DECLINED }
}
