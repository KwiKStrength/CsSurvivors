package Interface.InterfaceAdmin.controllers.panes;

import Class.Category;
import Class.Connexion;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.ResourceBundle;

public class AddProductPaneController implements Initializable {
    @FXML
    private Text categoryError;
    @FXML
    private TextField productNameField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField priceField;
    @FXML
    private Label selectedFileLabel;
    @FXML
    private ImageView previewImageView;
    @FXML
    private Label selectedCategoryFileLabel;
    @FXML
    private ImageView categoryPreviewImageView;
    @FXML
    private MFXButton addCategoryButton;
    @FXML
    private MFXComboBox<Category> categoryComboBox;
    @FXML
    private MFXCheckbox platDuJour;
    @FXML
    private MFXTextField categoryName;

    private final ObservableList<Category> categories = FXCollections.observableArrayList();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCategoriesFromDatabase();
        setupCategoryComboBox();
    }

    private void loadCategoriesFromDatabase() {
        String query = "SELECT CATEGORYID, categoryname FROM CATEGORY";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            categories.clear();
            while (rs.next()) {
                int categoryId = rs.getInt("CATEGORYID");
                String categoryName = rs.getString("categoryname");
                categories.add(new Category(categoryId, categoryName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCategoryComboBox() {
        loadCategoriesFromDatabase();
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().addAll(categories);  // Add all category objects

        // Setup a StringConverter to display the category name
        categoryComboBox.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                if (category != null) {
                    return category.getCategoryName();
                }
                return null;  // Or appropriate default text
            }

            @Override
            public Category fromString(String string) {
                return categoryComboBox.getItems().stream().filter(item -> item.getCategoryName().equals(string)).findFirst().orElse(null);
            }
        });
    }

    @FXML
    private void chooseFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            selectedFileLabel.setText(selectedFile.getAbsolutePath());
            // Load and display the selected image in the preview
            Image image = new Image(selectedFile.toURI().toString());
            previewImageView.setImage(image);
        }
    }

    @FXML
    private void chooseCategoryImageAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Category Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
//            selectedCategoryFileLabel.setText(selectedFile.getAbsolutePath());
            // Load and display the selected image in the category preview
            Image image = new Image(selectedFile.toURI().toString());
            categoryPreviewImageView.setImage(image);
        }
    }

    @FXML
    private void addCategoryAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Category Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(new Stage());

        if (selectedFile != null) {
            selectedCategoryFileLabel.setText(selectedFile.getAbsolutePath());
            // Load and display the selected image in the category preview
            Image image = new Image(selectedFile.toURI().toString());
            categoryPreviewImageView.setImage(image);
        }
    }


    @FXML
    private void addCategory() {
        String productName = categoryName.getText();
        Image image = categoryPreviewImageView.getImage();

        // Vérification si l'image ou le nom de la catégorie est nul
        if (image == null || productName == null || productName.isEmpty()) {

            categoryError.setText("image and category requires.");
            System.out.println("L'image et le nom de la catégorie sont requis.");
            // Afficher une alerte à l'utilisateur ou une notification indiquant que l'image est requise
            return; // Arrête l'exécution de la méthode si l'image ou le nom de la catégorie est manquant
        }

        try {
            // Convert Image to Base64 String
            String base64Image = convertImageToBase64(image);

            Connection connection = Connexion.etablirConnexion();
            if (connection != null) {
                try {
                    String sql = "INSERT INTO CATEGORY (categoryname, image) VALUES (?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setString(1, productName);
                    preparedStatement.setString(2, base64Image);

                    int rowsInserted = preparedStatement.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Une nouvelle catégorie a été insérée avec succès !");
                    } else {
                        System.out.println("Échec de l'insertion de la catégorie.");
                    }

                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        connection.close();
                        categoryName.setText("");
                        categoryPreviewImageView.setImage(null);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Échec de l'établissement de la connexion à la base de données.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Convert Image to Base64 String
    private String convertImageToBase64(Image image) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] imageData = baos.toByteArray();
        baos.close();

        // Encode byte array to Base64 String
        return Base64.getEncoder().encodeToString(imageData);
    }


    @FXML
    private void saveAction() {
        String productName = productNameField.getText();
        String description = descriptionArea.getText();
        String price = priceField.getText();
        Image imagePath = previewImageView.getImage();
        Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();

        if (selectedCategory == null || imagePath == null || productName.isEmpty() || description.isEmpty() || price.isEmpty()) {
            displayError("Please fill in all fields and select an image.");
            return;
        }

        // Establish connection to the database
        Connection connection = Connexion.etablirConnexion();
        if (connection != null) {
            try {
                String base64Image = convertImageToBase64(imagePath);
                String sql = "INSERT INTO ITEM (CATEGORYID, name, unitprice, description, pdj, availability, image) VALUES (?, ?, ?, ?, ?, ?, ?)";

                // Create a PreparedStatement with the SQL query
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, selectedCategory.getCategoryId());
                preparedStatement.setString(2, productName);
                preparedStatement.setString(3, price);
                preparedStatement.setString(4, description);
                preparedStatement.setInt(5, 0); // Assume 'pdj' and 'availability' are being set statically
                preparedStatement.setInt(6, 1);
                preparedStatement.setString(7, base64Image);

                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new product was inserted successfully!");
                    clearFormFields();
                } else {
                    displayError("Failed to insert the product.");
                }

                preparedStatement.close();
            } catch (SQLException e) {
                displayError("Database error: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                displayError("Image processing error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    displayError("Error closing database connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            displayError("Failed to establish connection to the database.");
        }
    }

    private void displayError(String message) {
        // Log the error or display it using a dialog/alert
        System.err.println(message);
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }


    private void clearFormFields() {
        productNameField.setText("");
        descriptionArea.setText("");
        priceField.setText("");
        selectedFileLabel.setText("No file selected");
        previewImageView.setImage(null);
        categoryComboBox.getSelectionModel().clearSelection();
        categoryName.setText("");
        categoryPreviewImageView.setImage(null);
        platDuJour.setSelected(false);
    }
}
