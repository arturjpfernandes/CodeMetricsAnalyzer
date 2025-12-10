package pt.iscte.metrics.infrastructure;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import pt.iscte.metrics.domain.ClassMetrics;
import java.util.Map;

public class JdtAnalyzer {

    public static ClassMetrics analyze(String sourceCode, String filename, String projectPath) {
        // 1. Criar o Parser
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(sourceCode.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        // 2. Configurar Bindings (Essencial para CBO e DIT)
        // Sem isto, o JDT não sabe quem é "pai" de quem, nem que tipos são usados.
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setUnitName(filename);

        // 3. Configurar Ambiente (Classpath e Sourcepath)
        // Dizemos ao parser onde procurar as outras classes do projeto
        String[] classpathEntries = { System.getProperty("java.class.path") };
        String[] sourcepathEntries = { projectPath };
        String[] encodings = { "UTF-8" };

        parser.setEnvironment(classpathEntries, sourcepathEntries, encodings, true);

        // 4. Opções do Compilador (Garantir compatibilidade com Java 17)
        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_17, options);
        parser.setCompilerOptions(options);

        // 5. Criar a Árvore AST
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        // 6. Correr o Visitor (O nosso "inspetor")
        MetricsVisitor visitor = new MetricsVisitor();
        cu.accept(visitor);

        // 7. Retornar os Resultados
        // ATENÇÃO: A ordem aqui tem de ser IGUAL ao construtor do ClassMetrics
        // No final do JdtAnalyzer.analyze(...)
        return new ClassMetrics(
                visitor.getPackageName(),
                visitor.getClassName(),
                visitor.getSuperClassName(), // Confirma se estás a passar isto!
                visitor.getLoc(),
                visitor.getWmc(),
                visitor.getMethodCount(),
                visitor.getFieldCount(),     // <--- Confirma se estás a passar isto!
                visitor.getDit(),
                visitor.getCbo(),
                0, // NOC (Sempre 0 aqui)
                0, // LCOM
                0, // FanIn
                0  // FanOut
        );
    }
}