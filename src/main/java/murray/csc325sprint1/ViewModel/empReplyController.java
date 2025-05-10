package murray.csc325sprint1.ViewModel;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import murray.csc325sprint1.Model.EmployeeSupport;
import murray.csc325sprint1.Model.SupportFirestoreFunctions;

public class empReplyController {
    @FXML private TextArea TicketIDTA;
    @FXML private TextArea usernameTA;
    @FXML private TextArea subjectTA;
    @FXML private TextArea customerConcernTA;
    @FXML private TextArea statusTA;
    @FXML private TextField empResponseTF;
    @FXML private Button sendButton;
    @FXML private Button backButton;

    private EmployeeSupport currentTicket;
    private Stage parentStage;
    private final SupportFirestoreFunctions firestore = SupportFirestoreFunctions.getInstance();

    @FXML
    public void initialize() {
        // Ensure proper window sizing after UI is fully loaded
        Platform.runLater(() -> {
            if (backButton.getScene() != null && backButton.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) backButton.getScene().getWindow();

                // Force layout pass to calculate proper size
                backButton.getScene().getRoot().applyCss();
                backButton.getScene().getRoot().layout();

                double prefWidth = backButton.getScene().getRoot().prefWidth(-1);
                double prefHeight = backButton.getScene().getRoot().prefHeight(-1);

                // Add a bit of padding
                stage.setWidth(prefWidth + 20);
                stage.setHeight(prefHeight + 20);
                stage.centerOnScreen();
            }
        });
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void initData(EmployeeSupport ticket) {
        this.currentTicket = ticket;

        TicketIDTA.setText(String.valueOf(ticket.getTicketID()));
        usernameTA.setText(ticket.getUser());
        subjectTA.setText(ticket.getSubject());
        customerConcernTA.setText(ticket.getCusmsg());
        statusTA.setText(ticket.isClosed() ? "Closed" : "Open");

        if (ticket.isClosed()) {
            empResponseTF.setText(ticket.getResponse());
            empResponseTF.setEditable(false);
            sendButton.setVisible(false);
        }
    }

    @FXML
    private void handleSendButton() {
        String response = empResponseTF.getText().trim();

        if (response.isEmpty()) {
            showAlert("Error", "Response cannot be empty!");
            return;
        }
        currentTicket.setResponse(response);
        currentTicket.setClosed(true);
        firestore.updateResponse(currentTicket.getTicketID(), response);
        showAlert("Success", "Response sent successfully!");
        closeWindow();
    }

    @FXML
    private void handleBackButton() {
        // Simply close the current window
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void closeWindow() {
        Stage stage = (Stage) empResponseTF.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}