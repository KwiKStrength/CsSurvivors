package Class;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private Customers customer;
    private Date date;
    private String orderStatus;
    private double orderPrice;
    private List<Product> products; // List of products

    public Order(String orderId, Customers customer, Date date, String orderStatus, double orderPrice) {
        this.orderId = orderId;
        this.customer = customer;
        this.date = date;
        this.orderStatus = orderStatus;
        this.orderPrice = orderPrice;
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    // Getters and setters
    public List<Product> getProducts() {
        return products;
    }


    // Getters and setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Customers getCustomer() {
        return customer;
    }

    public void setCustomer(Customers customer) {
        this.customer = customer;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }


    public enum StatusOrder {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, RECEIVED
    }

}
