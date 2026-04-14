package compilador.sintactico.nodos;

/**
 * Nodo para operaciones binarias aritméticas y relacionales.
 * Ejemplo: a + b, x > 10, y == z
 * Equivalente a ExpBin del código C del profesor.
 */
public class NodoBinario extends NodoAST {
    private String operador;
    private NodoAST izquierda;
    private NodoAST derecha;

    public NodoBinario(String operador, NodoAST izquierda, NodoAST derecha) {
        this.operador  = operador;
        this.izquierda = izquierda;
        this.derecha   = derecha;
    }

    public String getOperador()    { return operador; }
    public NodoAST getIzquierda()  { return izquierda; }
    public NodoAST getDerecha()    { return derecha; }

    @Override
    public String toString() {
        return "Bin(" + operador + ", " + izquierda + ", " + derecha + ")";
    }
}
