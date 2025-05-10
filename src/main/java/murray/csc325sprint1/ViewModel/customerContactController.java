package murray.csc325sprint1.ViewModel;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import murray.csc325sprint1.Model.EmployeeSupport;
import murray.csc325sprint1.Model.SupportFirestoreFunctions;

import java.io.IOException;
import java.util.Random;

public class customerContactController {
    @FXML private TextArea EnterMessage;
    @FXML private TextField EnterSubject;
    @FXML private TextField EnterUsernameTF;
    @FXML private Button goBackButton;
    @FXML private Button sendButton;
    @FXML private Button viewRequest;
    @FXML private Label yourUsernameLabel;

    private final SupportFirestoreFunctions firestore = SupportFirestoreFunctions.getInstance();
    private int currentTicketID = -1;

    @FXML
    public void initialize() {
        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (EnterMessage.getScene() != null && EnterMessage.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) EnterMessage.getScene().getWindow();

                // Force layout pass to calculate proper size
                EnterMessage.getScene().getRoot().applyCss();
                EnterMessage.getScene().getRoot().layout();

                double prefWidth = EnterMessage.getScene().getRoot().prefWidth(-1);
                double prefHeight = EnterMessage.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    @FXML
    private void handleGoBackButton() {
        try {
            Stage currentStage = (Stage) goBackButton.getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/murray/csc325sprint1/customer-main.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
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

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go back to main menu");
        }
    }

    @FXML
    void handleSendButton(ActionEvent event) {
        String username = EnterUsernameTF.getText().trim();
        String subject = EnterSubject.getText().trim();
        String message = EnterMessage.getText().trim();

        if (username.isEmpty() || subject.isEmpty() || message.isEmpty()) {
            showAlert("Error", "All fields must be filled!");
            return;
        }

        currentTicketID = generateTicketID();
        EmployeeSupport ticket = new EmployeeSupport(
                currentTicketID,
                username,
                subject,
                message,
                false,
                "",
                System.currentTimeMillis() // Add timestamp here
        );

        firestore.insertTicket(ticket);
        showAlert("Success", "Ticket #" + currentTicketID + " created successfully!");
        clearFields();
    }

    @FXML
    void handleViewRequest(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/murray/csc325sprint1/customer-view-request.fxml"));
            Parent root = loader.load();

            customerViewRequestController controller = loader.getController();
            controller.initData(firestore.getTicket(currentTicketID));

            Stage stage = new Stage();
            Scene scene = new Scene(root);
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
            showAlert("Error", "Failed to open view request window");
        }
    }

    private int generateTicketID() {
        Random rand = new Random();
        return 100000 + rand.nextInt(900000);
    }

    private void clearFields() {
        EnterSubject.clear();
        EnterMessage.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}