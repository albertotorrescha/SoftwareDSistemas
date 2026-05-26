package compilador.sintactico.nodos;

/**
 * Nodo para literales booleanos.
 * Palabras reservadas: verdadero, falso
 */
public class NodoBooleano extends NodoAST {
    private boolean valor;

    public NodoBooleano(boolean valor) {
        this.valor = valor;
    }

    public boolean getValor() { return valor; }

    @Override
    public String toString() {
        return "Bool(" + (valor ? "verdadero" : "falso") + ")";
    }
}
