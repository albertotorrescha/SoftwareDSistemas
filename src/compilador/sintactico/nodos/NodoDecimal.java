package compilador.sintactico.nodos;

/**
 * Nodo para literales decimales.
 * Ejemplo: 3.14, -0.5, 100.0
 */
public class NodoDecimal extends NodoAST {
    private double valor;

    public NodoDecimal(double valor) {
        this.valor = valor;
    }

    public double getValor() { return valor; }

    @Override
    public String toString() {
        return "Decimal(" + valor + ")";
    }
}
