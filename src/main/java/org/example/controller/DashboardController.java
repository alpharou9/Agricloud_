package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.dao.DatabaseConnection;
import org.example.dao.OrderDAO;
import org.example.dao.ProductDAO;

public class DashboardController {

    @FXML private Label productCountLabel;
    @FXML private Label orderCountLabel;
    @FXML private Label revenueLabel;
    @FXML private Label dbStatusLabel;
    @FXML private VBox alertsCard;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    @FXML
    public void initialize() {
        populateLowStockAlerts();
    }

    public void refreshStats() {
        try {
            int productCount = productDAO.getProductCount();
            int orderCount = orderDAO.getActiveOrderCount();
            double revenue = orderDAO.getTotalRevenue();

            productCountLabel.setText(String.valueOf(productCount));
            orderCountLabel.setText(String.valueOf(orderCount));
            revenueLabel.setText(String.format("$%.0f", revenue));

            try {
                DatabaseConnection.getConnection();
                dbStatusLabel.setText("Connected");
                dbStatusLabel.getStyleClass().removeAll("status-disconnected");
                if (!dbStatusLabel.getStyleClass().contains("status-connected"))
                    dbStatusLabel.getStyleClass().add("status-connected");
            } catch (Exception ex) {
                dbStatusLabel.setText("Offline");
                dbStatusLabel.getStyleClass().removeAll("status-connected");
                if (!dbStatusLabel.getStyleClass().contains("status-disconnected"))
                    dbStatusLabel.getStyleClass().add("status-disconnected");
            }
        } catch (Exception e) {
            System.out.println("[DashboardController] Error refreshing stats: " + e.getMessage());
        }
    }

    private void populateLowStockAlerts() {
        try {
            var lowStock = productDAO.getLowStockProducts(5);
            if (lowStock.isEmpty()) {
                Label ok = new Label("All products are well stocked.");
                ok.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
                alertsCard.getChildren().add(ok);
            } else {
                for (var p : lowStock) {
                    Label warn = new Label("  [!]  " + p.getName() + " - only " + p.getQuantity() + " left");
                    warn.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px;");
                    alertsCard.getChildren().add(warn);
                }
            }
        } catch (Exception e) {
            Label err = new Label("Could not check stock levels.");
            err.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 12px;");
            alertsCard.getChildren().add(err);
        }
    }
}
