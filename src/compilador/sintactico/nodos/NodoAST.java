package compilador.sintactico.nodos;

/**
 * Clase base abstracta para todos los nodos del AST.
 * Equivalente al 'struct Exp' del código C del profesor.
 */
public abstract class NodoAST {
    private int linea;

    public int getLinea() { return linea; }
    public void setLinea(int linea) { this.linea = linea; }
}
