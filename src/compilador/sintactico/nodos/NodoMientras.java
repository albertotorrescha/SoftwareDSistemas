package compilador.sintactico.nodos;

import java.util.List;

/**
 * Nodo para el ciclo mientras (while).
 *
 * Sintaxis:
 *   mientras condicion
 *       instrucciones
 *   fin
 */
public class NodoMientras extends NodoAST {
    private NodoAST condicion;
    private List<NodoAST> cuerpo;

    public NodoMientras(NodoAST condicion, List<NodoAST> cuerpo) {
        this.condicion = condicion;
        this.cuerpo    = cuerpo;
    }

    public NodoAST getCondicion()    { return condicion; }
    public List<NodoAST> getCuerpo() { return cuerpo; }

    @Override
    public String toString() {
        return "Mientras(" + condicion + ", " + cuerpo.size() + " instrucciones)";
    }
}
