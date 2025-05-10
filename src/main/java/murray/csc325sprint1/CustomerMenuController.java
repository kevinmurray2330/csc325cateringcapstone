package murray.csc325sprint1;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import murray.csc325sprint1.Model.User;
import murray.csc325sprint1.Model.Util;
import murray.csc325sprint1.Model.ViewPaths;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CustomerMenuController implements Initializable {

    @FXML
    private Button cusContact;

    @FXML
    private Button cusLogOut;

    @FXML
    private Button cusMenu;

    @FXML
    private Button cusOrder;

    @FXML
    private Button cusOrderHistory;

    @FXML
    private Button cusQuote;

    @FXML
    private Label welcomeLabel; // Add this if you have a welcome label

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get the current user from Util
        currentUser = Util.getCurrentUser();

        // Update welcome message if we have a welcome label and a user
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getfName() + "!");
        }

        // Ensure proper window resizing after scene is fully loaded
        Platform.runLater(() -> {
            Stage stage = (Stage) cusMenu.getScene().getWindow();
            double width = stage.getScene().getRoot().prefWidth(-1);
            double height = stage.getScene().getRoot().prefHeight(-1);

            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        });

        // Initialize the Quote button click event
        cusQuote.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.QUOTE_VIEW_SCREEN));
                Parent root = loader.load();
                Stage stage = (Stage) cusQuote.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                // Ensure proper sizing after loading
                adjustStageSize(stage);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading quote view: " + e.getMessage());
            }
        });

        // Initialize the Order button click event
        cusOrder.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.ORDER_VIEW_SCREEN));
                Parent root = loader.load();

                // Pass user email to the order controller if needed
                if (loader.getController() instanceof OrderController && currentUser != null) {
                    OrderController controller = loader.getController();
                    controller.setUserEmail(currentUser.getEmail());
                }

                Stage stage = (Stage) cusOrder.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

                // Ensure proper sizing after loading
                adjustStageSize(stage);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading order view: " + e.getMessage());
            }
        });

        // Initialize log out functionality - simply close the application
        cusLogOut.setOnAction(event -> {
            try {
                // Clear current user session
                Util.setCurrentUser(null);

                // Get the current stage
                Stage currentStage = (Stage) cusLogOut.getScene().getWindow();

                // Show confirmation dialog
                Alert confirmExit = new Alert(Alert.AlertType.CONFIRMATION);
                confirmExit.setTitle("Log Out");
                confirmExit.setHeaderText("Log Out");
                confirmExit.setContentText("Are you sure you want to log out and exit the application?");

                if (confirmExit.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    // Close the application
                    currentStage.close();
                    // Optionally force exit the application
                    // Platform.exit();
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error logging out: " + e.getMessage());
            }
        });

        // Initialize contact button with UpdatedMain-employee-customer-Support functionality
        cusContact.setOnAction(event -> {
            try {
                // Load the customer contact FXML file from UpdatedMain branch
                FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.CUSTOMER_CONTACT_SCREEN));
                Parent root = loader.load();

                // Get the current stage
                Stage stage = (Stage) cusContact.getScene().getWindow();

                // Set the new scene
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Customer Support");
                stage.show();

                // Ensure proper sizing after loading
                adjustStageSize(stage);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading customer contact screen: " + e.getMessage());
            }
        });

        // Set up remaining buttons
        cusMenu.setOnAction(this::showCateringMenu);
        cusOrderHistory.setOnAction(this::showOrderHistory);
    }

    /**
     * Show the catering menu (read-only version)
     */
    private void showCateringMenu(ActionEvent event) {
        try {
            // Get the stage from the event source
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Load the catering menu view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.CATERING_MENU_VIEW_SCREEN));
            Parent root = loader.load();

            // Create the scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

            // Ensure proper sizing after loading
            adjustStageSize(stage);
        } catch (IOException e) {
            System.err.println("Failed to load catering menu view");
            e.printStackTrace();
            showError("Failed to load catering menu view: " + e.getMessage());
        }
    }

    /**
     * Show order history
     */
    private void showOrderHistory(ActionEvent event) {
        try {
            // Load the order list view
            FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.ORDER_LIST_VIEW_SCREEN));
            Parent root = loader.load();

            // Create and show the scene
            Scene scene = new Scene(root);
            Stage stage = (Stage) cusOrderHistory.getScene().getWindow();
            stage.setScene(scene);
            stage.show();

            // Ensure proper sizing after loading
            adjustStageSize(stage);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load order history: " + e.getMessage());
        }
    }

    /**
     * Adjust stage size to fit content
     */
    private void adjustStageSize(Stage stage) {
        Platform.runLater(() -> {
            Parent root = stage.getScene().getRoot();
            double width = root.prefWidth(-1);
            double height = root.prefHeight(-1);

            // Add a bit of padding to prevent scrollbars
            stage.setWidth(width + 20);
            stage.setHeight(height + 20);
            stage.centerOnScreen();
        });
    }

    /**
     * Show an error alert dialog
     */
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}