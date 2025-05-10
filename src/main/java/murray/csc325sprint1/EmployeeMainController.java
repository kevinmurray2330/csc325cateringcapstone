package murray.csc325sprint1;

import javafx.application.Platform;
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


public class EmployeeMainController implements Initializable {

    @FXML
    private Button EmpContact;

    @FXML
    private Button EmpInventory;

    @FXML
    private Button EmpLogOut;

    @FXML
    private Button EmpOrders;

    @FXML
    private Button EmpSchedule;

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
            Stage stage = (Stage) EmpContact.getScene().getWindow();
            double width = stage.getScene().getRoot().prefWidth(-1);
            double height = stage.getScene().getRoot().prefHeight(-1);

            stage.setWidth(width);
            stage.setHeight(height);
            stage.centerOnScreen();
        });

        // Initialize contact button functionality
        EmpContact.setOnAction(event -> ContactBtnClicked());

        // Initialize logout button functionality - simply close the application
        EmpLogOut.setOnAction(event -> {
            try {
                // Clear current user session
                Util.setCurrentUser(null);

                // Get the current stage
                Stage currentStage = (Stage) EmpLogOut.getScene().getWindow();

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

        // Set up order management button
        EmpOrders.setOnAction(event -> {
            try {
                // Load the order list view (can be the same as customer view but with more controls)
                FXMLLoader loader = new FXMLLoader(getClass().getResource(ViewPaths.ORDER_LIST_VIEW_SCREEN));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) EmpOrders.getScene().getWindow();
                stage.setScene(scene);
                stage.show();

                // Ensure proper sizing after loading
                adjustStageSize(stage);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error loading orders view: " + e.getMessage());
            }
        });

        // Add additional button handlers as needed
        EmpInventory.setOnAction(event -> {
            showNotImplementedMessage();
        });

        EmpSchedule.setOnAction(event -> {
            showNotImplementedMessage();
        });
    }

    private void ContactBtnClicked() {
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(ViewPaths.EMPLOYEE_CONTACT_SCREEN));
            Parent root = fxmlLoader.load();

            // Use existing stage instead of creating a new one
            Stage stage = (Stage) EmpContact.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

            // Ensure proper sizing after loading
            adjustStageSize(stage);
        }catch(IOException e){
            e.printStackTrace();
            showError("Error loading contact screen: " + e.getMessage());
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

    /**
     * Show a message for features not yet implemented
     */
    private void showNotImplementedMessage() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(null);
        alert.setContentText("This feature is not yet implemented in the current version.");
        alert.showAndWait();
    }
}