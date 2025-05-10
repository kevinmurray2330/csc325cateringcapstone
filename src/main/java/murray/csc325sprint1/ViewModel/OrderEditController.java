package murray.csc325sprint1.ViewModel;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import murray.csc325sprint1.FirestoreContext;
import murray.csc325sprint1.Model.OrderListItem;
import murray.csc325sprint1.OrderItemRow;
import murray.csc325sprint1.OrderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class OrderEditController {

    @FXML
    private Label orderIdLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private TableView<OrderItemRow> orderItemsTable;

    @FXML
    private TableColumn<OrderItemRow, String> itemNameColumn;

    @FXML
    private TableColumn<OrderItemRow, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItemRow, String> itemPriceColumn;

    @FXML
    private TableColumn<OrderItemRow, Void> itemActionColumn;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private Label availabilityLabel;

    @FXML
    private Button cancelOrderButton;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button closeButton;

    private OrderService orderService;
    private Firestore db;
    private String orderId;
    private String originalDate;
    private String originalTime;
    private OrderListController parentController;
    private double originalTotal;
    private Map<String, Double> menuItemPrices = new HashMap<>();
    private Map<String, Integer> orderItems = new HashMap<>();
    private String orderStatus;
    private double orderTotal;
    private String userEmail;

    @FXML
    public void initialize() {
        // Add event listeners to the date picker and time combo box
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> handleDateTimeChange());
        timeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handleDateTimeChange());

        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (closeButton.getScene() != null && closeButton.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) closeButton.getScene().getWindow();

                // Force layout pass to calculate proper size
                closeButton.getScene().getRoot().applyCss();
                closeButton.getScene().getRoot().layout();

                double prefWidth = closeButton.getScene().getRoot().prefWidth(-1);
                double prefHeight = closeButton.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    /**
     * Initialize the dialog with order data
     */
    public void initData(OrderListItem orderItem, OrderListController controller) {
        try {
            this.parentController = controller;
            this.orderService = new OrderService();
            this.db = FirestoreContext.getInstance().getFirestore();

            // Fetch the complete order data from Firestore
            DocumentSnapshot orderDoc = db.collection("orders")
                    .document(orderItem.getOrderId())
                    .get()
                    .get();

            if (orderDoc.exists()) {
                // Store order ID and display it
                orderId = orderDoc.getString("orderId");
                orderIdLabel.setText(orderId);

                // Store original date and time
                originalDate = orderDoc.getString("pickupDate");
                originalTime = orderDoc.getString("pickupTime");

                // Get total and display it
                originalTotal = orderDoc.getDouble("orderTotal");
                orderTotal = originalTotal;
                totalLabel.setText(String.format("$%.2f", originalTotal));

                // Store order status
                orderStatus = orderDoc.getString("orderStatus");

                // Store user email
                userEmail = orderDoc.getString("userEmail");

                // Load menu item prices for reference
                loadMenuItemPrices();

                // Set up order items table
                setupOrderItemsTable();

                // Load order items
                Map<String, Object> orderItemsMap = (Map<String, Object>) orderDoc.get("orderItems");
                if (orderItemsMap != null) {
                    loadOrderItems(orderItemsMap);
                }

                // Set up date picker
                setupDatePicker();

                // Set up time combo box
                setupTimeComboBox();

                // Initialize date and time with current values
                initializeDateAndTime();

                // Update availability info
                updateAvailabilityInfo();

                // Ensure dialog has proper size after everything is loaded
                Platform.runLater(() -> {
                    Stage stage = (Stage) orderIdLabel.getScene().getWindow();

                    // Force layout pass to calculate proper size
                    orderIdLabel.getScene().getRoot().applyCss();
                    orderIdLabel.getScene().getRoot().layout();

                    double prefWidth = orderIdLabel.getScene().getRoot().prefWidth(-1);
                    double prefHeight = orderIdLabel.getScene().getRoot().prefHeight(-1);

                    // Add a bit of padding
                    stage.setWidth(prefWidth + 20);
                    stage.setHeight(prefHeight + 20);
                    stage.centerOnScreen();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load order details: " + e.getMessage());
        }
    }

    /**
     * Load menu item prices from Firestore for reference
     */
    private void loadMenuItemPrices() throws ExecutionException, InterruptedException {
        QuerySnapshot menuSnapshot = db.collection("menu_items").get().get();
        for (QueryDocumentSnapshot doc : menuSnapshot.getDocuments()) {
            String name = doc.getString("name");
            Double price = doc.getDouble("price");
            if (name != null && price != null) {
                menuItemPrices.put(name, price);
            }
        }
    }

    /**
     * Set up the order items table
     */
    private void setupOrderItemsTable() {
        // Set up columns
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Set up the action column with a remove button
        setupActionColumn();
    }

    /**
     * Set up the action column in the order items table
     */
    private void setupActionColumn() {
        Callback<TableColumn<OrderItemRow, Void>, TableCell<OrderItemRow, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<OrderItemRow, Void> call(TableColumn<OrderItemRow, Void> param) {
                        return new TableCell<>() {
                            private final Button removeButton = new Button("Remove");

                            {
                                removeButton.setStyle("-fx-background-color: #ff6347; -fx-text-fill: white;");
                                removeButton.setOnAction(event -> {
                                    OrderItemRow item = getTableView().getItems().get(getIndex());
                                    removeOrderItem(item);
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(removeButton);
                                }
                            }
                        };
                    }
                };

        itemActionColumn.setCellFactory(cellFactory);
    }

    /**
     * Load order items from Firestore data
     */
    private void loadOrderItems(Map<String, Object> orderItemsMap) {
        ObservableList<OrderItemRow> items = FXCollections.observableArrayList();

        for (Map.Entry<String, Object> entry : orderItemsMap.entrySet()) {
            String itemName = entry.getKey();
            Long quantity = (Long) entry.getValue();
            Double price = menuItemPrices.getOrDefault(itemName, 0.0);

            items.add(new OrderItemRow(itemName, quantity.intValue(), String.format("$%.2f", price * quantity)));

            // Store for tracking
            orderItems.put(itemName, quantity.intValue());
        }

        orderItemsTable.setItems(items);
    }

    /**
     * Set up the date picker with constraints
     */
    private void setupDatePicker() {
        // Set the day cell factory to disable dates before tomorrow
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable dates before tomorrow
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                setDisable(empty || date.compareTo(tomorrow) < 0);
            }
        });
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
    }

    /**
     * Initialize the date picker and time combo box with current values
     */
    private void initializeDateAndTime() {
        try {
            // Parse and set the date
            LocalDate date = LocalDate.parse(originalDate);
            datePicker.setValue(date);

            // Set the time
            timeComboBox.setValue(originalTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the availability information label
     */
    private void updateAvailabilityInfo() {
        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            return;
        }

        String date = datePicker.getValue().toString();
        String time = timeComboBox.getValue();

        // If date and time are the same as original, no need to check availability
        if (date.equals(originalDate) && time.equals(originalTime)) {
            availabilityLabel.setText("Original time slot selected");
            availabilityLabel.setStyle("-fx-text-fill: blue;");
            saveChangesButton.setDisable(false);
            return;
        }

        try {
            // Get current count for this time slot, excluding this order
            int currentCount = orderService.countOrdersInTimeSlot(date, time);
            int maxOrders = orderService.getMaxOrdersPerTimeSlot();
            int remainingSlots = maxOrders - currentCount;

            if (remainingSlots > 0) {
                availabilityLabel.setText("Available slots: " + remainingSlots + " of " + maxOrders);
                availabilityLabel.setStyle("-fx-text-fill: green;");
                saveChangesButton.setDisable(false);
            } else {
                availabilityLabel.setText("This time slot is fully booked. Please select another time.");
                availabilityLabel.setStyle("-fx-text-fill: red;");
                saveChangesButton.setDisable(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            availabilityLabel.setText("Error checking availability");
            availabilityLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Remove an item from the order
     */
    private void removeOrderItem(OrderItemRow item) {
        try {
            // Find the price of this item
            Double price = menuItemPrices.getOrDefault(item.getItemName(), 0.0);

            // Remove from tracking
            orderItems.remove(item.getItemName());

            // Update the order total
            orderTotal -= price * item.getQuantity();
            totalLabel.setText(String.format("$%.2f", orderTotal));

            // Remove from table
            orderItemsTable.getItems().remove(item);

            if (orderItemsTable.getItems().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning",
                        "You've removed all items from this order. You may want to cancel it instead.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove item: " + e.getMessage());
        }
    }

    /**
     * Handle date or time change
     */
    @FXML
    private void handleDateTimeChange() {
        updateAvailabilityInfo();
    }

    /**
     * Save changes to the order
     */
    @FXML
    private void saveChanges() {
        if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a date and time for pickup.");
            return;
        }

        // Check if the current time is within 24 hours of the original pickup time
        boolean canModify = canModifyOrder();
        if (!canModify) {
            showAlert(Alert.AlertType.ERROR, "Cannot Modify Order",
                    "Orders can only be modified if it's more than 24 hours before the scheduled pickup time.");
            return;
        }

        // Update pickup date and time
        String newDate = datePicker.getValue().toString();
        String newTime = timeComboBox.getValue();

        try {
            // Prepare updates
            Map<String, Object> updates = new HashMap<>();
            updates.put("pickupDate", newDate);
            updates.put("pickupTime", newTime);

            // If items were modified, update them and the total
            if (Math.abs(orderTotal - originalTotal) > 0.01) {
                updates.put("orderItems", orderItems);
                updates.put("orderTotal", orderTotal);
            }

            // Save to Firestore
            db.collection("orders")
                    .document(orderId)
                    .update(updates)
                    .get();

            showAlert(Alert.AlertType.INFORMATION, "Success", "Order updated successfully.");

            // Refresh the parent controller's order list
            if (parentController != null) {
                parentController.refreshOrders();
            }

            // Close the dialog
            closeDialog(null);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update order: " + e.getMessage());
        }
    }

    /**
     * Check if the order can be modified (more than 24 hours before pickup)
     */
    private boolean canModifyOrder() {
        // If order is already cancelled or completed, it cannot be modified
        if (orderStatus.equals("Cancelled") || orderStatus.equals("Completed")) {
            return false;
        }

        try {
            // Parse date and time
            LocalDate date = LocalDate.parse(originalDate);

            // Parse time using pattern like "10:30 AM"
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            LocalTime time = LocalTime.parse(originalTime, timeFormatter);

            // Combine into LocalDateTime
            LocalDateTime pickupDateTime = LocalDateTime.of(date, time);

            // Check if current time is more than 24 hours before pickup
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime cutoffTime = pickupDateTime.minusHours(24);

            return currentTime.isBefore(cutoffTime);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cancel the entire order
     */
    @FXML
    private void cancelOrder() {
        // Check if the order can be cancelled (more than 24 hours before pickup)
        boolean canModify = canModifyOrder();
        if (!canModify) {
            showAlert(Alert.AlertType.ERROR, "Cannot Cancel Order",
                    "Orders can only be cancelled if it's more than 24 hours before the scheduled pickup time.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Order");
        confirmation.setHeaderText("Cancel Order #" + orderId);
        confirmation.setContentText("Are you sure you want to cancel this order? This action cannot be undone.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Update order status to Cancelled
                db.collection("orders")
                        .document(orderId)
                        .update("orderStatus", "Cancelled")
                        .get();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Order cancelled successfully.");

                // Refresh the parent controller's order list
                if (parentController != null) {
                    parentController.refreshOrders();
                }

                // Close the dialog
                closeDialog(null);

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order: " + e.getMessage());
            }
        }
    }

    /**
     * Close the dialog
     */
    @FXML
    private void closeDialog(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}