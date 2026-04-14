package compilador.sintactico.nodos;

/**
 * Nodo para la instrucción de impresión.
 * Sintaxis: ! expresion
 * Equivalente a ExpEscribir del código C del profesor.
 */
public class NodoImprimir extends NodoAST {
    private NodoAST valor;

    public NodoImprimir(NodoAST valor) {
        this.valor = valor;
    }

    public NodoAST getValor() { return valor; }

    @Override
    public String toString() {
        return "Imprimir(" + valor + ")";
    }
}
