package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción romper (break).
 * Sintaxis: romper
 */
public class NodoRomper extends NodoAST {
    public NodoRomper() {}

    @Override
    public String toString() { return "Romper()"; }
}
