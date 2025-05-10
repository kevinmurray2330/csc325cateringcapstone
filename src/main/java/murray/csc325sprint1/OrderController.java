package murray.csc325sprint1;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class OrderController implements Initializable {

    @FXML private VBox menuContainer;
    @FXML private Button cartBtn;
    @FXML private Button homeBtn;

    private Order currentOrder;
    private Menu menuService;
    private String userEmail;

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the menu from MainApp
        menuService = MainApp.getMenu();

        // Initialize the current order
        currentOrder = new Order();

        // Load menu items
        loadMenuItems();

        // Set user email on the current order
        currentOrder.setUserEmail(userEmail);

        // Set up home button event handler (if it exists)
        if (homeBtn != null) {
            homeBtn.setOnAction(event -> handleHomeButtonClick());
        }

        // Ensure proper window resizing after scene is fully loaded
        Platform.runLater(() -> {
            Stage stage = (Stage) menuContainer.getScene().getWindow();
            double width = stage.getScene().getRoot().prefWidth(-1);
            double height = stage.getScene().getRoot().prefHeight(-1);

            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
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

        // Create add button
        Button addButton = new Button("+");
        addButton.setStyle("-fx-background-radius: 15; -fx-background-color: white; -fx-border-color: #CCCCCC; -fx-border-radius: 15;");
        addButton.setPrefSize(30, 30);
        addButton.setMinSize(30, 30);
        addButton.setMaxSize(30, 30);

        // Set up add button action
        addButton.setOnAction(event -> {
            currentOrder.addItem(item, 1);
            showAddedToCartAlert(item);
        });

        // Add everything to the item container
        itemContainer.getChildren().addAll(detailsContainer, imageView, addButton);

        // Add item container to the menu
        menuContainer.getChildren().add(itemContainer);
    }

    /**
     * Show a small alert when an item is added to the cart
     *
     * @param item The item that was added
     */
    private void showAddedToCartAlert(MenuItem item) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Item Added");
        alert.setHeaderText(null);
        alert.setContentText(item.getName() + " added to cart.");
        alert.showAndWait();
    }

    /**
     * Show the order details dialog when the cart button is clicked
     */
    @FXML
    private void showOrderDetails(ActionEvent event) {
        try {
            // Check if there are items in the order
            if (currentOrder.getOrderItems().isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Empty Cart");
                alert.setHeaderText(null);
                alert.setContentText("Your cart is empty. Please add some items first.");
                alert.showAndWait();
                return;
            }

            // Try to load the dialog from different possible paths
            URL fxmlLocation = getClass().getResource("/fxml/OrderDetailsDialog.fxml");
            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("/murray/csc325sprint1/OrderDetailsDialog.fxml");
            }

            if (fxmlLocation == null) {
                throw new IOException("Could not find OrderDetailsDialog.fxml");
            }

            // Load the order details dialog
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Get the controller and pass the current order
            OrderDetailsController controller = loader.getController();
            controller.setOrder(currentOrder);

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED);

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            // Adjust the dialog stage size to properly fit content
            root.applyCss();
            root.layout();
            double dialogWidth = root.prefWidth(-1) + 20;
            double dialogHeight = root.prefHeight(-1) + 20;
            dialogStage.setWidth(dialogWidth);
            dialogStage.setHeight(dialogHeight);
            dialogStage.centerOnScreen();

            dialogStage.showAndWait();

            // Check if order was placed successfully
            if (controller.isOrderPlaced()) {
                // Reset the current order
                currentOrder = new Order();

                // Show success message
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Order Placed");
                alert.setHeaderText(null);
                alert.setContentText("Your order has been placed successfully!");
                alert.showAndWait();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while opening the order details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Set the user email (to be called from the login screen)
     *
     * @param email The user's email
     */
    public void setUserEmail(String email) {
        this.userEmail = email;

        // When we create the current order, also set the user email
        if (currentOrder != null) {
            currentOrder.setUserEmail(userEmail);
        }
    }

    /**
     * Add a button and handler to navigate back to the customer menu
     */
    @FXML
    private void handleHomeButtonClick() {
        try {
            // Navigate back to customer menu
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/murray/csc325sprint1/customer-main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) cartBtn.getScene().getWindow();
            stage.setScene(scene);

            // Ensure proper sizing after loading
            Platform.runLater(() -> {
                root.applyCss();
                root.layout();
                double width = root.prefWidth(-1) + 20;
                double height = root.prefHeight(-1) + 20;

                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to navigate to home: " + e.getMessage());
            alert.showAndWait();
        }
    }
}