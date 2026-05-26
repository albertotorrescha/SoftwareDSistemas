package compilador.sintactico.nodos;

/**
 * Nodo para el valor nulo.
 * Palabra reservada: nulo
 */
public class NodoNulo extends NodoAST {
    public NodoNulo() {}

    @Override
    public String toString() { return "Nulo()"; }
}
