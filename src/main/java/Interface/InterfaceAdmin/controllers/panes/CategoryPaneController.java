package Interface.InterfaceAdmin.controllers.panes;

import Class.Category;
import Class.Connexion;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.utils.SwingFXUtils;
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
import java.sql.*;
import java.util.Base64;
import java.util.ResourceBundle;

public class CategoryPaneController implements Initializable {

    @FXML
    private VBox tableContent;

    private final ObservableList<Category> categories = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reloadData();
    }

    private void reloadData() {
        categories.clear();
        tableContent.getChildren().clear();
        loadCategoriesFromDatabase();
        setupTable();
    }

        private void loadCategoriesFromDatabase() {
            String query = "SELECT CATEGORYID, categoryname, image FROM CATEGORY";
            try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int categoryId = rs.getInt("CATEGORYID");
                    String categoryName = rs.getString("categoryname");
                    String base64Image = rs.getString("image");
                    categories.add(new Category(categoryId, categoryName, base64Image));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    private void setupTable() {
        HBox headers = createHeaders();
        tableContent.getChildren().add(headers);
        for (Category category : categories) {
            HBox row = createCategoryRow(category);
            tableContent.getChildren().add(row);
        }
    }

    private HBox createHeaders() {
        HBox headers = new HBox();
//        headers.getChildren().addAll(createCellLabel("Category ID"), createCellLabel("Category Name"), createCellLabel("Image"), createCellLabel("Actions"));
        return headers;
    }

    private HBox createCategoryRow(Category category) {
        HBox row = new HBox();
        row.getChildren().addAll(createCellLabel(String.valueOf(category.getCategoryId())), createCellLabel(category.getCategoryName()), createImageView(category.getImage()), createActions(category));
        return row;
    }

    private HBox createActions(Category category) {
        HBox actionBox = new HBox(10);
        MFXButton editBtn = new MFXButton("Edit");
        MFXButton deleteBtn = new MFXButton("Delete");

        editBtn.setOnAction(e -> openEditCategoryDialog(category));
        deleteBtn.setOnAction(e -> {
            deleteCategory(category);
            reloadData(); // Reload the entire pane to update the UI
        });

        actionBox.getChildren().addAll(editBtn, deleteBtn);
        return actionBox;
    }

    private void deleteCategory(Category category) {
        String sql = "DELETE FROM CATEGORY WHERE CATEGORYID = ?";
        try (Connection conn = Connexion.etablirConnexion(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, category.getCategoryId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Category deleted successfully");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openEditCategoryDialog(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(category == null ? "Create New Category" : "Edit Category");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField categoryName = new TextField();
        categoryName.setPromptText("Category Name");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        Button chooseImageButton = new Button("Choose Image");

        if (category != null) {
            categoryName.setText(category.getCategoryName());
            if (category.getImage() != null && !category.getImage().isEmpty()) {
                imageView.setImage(new Image(new ByteArrayInputStream(Base64.getDecoder().decode(category.getImage()))));
            }
        }

        grid.add(new Label("Category Name:"), 0, 0);
        grid.add(categoryName, 1, 0);
        grid.add(new Label("Category Image:"), 0, 1);
        grid.add(imageView, 1, 1);
        grid.add(chooseImageButton, 2, 1);

        dialog.getDialogPane().setContent(grid);

        chooseImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image File");
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                Image image = new Image(file.toURI().toString());
                imageView.setImage(image);
                category.setImage(encodeImageToBase64(image));  // Set the new image in base64 format
            }
        });

        final Category[] container = new Category[1];
        container[0] = category;

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try (Connection conn = Connexion.etablirConnexion()) {
                    String sql;
                    if (container[0] == null) {
                        sql = "INSERT INTO CATEGORY (categoryname, image) VALUES (?, ?)";
                    } else {
                        sql = "UPDATE CATEGORY SET categoryname = ?, image = ? WHERE CATEGORYID = ?";
                    }

                    PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, categoryName.getText());
                    ps.setString(2, encodeImageToBase64(imageView.getImage()));

                    if (container[0] != null) {
                        ps.setInt(3, container[0].getCategoryId());
                    }

                    int affectedRows = ps.executeUpdate();
                    if (affectedRows == 0) {
                        throw new SQLException("Creating/updating category failed, no rows affected.");
                    }

                    if (container[0] == null) {
                        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                container[0] = new Category(generatedKeys.getInt(1), categoryName.getText(), encodeImageToBase64(imageView.getImage()));
                            } else {
                                throw new SQLException("Creating category failed, no ID obtained.");
                            }
                        }
                    }
                    ps.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                return container[0];
            }
            return null;
        });


        dialog.showAndWait().ifPresent(newCategory -> {
            System.out.println("Category Saved: " + newCategory.getCategoryName());
            reloadData(); // Reload data to update the view

            // Reload or refresh your data here
        });
    }


    private String encodeImageToBase64(Image image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bImage, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private void clearFormFields() {
        // Clear fields logic here
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


    private Label createCellLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-padding: 5px;");
        label.setPrefWidth(170); // Set the preferred width for the cell
        label.setMinWidth(170); // Set the minimum width for the cell
        label.setMaxWidth(170); // Set the maximum width for the cell
        return label;
    }
}
