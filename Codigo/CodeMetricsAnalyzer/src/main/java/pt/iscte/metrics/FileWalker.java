package pt.iscte.metrics.infrastructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileWalker {

    // Método recursivo que encontra todos os .java dentro de uma pasta
    public List<File> findAllJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();

        if (directory == null || !directory.exists()) {
            return javaFiles;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Se for pasta, entra nela (recursão)
                    javaFiles.addAll(findAllJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    // Se for ficheiro Java, adiciona à lista
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }
}