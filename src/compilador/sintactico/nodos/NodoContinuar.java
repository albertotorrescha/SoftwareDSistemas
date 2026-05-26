package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción continuar (continue).
 * Sintaxis: continuar
 */
public class NodoContinuar extends NodoAST {
    public NodoContinuar() {}

    @Override
    public String toString() { return "Continuar()"; }
}
