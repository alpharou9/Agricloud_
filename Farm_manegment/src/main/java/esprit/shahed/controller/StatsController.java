package esprit.shahed.controller;

import esprit.shahed.models.Farm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {
    @FXML private PieChart landPieChart;

    public void setData(List<Farm> farms) {
        if (farms.isEmpty()) return;

        // 1. Group by Farm Type and COUNT the occurrences
        Map<String, Long> countsByType = farms.stream()
                .collect(Collectors.groupingBy(
                        Farm::getFarmType,
                        Collectors.counting()
                ));

        double totalFarms = farms.size();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // 2. Calculate the percentage for each slice
        countsByType.forEach((type, count) -> {
            double percentage = (count / totalFarms) * 100;

            // Format the label to show: "Type: 30.0%"
            String label = String.format("%s: %.1f%%", type, percentage);

            pieChartData.add(new PieChart.Data(label, count));
        });

        landPieChart.setData(pieChartData);
        landPieChart.setTitle("Farm Distribution by Type (%)");
    }

    @FXML
    private void handleClose() {
        ((Stage) landPieChart.getScene().getWindow()).close();
    }
}