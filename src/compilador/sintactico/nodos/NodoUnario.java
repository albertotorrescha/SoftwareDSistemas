package compilador.sintactico.nodos;

/**
 * Nodo para operaciones unarias.
 * Ejemplo: -x  (negación aritmética)
 */
public class NodoUnario extends NodoAST {
    private String operador;
    private NodoAST operando;

    public NodoUnario(String operador, NodoAST operando) {
        this.operador = operador;
        this.operando = operando;
    }

    public String getOperador()  { return operador; }
    public NodoAST getOperando() { return operando; }

    @Override
    public String toString() {
        return "Una(" + operador + ", " + operando + ")";
    }
}
