package compilador.sintactico.nodos;

/**
 * Nodo para declaración y asignación de variables.
 * Sintaxis: var nombre <- expresion
 *           const nombre <- expresion
 *           nombre <- expresion   (reasignación)
 *           nombre -> expresion   (asignación inversa)
 */
public class NodoAsignacion extends NodoAST {
    private String nombre;
    private NodoAST valor;
    private boolean esConstante;   // true si fue declarada con 'const'
    private boolean esDeclaracion; // true si usa var/const (primera vez)

    public NodoAsignacion(String nombre, NodoAST valor,
                          boolean esConstante, boolean esDeclaracion) {
        this.nombre        = nombre;
        this.valor         = valor;
        this.esConstante   = esConstante;
        this.esDeclaracion = esDeclaracion;
    }

    public String getNombre()       { return nombre; }
    public NodoAST getValor()       { return valor; }
    public boolean isConstante()    { return esConstante; }
    public boolean isDeclaracion()  { return esDeclaracion; }

    @Override
    public String toString() {
        String prefijo = esDeclaracion ? (esConstante ? "const " : "var ") : "";
        return "Asig(" + prefijo + nombre + " <- " + valor + ")";
    }
}
