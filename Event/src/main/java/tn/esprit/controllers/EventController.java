package tn.esprit.controllers;
import javafx.scene.control.Tooltip;
import javafx.util.Duration; // For customizing how fast the tip appears
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.*; // New Import
import tn.esprit.entities.Event;
import tn.esprit.entities.Participation;
import tn.esprit.services.EventService;
import tn.esprit.services.ParticipationService;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.regex.Pattern;

import javafx.scene.shape.Circle;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import tn.esprit.utils.email;
import tn.esprit.utils.PdfGenerator;

public class EventController {

    @FXML
    private TextField titleF, locF, slotsF, nameF, emailF, phoneF, searchEventF, searchPartF;
    @FXML
    private TextArea descF;
    @FXML
    private DatePicker dateF;
    @FXML
    private ComboBox<Event> eventDrop;
    @FXML
    private ComboBox<String> statusDrop, sortEventCombo, filterStatusCombo;

    @FXML
    private TableView<Event> eventTab;
    @FXML
    private TableColumn<Event, String> colT, colL, colD;
    @FXML
    private TableColumn<Event, Integer> colSlo;
    @FXML
    private TableColumn<Event, Timestamp> colDate;

    @FXML
    private TableView<Participation> partTab;
    @FXML
    private TableColumn<Participation, String> colN, colE, colP, colS, colEventTitle;

    // Chart Components
    @FXML
    private PieChart statusPieChart;
    @FXML
    private BarChart<String, Number> eventBarChart;
    @FXML
    private CategoryAxis xAxis;

    private EventService es = new EventService();
    private ParticipationService ps = new ParticipationService();
    private ObservableList<Event> masterEventData = FXCollections.observableArrayList();
    private ObservableList<Participation> masterPartData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        statusDrop.setItems(FXCollections.observableArrayList("pending", "confirmed", "cancelled"));
        sortEventCombo.setItems(FXCollections.observableArrayList("A-Z", "Z-A"));
        filterStatusCombo.setItems(FXCollections.observableArrayList("All", "pending", "confirmed", "cancelled"));

        colT.setCellValueFactory(new PropertyValueFactory<>("title"));
        colL.setCellValueFactory(new PropertyValueFactory<>("location"));
        colD.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSlo.setCellValueFactory(new PropertyValueFactory<>("slots"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("eventDate"));

        colN.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colE.setCellValueFactory(new PropertyValueFactory<>("email"));
        colP.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colS.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEventTitle.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));

        refresh();
        setupFilters();

        eventTab.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                titleF.setText(newVal.getTitle());
                locF.setText(newVal.getLocation());
                slotsF.setText(String.valueOf(newVal.getSlots()));
                descF.setText(newVal.getDescription());
                dateF.setValue(newVal.getEventDate().toLocalDateTime().toLocalDate());
                loadParticipations(newVal.getId());
            }
        });

        partTab.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                nameF.setText(newVal.getFullName());
                emailF.setText(newVal.getEmail());
                phoneF.setText(newVal.getPhone());
                statusDrop.setValue(newVal.getStatus());
            }
        });
        colS.setCellFactory(column -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Create the dot
                    Circle dot = new Circle(5); // Radius of 5

                    if (item.equalsIgnoreCase("confirmed")) dot.setFill(Color.web("#27ae60")); // Green
                    else if (item.equalsIgnoreCase("pending")) dot.setFill(Color.web("#f39c12")); // Orange
                    else if (item.equalsIgnoreCase("cancelled")) dot.setFill(Color.web("#e74c3c")); // Red
                    else dot.setFill(Color.GREY);

                    // Container to hold dot and text
                    HBox hBox = new HBox(8); // 8px spacing
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    Label label = new Label(item.toUpperCase());
                    label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                    hBox.getChildren().addAll(dot, label);
                    setGraphic(hBox);
                }
            }
        });
        // --- COLOR CODING FOR SLOTS COLUMN ---
        colSlo.setCellFactory(column -> new TableCell<Event, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    Circle dot = new Circle(5);
                    String msg;

                    // Logic for colors + Tooltip messages
                    if (item <= 5) {
                        dot.setFill(Color.web("#e74c3c")); // Red
                        msg = "⚠️ Critical: Only " + item + " slots left! Event is almost full.";
                    } else if (item <= 15) {
                        dot.setFill(Color.web("#f39c12")); // Orange
                        msg = "📊 Popular: " + item + " slots remaining. Filling up steadily.";
                    } else {
                        dot.setFill(Color.web("#27ae60")); // Green
                        msg = "✅ Available: " + item + " slots left. Plenty of room!";
                    }

                    // Create the container
                    HBox hBox = new HBox(8);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    Label label = new Label(item.toString());
                    hBox.getChildren().addAll(dot, label);

                    // Create and customize the Tooltip
                    Tooltip tip = new Tooltip(msg);
                    tip.setShowDelay(Duration.millis(200)); // Make it appear quickly
                    tip.setStyle("-fx-font-size: 12; -fx-background-color: #34495e; -fx-text-fill: white;");

                    setGraphic(hBox);
                    setTooltip(tip); // This attaches the tip to the entire cell
                }
            }
        });

    }

    @FXML
    public void refreshCharts() {
        // Pie Chart Update
        long pending = masterPartData.stream().filter(p -> "pending".equals(p.getStatus())).count();
        long confirmed = masterPartData.stream().filter(p -> "confirmed".equals(p.getStatus())).count();
        long cancelled = masterPartData.stream().filter(p -> "cancelled".equals(p.getStatus())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Pending (" + pending + ")", pending),
                new PieChart.Data("Confirmed (" + confirmed + ")", confirmed),
                new PieChart.Data("Cancelled (" + cancelled + ")", cancelled)
        );
        statusPieChart.setData(pieData);

        // Bar Chart Update
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Participants");
        for (Event e : masterEventData) {
            // Count participants associated with this event
            int count = ps.getByEvent(e.getId()).size();
            series.getData().add(new XYChart.Data<>(e.getTitle(), count));
        }
        eventBarChart.getData().clear();
        eventBarChart.getData().add(series);
    }

    private void setupFilters() {
        FilteredList<Event> filteredEvents = new FilteredList<>(masterEventData, p -> true);
        searchEventF.textProperty().addListener((obs, old, newVal) -> {
            filteredEvents.setPredicate(e -> newVal == null || newVal.isEmpty() || e.getTitle().toLowerCase().contains(newVal.toLowerCase()));
        });
        SortedList<Event> sortedEvents = new SortedList<>(filteredEvents);
        sortEventCombo.setOnAction(e -> {
            if ("A-Z".equals(sortEventCombo.getValue()))
                sortedEvents.setComparator(Comparator.comparing(Event::getTitle));
            else sortedEvents.setComparator(Comparator.comparing(Event::getTitle).reversed());
        });
        eventTab.setItems(sortedEvents);

        FilteredList<Participation> filteredParts = new FilteredList<>(masterPartData, p -> true);
        searchPartF.textProperty().addListener((obs, old, newVal) -> applyPartFilter(filteredParts));
        filterStatusCombo.setOnAction(e -> applyPartFilter(filteredParts));
        partTab.setItems(filteredParts);
    }

    private void applyPartFilter(FilteredList<Participation> list) {
        String search = searchPartF.getText().toLowerCase();
        String status = filterStatusCombo.getValue();
        list.setPredicate(p -> {
            boolean matchesSearch = search.isEmpty() || p.getFullName().toLowerCase().contains(search);
            boolean matchesStatus = status == null || "All".equals(status) || p.getStatus().equals(status);
            return matchesSearch && matchesStatus;
        });
    }

    private void refresh() {
        masterEventData.setAll(es.getAll());
        eventDrop.setItems(masterEventData);
        refreshCharts(); // Update charts on main refresh
    }

    private void loadParticipations(int eventId) {
        masterPartData.setAll(ps.getByEvent(eventId));
        refreshCharts(); // Update charts when list updates
    }

    private boolean validateEventInput() {
        if (titleF.getText().isEmpty() || dateF.getValue() == null) {
            showAlert("Title and Date required!");
            return false;
        }
        try {
            Integer.parseInt(slotsF.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert("Slots must be a number!");
            return false;
        }
    }

    private boolean validatePartInput() {
        if (nameF.getText().isEmpty()) {
            showAlert("Participant Name required!");
            return false;
        }
        if (!Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$").matcher(emailF.getText()).matches()) {
            showAlert("Invalid Email!");
            return false;
        }
        return true;
    }

    @FXML
    void printTicket() {
        Participation selected = partTab.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 1. Find the full Event object to get Location and Date
            // (Assuming you have a list of events called 'es.getAll()')
            tn.esprit.entities.Event event = es.getAll().stream()
                    .filter(e -> e.getId() == selected.getEventId())
                    .findFirst()
                    .orElse(null);

            if (event == null) {
                showAlert("Error: Could not find event details.");
                return;
            }

            // 2. Setup FileChooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Ticket");
            fileChooser.setInitialFileName("Ticket_" + selected.getFullName().replace(" ", "_") + ".pdf");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(partTab.getScene().getWindow());

            if (file != null) {
                // 3. Prepare the Web API QR Code (Map Location)
                String location = event.getLocation();
                String date = event.getEventDate().toString();

                try {
                    // Encode the location for Google Maps
                    String mapUrl = "https://www.google.com/maps/search/?api=1&query=" +
                            java.net.URLEncoder.encode(location, java.nio.charset.StandardCharsets.UTF_8);

                    // Encode that Map URL for the QR API
                    String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" +
                            java.net.URLEncoder.encode(mapUrl, java.nio.charset.StandardCharsets.UTF_8);

                    // 4. Call your UPDATED PdfGenerator (the one that downloads the API image)
                    PdfGenerator.generateTicket(selected, event.getTitle(), date, location, qrApiUrl, file.getAbsolutePath());

                    // 5. Automatically open the file
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }

                    new Alert(Alert.AlertType.INFORMATION, "Ticket generated with API QR Code!").show();

                } catch (Exception e) {
                    System.out.println("Error generating ticket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showAlert("Please select a participant first!");
        }
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

    @FXML
    void addEvent() {
        if (validateEventInput()) {
            es.add(new Event(0, titleF.getText(), descF.getText(), locF.getText(), Timestamp.valueOf(dateF.getValue().atStartOfDay()), Integer.parseInt(slotsF.getText())));
            refresh();
        }
    }

    @FXML
    void updateEvent() {
        Event s = eventTab.getSelectionModel().getSelectedItem();
        if (s != null && validateEventInput()) {
            es.update(new Event(s.getId(), titleF.getText(), descF.getText(), locF.getText(), Timestamp.valueOf(dateF.getValue().atStartOfDay()), Integer.parseInt(slotsF.getText())));
            refresh();
        }
    }

    @FXML
    void deleteEvent() {
        Event s = eventTab.getSelectionModel().getSelectedItem();
        if (s != null) {
            es.delete(s.getId());
            refresh();
        }
    }

    @FXML
    void addParticipation() {
        if (validatePartInput() && eventDrop.getValue() != null) {
            String name = nameF.getText();
            String emailAddr = emailF.getText();
            String title = eventDrop.getValue().getTitle();
            String loc = eventDrop.getValue().getLocation();
            String date = eventDrop.getValue().getEventDate().toString();

            // 1. Generate Google Maps Link & QR API Link
            String mapUrl = "http://maps.google.com/?q=" + java.net.URLEncoder.encode(loc, java.nio.charset.StandardCharsets.UTF_8);
            String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" +
                    java.net.URLEncoder.encode(mapUrl, java.nio.charset.StandardCharsets.UTF_8);

            // 2. Save to DB
            Participation p = new Participation(0, eventDrop.getValue().getId(), name, emailAddr, phoneF.getText(), statusDrop.getValue());
            ps.add(p);

            // 3. Generate PDF
            String pdfPath = System.getProperty("user.home") + "/Desktop/Ticket_" + name.replace(" ", "_") + ".pdf";
            PdfGenerator.generateTicket(p, title, date, loc, qrApiUrl, pdfPath);

            // 4. Send Email
            email mailService = new email();
            mailService.sendEmailWithQR(emailAddr, "Ticket for " + title, name, title, date, loc, qrApiUrl);

            loadParticipations(eventDrop.getValue().getId());
        }
    }

    @FXML
    void updateParticipation() {
        Participation s = partTab.getSelectionModel().getSelectedItem();
        if (s != null && validatePartInput()) {
            int evId = (eventDrop.getValue() != null) ? eventDrop.getValue().getId() : s.getEventId();
            ps.update(new Participation(s.getId(), evId, nameF.getText(), emailF.getText(), phoneF.getText(), statusDrop.getValue()));
            loadParticipations(evId);
        }
    }

    @FXML
    void deleteParticipation() {
        Participation s = partTab.getSelectionModel().getSelectedItem();
        if (s != null) {
            ps.delete(s.getId());
            loadParticipations(s.getEventId());
        }
    }

    @FXML
    void handleLogout() {
        System.exit(0);
    }

    @FXML
    void goToUserEvents(javafx.event.ActionEvent event) {
        try {
            // 1. Load the user interface file
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/UserEvents.fxml"));
            javafx.scene.Parent root = loader.load();

            // 2. Get the current "Stage" (the window)
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // 3. Set the new scene
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("AgriCloud - User Events");
            stage.show();

        } catch (java.io.IOException e) {
            System.out.println("Error switching to User View: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogout(javafx.event.ActionEvent event) {
        // Just a placeholder so the FXML doesn't crash
        System.out.println("Logout clicked");
    }
}