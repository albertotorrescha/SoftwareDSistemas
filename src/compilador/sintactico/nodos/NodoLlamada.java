package compilador.sintactico.nodos;

import java.util.List;

/**
 * Nodo para llamadas a función.
 *
 * Sintaxis: nombre(arg1, arg2)
 */
public class NodoLlamada extends NodoAST {
    private String nombre;
    private List<NodoAST> argumentos;

    public NodoLlamada(String nombre, List<NodoAST> argumentos) {
        this.nombre     = nombre;
        this.argumentos = argumentos;
    }

    public String getNombre()               { return nombre; }
    public List<NodoAST> getArgumentos()    { return argumentos; }

    @Override
    public String toString() {
        return "Llamada(" + nombre + ", args=" + argumentos.size() + ")";
    }
}
