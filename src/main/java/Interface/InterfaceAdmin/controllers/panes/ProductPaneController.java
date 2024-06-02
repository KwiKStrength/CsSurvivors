package Interface.InterfaceAdmin.controllers.panes;

import Class.Product;
import Class.Connexion;

import io.github.palexdev.mfxcore.utils.fx.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductPaneController implements Initializable {
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    private VBox tableContent;

    @FXML
    private TextField nameField, priceField, descriptionField; // Add fields for creating new products

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadProductsFromDatabase();
        setupProductTable();
    }

    private void loadProductsFromDatabase() {
        String query = "SELECT ITEMID, name, unitprice, description, image FROM ITEM";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(rs.getInt("ITEMID"), rs.getString("name"), rs.getString("image"), rs.getDouble("unitprice"), rs.getString("description")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupProductTable() {
        tableContent.getChildren().clear();  // Clear previous content
        for (Product product : products) {
            HBox row = new HBox(10);
            row.getChildren().addAll(createCellLabel(String.valueOf(product.getProductId())), createCellLabel(product.getProductName()), createImageView(product.getImage()), createCellLabel(String.format("%.2f", product.getPrice())), createCellLabel(product.getDescription()), createActions(product));
            tableContent.getChildren().add(row);
        }
    }

    private HBox createActions(Product product) {
        HBox actions = new HBox(10);
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        editButton.setOnAction(e -> editProduct(product));
        deleteButton.setOnAction(e -> deleteProduct(product));

        actions.getChildren().addAll(editButton, deleteButton);
        return actions;
    }

    private void editProduct(Product product) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit the product: " + product.getProductName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(product.getProductName());
        TextField priceField = new TextField(String.valueOf(product.getPrice()));
        TextField descriptionField = new TextField(product.getDescription());
        ImageView imageView = new ImageView();
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            imageView.setImage(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(product.getImage()))));
        }
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        Button chooseImageButton = new Button("Choose Image");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Image:"), 0, 3);
        grid.add(imageView, 1, 3);
        grid.add(chooseImageButton, 2, 3);

        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                product.setImage(encodeImageToBase64(image));
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int productId = product.getProductId();
            String newName = nameField.getText();
            double newPrice = Double.parseDouble(priceField.getText());
            String newDescription = descriptionField.getText();
            String newImageBase64 = encodeImageToBase64(imageView.getImage());

            // Mettre à jour dans la base de données
            updateProductInDatabase(productId, newName, newPrice, newDescription, newImageBase64);

            // Mettre à jour localement sans recharger toute la liste
            product.setProductName(newName);
            product.setPrice(newPrice);
            product.setDescription(newDescription);
            product.setImage(newImageBase64);
            setupProductTable(); // Rafraîchir uniquement l'affichage du tableau
        }
    }

    public String encodeImageToBase64(Image image) {
        // Create a ByteArrayOutputStream to hold the image bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Convert Image to BufferedImage
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            // Write the BufferedImage to the ByteArrayOutputStream as a PNG
            ImageIO.write(bImage, "png", baos);
            // Convert the ByteArrayOutputStream to a byte array
            byte[] imageBytes = baos.toByteArray();
            // Encode the byte array to a Base64 string
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null; // In case of an error, return null or handle it as per your requirement
        } finally {
            // Try to close the ByteArrayOutputStream
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateProductInDatabase(int productId, String name, double price, String description, String base64Image) {
        String sql = "UPDATE ITEM SET name = ?, unitprice = ?, description = ?, image = ? WHERE ITEMID = ?";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, description);
            ps.setString(4, base64Image); // Set the image as a Base64 string
            ps.setInt(5, productId);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Product updated successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void deleteProduct(Product product) {
        String sql = "DELETE FROM ITEM WHERE ITEMID = ?";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getProductId());
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Product deleted successfully!");
                products.remove(product);
                setupProductTable();  // Refresh table content
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Label createCellLabel(String text) {
        Label label = new Label(text);
        label.setMinWidth(100);
        label.setMaxWidth(Double.MAX_VALUE); // Ensures the label takes up all available space
        return label;
    }

    private ImageView createImageView(String base64Image) {
        ImageView imageView = new ImageView();
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageData = Base64.getDecoder().decode(base64Image);
                if (imageData.length > 0) {
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    imageView.setImage(image);
                    imageView.setFitWidth(100); // Set the width of the image
                    imageView.setFitHeight(100); // Set the height of the image
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error decoding Base64 image: " + e.getMessage());
            }
        } else {
            System.out.println("Base64 image string is empty or null");
        }
        return imageView;
    }
}
