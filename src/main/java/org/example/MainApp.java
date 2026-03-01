package org.example;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private static Scene mainScene;
    private static StackPane toastLayer;
    private static final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/main-view.fxml"));
        HBox root = loader.load();

        mainScene = new Scene(root, 1050, 650);
        mainScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Dark mode listener
        darkMode.addListener((obs, wasD, isD) -> {
            if (isD) {
                mainScene.getRoot().getStyleClass().add("theme-dark");
            } else {
                mainScene.getRoot().getStyleClass().remove("theme-dark");
            }
        });

        primaryStage.setTitle("AgriCloud - Farm Management");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(550);
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    // ==================== TOAST SYSTEM ====================

    public static void showToast(String message, String type) {
        if (toastLayer == null) return;

        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", "toast-" + type);
        toast.setMaxWidth(350);
        toast.setWrapText(true);

        toastLayer.getChildren().add(toast);
        StackPane.setAlignment(toast, Pos.TOP_RIGHT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition hold = new PauseTransition(Duration.seconds(3));
        hold.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toast);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> toastLayer.getChildren().remove(toast));
            fadeOut.play();
        });
        hold.play();
    }

    public static boolean isDarkMode() {
        return darkMode.get();
    }

    public static BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public static void setToastLayer(StackPane layer) {
        toastLayer = layer;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
