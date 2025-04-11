import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LogAnalyzerApp extends Application {

    @FXML private Button loadButton;
    @FXML private Button viewAllButton;
    @FXML private Button filterByLevelButton;
    @FXML private ComboBox<String> levelComboBox;
    @FXML private TableView<LogEntry> tableView;
    @FXML private TableColumn<LogEntry, String> timestampColumn;
    @FXML private TableColumn<LogEntry, String> levelColumn;
    @FXML private TableColumn<LogEntry, String> messageColumn;
    @FXML private Button countByLevelButton;
    @FXML private TextArea countByLevelLabel;
    @FXML private TextArea frequentErrorLabel;
    @FXML private Button frequentErrorButton;
    @FXML private TextField searchKeywordField;
    @FXML private Button searchByKeywordButton;
    @FXML private Button browseButton;
    @FXML private Button sortByDateButton;
    @FXML
    private ChoiceBox<String> exportLevelChoiceBox;
    @FXML
    private Label exportStatusLabel;
    @FXML
    private ListView logListView;



    private LogAnalyzer analyzer;


    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LogAnalyzerApp.fxml"));
        // Set the controller manually
        loader.setController(this);
        VBox root = loader.load();

        // Load CSS
        String css = this.getClass().getResource("/styles.css").toExternalForm();
        root.getStylesheets().add(css);

        // Initialize LogAnalyzer
        analyzer=null;

        // Initialize table columns
        timestampColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));
        levelColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLevel()));
        messageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));

        //browsing files
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Log File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Log Files", "*.log")
            );

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                analyzer = new LogAnalyzer(selectedFile.getAbsolutePath());
                showAlert("Log File Selected", "✅ File loaded: " + selectedFile.getName());
            }
        });


        // Load logs button action
        loadButton.setOnAction(e -> {
            if(analyzer==null){
                showAlert("No File Selected", "⚠️ Please select a log file first.");
                return;
            }
            analyzer.loadlogs();

            List<LogEntry> logs = analyzer.getLogEntries();
            Set<String> levels = logs.stream()
                    .map(LogEntry::getLevel)
                    .collect(Collectors.toCollection(TreeSet::new));
            levelComboBox.setItems(FXCollections.observableArrayList(levels));
            levelComboBox.setValue(levels.stream().findFirst().orElse(null));

            exportLevelChoiceBox.setItems(FXCollections.observableArrayList(levels));
            // Show alert after loading logs
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("✅ Logs loaded successfully!");
            alert.showAndWait();
        });

        sortByDateButton.setOnAction(e -> handleSortByDate());

        // View all logs button action
        viewAllButton.setOnAction(e -> {
            ObservableList<LogEntry> logList = FXCollections.observableArrayList(analyzer.getLogEntries());
            tableView.setItems(logList);
        });

        filterByLevelButton.setOnAction(e->{
            String selectedLevel = levelComboBox.getValue();
            ObservableList<LogEntry> filteredLogs = FXCollections.observableArrayList(analyzer.getFilteredLogs(selectedLevel));
            tableView.setItems(filteredLogs);
        });

        // Count by Level button action
        countByLevelButton.setOnAction(e -> {
            if (analyzer == null) {
                showAlert("No File Selected", "⚠️ Please select a log file first.");
                return;
            }

            Map<String, Integer> levelCount = analyzer.countByLevel();
            System.out.println("Level Count: " + levelCount);
            if (levelCount.isEmpty()) {
                countByLevelLabel.setText("No log entries available.");
                return;
            }

            StringBuilder countText = new StringBuilder("Log Level Counts:\n");
            for (Map.Entry<String, Integer> entry : levelCount.entrySet()) {
                countText.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }

            Platform.runLater(() -> {
                countByLevelLabel.setText(countText.toString());
                countByLevelLabel.setMaxWidth(Double.MAX_VALUE);
                countByLevelLabel.setWrapText(true);
            });

        });

        //frequent errors
        frequentErrorButton.setOnAction(e -> {
            Map<String, Integer> errorCount = analyzer.countErrorMessages();
            StringBuilder errorText = new StringBuilder("Frequent Errors:\n");

            if (errorCount.isEmpty()) {
                errorText.append("No errors found.");
            } else {
                errorCount.entrySet().stream()
                        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                        .forEach(entry -> errorText.append(entry.getKey())
                                .append(" - Count: ")
                                .append(entry.getValue())
                                .append("\n"));
            }
            Platform.runLater(() -> {
                frequentErrorLabel.setText(errorText.toString());
            });
        });

        //search by keyword
        searchByKeywordButton.setOnAction(e -> {
            String keyword = searchKeywordField.getText().trim();
            if (keyword.isEmpty()) {
                showAlert("Error", "Please enter a keyword to search.");
                return;
            }
            List<LogEntry> searchResults = analyzer.searchLogs(keyword);

            ObservableList<LogEntry> observableSearchResults = FXCollections.observableArrayList(searchResults);
            tableView.setItems(observableSearchResults);

            if (searchResults.isEmpty()) {
                showAlert("No Results", "No logs found with the given keyword.");
            }
        });

        // Set up the scene and stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Log Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleExportLogs() {
        String selectedLevel = exportLevelChoiceBox.getValue();
        if (selectedLevel == null || selectedLevel.isEmpty()) {
            exportStatusLabel.setText("❗ Please select a log level.");
            return;
        }

        // File chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Exported Logs");
        fileChooser.setInitialFileName("exported_logs.txt");

        File file = fileChooser.showSaveDialog(exportLevelChoiceBox.getScene().getWindow());

        if (file != null) {
            List<LogEntry> logsToExport = analyzer.getFilteredLogs(selectedLevel);
            boolean success = analyzer.exportLogsToFile(logsToExport, file.getAbsolutePath());

            if (success) {
                exportStatusLabel.setText("✅ Logs exported to: " + file.getAbsolutePath());
            } else {
                exportStatusLabel.setText("❌ Failed to export logs.");
            }
        } else {
            exportStatusLabel.setText("⚠️ Export canceled.");
        }
    }

    @FXML
    private void handleSortByDate() {
        if (analyzer == null) {
            showAlert(null,"No log file loaded.");
            return;
        }

        List<LogEntry> sortedLogs = analyzer.getSortedLogs(true); // true for ascending
        ObservableList<LogEntry> displayLogs = FXCollections.observableArrayList(sortedLogs);
        tableView.setItems(displayLogs);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
