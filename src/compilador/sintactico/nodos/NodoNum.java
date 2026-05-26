package compilador.sintactico.nodos;

/**
 * Nodo para literales enteros.
 * Ejemplo: 42, -7, 100
 */
public class NodoNum extends NodoAST {
    private int valor;

    public NodoNum(int valor) {
        this.valor = valor;
    }

    public int getValor() { return valor; }

    @Override
    public String toString() {
        return "Num(" + valor + ")";
    }
}
