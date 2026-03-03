package esprit.shahed.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class MapPopupController {
    @FXML private WebView mapWebView;
    private WebEngine webEngine;
    private double lat = 0.0, lng = 0.0;
    private String name = "";
    private boolean confirmed = false;

    public class MapBridge {
        public void updateLocationData(double lt, double lg, String nm) {
            lat = lt; lng = lg; name = nm;
            System.out.println("Point Selected: " + lat + ", " + lng);
        }
    }

    @FXML
    public void initialize() {
        webEngine = mapWebView.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, old, nS) -> {
            if (nS == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject win = (JSObject) webEngine.executeScript("window");
                win.setMember("app", new MapBridge());

                // FIXED: Changed fixMap() to refreshMap() to match map.html
                webEngine.executeScript("refreshMap()");
            }
        });
        webEngine.load(getClass().getResource("/map.html").toExternalForm());
    }

    @FXML
    void handleConfirm() {
        if (lat != 0.0) {
            confirmed = true;
            ((Stage) mapWebView.getScene().getWindow()).close();
        } else {
            new Alert(Alert.AlertType.WARNING, "No point selected. Please click on the map.").showAndWait();
        }
    }

    @FXML void handleCancel() { confirmed = false; ((Stage) mapWebView.getScene().getWindow()).close(); }

    public boolean isConfirmed() { return confirmed; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getName() { return name; }
}