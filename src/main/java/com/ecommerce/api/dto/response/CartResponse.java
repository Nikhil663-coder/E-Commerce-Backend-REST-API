package com.ecommerce.api.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartResponse {
    private Long id;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal grandTotal;

    public CartResponse() {
    }

    public CartResponse(Long id, List<CartItemResponse> items, BigDecimal grandTotal) {
        this.id = id;
        this.items = items;
        this.grandTotal = grandTotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }
}
