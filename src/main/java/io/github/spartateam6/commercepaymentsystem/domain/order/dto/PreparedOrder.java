package io.github.spartateam6.commercepaymentsystem.domain.order.dto;

import java.math.BigDecimal;
import java.util.List;

public record PreparedOrder(List<PreparedItem> items, BigDecimal totalAmount) {
}
