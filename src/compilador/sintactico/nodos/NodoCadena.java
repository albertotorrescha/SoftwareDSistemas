package compilador.sintactico.nodos;

/**
 * Nodo para literales de cadena.
 * Ejemplo: "hola mundo", "texto"
 */
public class NodoCadena extends NodoAST {
    private String valor;

    public NodoCadena(String valor) {
        this.valor = valor;
    }

    public String getValor() { return valor; }

    @Override
    public String toString() {
        return "Cadena(" + valor + ")";
    }
}
