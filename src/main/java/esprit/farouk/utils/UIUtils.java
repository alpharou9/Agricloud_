package esprit.farouk.utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class UIUtils {

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static VBox createStatCard(String label, String value, String styleClass) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPadding(new Insets(15, 25, 15, 25));
        card.setMinWidth(130);

        Label valLabel = new Label(value);
        valLabel.getStyleClass().add("stat-card-value");
        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-card-label");

        card.getChildren().addAll(valLabel, nameLabel);
        return card;
    }
}
