package murray.csc325sprint1.ViewModel;

import com.google.cloud.firestore.Firestore;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import murray.csc325sprint1.FirestoreContext;
import murray.csc325sprint1.MainApp;
import murray.csc325sprint1.Model.OrderListItem;
import murray.csc325sprint1.Model.User;
import murray.csc325sprint1.Model.Util;
import murray.csc325sprint1.Model.ViewPaths;
import murray.csc325sprint1.Order;
import murray.csc325sprint1.OrderService;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.scene.Parent;

public class OrderListController implements Initializable {

    @FXML
    private TableView<OrderListItem> ordersTableView;

    @FXML
    private TableColumn<OrderListItem, String> orderIdColumn;

    @FXML
    private TableColumn<OrderListItem, String> pickupDateColumn;

    @FXML
    private TableColumn<OrderListItem, String> pickupTimeColumn;

    @FXML
    private TableColumn<OrderListItem, String> totalColumn;

    @FXML
    private TableColumn<OrderListItem, String> statusColumn;

    @FXML
    private TableColumn<OrderListItem, String> itemsColumn; // New column for order items

    @FXML
    private TableColumn<OrderListItem, String> actionsColumn;

    @FXML
    private Label orderStatusLabel;

    @FXML
    private Label noOrdersLabel;

    @FXML
    private Button backBtn;

    @FXML
    private Button refreshBtn;

    private OrderService orderService;
    private Firestore db;
    private ObservableList<OrderListItem> ordersList;
    private User currentUser;
    private boolean isEmployeeView = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get instances of services
        orderService = MainApp.getOrderService();
        db = FirestoreContext.getInstance().getFirestore();

        // Get current user
        currentUser = Util.getCurrentUser();

        // Determine if this is employee view
        if (currentUser != null) {
            isEmployeeView = currentUser.isEmployee();
        }

        // Initialize the table columns
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        pickupDateColumn.setCellValueFactory(new PropertyValueFactory<>("pickupDate"));
        pickupTimeColumn.setCellValueFactory(new PropertyValueFactory<>("pickupTime"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up the items column with a custom cell factory
        setupItemsColumn();

        // Setup the actions column
        setupActionsColumn();

        // Load orders on initialization
        loadOrders();

        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (ordersTableView.getScene() != null && ordersTableView.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) ordersTableView.getScene().getWindow();

                // Force layout pass to calculate proper size
                ordersTableView.getScene().getRoot().applyCss();
                ordersTableView.getScene().getRoot().layout();

                double prefWidth = ordersTableView.getScene().getRoot().prefWidth(-1);
                double prefHeight = ordersTableView.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    /**
     * Set up the items column to display order items with quantities
     */
    private void setupItemsColumn() {
        itemsColumn.setCellValueFactory(cellData -> {
            OrderListItem order = cellData.getValue();
            Map<String, Integer> items = order.getOrderItems();

            if (items == null || items.isEmpty()) {
                return new SimpleStringProperty("No items");
            }

            // Create a nicely formatted string of items with quantities
            String itemsList = items.entrySet().stream()
                    .map(entry -> entry.getValue() + " × " + entry.getKey())
                    .collect(Collectors.joining(", "));

            return new SimpleStringProperty(itemsList);
        });

        // Add a button to show details in a popup for better viewing of long item lists
        itemsColumn.setCellFactory(column -> {
            return new TableCell<OrderListItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    if (item.length() > 50) {
                        // If text is long, display a truncated version with a "View" button
                        HBox container = new HBox(5);

                        Label itemsLabel = new Label(item.substring(0, 47) + "...");
                        Button viewBtn = new Button("View");
                        viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

                        viewBtn.setOnAction(event -> {
                            OrderListItem orderItem = getTableView().getItems().get(getIndex());
                            showItemsDetailsDialog(orderItem);
                        });

                        container.getChildren().addAll(itemsLabel, viewBtn);
                        setGraphic(container);
                        setText(null);
                    } else {
                        // If text is short enough, just display it
                        setText(item);
                        setGraphic(null);
                    }
                }
            };
        });
    }

    /**
     * Show a dialog with detailed order items
     */
    private void showItemsDetailsDialog(OrderListItem order) {
        try {
            // Create dialog
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Items");
            dialog.setHeaderText("Items for Order #" + order.getOrderId());

            // Create content
            VBox content = new VBox(10);
            content.setStyle("-fx-padding: 20;");

            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                // Create a list view to display items
                ListView<String> itemsListView = new ListView<>();
                ObservableList<String> items = FXCollections.observableArrayList();

                for (Map.Entry<String, Integer> entry : order.getOrderItems().entrySet()) {
                    items.add(entry.getValue() + " × " + entry.getKey());
                }

                itemsListView.setItems(items);
                itemsListView.setPrefHeight(300);
                itemsListView.setPrefWidth(400);

                content.getChildren().add(itemsListView);
            } else {
                content.getChildren().add(new Label("No items in this order"));
            }

            // Add close button
            ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            // Set the content
            dialog.getDialogPane().setContent(content);

            // Show the dialog
            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not display order items: " + e.getMessage());
        }
    }

    private void setupActionsColumn() {
        Callback<TableColumn<OrderListItem, String>, TableCell<OrderListItem, String>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<OrderListItem, String> call(final TableColumn<OrderListItem, String> param) {
                        return new TableCell<>() {
                            private final Button cancelButton = new Button("Cancel");
                            private final HBox buttonsBox = new HBox(5, cancelButton);

                            {
                                cancelButton.setStyle("-fx-background-color: #ff6347; -fx-text-fill: white;");

                                cancelButton.setOnAction(event -> {
                                    OrderListItem order = getTableView().getItems().get(getIndex());
                                    confirmCancelOrder(order);
                                });
                            }

                            @Override
                            protected void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);

                                if (empty) {
                                    setGraphic(null);
                                    return;
                                }

                                OrderListItem order = getTableView().getItems().get(getIndex());
                                boolean canModify = canModifyOrder(order);

                                // Disable buttons if order cannot be modified
                                cancelButton.setDisable(!canModify);

                                setGraphic(buttonsBox);
                            }
                        };
                    }
                };

        actionsColumn.setCellFactory(cellFactory);
    }

    /**
     * Load orders from Firestore
     */
    private void loadOrders() {
        try {
            List<OrderListItem> orders = new ArrayList<>();

            if (isEmployeeView) {
                // Employee view - get all orders
                List<Order> allOrders = orderService.getAllOrders();

                for (Order order : allOrders) {
                    OrderListItem orderItem = createOrderListItemFromOrder(order);
                    orders.add(orderItem);
                }
            } else {
                // Customer view - get only this user's orders
                String userEmail = currentUser != null ? currentUser.getEmail() : "";
                List<Order> userOrders = orderService.getUserOrders(userEmail);

                for (Order order : userOrders) {
                    OrderListItem orderItem = createOrderListItemFromOrder(order);
                    orders.add(orderItem);
                }
            }

            // Update the table with orders
            ordersList = FXCollections.observableArrayList(orders);
            ordersTableView.setItems(ordersList);

            // Update visibility of "No orders" message
            noOrdersLabel.setVisible(orders.isEmpty());
            ordersTableView.setVisible(!orders.isEmpty());

            // Update status label
            orderStatusLabel.setText("Found " + orders.size() + " orders.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load orders: " + e.getMessage());
        }
    }

    /**
     * Create an OrderListItem from an Order with item details
     */
    private OrderListItem createOrderListItemFromOrder(Order order) {
        OrderListItem orderItem = new OrderListItem(
                order.getOrderId(),
                order.getPickupDate(),
                order.getPickupTime(),
                String.format("$%.2f", order.getOrderTotal()),
                order.getOrderStatus()
        );

        // Set the order items map (new field in OrderListItem class)
        orderItem.setOrderItems(order.getOrderItems());

        return orderItem;
    }

    /**
     * Confirm and cancel an order
     */
    private void confirmCancelOrder(OrderListItem order) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancel Order");
        confirmation.setHeaderText("Cancel Order #" + order.getOrderId());
        confirmation.setContentText("Are you sure you want to cancel this order? This action cannot be undone.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cancelOrderInFirestore(order.getOrderId());
        }
    }

    /**
     * Update the order status to Cancelled in Firestore
     */
    private void cancelOrderInFirestore(String orderId) {
        try {
            if (orderService.cancelOrder(orderId)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Order cancelled successfully.");
                refreshOrders();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel order: " + e.getMessage());
        }
    }

    /**
     * Refresh the orders list
     */
    @FXML
    public void refreshOrders() {
        loadOrders();
    }

    /**
     * Go back to the home/main menu
     */
    @FXML
    private void goToHome(ActionEvent event) {
        try {
            // Navigate to appropriate menu based on user type
            String fxmlPath = isEmployeeView ? ViewPaths.EMPLOYEE_MAIN_SCREEN : ViewPaths.CUSTOMER_MAIN_SCREEN;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Get the stage from the back button
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(scene);

            // Ensure proper sizing after loading
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

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate back to main menu: " + e.getMessage());
        }
    }

    /**
     * Create a new order
     */
    @FXML
    private void createNewOrder(ActionEvent event) {
        try {
            // Navigate to the order view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.ORDER_VIEW_SCREEN));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ordersTableView.getScene().getWindow();
            stage.setScene(scene);

            // Ensure proper sizing after loading
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

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to navigate to new order screen: " + e.getMessage());
        }
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

    /**
     * Check if an order can be modified (more than 24 hours before pickup)
     */
    private boolean canModifyOrder(OrderListItem orderItem) {
        try {
            // If order is already cancelled or completed, it cannot be modified
            if (orderItem.getStatus().equals("Cancelled") || orderItem.getStatus().equals("Completed")) {
                return false;
            }

            // Parse date and time
            LocalDate date = LocalDate.parse(orderItem.getPickupDate());

            // Parse time using pattern like "10:30 AM"
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            LocalTime time = LocalTime.parse(orderItem.getPickupTime(), timeFormatter);

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
     * Find an order in the current list by ID
     */
    private OrderListItem findOrderById(String orderId) {
        if (ordersList == null) return null;

        for (OrderListItem item : ordersList) {
            if (item.getOrderId().equals(orderId)) {
                return item;
            }
        }

        return null;
    }
}