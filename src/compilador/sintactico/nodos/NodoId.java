package compilador.sintactico.nodos;

/**
 * Nodo para identificadores (variables).
 * Ejemplo: x, nombre, contador
 */
public class NodoId extends NodoAST {
    private String nombre;

    public NodoId(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    @Override
    public String toString() {
        return "Id(" + nombre + ")";
    }
}
