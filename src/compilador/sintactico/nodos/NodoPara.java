package compilador.sintactico.nodos;

import java.util.List;

/**
 * Nodo para el ciclo para (for).
 *
 * Sintaxis:
 *   para var i <- inicio hacer i <= fin
 *       instrucciones
 *   fin
 */
public class NodoPara extends NodoAST {
    private NodoAsignacion inicializacion; // var i <- valor_inicio
    private NodoAST        condicion;      // i <= fin
    private List<NodoAST>  cuerpo;

    public NodoPara(NodoAsignacion inicializacion,
                    NodoAST condicion,
                    List<NodoAST> cuerpo) {
        this.inicializacion = inicializacion;
        this.condicion      = condicion;
        this.cuerpo         = cuerpo;
    }

    public NodoAsignacion getInicializacion() { return inicializacion; }
    public NodoAST getCondicion()             { return condicion; }
    public List<NodoAST> getCuerpo()          { return cuerpo; }

    @Override
    public String toString() {
        return "Para(" + inicializacion + ", mientras " + condicion
               + ", " + cuerpo.size() + " instrucciones)";
    }
}
