package murray.csc325sprint1.ViewModel;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import murray.csc325sprint1.MainApp;
import murray.csc325sprint1.Menu;
import murray.csc325sprint1.MenuItem;

public class CateringMenuController implements Initializable {

    @FXML private VBox menuContainer;
    @FXML private Button backBtn;

    private Menu menuService;

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the menu from MainApp
        menuService = MainApp.getMenu();

        // Load menu items
        loadMenuItems();

        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (menuContainer.getScene() != null && menuContainer.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) menuContainer.getScene().getWindow();

                // Force layout pass to calculate proper size
                menuContainer.getScene().getRoot().applyCss();
                menuContainer.getScene().getRoot().layout();

                double prefWidth = menuContainer.getScene().getRoot().prefWidth(-1);
                double prefHeight = menuContainer.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    /**
     * Load menu items and display them
     */
    private void loadMenuItems() {
        // Clear existing menu items
        menuContainer.getChildren().clear();

        // Get appetizers
        List<MenuItem> appetizers = menuService.getMenuItemsByCategory("appetizer");
        addCategoryToMenu("Appetizers", appetizers);

        // Get entrees
        List<MenuItem> entrees = menuService.getMenuItemsByCategory("entree");
        addCategoryToMenu("Entrees", entrees);

        // Get desserts
        List<MenuItem> desserts = menuService.getMenuItemsByCategory("dessert");
        addCategoryToMenu("Desserts", desserts);
    }

    /**
     * Add a category of menu items to the UI
     *
     * @param categoryName The name of the category
     * @param items The list of items in this category
     */
    private void addCategoryToMenu(String categoryName, List<MenuItem> items) {
        // Create category header
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("category-header");
        categoryLabel.setStyle("-fx-background-color: #F0F0F0; -fx-padding: 10; -fx-font-size: 24; -fx-text-fill: #888888;");
        menuContainer.getChildren().add(categoryLabel);

        // Add each item
        for (MenuItem item : items) {
            addMenuItemToUI(item);
        }

        // Add some spacing after each category
        Region spacing = new Region();
        spacing.setPrefHeight(20);
        menuContainer.getChildren().add(spacing);
    }

    /**
     * Add a single menu item to the UI
     *
     * @param item The menu item to add
     */
    private void addMenuItemToUI(MenuItem item) {
        // Create the container for this menu item
        HBox itemContainer = new HBox();
        itemContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        itemContainer.setSpacing(10);
        itemContainer.setPadding(new Insets(10));

        // Create the left side with item details
        VBox detailsContainer = new VBox();
        detailsContainer.setPrefWidth(500);

        // Item name
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18;");

        // Item price
        Label priceLabel = new Label(item.getFormattedPriceWithUnit());
        priceLabel.setStyle("-fx-font-size: 14;");

        // Item description
        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 14; -fx-wrap-text: true;");

        // Add all to details container
        detailsContainer.getChildren().addAll(nameLabel, priceLabel, descLabel);

        // Create image view with the item's image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(150);
        imageView.setPreserveRatio(true);

        // Default to a colored rectangle background
        imageView.setStyle("-fx-background-color: #CCCCCC;");

        // Get the image path and try to load it
        try {
            // First try to load the food placeholder image as a fallback
            try {
                Image placeholderImage = new Image(getClass().getResourceAsStream("/images/food_placeholder.png"));
                if (placeholderImage != null && !placeholderImage.isError()) {
                    imageView.setImage(placeholderImage);
                }
            } catch (Exception e) {
                System.err.println("Could not load placeholder image: " + e.getMessage());
            }

            // Then try to load the specific item image if available
            String imagePath = item.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Image itemImage = new Image(getClass().getResourceAsStream(imagePath));
                    if (itemImage != null && !itemImage.isError()) {
                        imageView.setImage(itemImage);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load image for " + item.getName() + ": " + e.getMessage());
                    // We'll keep using the placeholder image if it's already set
                }
            }
        } catch (Exception e) {
            System.err.println("Error in image loading process: " + e.getMessage());
        }

        // Add everything to the item container
        itemContainer.getChildren().addAll(detailsContainer, imageView);

        // Add item container to the menu
        menuContainer.getChildren().add(itemContainer);
    }

    /**
     * Go back to the customer main menu
     */
    @FXML
    private void goBack(ActionEvent event) {
        try {
            // Navigate back to customer main menu
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/murray/csc325sprint1/customer-main.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);

            // Ensure proper sizing after loading new scene
            Platform.runLater(() -> {
                root.applyCss();
                root.layout();
                double prefWidth = root.prefWidth(-1);
                double prefHeight = root.prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            });

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not return to menu: " + e.getMessage());
        }
    }

    /**
     * Show an error alert dialog
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}