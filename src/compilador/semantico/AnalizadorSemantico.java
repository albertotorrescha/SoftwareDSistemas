package compilador.semantico;

import compilador.sintactico.nodos.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el AST, construye la tabla de simbolos y verifica reglas semanticas.
 */
public class AnalizadorSemantico {
    private TablaSimbolos tabla;
    private final List<ErrorSemantico> errores = new ArrayList<>();
    private int nivelCiclo;
    private Simbolo funcionActual;

    public void analizar(List<NodoAST> programa) {
        tabla = new TablaSimbolos();
        errores.clear();
        nivelCiclo = 0;
        funcionActual = null;

        registrarFunciones(programa);

        // Las globales se procesan antes que los cuerpos de funcion.
        for (NodoAST nodo : programa) {
            if (!(nodo instanceof NodoFuncion)) {
                analizarNodo(nodo);
            }
        }
        for (NodoAST nodo : programa) {
            if (nodo instanceof NodoFuncion) {
                analizarFuncion((NodoFuncion) nodo);
            }
        }
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    public List<ErrorSemantico> getErrores() {
        return new ArrayList<>(errores);
    }

    public TablaSimbolos getTablaSimbolos() {
        return tabla;
    }

    private void registrarFunciones(List<NodoAST> programa) {
        for (NodoAST nodo : programa) {
            if (nodo instanceof NodoFuncion) {
                NodoFuncion funcion = (NodoFuncion) nodo;
                Simbolo simbolo = new Simbolo(funcion.getNombre(), CategoriaSimbolo.FUNCION,
                        TipoDato.DESCONOCIDO, funcion.getLinea(), tabla.getAmbitoActual(),
                        funcion.getParametros().size());
                if (!tabla.declarar(simbolo)) {
                    error(funcion, "el identificador '" + funcion.getNombre()
                            + "' ya fue declarado en este ambito.");
                }
            }
        }
    }

    private TipoDato analizarNodo(NodoAST nodo) {
        if (nodo == null) {
            return TipoDato.NULO;
        }
        if (nodo instanceof NodoNum) return TipoDato.ENTERO;
        if (nodo instanceof NodoDecimal) return TipoDato.DECIMAL;
        if (nodo instanceof NodoCadena) return TipoDato.CADENA;
        if (nodo instanceof NodoBooleano) return TipoDato.BOOLEANO;
        if (nodo instanceof NodoNulo) return TipoDato.NULO;
        if (nodo instanceof NodoId) return analizarIdentificador((NodoId) nodo);
        if (nodo instanceof NodoAsignacion) return analizarAsignacion((NodoAsignacion) nodo);
        if (nodo instanceof NodoBinario) return analizarBinario((NodoBinario) nodo);
        if (nodo instanceof NodoUnario) return analizarUnario((NodoUnario) nodo);
        if (nodo instanceof NodoImprimir) {
            analizarNodo(((NodoImprimir) nodo).getValor());
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoLeer) {
            analizarLectura((NodoLeer) nodo);
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoLimpiar) return TipoDato.VACIO;
        if (nodo instanceof NodoSi) {
            analizarSi((NodoSi) nodo);
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoMientras) {
            analizarMientras((NodoMientras) nodo);
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoPara) {
            analizarPara((NodoPara) nodo);
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoLlamada) return analizarLlamada((NodoLlamada) nodo);
        if (nodo instanceof NodoRetornar) {
            analizarRetorno((NodoRetornar) nodo);
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoRomper) {
            if (nivelCiclo == 0) {
                error(nodo, "'romper' solo puede usarse dentro de un ciclo.");
            }
            return TipoDato.VACIO;
        }
        if (nodo instanceof NodoContinuar) {
            if (nivelCiclo == 0) {
                error(nodo, "'continuar' solo puede usarse dentro de un ciclo.");
            }
            return TipoDato.VACIO;
        }
        return TipoDato.DESCONOCIDO;
    }

    private TipoDato analizarIdentificador(NodoId nodo) {
        Simbolo simbolo = tabla.buscar(nodo.getNombre());
        if (simbolo == null || simbolo.getCategoria() == CategoriaSimbolo.FUNCION) {
            error(nodo, "la variable '" + nodo.getNombre() + "' no ha sido declarada.");
            return TipoDato.DESCONOCIDO;
        }
        return simbolo.getTipo();
    }

    private TipoDato analizarAsignacion(NodoAsignacion nodo) {
        TipoDato tipoValor = analizarNodo(nodo.getValor());
        if (nodo.isDeclaracion()) {
            CategoriaSimbolo categoria = nodo.isConstante()
                    ? CategoriaSimbolo.CONSTANTE : CategoriaSimbolo.VARIABLE;
            Simbolo nuevo = new Simbolo(nodo.getNombre(), categoria, tipoValor,
                    nodo.getLinea(), tabla.getAmbitoActual());
            if (!tabla.declarar(nuevo)) {
                error(nodo, "el identificador '" + nodo.getNombre()
                        + "' ya fue declarado en este ambito.");
            }
            return tipoValor;
        }

        Simbolo existente = tabla.buscar(nodo.getNombre());
        if (existente == null || existente.getCategoria() == CategoriaSimbolo.FUNCION) {
            error(nodo, "no se puede asignar a '" + nodo.getNombre()
                    + "' porque no ha sido declarado como variable.");
            return tipoValor;
        }
        if (existente.esConstante()) {
            error(nodo, "no se puede modificar la constante '" + nodo.getNombre() + "'.");
            return tipoValor;
        }
        if (!sonAsignables(existente.getTipo(), tipoValor)) {
            error(nodo, "no se puede asignar un valor " + tipoValor + " a '"
                    + nodo.getNombre() + "' de tipo " + existente.getTipo() + ".");
        } else if ((existente.getTipo() == TipoDato.NULO
                || existente.getTipo() == TipoDato.DESCONOCIDO)
                && tipoValor != TipoDato.NULO) {
            existente.setTipo(tipoValor);
        }
        return existente.getTipo();
    }

    private TipoDato analizarBinario(NodoBinario nodo) {
        TipoDato izquierda = analizarNodo(nodo.getIzquierda());
        TipoDato derecha = analizarNodo(nodo.getDerecha());
        String operador = nodo.getOperador();

        if ("+".equals(operador) || "-".equals(operador)
                || "*".equals(operador) || "/".equals(operador)) {
            if (esIndeterminado(izquierda) || esIndeterminado(derecha)) {
                return TipoDato.DESCONOCIDO;
            }
            if (!izquierda.esNumerico() || !derecha.esNumerico()) {
                error(nodo, "el operador '" + operador
                        + "' requiere operandos numericos, pero recibio "
                        + izquierda + " y " + derecha + ".");
                return TipoDato.DESCONOCIDO;
            }
            return "/".equals(operador) || izquierda == TipoDato.DECIMAL
                    || derecha == TipoDato.DECIMAL ? TipoDato.DECIMAL : TipoDato.ENTERO;
        }

        if ("<".equals(operador) || ">".equals(operador)
                || "<=".equals(operador) || ">=".equals(operador)) {
            if (!esIndeterminado(izquierda) && !esIndeterminado(derecha)
                    && (!izquierda.esNumerico() || !derecha.esNumerico())) {
                error(nodo, "la comparacion '" + operador
                        + "' requiere operandos numericos.");
            }
            return TipoDato.BOOLEANO;
        }

        if ("==".equals(operador) || "!=".equals(operador)) {
            if (!sonComparables(izquierda, derecha)) {
                error(nodo, "no se pueden comparar valores " + izquierda
                        + " y " + derecha + ".");
            }
            return TipoDato.BOOLEANO;
        }
        return TipoDato.DESCONOCIDO;
    }

    private TipoDato analizarUnario(NodoUnario nodo) {
        TipoDato tipo = analizarNodo(nodo.getOperando());
        if (!esIndeterminado(tipo) && !tipo.esNumerico()) {
            error(nodo, "el operador '" + nodo.getOperador()
                    + "' requiere un operando numerico.");
            return TipoDato.DESCONOCIDO;
        }
        return tipo;
    }

    private void analizarLectura(NodoLeer nodo) {
        Simbolo simbolo = tabla.buscar(nodo.getIdentificador());
        if (simbolo == null || simbolo.getCategoria() == CategoriaSimbolo.FUNCION) {
            error(nodo, "no se puede leer en '" + nodo.getIdentificador()
                    + "' porque no es una variable declarada.");
        } else if (simbolo.esConstante()) {
            error(nodo, "no se puede leer un valor en la constante '"
                    + nodo.getIdentificador() + "'.");
        }
    }

    private void analizarSi(NodoSi nodo) {
        verificarCondicion(nodo.getCondicion(), nodo);
        analizarBloque(nodo.getEntonces(), "si");
        List<NodoAST> condiciones = nodo.getCondicionesSinoSi();
        List<List<NodoAST>> cuerpos = nodo.getCuerposSinoSi();
        for (int i = 0; i < condiciones.size(); i++) {
            verificarCondicion(condiciones.get(i), nodo);
            analizarBloque(cuerpos.get(i), "sino_si");
        }
        if (nodo.getSino() != null) {
            analizarBloque(nodo.getSino(), "sino");
        }
    }

    private void analizarMientras(NodoMientras nodo) {
        verificarCondicion(nodo.getCondicion(), nodo);
        nivelCiclo++;
        analizarBloque(nodo.getCuerpo(), "mientras");
        nivelCiclo--;
    }

    private void analizarPara(NodoPara nodo) {
        tabla.abrirAmbito("para");
        analizarNodo(nodo.getInicializacion());
        verificarCondicion(nodo.getCondicion(), nodo);
        nivelCiclo++;
        analizarLista(nodo.getCuerpo());
        nivelCiclo--;
        tabla.cerrarAmbito();
    }

    private void verificarCondicion(NodoAST condicion, NodoAST sentencia) {
        TipoDato tipo = analizarNodo(condicion);
        if (!esIndeterminado(tipo) && tipo != TipoDato.BOOLEANO) {
            error(sentencia, "la condicion debe ser BOOLEANO y se obtuvo " + tipo + ".");
        }
    }

    private void analizarFuncion(NodoFuncion nodo) {
        Simbolo anterior = funcionActual;
        funcionActual = tabla.buscar(nodo.getNombre());
        tabla.abrirAmbito("funcion " + nodo.getNombre());
        for (String parametro : nodo.getParametros()) {
            Simbolo simbolo = new Simbolo(parametro, CategoriaSimbolo.PARAMETRO,
                    TipoDato.DESCONOCIDO, nodo.getLinea(), tabla.getAmbitoActual());
            if (!tabla.declarar(simbolo)) {
                error(nodo, "el parametro '" + parametro + "' esta repetido.");
            }
        }
        analizarLista(nodo.getCuerpo());
        tabla.cerrarAmbito();
        funcionActual = anterior;
    }

    private TipoDato analizarLlamada(NodoLlamada nodo) {
        Simbolo funcion = tabla.buscar(nodo.getNombre());
        for (NodoAST argumento : nodo.getArgumentos()) {
            analizarNodo(argumento);
        }
        if (funcion == null || funcion.getCategoria() != CategoriaSimbolo.FUNCION) {
            error(nodo, "la funcion '" + nodo.getNombre() + "' no ha sido declarada.");
            return TipoDato.DESCONOCIDO;
        }
        if (funcion.getNumeroParametros() != nodo.getArgumentos().size()) {
            error(nodo, "la funcion '" + nodo.getNombre() + "' espera "
                    + funcion.getNumeroParametros() + " argumentos y recibio "
                    + nodo.getArgumentos().size() + ".");
        }
        return funcion.getTipo();
    }

    private void analizarRetorno(NodoRetornar nodo) {
        TipoDato retorno = nodo.getValor() == null
                ? TipoDato.NULO : analizarNodo(nodo.getValor());
        if (funcionActual == null) {
            error(nodo, "'retornar' solo puede usarse dentro de una funcion.");
            return;
        }
        if (funcionActual.getTipo() == TipoDato.DESCONOCIDO) {
            funcionActual.setTipo(retorno);
        } else if (!sonAsignables(funcionActual.getTipo(), retorno)
                && !sonAsignables(retorno, funcionActual.getTipo())) {
            error(nodo, "la funcion '" + funcionActual.getNombre()
                    + "' tiene retornos de tipos incompatibles.");
        } else if (funcionActual.getTipo() == TipoDato.ENTERO
                && retorno == TipoDato.DECIMAL) {
            funcionActual.setTipo(TipoDato.DECIMAL);
        }
    }

    private void analizarBloque(List<NodoAST> nodos, String nombre) {
        tabla.abrirAmbito(nombre);
        analizarLista(nodos);
        tabla.cerrarAmbito();
    }

    private void analizarLista(List<NodoAST> nodos) {
        for (NodoAST nodo : nodos) {
            analizarNodo(nodo);
        }
    }

    private boolean esIndeterminado(TipoDato tipo) {
        return tipo == TipoDato.DESCONOCIDO || tipo == TipoDato.NULO;
    }

    private boolean sonAsignables(TipoDato destino, TipoDato origen) {
        return destino == TipoDato.DESCONOCIDO || origen == TipoDato.DESCONOCIDO
                || destino == TipoDato.NULO || origen == TipoDato.NULO
                || destino == origen
                || (destino == TipoDato.DECIMAL && origen == TipoDato.ENTERO);
    }

    private boolean sonComparables(TipoDato izquierda, TipoDato derecha) {
        return esIndeterminado(izquierda) || esIndeterminado(derecha)
                || izquierda == derecha
                || (izquierda.esNumerico() && derecha.esNumerico());
    }

    private void error(NodoAST nodo, String mensaje) {
        errores.add(new ErrorSemantico(nodo.getLinea(), mensaje));
    }
}
