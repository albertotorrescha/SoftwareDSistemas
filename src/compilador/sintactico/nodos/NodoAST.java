package compilador.sintactico.nodos;

public abstract class NodoAST {
    private int linea;

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }
}
