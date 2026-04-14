package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción de limpiar pantalla.
 * Sintaxis: #
 */
public class NodoLimpiar extends NodoAST {

    public NodoLimpiar() {}

    @Override
    public String toString() {
        return "LimpiarPantalla()";
    }
}
