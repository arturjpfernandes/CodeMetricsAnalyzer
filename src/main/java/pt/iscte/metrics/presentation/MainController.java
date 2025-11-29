package pt.iscte.metrics.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import pt.iscte.metrics.domain.ClassMetrics;
import pt.iscte.metrics.infrastructure.FileWalker;
import pt.iscte.metrics.infrastructure.JdtAnalyzer;
import javafx.stage.FileChooser;
import pt.iscte.metrics.infrastructure.CsvExporter;
import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Label lblStatus;
    @FXML private TableView<ClassMetrics> tblMetrics;
    @FXML private TableColumn<ClassMetrics, String> colPackage;
    @FXML private TableColumn<ClassMetrics, String> colClass;
    @FXML private TableColumn<ClassMetrics, Integer> colLoc;
    @FXML private TableColumn<ClassMetrics, Integer> colMethods;
    @FXML private TableColumn<ClassMetrics, Integer> colWmc;
    @FXML private TableColumn<ClassMetrics, Integer> colDit;
    @FXML private TableColumn<ClassMetrics, Integer> colCbo;


    // Novos Elementos Gráficos
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        colPackage.setCellValueFactory(new PropertyValueFactory<>("packageName"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("loc"));
        colMethods.setCellValueFactory(new PropertyValueFactory<>("methodCount"));
        colWmc.setCellValueFactory(new PropertyValueFactory<>("wmc"));
        colDit.setCellValueFactory(new PropertyValueFactory<>("dit"));
        colCbo.setCellValueFactory(new PropertyValueFactory<>("cbo"));
    }

    @FXML
    public void handleSelectProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar Projeto Java");
        File selectedDirectory = directoryChooser.showDialog(lblStatus.getScene().getWindow());

        if (selectedDirectory != null) {
            lblStatus.setText("A processar...");

            FileWalker walker = new FileWalker();
            List<File> javaFiles = walker.findAllJavaFiles(selectedDirectory);

            ObservableList<ClassMetrics> data = FXCollections.observableArrayList();

            for (File file : javaFiles) {
                try {
                    String sourceCode = Files.readString(file.toPath());
                    ClassMetrics metrics = JdtAnalyzer.analyze(sourceCode, file.getName(), selectedDirectory.getAbsolutePath());
                    data.add(metrics);
                } catch (Exception e) {
                    System.err.println("Erro: " + file.getName());
                }
            }

            // 1. Encher a Tabela
            tblMetrics.setItems(data);

            // 2. Atualizar Gráficos
            updateCharts(data);

            lblStatus.setText("Concluído. " + data.size() + " classes analisadas.");
        }
    }

    private void updateCharts(ObservableList<ClassMetrics> data) {
        // --- Bar Chart: Top 5 Classes mais Complexas ---
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Ordenar por WMC (decrescente) e pegar nas 5 primeiras
        List<ClassMetrics> top5 = data.stream()
                .sorted(Comparator.comparingInt(ClassMetrics::getWmc).reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (ClassMetrics m : top5) {
            series.getData().add(new XYChart.Data<>(m.getClassName(), m.getWmc()));
        }
        barChart.getData().add(series);

        // --- Pie Chart: Métodos vs Atributos (Exemplo Simples) ---
        // Vamos contar quantas classes têm WMC > 10 (Complexas) vs < 10 (Simples)
        // Em MainController.java, no fundo do método updateCharts:

// Muda de 10 para 2 apenas para veres o gráfico a dividir as cores com este projeto pequeno
        long complexas = data.stream().filter(m -> m.getWmc() >= 5).count();
        long simples = data.stream().filter(m -> m.getWmc() < 5).count();

        pieChart.getData().clear();
        pieChart.getData().add(new PieChart.Data("Complexas (>5)", complexas));
        pieChart.getData().add(new PieChart.Data("Simples (<5)", simples));
    }

    @FXML
    public void handleExport() {
        // Verificar se há dados para exportar
        if (tblMetrics.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText("Sem dados");
            alert.setContentText("Primeiro tens de analisar um projeto!");
            alert.showAndWait();
            return;
        }

        // 1. Escolher onde guardar o ficheiro
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Relatório CSV");
        fileChooser.setInitialFileName("metricas_codigo.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(lblStatus.getScene().getWindow());

        if (file != null) {
            try {
                // 2. Chamar o nosso Exporter
                CsvExporter.exportToCSV(tblMetrics.getItems(), file);

                lblStatus.setText("Sucesso! Guardado em: " + file.getName());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sucesso");
                alert.setHeaderText("Exportação Concluída");
                alert.setContentText("O ficheiro CSV foi criado com sucesso.");
                alert.showAndWait();

            } catch (Exception e) {
                lblStatus.setText("Erro ao exportar.");
                e.printStackTrace();
            }
        }
    }

}