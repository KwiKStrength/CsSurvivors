package Class;

public class Product {
    private int productId;
    private String productName;
    private String image;
    private double price;
    private String description;
    private double quantity;

    public Product(int productId, String productName, String image, double price, String description) {
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.price = price;
        this.description = description;

    }

    public Product(int productId, String productName, String image, double price, String description, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.image = image;
        this.price = price;
        this.description = description;
        this.quantity = quantity;

    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    // Getters and setters
}
