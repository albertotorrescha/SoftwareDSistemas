package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción de lectura.
 * Sintaxis: ? identificador
 */
public class NodoLeer extends NodoAST {
    private String identificador;

    public NodoLeer(String identificador) {
        this.identificador = identificador;
    }

    public String getIdentificador() { return identificador; }

    @Override
    public String toString() {
        return "Leer(" + identificador + ")";
    }
}
