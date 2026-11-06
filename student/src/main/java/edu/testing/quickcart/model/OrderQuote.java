package edu.testing.quickcart.model;
import java.math.BigDecimal;
public record OrderQuote(BigDecimal merchandise, BigDecimal shipping, BigDecimal tax, BigDecimal total) {}
