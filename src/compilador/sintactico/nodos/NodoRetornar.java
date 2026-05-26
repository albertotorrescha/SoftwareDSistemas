package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción retornar.
 * Sintaxis: retornar expresion
 *           retornar nulo
 */
public class NodoRetornar extends NodoAST {
    private NodoAST valor; // puede ser null si retorna nulo

    public NodoRetornar(NodoAST valor) {
        this.valor = valor;
    }

    public NodoAST getValor() { return valor; }

    @Override
    public String toString() {
        return "Retornar(" + (valor != null ? valor : "nulo") + ")";
    }
}
