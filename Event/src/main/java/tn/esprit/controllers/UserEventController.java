package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.entities.Event;
import tn.esprit.entities.Participation;
import tn.esprit.services.EventService;
import tn.esprit.services.ParticipationService;
import tn.esprit.utils.email;
import tn.esprit.utils.PdfGenerator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class UserEventController {

    @FXML private FlowPane eventGrid;
    @FXML private StackPane bookingModal;
    @FXML private Label modalTitle;
    @FXML private TextField userNameF, userEmailF, userPhoneF, searchEventF;
    @FXML private ComboBox<String> sortCombo;

    private EventService es = new EventService();
    private ParticipationService ps = new ParticipationService();
    private Event selectedEvent;
    private List<Event> allEvents = new ArrayList<>();

    @FXML
    public void initialize() {
        // Load initial data
        allEvents = es.getAll();

        // Setup Sort ComboBox
        sortCombo.setItems(FXCollections.observableArrayList("Title (A-Z)", "Title (Z-A)"));

        // Add Listeners for real-time search and sorting
        searchEventF.textProperty().addListener((obs, oldVal, newVal) -> updateView());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateView());

        updateView();
    }

    /**
     * Refreshes the grid based on search text and sort selection
     */
    private void updateView() {
        String searchText = (searchEventF.getText() == null) ? "" : searchEventF.getText().toLowerCase();
        String sortOrder = sortCombo.getValue();

        List<Event> filtered = allEvents.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        if ("Title (A-Z)".equals(sortOrder)) {
            filtered.sort(Comparator.comparing(Event::getTitle));
        } else if ("Title (Z-A)".equals(sortOrder)) {
            filtered.sort(Comparator.comparing(Event::getTitle).reversed());
        }

        eventGrid.getChildren().clear();
        for (Event e : filtered) {
            eventGrid.getChildren().add(createCard(e));
        }
    }

    private VBox createCard(Event e) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        card.setPrefWidth(320);

        // 1. Header with Title and Status Circle
        HBox header = new HBox(10);
        Label title = new Label(e.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        Circle statusCircle = new Circle(6);
        if (e.getSlots() <= 5) {
            statusCircle.setFill(Color.web("#e74c3c")); // Red (Critical)
        } else if (e.getSlots() <= 15) {
            statusCircle.setFill(Color.web("#f39c12")); // Orange (Popular)
        } else {
            statusCircle.setFill(Color.web("#27ae60")); // Green (Available)
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, statusCircle);

        // 2. Description
        Label desc = new Label(e.getDescription());
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");
        desc.setMinHeight(50);
        desc.setMaxHeight(80);

        // 3. Event Details & Slots
        Label details = new Label("📅 " + e.getEventDate().toString() +
                "\n📍 " + e.getLocation() +
                "\n👥 Available Slots: " + e.getSlots());
        details.setStyle("-fx-line-spacing: 5;");

        // 4. Action Buttons
        HBox actions = new HBox(10);

        Button joinBtn = new Button("Join Event");
        joinBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
        joinBtn.setPrefWidth(120);
        joinBtn.setOnAction(event -> openBooking(e));

        Button printBtn = new Button("🖨️ Print Ticket");
        printBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
        printBtn.setPrefWidth(120);
        printBtn.setOnAction(event -> quickPrint(e));

        actions.getChildren().addAll(joinBtn, printBtn);

        card.getChildren().addAll(header, desc, details, actions);
        return card;
    }

    /**
     * Logic for individual Print Ticket button on the card
     */
    private void quickPrint(Event e) {
        // 1. Ask for the user's name (since we don't have a selection table here)
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Print Ticket");
        dialog.setHeaderText("Generate Ticket for: " + e.getTitle());
        dialog.setContentText("Please enter your full name:");

        dialog.showAndWait().ifPresent(name -> {
            // 2. Setup FileChooser (Just like your dashboard method)
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Ticket");
            fileChooser.setInitialFileName("Ticket_" + name.replace(" ", "_") + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(eventGrid.getScene().getWindow());

            if (file != null) {
                try {
                    // 3. Prepare the Web API QR Code (Map Location)
                    String location = e.getLocation();
                    String date = e.getEventDate().toString();

                    // Encode the location for Google Maps
                    String mapUrl = "https://www.google.com/maps/search/?api=1&query=" +
                            java.net.URLEncoder.encode(location, java.nio.charset.StandardCharsets.UTF_8);

                    // Encode that Map URL for the QR API
                    String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" +
                            java.net.URLEncoder.encode(mapUrl, java.nio.charset.StandardCharsets.UTF_8);

                    // 4. Create a temporary participation object for the generator
                    // We use "N/A" for email/phone because this is a quick-print preview
                    Participation tempP = new Participation(0, e.getId(), name, "N/A", "N/A", "Confirmed");

                    // 5. Call your PdfGenerator
                    PdfGenerator.generateTicket(tempP, e.getTitle(), date, location, qrApiUrl, file.getAbsolutePath());

                    // 6. Automatically open the file (Desktop integration)
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }

                    new Alert(Alert.AlertType.INFORMATION, "Ticket generated successfully!").show();

                } catch (Exception ex) {
                    System.err.println("Error generating ticket: " + ex.getMessage());
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to generate ticket.").show();
                }
            }
        });
    }

    @FXML
    void submitBooking() {
        if (userNameF.getText().isEmpty() || userEmailF.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please fill in your name and email!").show();
            return;
        }

        try {
            // Logic for Map QR API
            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(selectedEvent.getLocation(), StandardCharsets.UTF_8);
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + URLEncoder.encode(mapUrl, StandardCharsets.UTF_8);

            // Save to DB
            Participation p = new Participation(0, selectedEvent.getId(), userNameF.getText(), userEmailF.getText(), userPhoneF.getText(), "Confirmed");
            ps.add(p);

            // Generate File & Send Email
            String path = System.getProperty("user.home") + "/Desktop/Ticket_" + p.getFullName().replace(" ", "_") + ".pdf";
            PdfGenerator.generateTicket(p, selectedEvent.getTitle(), selectedEvent.getEventDate().toString(), selectedEvent.getLocation(), qrUrl, path);

            new email().sendEmailWithQR(p.getEmail(), "AgriCloud Registration Confirmed", p.getFullName(), selectedEvent.getTitle(), selectedEvent.getEventDate().toString(), selectedEvent.getLocation(), qrUrl);

            closeModal();
            allEvents = es.getAll(); // Refresh slots data from DB
            updateView();
            new Alert(Alert.AlertType.INFORMATION, "Registration complete! Your ticket has been sent to your email and saved to your desktop.").show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    void backToAdmin(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/EventManagement.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AgriCloud Admin Dashboard");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openBooking(Event e) {
        if (e.getSlots() <= 0) {
            new Alert(Alert.AlertType.ERROR, "Sorry, this event is fully booked!").show();
            return;
        }
        this.selectedEvent = e;
        modalTitle.setText("Join " + e.getTitle());
        bookingModal.setVisible(true);
    }

    @FXML void closeModal() { bookingModal.setVisible(false); }
    @FXML void handleLogout() { System.exit(0); }
}