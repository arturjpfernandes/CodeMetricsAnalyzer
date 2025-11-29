package pt.iscte.metrics.domain;

public class ClassMetrics {
    private String className;
    private String packageName;
    private int loc;
    private int wmc;
    private int methodCount;
    private int dit;
    private int cbo; // Variável para o Acoplamento

    // Construtor: Agora aceita TODOS os argumentos, incluindo DIT e CBO
    public ClassMetrics(String packageName, String className, int loc, int wmc, int methodCount, int dit, int cbo) {
        this.packageName = packageName;
        this.className = className;
        this.loc = loc;
        this.wmc = wmc;
        this.methodCount = methodCount;
        this.dit = dit;
        this.cbo = cbo; // Guardamos o valor aqui
    }

    // --- Getters (Necessários para a Tabela ler os valores) ---

    public String getClassName() { return className; }

    public String getPackageName() { return packageName; }

    public int getLoc() { return loc; }

    public int getWmc() { return wmc; }

    public int getMethodCount() { return methodCount; }

    public int getDit() { return dit; }

    public int getCbo() { return cbo; }

    @Override
    public String toString() {
        return className + " (LOC: " + loc + ", WMC: " + wmc + ", DIT: " + dit + ", CBO: " + cbo + ")";
    }
}