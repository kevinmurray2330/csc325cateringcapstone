package murray.csc325sprint1;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;


public class OrderDetailsController implements Initializable {

    @FXML private Label totalLabel;
    @FXML private VBox orderedItemsContainer;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private Button placeOrderButton;
    @FXML private Button closeButton;
    @FXML private Label availabilityLabel; // Label to show availability info

    private Order currentOrder;
    private boolean orderPlaced = false;
    private OrderService orderService;
    private Menu menuService;
    private Map<String, Spinner<Integer>> itemQuantitySpinners = new HashMap<>();

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the order service and menu service from MainApp
        orderService = MainApp.getOrderService();
        menuService = MainApp.getMenu();

        // Apply CSS styling
        applyStyles();

        // Set up date picker with min date as tomorrow
        datePicker.setValue(LocalDate.now().plusDays(1));

        // Set the day cell factory to disable dates before tomorrow
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);

                        // Disable dates before tomorrow
                        LocalDate tomorrow = LocalDate.now().plusDays(1);
                        setDisable(empty || date.compareTo(tomorrow) < 0);
                    }
                };
            }
        });

        // Set up time combo box with available times (9 AM - 7 PM in 30-min intervals)
        setupTimeComboBox();

        // Add listeners to update availability info when date or time changes
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailabilityInfo());
        timeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailabilityInfo());

        // Set styles for other elements
        totalLabel.getStyleClass().add("total-label");
        placeOrderButton.getStyleClass().add("place-order-button");

        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (totalLabel.getScene() != null && totalLabel.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) totalLabel.getScene().getWindow();
                stage.sizeToScene();
            }
        });
    }

    /**
     * Apply CSS styles to the UI
     */
    private void applyStyles() {
        try {
            // Try to load the stylesheet
            String stylesheet = "/murray/csc325sprint1/order-details-styles.css";
            URL styleUrl = getClass().getResource(stylesheet);

            if (styleUrl != null && totalLabel.getScene() != null) {
                // Add the stylesheet to the scene
                totalLabel.getScene().getStylesheets().add(styleUrl.toExternalForm());
            } else {
                System.err.println("Could not find order-details-styles.css stylesheet or scene not ready.");
            }
        } catch (Exception e) {
            System.err.println("Error loading stylesheet: " + e.getMessage());
        }
    }

    /**
     * Set up the time combo box with available times
     */
    private void setupTimeComboBox() {
        ObservableList<String> availableTimes = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(19, 0);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

        LocalTime currentTime = startTime;
        while (!currentTime.isAfter(endTime)) {
            availableTimes.add(currentTime.format(timeFormatter));
            currentTime = currentTime.plusMinutes(30);
        }

        timeComboBox.setItems(availableTimes);
        if (!availableTimes.isEmpty()) {
            timeComboBox.setValue(availableTimes.get(0)); // Default to first available time
        }
    }

    /**
     * Update the availability information displayed to the user
     */
    private void updateAvailabilityInfo() {
        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            return;
        }

        String date = datePicker.getValue().toString();
        String time = timeComboBox.getValue();

        try {
            int currentCount = orderService.countOrdersInTimeSlot(date, time);
            int maxOrders = orderService.getMaxOrdersPerTimeSlot();
            int remainingSlots = maxOrders - currentCount;

            if (availabilityLabel != null) {
                if (remainingSlots > 0) {
                    availabilityLabel.setText("Available slots: " + remainingSlots + " of " + maxOrders);
                    availabilityLabel.setStyle("-fx-text-fill: green;");
                } else {
                    availabilityLabel.setText("This time slot is fully booked. Please select another time.");
                    availabilityLabel.setStyle("-fx-text-fill: red;");
                }
            }

            // Enable or disable the place order button based on availability
            if (placeOrderButton != null) {
                placeOrderButton.setDisable(remainingSlots <= 0 || currentOrder.getOrderItems().isEmpty());
            }

        } catch (Exception e) {
            System.err.println("Error updating availability info: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the order to display in this dialog
     *
     * @param order The order to display
     */
    public void setOrder(Order order) {
        this.currentOrder = order;

        // Make sure styles are applied after scene is fully initialized
        if (totalLabel.getScene() != null) {
            applyStyles();
        }

        // Update the UI with order details
        updateOrderDetails();

        // Update availability info for the default date/time
        updateAvailabilityInfo();

        // Ensure the dialog size adjusts to fit content
        Platform.runLater(() -> {
            if (totalLabel.getScene() != null && totalLabel.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) totalLabel.getScene().getWindow();

                // Force layout pass to calculate proper size
                totalLabel.getScene().getRoot().applyCss();
                totalLabel.getScene().getRoot().layout();

                double prefWidth = totalLabel.getScene().getRoot().prefWidth(-1);
                double prefHeight = totalLabel.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    /**
     * Update the UI with the current order details
     */
    private void updateOrderDetails() {
        if (currentOrder == null) {
            return;
        }

        // Set the total
        updateOrderTotal();

        // Clear existing items and spinners
        orderedItemsContainer.getChildren().clear();
        itemQuantitySpinners.clear();

        // Add each ordered item with quantity controls
        for (Map.Entry<String, Integer> entry : currentOrder.getOrderItems().entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();

            // Get the actual menu item to access price information
            MenuItem menuItem = findMenuItemByName(itemName);
            if (menuItem == null) {
                continue; // Skip if item not found
            }

            // Create an HBox for each item row
            HBox itemRow = new HBox();
            itemRow.getStyleClass().add("item-row");
            itemRow.setSpacing(10);
            itemRow.setPadding(new Insets(5));
            itemRow.setAlignment(Pos.CENTER_LEFT);

            // Create a label for this item
            Label itemLabel = new Label(itemName);
            itemLabel.getStyleClass().add("item-name");
            HBox.setHgrow(itemLabel, Priority.ALWAYS);

            // Add price info
            double itemPrice = menuItem.getPrice();
            Label priceLabel = new Label(String.format("$%.2f", itemPrice));
            priceLabel.getStyleClass().add("item-price");

            // Create quantity spinner
            Spinner<Integer> quantitySpinner = new Spinner<>();
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, quantity);
            quantitySpinner.setValueFactory(valueFactory);
            quantitySpinner.getStyleClass().add("quantity-spinner");
            quantitySpinner.setEditable(true);

            // Store spinner for later access
            itemQuantitySpinners.put(itemName, quantitySpinner);

            // Add listener to update order when quantity changes
            quantitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                updateItemQuantity(menuItem, oldValue, newValue);
            });

            // Create remove button
            Button removeButton = new Button("✕");
            removeButton.getStyleClass().add("remove-button");

            // Add action to remove button
            removeButton.setOnAction(event -> removeItem(menuItem));

            // Add a spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Add the elements to the item row
            itemRow.getChildren().addAll(itemLabel, priceLabel, spacer, quantitySpinner, removeButton);

            // Add to container
            orderedItemsContainer.getChildren().add(itemRow);
        }

        // Add empty space message if no items
        if (orderedItemsContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.getStyleClass().add("empty-cart");
            orderedItemsContainer.getChildren().add(emptyLabel);

            // Disable place order button when cart is empty
            placeOrderButton.setDisable(true);
        }
    }

    /**
     * Update order when item quantity is changed
     */
    private void updateItemQuantity(MenuItem item, int oldValue, int newValue) {
        if (item == null || oldValue == newValue) return;

        if (newValue > oldValue) {
            // Add more of this item
            currentOrder.addItem(item, newValue - oldValue);
        } else {
            // Remove some of this item
            currentOrder.removeItem(item, oldValue - newValue);
        }

        // Update the order total display
        updateOrderTotal();

        // Check if cart became empty and update UI accordingly
        if (currentOrder.getOrderItems().isEmpty()) {
            updateOrderDetails();
        }

        // Update availability info with new order state
        updateAvailabilityInfo();
    }

    /**
     * Remove an item from the order
     */
    private void removeItem(MenuItem item) {
        if (item == null) return;

        // Get current quantity
        int quantity = currentOrder.getOrderItems().getOrDefault(item.getName(), 0);

        // Remove all of this item
        currentOrder.removeItem(item, quantity);

        // Update the UI
        updateOrderDetails();

        // Update availability info with new order state
        updateAvailabilityInfo();
    }

    /**
     * Find a menu item by name
     */
    private MenuItem findMenuItemByName(String name) {
        for (MenuItem item : menuService.getAllMenuItems()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Update the order total display
     */
    private void updateOrderTotal() {
        totalLabel.setText("Total: " + currentOrder.getFormattedTotal());
        totalLabel.getStyleClass().add("total-label");
    }

    /**
     * Handle the place order button click
     */
    @FXML
    private void placeOrder(ActionEvent event) {
        // Validate inputs
        if (datePicker.getValue() == null) {
            showError("Please select a date for pickup.");
            return;
        }

        if (timeComboBox.getValue() == null) {
            showError("Please select a time for pickup.");
            return;
        }

        // Check if cart is empty
        if (currentOrder.getOrderItems().isEmpty()) {
            showError("Your cart is empty. Please add items before placing an order.");
            return;
        }

        // Set the pickup date and time on the order
        currentOrder.setPickupDate(datePicker.getValue().toString());
        currentOrder.setPickupTime(timeComboBox.getValue());

        // Ensure user email is set (it comes from OrderController)
        if (currentOrder.getUserEmail() == null || currentOrder.getUserEmail().isEmpty()) {
            // Use default email if none provided (normally would come from logged-in user)
            currentOrder.setUserEmail("customer@example.com");
        }

        // Check if the time slot is available
        try {
            if (!orderService.isTimeSlotAvailable(currentOrder.getPickupDate(), currentOrder.getPickupTime())) {
                showError("This time slot is fully booked. Please select another time.");
                updateAvailabilityInfo(); // Refresh the availability display
                return;
            }

            // Save the order with user email
            boolean success = orderService.saveOrder(currentOrder);

            if (success) {
                // Mark the order as placed
                orderPlaced = true;

                // Close the dialog
                closeDialog(null);
            } else {
                showError("Failed to place order. The time slot may have been filled by another customer. Please try again.");
                updateAvailabilityInfo(); // Refresh the availability display
            }
        } catch (Exception e) {
            showError("Error placing order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle the close button click
     */
    @FXML
    private void closeDialog(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Show an error message
     *
     * @param message The error message to show
     */
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Check if the order was successfully placed
     *
     * @return true if the order was placed, false otherwise
     */
    public boolean isOrderPlaced() {
        return orderPlaced;
    }
}