package pt.iscte.metrics.infrastructure;

import pt.iscte.metrics.domain.ClassMetrics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    public static void exportToCSV(List<ClassMetrics> metrics, File destinationFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFile))) {
            // 1. Escrever o Cabeçalho
            writer.write("Pacote;Classe;LOC;Metodos;WMC;DIT;CBO"); // Adicionei DIT e CBO
            writer.newLine();

            for (ClassMetrics m : metrics) {
                // 2. Atualiza a linha de dados
                String line = String.format("%s;%s;%d;%d;%d;%d;%d",
                        m.getPackageName(),
                        m.getClassName(),
                        m.getLoc(),
                        m.getMethodCount(),
                        m.getWmc(),
                        m.getDit(), // <-- Adicionado
                        m.getCbo()  // <-- Adicionado
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }
}