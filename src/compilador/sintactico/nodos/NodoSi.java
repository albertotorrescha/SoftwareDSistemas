package compilador.sintactico.nodos;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo para la estructura condicional si/sino_si/sino.
 *
 * Sintaxis:
 *   si condicion
 *       instrucciones
 *   sino_si condicion
 *       instrucciones
 *   sino
 *       instrucciones
 *   fin
 *
 */
public class NodoSi extends NodoAST {
    private NodoAST condicion;
    private List<NodoAST> entonces;

    // Lista de pares (condicion, cuerpo) para los sino_si
    private List<NodoAST>         condicionesSinoSi;
    private List<List<NodoAST>>   cuerposSinoSi;

    private List<NodoAST> sino; // puede ser null si no hay sino

    public NodoSi(NodoAST condicion,
                  List<NodoAST> entonces,
                  List<NodoAST> condicionesSinoSi,
                  List<List<NodoAST>> cuerposSinoSi,
                  List<NodoAST> sino) {
        this.condicion         = condicion;
        this.entonces          = entonces;
        this.condicionesSinoSi = condicionesSinoSi;
        this.cuerposSinoSi     = cuerposSinoSi;
        this.sino              = sino;
    }

    public NodoAST getCondicion()                      { return condicion; }
    public List<NodoAST> getEntonces()                 { return entonces; }
    public List<NodoAST> getCondicionesSinoSi()        { return condicionesSinoSi; }
    public List<List<NodoAST>> getCuerposSinoSi()      { return cuerposSinoSi; }
    public List<NodoAST> getSino()                     { return sino; }

    @Override
    public String toString() {
        return "Si(" + condicion + ", entonces=" + entonces.size()
               + " instrucciones, sino=" + (sino != null ? sino.size() : 0) + ")";
    }
}
