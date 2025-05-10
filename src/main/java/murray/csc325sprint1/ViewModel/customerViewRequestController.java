package murray.csc325sprint1.ViewModel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import murray.csc325sprint1.Model.EmployeeSupport;
import murray.csc325sprint1.Model.SupportFirestoreFunctions;

import java.net.URL;
import java.util.ResourceBundle;

public class customerViewRequestController implements Initializable {
    @FXML private TextArea SubjectTA;
    @FXML private TextArea TicketIDTA;
    @FXML private TextArea usernameTA;
    @FXML private TextArea statusTA;
    @FXML private TextArea customerConcernTA;
    @FXML private TextArea empResponseTA;
    @FXML private Button backButton;

    private SupportFirestoreFunctions firestore;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        firestore = SupportFirestoreFunctions.getInstance();
        TicketIDListener();

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

    private void TicketIDListener() {
        TicketIDTA.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!newValue.isEmpty()) {
                TicketIDEntered(newValue.trim());
            }else{
                clearTicket();
            }
        });
    }

    private void TicketIDEntered(String cusTicketID) {
        try{
            int ticketID = Integer.parseInt(cusTicketID);
            loadTicket(ticketID);
        } catch (NumberFormatException e) {
            clearTicket();
        }
    }
    private void loadTicket(int ticketID) {
        EmployeeSupport ticket = firestore.getTicket(ticketID);
        if(ticket != null) {
            populateFields(ticket);
        }else{
            clearTicket();
        }
    }

    private void populateFields(EmployeeSupport ticket) {
        SubjectTA.setText(ticket.getSubject());
        usernameTA.setText(ticket.getUser());
        statusTA.setText(ticket.isClosed() ? "Closed" : "Open");
        customerConcernTA.setText(ticket.getCusmsg());
        empResponseTA.setText(ticket.getResponse() != null ? ticket.getResponse() : "No response available");
    }

    private void clearTicket() {
        SubjectTA.clear();
        usernameTA.clear();
        statusTA.clear();
        customerConcernTA.clear();
        empResponseTA.clear();
    }

    public void initData(EmployeeSupport ticket) {
        if(ticket != null) {
            populateFields(ticket);
            if (TicketIDTA != null) {
                TicketIDTA.setText(String.valueOf(ticket.getTicketID()));
            }
        }
    }

    @FXML
    private void handleBackButton() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }
}