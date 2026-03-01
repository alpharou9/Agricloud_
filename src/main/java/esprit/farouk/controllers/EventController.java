package esprit.farouk.controllers;

import esprit.farouk.models.Event;
import esprit.farouk.models.Participation;
import esprit.farouk.models.User;
import esprit.farouk.services.EventService;
import esprit.farouk.services.ParticipationService;
import esprit.farouk.utils.EmailUtils;
import esprit.farouk.utils.UIUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class EventController {

    private final StackPane contentArea;
    private final User currentUser;
    private final EventService eventService = new EventService();
    private final ParticipationService participationService = new ParticipationService();

    public EventController(StackPane contentArea, User currentUser) {
        this.contentArea = contentArea;
        this.currentUser = currentUser;
    }

    public void showEventsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Event Management");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("action-button-add");
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("action-button-edit");
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-delete");
        Button cancelBtn = new Button("Cancel Event");
        cancelBtn.getStyleClass().add("action-button-reject");

        header.getChildren().addAll(title, spacer, addBtn, editBtn, deleteBtn, cancelBtn);

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or location...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList("All", "Upcoming", "Ongoing", "Completed", "Cancelled"));
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("filter-combo");

        filterBar.getChildren().addAll(searchField, statusFilter);

        TableView<Event> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Event, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(50);

        TableColumn<Event, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Event, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantCount"));
        participantsCol.setMaxWidth(80);

        TableColumn<Event, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        table.getColumns().addAll(idCol, titleCol, organizerCol, locationCol, categoryCol, participantsCol, statusCol);

        ObservableList<Event> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(eventService.getAll());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }

        FilteredList<Event> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> filterEvent(event, newVal, statusFilter.getValue())));
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> filterEvent(event, searchField.getText(), newVal)));

        SortedList<Event> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Double-click to view participants
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Event selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventParticipantsView(selected);
                }
            }
        });

        Runnable reloadTable = () -> {
            masterData.clear();
            try {
                masterData.addAll(eventService.getAll());
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + ex.getMessage());
            }
        };

        addBtn.setOnAction(e -> {
            showEventFormDialog(null);
            reloadTable.run();
        });

        editBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to edit.");
                return;
            }
            showEventFormDialog(selected);
            reloadTable.run();
        });

        deleteBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to delete.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete event '" + selected.getTitle() + "'?");
            confirm.setHeaderText("Confirm Deletion");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    eventService.delete(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete event: " + ex.getMessage());
                }
            }
        });

        cancelBtn.setOnAction(e -> {
            Event selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to cancel.");
                return;
            }
            if ("cancelled".equals(selected.getStatus())) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Already Cancelled", "This event is already cancelled.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel event '" + selected.getTitle() + "'?");
            confirm.setHeaderText("Confirm Cancellation");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    eventService.cancel(selected.getId());
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel event: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, filterBar, table);
        contentArea.getChildren().add(container);
    }

    private boolean filterEvent(Event event, String searchText, String statusFilter) {
        boolean matchesSearch = true;
        boolean matchesStatus = true;

        if (searchText != null && !searchText.trim().isEmpty()) {
            String lower = searchText.toLowerCase();
            matchesSearch = (event.getTitle() != null && event.getTitle().toLowerCase().contains(lower))
                    || (event.getLocation() != null && event.getLocation().toLowerCase().contains(lower));
        }

        if (statusFilter != null && !"All".equals(statusFilter)) {
            matchesStatus = statusFilter.toLowerCase().equals(event.getStatus());
        }

        return matchesSearch && matchesStatus;
    }

    private void showEventFormDialog(Event event) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(event == null ? "Add Event" : "Edit Event");
        dialog.setHeaderText(event == null ? "Create a new event" : "Edit event details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Event title");
        if (event != null) titleField.setText(event.getTitle());

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Event description");
        descriptionArea.setPrefRowCount(3);
        if (event != null && event.getDescription() != null) descriptionArea.setText(event.getDescription());

        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        if (event != null) locationField.setText(event.getLocation());

        DatePicker eventDatePicker = new DatePicker();
        eventDatePicker.setPromptText("Event date");
        if (event != null && event.getEventDate() != null) eventDatePicker.setValue(event.getEventDate().toLocalDate());

        TextField eventTimeField = new TextField();
        eventTimeField.setPromptText("Time (HH:mm)");
        if (event != null && event.getEventDate() != null) {
            eventTimeField.setText(event.getEventDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity (optional)");
        if (event != null && event.getCapacity() != null) capacityField.setText(String.valueOf(event.getCapacity()));

        ComboBox<String> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Workshop", "Seminar", "Fair", "Training", "Networking", "Exhibition", "Conference", "Other"));
        categoryCombo.setPromptText("Select category");
        if (event != null && event.getCategory() != null) categoryCombo.setValue(event.getCategory());

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "upcoming", "ongoing", "completed", "cancelled"));
        statusCombo.setValue(event != null ? event.getStatus() : "upcoming");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Date:"), 0, 3);
        grid.add(eventDatePicker, 1, 3);
        grid.add(new Label("Time:"), 0, 4);
        grid.add(eventTimeField, 1, 4);
        grid.add(new Label("Capacity:"), 0, 5);
        grid.add(capacityField, 1, 5);
        grid.add(new Label("Category:"), 0, 6);
        grid.add(categoryCombo, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(statusCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);

        while (true) {
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                String titleText = titleField.getText().trim();
                String descText = descriptionArea.getText().trim();
                String locationText = locationField.getText().trim();
                LocalDate dateVal = eventDatePicker.getValue();
                String timeText = eventTimeField.getText().trim();

                if (titleText.isEmpty() || titleText.length() < 3) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Title must be at least 3 characters.");
                    continue;
                }
                if (locationText.isEmpty()) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Location is required.");
                    continue;
                }
                if (dateVal == null) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Event date is required.");
                    continue;
                }

                LocalTime timeVal = LocalTime.of(9, 0);
                if (!timeText.isEmpty()) {
                    try {
                        timeVal = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ex) {
                        UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid time format. Use HH:mm.");
                        continue;
                    }
                }

                Integer capacity = null;
                if (!capacityField.getText().trim().isEmpty()) {
                    try {
                        capacity = Integer.parseInt(capacityField.getText().trim());
                        if (capacity <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException ex) {
                        UIUtils.showAlert(Alert.AlertType.ERROR, "Validation Error", "Capacity must be a positive number.");
                        continue;
                    }
                }

                try {
                    if (event == null) {
                        Event newEvent = new Event();
                        newEvent.setUserId(currentUser.getId());
                        newEvent.setTitle(titleText);
                        newEvent.setSlug(titleText.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                        newEvent.setDescription(descText);
                        newEvent.setLocation(locationText);
                        newEvent.setEventDate(LocalDateTime.of(dateVal, timeVal));
                        newEvent.setCapacity(capacity);
                        newEvent.setCategory(categoryCombo.getValue());
                        newEvent.setStatus(statusCombo.getValue());
                        eventService.add(newEvent);
                    } else {
                        event.setTitle(titleText);
                        event.setSlug(titleText.toLowerCase().replaceAll("[^a-z0-9]+", "-"));
                        event.setDescription(descText);
                        event.setLocation(locationText);
                        event.setEventDate(LocalDateTime.of(dateVal, timeVal));
                        event.setCapacity(capacity);
                        event.setCategory(categoryCombo.getValue());
                        event.setStatus(statusCombo.getValue());
                        eventService.update(event);
                    }
                    break;
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Database Error", ex.getMessage());
                }
            } else {
                break;
            }
        }
    }

    private void showEventParticipantsView(Event event) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showEventsView());

        Label title = new Label("Participants: " + event.getTitle());
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAttendedBtn = new Button("Mark Attended");
        markAttendedBtn.getStyleClass().add("action-button-approve");
        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("action-button-delete");

        header.getChildren().addAll(backBtn, title, spacer, markAttendedBtn, removeBtn);

        TableView<Participation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Participation, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMaxWidth(50);

        TableColumn<Participation, String> userCol = new TableColumn<>("Participant");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Participation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Participation, Boolean> attendedCol = new TableColumn<>("Attended");
        attendedCol.setCellValueFactory(new PropertyValueFactory<>("attended"));

        table.getColumns().addAll(idCol, userCol, statusCol, attendedCol);

        ObservableList<Participation> data = FXCollections.observableArrayList();
        try {
            data.addAll(participationService.getByEventId(event.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load participants: " + e.getMessage());
        }
        table.setItems(data);

        Runnable reloadTable = () -> {
            data.clear();
            try {
                data.addAll(participationService.getByEventId(event.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load participants: " + ex.getMessage());
            }
        };

        markAttendedBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a participant.");
                return;
            }
            try {
                participationService.markAttended(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to mark attended: " + ex.getMessage());
            }
        });

        removeBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a participant to remove.");
                return;
            }
            try {
                participationService.delete(selected.getId());
                reloadTable.run();
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove participant: " + ex.getMessage());
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    public void showBrowseEventsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Upcoming Events");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Search events...");
        searchField.getStyleClass().add("search-field");

        Button myParticipationsBtn = new Button("My Registrations");
        myParticipationsBtn.getStyleClass().add("action-button-edit");
        myParticipationsBtn.setOnAction(e -> showMyParticipationsView());

        header.getChildren().addAll(title, spacer, searchField, myParticipationsBtn);

        TableView<Event> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Event, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Event, String> organizerCol = new TableColumn<>("Organizer");
        organizerCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Event, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        TableColumn<Event, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Event, Integer> participantsCol = new TableColumn<>("Participants");
        participantsCol.setCellValueFactory(new PropertyValueFactory<>("participantCount"));
        participantsCol.setMaxWidth(80);

        table.getColumns().addAll(titleCol, organizerCol, locationCol, categoryCol, participantsCol);

        ObservableList<Event> masterData = FXCollections.observableArrayList();
        try {
            masterData.addAll(eventService.getUpcoming());
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events: " + e.getMessage());
        }

        FilteredList<Event> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(event -> {
                    if (newVal == null || newVal.trim().isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (event.getTitle() != null && event.getTitle().toLowerCase().contains(lower))
                            || (event.getLocation() != null && event.getLocation().toLowerCase().contains(lower));
                }));

        SortedList<Event> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Double-click to view details and register
        table.setOnMouseClicked(ev -> {
            if (ev.getClickCount() == 2) {
                Event selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetailView(selected);
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }

    private void showEventDetailView(Event event) {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(10));

        Button backBtn = new Button("Back to Events");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBrowseEventsView());

        Label titleLabel = new Label(event.getTitle());
        titleLabel.getStyleClass().add("content-title");
        titleLabel.setWrapText(true);

        Label metaLabel = new Label("By: " + (event.getUserName() != null ? event.getUserName() : "Unknown") +
                " | Location: " + event.getLocation() +
                " | Category: " + (event.getCategory() != null ? event.getCategory() : "N/A"));
        metaLabel.setStyle("-fx-text-fill: #757575;");

        Label dateLabel = new Label("Date: " + (event.getEventDate() != null ?
                event.getEventDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")) : "TBD"));
        dateLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label capacityLabel = new Label("Capacity: " + (event.getCapacity() != null ?
                event.getParticipantCount() + " / " + event.getCapacity() : event.getParticipantCount() + " registered"));

        Label descriptionLabel = new Label(event.getDescription() != null ? event.getDescription() : "No description.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14;");

        // Check if already registered
        Button registerBtn = new Button("Register for Event");
        registerBtn.getStyleClass().add("action-button-add");

        try {
            Participation existing = participationService.getByEventAndUser(event.getId(), currentUser.getId());
            if (existing != null) {
                if ("cancelled".equals(existing.getStatus())) {
                    registerBtn.setText("Register Again");
                } else {
                    registerBtn.setText("Already Registered");
                    registerBtn.setDisable(true);
                }
            }
        } catch (SQLException e) {
            // Ignore
        }

        // Check capacity
        if (event.getCapacity() != null && event.getParticipantCount() >= event.getCapacity()) {
            registerBtn.setText("Event Full");
            registerBtn.setDisable(true);
        }

        registerBtn.setOnAction(e -> {
            try {
                Participation existing = participationService.getByEventAndUser(event.getId(), currentUser.getId());
                if (existing != null) {
                    if (!"cancelled".equals(existing.getStatus())) {
                        UIUtils.showAlert(Alert.AlertType.WARNING, "Already Registered", "You are already registered for this event.");
                        return;
                    }
                    // Re-register: update existing record
                    existing.setStatus("confirmed");
                    existing.setCancelledAt(null);
                    existing.setCancelledReason(null);
                    participationService.update(existing);
                } else {
                    // New registration
                    Participation p = new Participation();
                    p.setEventId(event.getId());
                    p.setUserId(currentUser.getId());
                    p.setStatus("confirmed");
                    participationService.add(p);
                }
                // Send ticket email with QR code in background thread
                Participation registered = participationService.getByEventAndUser(event.getId(), currentUser.getId());
                final long participationId = registered != null ? registered.getId() : 0;
                new Thread(() -> {
                    try {
                        EmailUtils.sendEventTicket(currentUser.getEmail(), currentUser.getName(), event, participationId);
                    } catch (Exception ex) {
                        System.err.println("Failed to send ticket email: " + ex.getMessage());
                    }
                }).start();
                UIUtils.showAlert(Alert.AlertType.INFORMATION, "Registered!", "You have successfully registered for this event!\nA ticket with QR code has been sent to " + currentUser.getEmail());
                showEventDetailView(eventService.getById(event.getId())); // Refresh
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to register: " + ex.getMessage());
            }
        });

        VBox content = new VBox(15, titleLabel, metaLabel, dateLabel, capacityLabel, new Separator(), descriptionLabel, registerBtn);
        content.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        container.getChildren().addAll(backBtn, scrollPane);
        contentArea.getChildren().add(container);
    }

    private void showMyParticipationsView() {
        contentArea.getChildren().clear();

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("action-button-block");
        backBtn.setOnAction(e -> showBrowseEventsView());

        Label title = new Label("My Event Registrations");
        title.getStyleClass().add("content-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel Registration");
        cancelBtn.getStyleClass().add("action-button-reject");

        header.getChildren().addAll(backBtn, title, spacer, cancelBtn);

        TableView<Participation> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Participation, String> eventCol = new TableColumn<>("Event");
        eventCol.setCellValueFactory(new PropertyValueFactory<>("eventTitle"));

        TableColumn<Participation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Participation, Boolean> attendedCol = new TableColumn<>("Attended");
        attendedCol.setCellValueFactory(new PropertyValueFactory<>("attended"));

        table.getColumns().addAll(eventCol, statusCol, attendedCol);

        ObservableList<Participation> data = FXCollections.observableArrayList();
        try {
            data.addAll(participationService.getByUserId(currentUser.getId()));
        } catch (SQLException e) {
            UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load registrations: " + e.getMessage());
        }
        table.setItems(data);

        Runnable reloadTable = () -> {
            data.clear();
            try {
                data.addAll(participationService.getByUserId(currentUser.getId()));
            } catch (SQLException ex) {
                UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load registrations: " + ex.getMessage());
            }
        };

        cancelBtn.setOnAction(e -> {
            Participation selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a registration to cancel.");
                return;
            }
            if ("cancelled".equals(selected.getStatus())) {
                UIUtils.showAlert(Alert.AlertType.WARNING, "Already Cancelled", "This registration is already cancelled.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancel registration for '" + selected.getEventTitle() + "'?");
            confirm.setHeaderText("Confirm Cancellation");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    participationService.cancel(selected.getId(), "User cancelled");
                    reloadTable.run();
                } catch (SQLException ex) {
                    UIUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel: " + ex.getMessage());
                }
            }
        });

        container.getChildren().addAll(header, table);
        contentArea.getChildren().add(container);
    }
}
