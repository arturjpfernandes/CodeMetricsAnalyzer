package pt.iscte.metrics.domain;

public class ClassMetrics {
    // Identificação
    private String className;
    private String packageName;
    private String superClassName; // Necessário para calcular o NOC

    // Métricas de Contagem
    private int loc;
    private int methodCount;
    private int fieldCount; // Atributos da classe

    // Métricas de Complexidade e Qualidade
    private int wmc;
    private int dit;
    private int cbo;
    private int noc;
    private int lcom;
    private int fanIn;
    private int fanOut;

    // Construtor Completo
    // A ordem dos argumentos aqui tem de bater certo com o "return" no JdtAnalyzer!
    public ClassMetrics(String packageName, String className, String superClassName,
                        int loc, int wmc, int methodCount, int fieldCount,
                        int dit, int cbo, int noc, int lcom, int fanIn, int fanOut) {

        this.packageName = packageName;
        this.className = className;
        this.superClassName = superClassName;

        this.loc = loc;
        this.wmc = wmc;
        this.methodCount = methodCount;
        this.fieldCount = fieldCount;

        this.dit = dit;
        this.cbo = cbo;
        this.noc = noc; // Geralmente vem a 0 do JdtAnalyzer e é calculado depois no Controller
        this.lcom = lcom;
        this.fanIn = fanIn;
        this.fanOut = fanOut;
    }

    // --- Getters ---

    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getSuperClassName() { return superClassName; }
    public String getQualifiedName() {
        return (packageName == null || packageName.isEmpty() || packageName.equals("default"))
                ? className
                : packageName + "." + className;
    }

    public int getLoc() { return loc; }
    public int getWmc() { return wmc; }
    public int getMethodCount() { return methodCount; }
    public int getFieldCount() { return fieldCount; }

    public int getDit() { return dit; }
    public int getCbo() { return cbo; }
    public int getNoc() { return noc; }
    public int getLcom() { return lcom; }
    public int getFanIn() { return fanIn; }
    public int getFanOut() { return fanOut; }

    // --- Setters (Para pós-processamento) ---

    // O NOC é calculado no Controller depois da leitura, por isso precisa de Setter
    public void setNoc(int noc) {
        this.noc = noc;
    }

    @Override
    public String toString() {
        return String.format("%s [LOC:%d, WMC:%d, NOC:%d, CBO:%d]", className, loc, wmc, noc, cbo);
    }
}