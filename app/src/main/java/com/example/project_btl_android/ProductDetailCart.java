package com.example.project_btl_android;

public class ProductDetailCart {
    private String cartId;
    private String productId;
    private int quantity;
    private Product product;

    public ProductDetailCart() {
    }

    public ProductDetailCart(String cartId, String productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getCartId() {
        return cartId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
