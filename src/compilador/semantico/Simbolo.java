package compilador.semantico;

public class Simbolo {
    private final String nombre;
    private final CategoriaSimbolo categoria;
    private final int linea;
    private final String ambito;
    private final int numeroParametros;
    private TipoDato tipo;

    public Simbolo(String nombre, CategoriaSimbolo categoria, TipoDato tipo,
                   int linea, String ambito) {
        this(nombre, categoria, tipo, linea, ambito, -1);
    }

    public Simbolo(String nombre, CategoriaSimbolo categoria, TipoDato tipo,
                   int linea, String ambito, int numeroParametros) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.tipo = tipo;
        this.linea = linea;
        this.ambito = ambito;
        this.numeroParametros = numeroParametros;
    }

    public String getNombre() { return nombre; }
    public CategoriaSimbolo getCategoria() { return categoria; }
    public TipoDato getTipo() { return tipo; }
    public void setTipo(TipoDato tipo) { this.tipo = tipo; }
    public int getLinea() { return linea; }
    public String getAmbito() { return ambito; }
    public int getNumeroParametros() { return numeroParametros; }
    public boolean esConstante() { return categoria == CategoriaSimbolo.CONSTANTE; }
}
