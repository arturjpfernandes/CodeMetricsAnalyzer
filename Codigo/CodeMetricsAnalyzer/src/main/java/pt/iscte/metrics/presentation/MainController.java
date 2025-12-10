package pt.iscte.metrics.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import pt.iscte.metrics.domain.ClassMetrics;
import pt.iscte.metrics.infrastructure.CsvExporter;
import pt.iscte.metrics.infrastructure.FileWalker;
import pt.iscte.metrics.infrastructure.JdtAnalyzer;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController {

    @FXML private Label lblStatus;
    @FXML private TableView<ClassMetrics> tblMetrics;

    // Colunas da Tabela
    @FXML private TableColumn<ClassMetrics, String> colPackage;
    @FXML private TableColumn<ClassMetrics, String> colClass;
    @FXML private TableColumn<ClassMetrics, Integer> colLoc;
    @FXML private TableColumn<ClassMetrics, Integer> colMethods;
    @FXML private TableColumn<ClassMetrics, Integer> colWmc;
    @FXML private TableColumn<ClassMetrics, Integer> colDit;
    @FXML private TableColumn<ClassMetrics, Integer> colCbo;

    // --- NOVAS COLUNAS ---
    @FXML private TableColumn<ClassMetrics, Integer> colFields; // Atributos
    @FXML private TableColumn<ClassMetrics, Integer> colNoc;    // NOC

    // Gráficos
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;

    @FXML
    public void initialize() {
        // Ligar as colunas aos getters da classe ClassMetrics
        colPackage.setCellValueFactory(new PropertyValueFactory<>("packageName"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colLoc.setCellValueFactory(new PropertyValueFactory<>("loc"));
        colMethods.setCellValueFactory(new PropertyValueFactory<>("methodCount"));
        colWmc.setCellValueFactory(new PropertyValueFactory<>("wmc"));
        colDit.setCellValueFactory(new PropertyValueFactory<>("dit"));
        colCbo.setCellValueFactory(new PropertyValueFactory<>("cbo"));

        // Novas métricas
        colFields.setCellValueFactory(new PropertyValueFactory<>("fieldCount"));
        colNoc.setCellValueFactory(new PropertyValueFactory<>("noc"));
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

            // 1. Analisar cada ficheiro individualmente
            for (File file : javaFiles) {
                try {
                    String sourceCode = Files.readString(file.toPath());
                    // Analisa e cria o objeto ClassMetrics (o NOC vem a 0 aqui)
                    ClassMetrics metrics = JdtAnalyzer.analyze(sourceCode, file.getName(), selectedDirectory.getAbsolutePath());
                    data.add(metrics);
                } catch (Exception e) {
                    System.err.println("Erro ao ler ficheiro: " + file.getName());
                }
            }

            // 2. Calcular o NOC (Agora que temos a lista completa de classes)
            calculateNOC(data);

            // 3. Mostrar na Tabela e Gráficos
            tblMetrics.setItems(data);
            updateCharts(data);

            lblStatus.setText("Concluído. " + data.size() + " classes analisadas.");
        }
    }


    // --- Lógica de Cálculo do NOC  ---
    private void calculateNOC(List<ClassMetrics> allMetrics) {
        // Mapa: Nome Completo do Pai -> Quantidade de Filhos
        Map<String, Integer> childrenCount = new HashMap<>();

        // Passo A: Contar quantas vezes cada classe aparece como "Pai"
        for (ClassMetrics m : allMetrics) {
            String father = m.getSuperClassName();
            // Ignorar se não tiver pai ou se for herança básica do Java
            if (father != null && !father.equals("java.lang.Object")) {
                childrenCount.merge(father, 1, Integer::sum);
            }
        }

        // Passo B: Distribuir os contadores pelas classes corretas
        for (ClassMetrics m : allMetrics) {
            // CORREÇÃO: Usar o nome QUALIFICADO (com pacote) para bater certo com o nome do pai
            String myFullName = m.getQualifiedName();

            // Agora a chave do mapa (ex: pt.iscte.App) vai bater certo com o meu nome
            int myNoc = childrenCount.getOrDefault(myFullName, 0);
            m.setNoc(myNoc);
        }
    }

    private void updateCharts(ObservableList<ClassMetrics> data) {
        // --- Bar Chart: Top 5 Classes mais Complexas ---
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        List<ClassMetrics> top5 = data.stream()
                .sorted(Comparator.comparingInt(ClassMetrics::getWmc).reversed())
                .limit(5)
                .collect(Collectors.toList());

        for (ClassMetrics m : top5) {
            series.getData().add(new XYChart.Data<>(m.getClassName(), m.getWmc()));
        }
        barChart.getData().add(series);

        // --- Pie Chart: Complexidade ---
        long complexas = data.stream().filter(m -> m.getWmc() >= 5).count();
        long simples = data.stream().filter(m -> m.getWmc() < 5).count();

        pieChart.getData().clear();
        pieChart.getData().add(new PieChart.Data("Complexas (>5)", complexas));
        pieChart.getData().add(new PieChart.Data("Simples (<5)", simples));
    }

    @FXML
    public void handleExport() {
        if (tblMetrics.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aviso", "Sem dados", "Primeiro tens de analisar um projeto!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Relatório CSV");
        fileChooser.setInitialFileName("metricas_codigo.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(lblStatus.getScene().getWindow());

        if (file != null) {
            try {
                CsvExporter.exportToCSV(tblMetrics.getItems(), file);
                lblStatus.setText("Sucesso! Guardado em: " + file.getName());
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Exportação Concluída", "Ficheiro CSV criado.");
            } catch (Exception e) {
                lblStatus.setText("Erro ao exportar.");
                e.printStackTrace();
            }
        }
    }

    // Helper para mostrar alertas
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}