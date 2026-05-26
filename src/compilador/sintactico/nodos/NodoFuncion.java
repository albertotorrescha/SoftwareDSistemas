package compilador.sintactico.nodos;

import java.util.List;

/**
 * Nodo para la declaración de función.
 *
 * Sintaxis:
 *   funcion nombre(param1, param2)
 *       instrucciones
 *       retornar expresion
 *   fin
 */
public class NodoFuncion extends NodoAST {
    private String nombre;
    private List<String> parametros;
    private List<NodoAST> cuerpo;

    public NodoFuncion(String nombre, List<String> parametros, List<NodoAST> cuerpo) {
        this.nombre     = nombre;
        this.parametros = parametros;
        this.cuerpo     = cuerpo;
    }

    public String getNombre()             { return nombre; }
    public List<String> getParametros()   { return parametros; }
    public List<NodoAST> getCuerpo()      { return cuerpo; }

    @Override
    public String toString() {
        return "Funcion(" + nombre + ", params=" + parametros + ", " + cuerpo.size() + " instrucciones)";
    }
}
