package murray.csc325sprint1.ViewModel;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import murray.csc325sprint1.Order;

public class QuoteDetailsController implements Initializable {

    @FXML private Label totalLabel;
    @FXML private VBox quotedItemsContainer;
    @FXML private Button closeButton;
    // No print button anymore
    @FXML private Label quoteExplanationLabel;

    private Order currentQuote;

    /**
     * Initialize the controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ensure proper window sizing after UI is fully loaded
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
     * Set the quote to display in this dialog
     *
     * @param quote The quote to display
     */
    public void setQuote(Order quote) {
        this.currentQuote = quote;

        // Update the UI with quote details
        updateQuoteDetails();

        // Make sure dialog is properly sized after updating content
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
     * Update the UI with the current quote details
     */
    private void updateQuoteDetails() {
        if (currentQuote == null) {
            return;
        }

        // Set the total
        totalLabel.setText("Total: " + currentQuote.getFormattedTotal());

        // Clear existing items
        quotedItemsContainer.getChildren().clear();

        // Add each ordered item
        for (Map.Entry<String, Integer> entry : currentQuote.getOrderItems().entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();

            // Create a label for this item
            Label itemLabel = new Label(itemName + " (x" + quantity + ")");
            itemLabel.setStyle("-fx-font-size: 14;");

            // Add to container
            quotedItemsContainer.getChildren().add(itemLabel);
        }

        // Set explanation text
        if (quoteExplanationLabel != null) {
            quoteExplanationLabel.setText("This is a price quote only. To place an actual order, " +
                    "please go to the order section from the main menu.");
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
}