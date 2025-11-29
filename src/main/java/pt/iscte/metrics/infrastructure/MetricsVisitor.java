package pt.iscte.metrics.infrastructure;

import org.eclipse.jdt.core.dom.*;
import java.util.HashSet;
import java.util.Set;

public class MetricsVisitor extends ASTVisitor {

    private String packageName = "default";
    private String className = "Anónima";
    private int methodCount = 0;
    private int loc = 0;
    private int wmc = 0;
    private int dit = 0;

    // Conjunto para guardar nomes de classes únicas (para o CBO)
    private Set<String> coupledClasses = new HashSet<>();

    // 1. Apanhar o Pacote
    @Override
    public boolean visit(PackageDeclaration node) {
        this.packageName = node.getName().getFullyQualifiedName();
        return true;
    }

    // 2. Apanhar a Classe (Nome, LOC e DIT)
    @Override
    public boolean visit(TypeDeclaration node) {
        this.className = node.getName().getIdentifier();

        // --- CÁLCULO DO DIT (Profundidade de Herança) ---
        ITypeBinding binding = node.resolveBinding();
        if (binding != null) {
            ITypeBinding current = binding.getSuperclass();
            while (current != null) {
                if (!current.getQualifiedName().equals("java.lang.Object")) {
                    dit++;
                }
                current = current.getSuperclass();
            }
            dit++; // Conta o próprio nível
        }

        // --- CÁLCULO DO LOC ---
        CompilationUnit root = (CompilationUnit) node.getRoot();
        int startLine = root.getLineNumber(node.getStartPosition());
        int endLine = root.getLineNumber(node.getStartPosition() + node.getLength() - 1);
        this.loc = (endLine - startLine) + 1;

        return true;
    }

    // 3. Apanhar Métodos (Contagem, WMC e CBO)
    @Override
    public boolean visit(MethodDeclaration node) {
        methodCount++;
        wmc++; // Complexidade base do método é 1

        // --- CBO: Verificar Tipo de Retorno ---
        addCoupling(node.getReturnType2());

        // --- CBO: Verificar Argumentos ---
        for (Object param : node.parameters()) {
            if (param instanceof SingleVariableDeclaration) {
                addCoupling(((SingleVariableDeclaration) param).getType());
            }
        }
        return true;
    }

    // 4. Apanhar Atributos (CBO)
    @Override
    public boolean visit(FieldDeclaration node) {
        addCoupling(node.getType());
        return true;
    }

    // --- Helper para CBO (Acoplamento) ---
    private void addCoupling(Type type) {
        if (type == null || type.isPrimitiveType()) return;

        if (type.isArrayType()) {
            addCoupling(((ArrayType) type).getElementType());
            return;
        }

        ITypeBinding binding = type.resolveBinding();
        String typeName = (binding != null) ? binding.getQualifiedName() : type.toString();

        // Ignora classes do Java (ex: String, List) para contar só as nossas
        if (!typeName.startsWith("java.lang") && !typeName.equals("void")) {
            coupledClasses.add(typeName);
        }
    }

    // --- Complexidade de McCabe (WMC) ---
    @Override public boolean visit(IfStatement node) { wmc++; return true; }
    @Override public boolean visit(ForStatement node) { wmc++; return true; }
    @Override public boolean visit(EnhancedForStatement node) { wmc++; return true; }
    @Override public boolean visit(WhileStatement node) { wmc++; return true; }
    @Override public boolean visit(DoStatement node) { wmc++; return true; }
    @Override public boolean visit(SwitchCase node) { if(!node.isDefault()) wmc++; return true; }
    @Override public boolean visit(CatchClause node) { wmc++; return true; }
    @Override public boolean visit(ConditionalExpression node) { wmc++; return true; }

    // --- Getters ---
    public String getPackageName() { return packageName; }
    public String getClassName() { return className; }
    public int getMethodCount() { return methodCount; }
    public int getLoc() { return loc; }
    public int getWmc() { return wmc; }
    public int getDit() { return dit; }

    public int getCbo() {
        // Remove a própria classe e pacote da contagem
        coupledClasses.remove(this.className);
        coupledClasses.remove(this.packageName + "." + this.className);
        return coupledClasses.size();
    }
}