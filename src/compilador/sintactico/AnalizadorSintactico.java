package compilador.sintactico;

import compilador.lexico.TipoToken;
import compilador.lexico.Token;
import compilador.sintactico.nodos.*;
import java.util.ArrayList;
import java.util.List;

public class AnalizadorSintactico {

    private final List<Token> tokens;
    private int pos;
    private Token tokenActual;

    // Lista de errores recolectados (modo pánico para no parar al primer error)
    private final List<String> errores = new ArrayList<>();

    public AnalizadorSintactico(List<Token> tokens) {
        this.tokens = tokens;
        this.pos    = 0;
        this.tokenActual = tokens.isEmpty() ? null : tokens.get(0);
    }

    // =========================================================
    //  UTILIDADES DE NAVEGACIÓN
    // =========================================================

    /** Avanza al siguiente token y lo retorna. */
    private Token avanzar() {
        Token anterior = tokenActual;
        pos++;
        tokenActual = (pos < tokens.size()) ? tokens.get(pos) : null;
        return anterior;
    }

    /**
     * Consume el token actual si su tipo coincide.
     * Lanza excepción descriptiva si no coincide.
     */
    private Token consumir(TipoToken tipo) {
        if (tokenActual != null && tokenActual.getTipo() == tipo) {
            return avanzar();
        }
        String encontrado = tokenActual != null
            ? "'" + tokenActual.getLexema() + "' (" + tokenActual.getTipo() + ")"
            : "fin de archivo";
        int linea = tokenActual != null ? tokenActual.getLinea() : -1;
        throw new ErrorSintactico(
            "Linea " + linea + ": se esperaba tipo " + tipo + " pero se encontró " + encontrado
        );
    }

    /**
     * Consume el token actual si su lexema coincide exactamente.
     */
    private Token consumirLexema(String lexema) {
        if (tokenActual != null && tokenActual.getLexema().equals(lexema)) {
            return avanzar();
        }
        String encontrado = tokenActual != null
            ? "'" + tokenActual.getLexema() + "'"
            : "fin de archivo";
        int linea = tokenActual != null ? tokenActual.getLinea() : -1;
        throw new ErrorSintactico(
            "Linea " + linea + ": se esperaba '" + lexema + "' pero se encontró " + encontrado
        );
    }

    /** ¿El token actual tiene este tipo? */
    private boolean verificar(TipoToken tipo) {
        return tokenActual != null && tokenActual.getTipo() == tipo;
    }

    /** ¿El token actual tiene exactamente este lexema? */
    private boolean verificarLexema(String lex) {
        return tokenActual != null && tokenActual.getLexema().equals(lex);
    }

    /** ¿Estamos al final de los tokens? */
    private boolean esFin() {
        return tokenActual == null;
    }

    /** ¿El token actual es una palabra reservada que cierra un bloque? */
    private boolean esCierreBLoque() {
        if (tokenActual == null) return true;
        String lex = tokenActual.getLexema();
        return lex.equals("fin")     || lex.equals("@fin")  ||
               lex.equals("sino")    || lex.equals("sino_si");
    }

    // =========================================================
    //  PUNTO DE ENTRADA
    // =========================================================

    /**
     * Inicia el análisis sintáctico.
     * @return Lista de nodos AST que forman el programa.
     */
    public List<NodoAST> parsear() {
        consumirLexema("@inicio");

        List<NodoAST> programa = new ArrayList<>();

        while (!esFin() && !verificarLexema("@fin")) {
            try {
                programa.add(parseDeclaracion());
            } catch (ErrorSintactico e) {
                errores.add(e.getMessage());
                sincronizar(); // recuperación de pánico
            }
        }

        if (!esFin()) {
            consumirLexema("@fin");
        } else {
            errores.add("Se esperaba '@fin' al final del programa.");
        }

        return programa;
    }

    // =========================================================
    //  DECLARACIONES
    // =========================================================

    private NodoAST parseDeclaracion() {

        if (tokenActual == null) return null;

        String lex = tokenActual.getLexema();

        switch (lex) {
            case "var":       return parseVar(false);
            case "const":     return parseConst();
            case "si":        return parseSi();
            case "mientras":  return parseMientras();
            case "para":      return parsePara();
            case "funcion":   return parseFuncion();
            case "retornar":  return parseRetornar();
            case "romper":    avanzar(); return new NodoRomper();
            case "continuar": avanzar(); return new NodoContinuar();
            case "nulo":      avanzar(); return new NodoNulo();
            case "#":         avanzar(); return new NodoLimpiar();
        }

        // Imprimir: ! expresion
        if (verificar(TipoToken.IMPRIMIR)) {
            return parseImprimir();
        }

        // Leer: ? identificador
        if (verificar(TipoToken.LEER)) {
            return parseLeer();
        }

        // Asignación directa: identificador <- expresion
        if (verificar(TipoToken.IDENTIFICADOR) && siguienteEsAsignacion()) {
            return parseAsignacionDirecta();
        }

        // Expresión suelta (llamada a función, operación, etc.)
        return parseExpresion();
    }

    /** Mira si el token DESPUÉS del identificador actual es un operador de asignación. */
    private boolean siguienteEsAsignacion() {
        if (pos + 1 >= tokens.size()) return false;
        String sig = tokens.get(pos + 1).getLexema();
        return sig.equals("=");
    }

    // =========================================================
    //  VAR / CONST
    // =========================================================

    private NodoAST parseVar(boolean esParam) {
        int linea = tokenActual.getLinea();
        consumirLexema("var");
        String nombre = consumir(TipoToken.IDENTIFICADOR).getLexema();
        String op = tokenActual != null ? tokenActual.getLexema() : "";

        if (!op.equals("=")) {
            throw new ErrorSintactico(
                "Linea " + linea + ": se esperaba '=' después de '" + nombre + "'");
        }
        consumir(TipoToken.ASIGNACION);

        NodoAST valor = parseExpresion();
        NodoAsignacion nodo = new NodoAsignacion(nombre, valor, false, true);
        nodo.setLinea(linea);
        return nodo;
    }

    private NodoAST parseConst() {
        int linea = tokenActual.getLinea();
        consumirLexema("const");
        String nombre = consumir(TipoToken.IDENTIFICADOR).getLexema();
        String op = tokenActual != null ? tokenActual.getLexema() : "";

        if (!op.equals("=")) {
            throw new ErrorSintactico(
                "Linea " + linea + ": se esperaba '=' después de '" + nombre + "'");
        }
        consumir(TipoToken.ASIGNACION);

        NodoAST valor = parseExpresion();
        NodoAsignacion nodo = new NodoAsignacion(nombre, valor, true, true);
        nodo.setLinea(linea);
        return nodo;
    }

    private NodoAST parseAsignacionDirecta() {
        int linea = tokenActual.getLinea();
        String nombre = consumir(TipoToken.IDENTIFICADOR).getLexema();
        consumir(TipoToken.ASIGNACION); // =
        NodoAST valor = parseExpresion();
        NodoAsignacion nodo = new NodoAsignacion(nombre, valor, false, false);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  SI / SINO_SI / SINO
    // =========================================================

    private NodoAST parseSi() {
        int linea = tokenActual.getLinea();
        consumirLexema("si");
        NodoAST condicion = parseCondicion();

        // Bloque "entonces"
        List<NodoAST> entonces = parseCuerpo();

        // Bloques "sino_si"
        List<NodoAST>        condicionesSinoSi = new ArrayList<>();
        List<List<NodoAST>>  cuerposSinoSi     = new ArrayList<>();

        while (verificarLexema("sino_si")) {
            consumirLexema("sino_si");
            condicionesSinoSi.add(parseCondicion());
            cuerposSinoSi.add(parseCuerpo());
        }

        // Bloque "sino"
        List<NodoAST> sino = null;
        if (verificarLexema("sino")) {
            consumirLexema("sino");
            sino = parseCuerpo();
        }

        consumirLexema("fin");

        NodoSi nodo = new NodoSi(condicion, entonces, condicionesSinoSi, cuerposSinoSi, sino);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  MIENTRAS
    // =========================================================

    private NodoAST parseMientras() {
        int linea = tokenActual.getLinea();
        consumirLexema("mientras");
        NodoAST condicion = parseCondicion();
        List<NodoAST> cuerpo = parseCuerpo();
        consumirLexema("fin");

        NodoMientras nodo = new NodoMientras(condicion, cuerpo);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  PARA
    //  para var i <- inicio hacer i <= fin  ... fin
    // =========================================================

    private NodoAST parsePara() {
        int linea = tokenActual.getLinea();
        consumirLexema("para");

        // inicialización: var i = expr
        consumirLexema("var");
        String varNombre = consumir(TipoToken.IDENTIFICADOR).getLexema();
        consumir(TipoToken.ASIGNACION); // =
        NodoAST inicio = parseExpresion();
        NodoAsignacion init = new NodoAsignacion(varNombre, inicio, false, true);

        // hacer
        consumirLexema("hacer");

        // condición de parada
        NodoAST condicion = parseCondicion();

        // cuerpo
        List<NodoAST> cuerpo = parseCuerpo();
        consumirLexema("fin");

        NodoPara nodo = new NodoPara(init, condicion, cuerpo);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  FUNCION
    //  funcion nombre(p1, p2) ... fin
    // =========================================================

    private NodoAST parseFuncion() {
        int linea = tokenActual.getLinea();
        consumirLexema("funcion");
        String nombre = consumir(TipoToken.IDENTIFICADOR).getLexema();

        // Parámetros opcionales entre paréntesis
        List<String> parametros = new ArrayList<>();
        if (verificarLexema("(")) {
            consumirLexema("(");
            while (!verificarLexema(")") && !esFin()) {
                parametros.add(consumir(TipoToken.IDENTIFICADOR).getLexema());
                if (verificarLexema(",")) avanzar(); // coma separadora (si la gramática la permite)
            }
            consumirLexema(")");
        }

        List<NodoAST> cuerpo = parseCuerpo();
        consumirLexema("fin");

        NodoFuncion nodo = new NodoFuncion(nombre, parametros, cuerpo);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  RETORNAR
    // =========================================================

    private NodoAST parseRetornar() {
        int linea = tokenActual.getLinea();
        consumirLexema("retornar");

        NodoAST valor = null;
        if (verificarLexema("nulo")) {
            avanzar();
            valor = new NodoNulo();
        } else if (!esCierreBLoque()) {
            valor = parseExpresion();
        }

        NodoRetornar nodo = new NodoRetornar(valor);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  IMPRIMIR / LEER / LIMPIAR
    // =========================================================

    private NodoAST parseImprimir() {
        int linea = tokenActual.getLinea();
        consumir(TipoToken.IMPRIMIR); // !
        NodoAST valor = parseExpresion();
        NodoImprimir nodo = new NodoImprimir(valor);
        nodo.setLinea(linea);
        return nodo;
    }

    private NodoAST parseLeer() {
        int linea = tokenActual.getLinea();
        consumir(TipoToken.LEER); // ?
        String id = consumir(TipoToken.IDENTIFICADOR).getLexema();
        NodoLeer nodo = new NodoLeer(id);
        nodo.setLinea(linea);
        return nodo;
    }

    // =========================================================
    //  CUERPO DE BLOQUE (lista de instrucciones hasta cierre)
    // =========================================================

    private List<NodoAST> parseCuerpo() {
        List<NodoAST> instrucciones = new ArrayList<>();
        while (!esCierreBLoque() && !esFin()) {
            try {
                instrucciones.add(parseDeclaracion());
            } catch (ErrorSintactico e) {
                errores.add(e.getMessage());
                sincronizar();
            }
        }
        return instrucciones;
    }

    // =========================================================
    //  CONDICIÓN  (expresión relacional)
    //  condicion → expresion (op_rel expresion)?
    // =========================================================

    private NodoAST parseCondicion() {
        NodoAST izq = parseExpresion();

        if (verificar(TipoToken.RELACIONAL)) {
            String op = avanzar().getLexema();
            NodoAST der = parseExpresion();
            return new NodoBinario(op, izq, der);
        }

        return izq; // condición simple (ej: variable booleana)
    }

    // =========================================================
    //  EXPRESIONES  (precedencia ascendente)
    // =========================================================

    private NodoAST parseExpresion() {
        return parseSuma();
    }

    /** suma → multiplicacion ( ('+' | '-') multiplicacion )* */
    private NodoAST parseSuma() {
        NodoAST izq = parseMultiplicacion();

        while (verificar(TipoToken.ARITMETICO) &&
               (verificarLexema("+") || verificarLexema("-"))) {
            int linea = tokenActual.getLinea();
            String op = avanzar().getLexema();
            NodoAST der = parseMultiplicacion();
            NodoBinario nodo = new NodoBinario(op, izq, der);
            nodo.setLinea(linea);
            izq = nodo;
        }

        return izq;
    }

    /** multiplicacion → primario ( ('*' | '/') primario )* */
    private NodoAST parseMultiplicacion() {
        NodoAST izq = parsePrimario();

        while (verificar(TipoToken.ARITMETICO) &&
               (verificarLexema("*") || verificarLexema("/"))) {
            int linea = tokenActual.getLinea();
            String op = avanzar().getLexema();
            NodoAST der = parsePrimario();
            NodoBinario nodo = new NodoBinario(op, izq, der);
            nodo.setLinea(linea);
            izq = nodo;
        }

        return izq;
    }

    /**
     * primario → ENTERO | DECIMAL | CADENA
     *           | verdadero | falso | nulo
     *           | IDENTIFICADOR ( '(' args? ')' )?
     *           | '(' expresion ')'
     */
    private NodoAST parsePrimario() {

        if (tokenActual == null) {
            throw new ErrorSintactico("Fin inesperado de archivo en expresión.");
        }

        int linea = tokenActual.getLinea();

        // Literal entero
        if (verificar(TipoToken.ENTERO)) {
            int val = Integer.parseInt(avanzar().getLexema());
            NodoNum nodo = new NodoNum(val);
            nodo.setLinea(linea);
            return nodo;
        }

        // Literal decimal
        if (verificar(TipoToken.DECIMAL)) {
            double val = Double.parseDouble(avanzar().getLexema());
            NodoDecimal nodo = new NodoDecimal(val);
            nodo.setLinea(linea);
            return nodo;
        }

        // Literal cadena
        if (verificar(TipoToken.CADENA)) {
            String val = avanzar().getLexema();
            NodoCadena nodo = new NodoCadena(val);
            nodo.setLinea(linea);
            return nodo;
        }

        // Booleanos
        if (verificarLexema("verdadero")) {
            avanzar();
            NodoBooleano nodo = new NodoBooleano(true);
            nodo.setLinea(linea);
            return nodo;
        }
        if (verificarLexema("falso")) {
            avanzar();
            NodoBooleano nodo = new NodoBooleano(false);
            nodo.setLinea(linea);
            return nodo;
        }

        // Nulo
        if (verificarLexema("nulo")) {
            avanzar();
            NodoNulo nodo = new NodoNulo();
            nodo.setLinea(linea);
            return nodo;
        }

        // Identificador o llamada a función: nombre  |  nombre(args)
        if (verificar(TipoToken.IDENTIFICADOR)) {
            String nombre = avanzar().getLexema();

            if (verificarLexema("(")) {
                // llamada a función
                consumirLexema("(");
                List<NodoAST> args = new ArrayList<>();
                while (!verificarLexema(")") && !esFin()) {
                    args.add(parseExpresion());
                    if (verificarLexema(",")) avanzar();
                }
                consumirLexema(")");
                NodoLlamada nodo = new NodoLlamada(nombre, args);
                nodo.setLinea(linea);
                return nodo;
            }

            NodoId nodo = new NodoId(nombre);
            nodo.setLinea(linea);
            return nodo;
        }

        // Expresión entre paréntesis
        if (verificarLexema("(")) {
            consumirLexema("(");
            NodoAST expr = parseExpresion();
            consumirLexema(")");
            return expr;
        }

        // Token inesperado
        throw new ErrorSintactico(
            "Línea " + linea + ": token inesperado '" + tokenActual.getLexema() + "'"
        );
    }

    // =========================================================
    //  RECUPERACIÓN DE ERRORES (modo pánico)
    // =========================================================

    /**
     * Avanza tokens hasta encontrar uno que pueda iniciar
     * una nueva instrucción válida, para continuar el análisis.
     */
    private void sincronizar() {
        while (!esFin()) {
            String lex = tokenActual.getLexema();
            if (lex.equals("@fin")    || lex.equals("fin")   ||
                lex.equals("si")      || lex.equals("sino")  ||
                lex.equals("sino_si") || lex.equals("mientras") ||
                lex.equals("para")    || lex.equals("funcion") ||
                lex.equals("var")     || lex.equals("const")  ||
                lex.equals("retornar")) {
                return;
            }
            avanzar();
        }
    }

    // =========================================================
    //  RESULTADOS
    // =========================================================

    public List<String> getErrores() { return errores; }
    public boolean tieneErrores()    { return !errores.isEmpty(); }

    // =========================================================
    //  IMPRESIÓN DEL AST (Preorden, como el profesor en C)
    // =========================================================

    /**
     * Imprime el AST completo en preorden, imitando la función
     * imprimirPre() del código C del profesor.
     */
    public void mostrarAST(List<NodoAST> programa) {

        // PREORDEN
        System.out.println("\n================ ARBOL SINTACTICO (Preorden) ================");
        for (int i = 0; i < programa.size(); i++) {
            System.out.print((i + 1) + ". ");
            imprimirNodoPre(programa.get(i), 0);
            System.out.println();
        }
        System.out.println("=============================================================");

        // INORDEN
        System.out.println("\n================ ARBOL SINTACTICO (Inorden) =================");
        for (int i = 0; i < programa.size(); i++) {
            System.out.print((i + 1) + ". ");
            imprimirNodoIn(programa.get(i), 0);
            System.out.println();
        }
        System.out.println("=============================================================");

        // POSTORDEN
        System.out.println("\n================ ARBOL SINTACTICO (Postorden) ===============");
        for (int i = 0; i < programa.size(); i++) {
            System.out.print((i + 1) + ". ");
            imprimirNodoPost(programa.get(i), 0);
            System.out.println();
        }
        System.out.println("=============================================================");
    }

    // =========================================================
    //  PREORDEN  raiz, izquierda, derecha
    // =========================================================
    private void imprimirNodoPre(NodoAST nodo, int nivel) {
        if (nodo == null) { System.out.print("null"); return; }

        String indent = "  ".repeat(nivel);

        if (nodo instanceof NodoNum)       { System.out.print(((NodoNum) nodo).getValor()); }
        else if (nodo instanceof NodoDecimal)   { System.out.print(((NodoDecimal) nodo).getValor()); }
        else if (nodo instanceof NodoCadena)    { System.out.print(((NodoCadena) nodo).getValor()); }
        else if (nodo instanceof NodoBooleano)  { System.out.print(((NodoBooleano) nodo).getValor() ? "verdadero" : "falso"); }
        else if (nodo instanceof NodoNulo)      { System.out.print("nulo"); }
        else if (nodo instanceof NodoId)        { System.out.print(((NodoId) nodo).getNombre()); }

        else if (nodo instanceof NodoBinario) {
            NodoBinario b = (NodoBinario) nodo;
            System.out.print("(" + b.getOperador() + " ");
            imprimirNodoPre(b.getIzquierda(), nivel);
            System.out.print(" ");
            imprimirNodoPre(b.getDerecha(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoUnario) {
            NodoUnario u = (NodoUnario) nodo;
            System.out.print("(" + u.getOperador() + " ");
            imprimirNodoPre(u.getOperando(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoAsignacion) {
            NodoAsignacion a = (NodoAsignacion) nodo;
            String pref = a.isDeclaracion() ? (a.isConstante() ? "const " : "var ") : "";
            System.out.print(pref + a.getNombre() + " <- ");
            imprimirNodoPre(a.getValor(), nivel);
        }

        else if (nodo instanceof NodoImprimir) {
            System.out.print("Imprimir(");
            imprimirNodoPre(((NodoImprimir) nodo).getValor(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoLeer) {
            System.out.print("Leer(" + ((NodoLeer) nodo).getIdentificador() + ")");
        }

        else if (nodo instanceof NodoLimpiar) {
            System.out.print("LimpiarPantalla()");
        }

        else if (nodo instanceof NodoSi) {
            NodoSi s = (NodoSi) nodo;
            System.out.println("Si(");
            System.out.print(indent + "  condicion: ");
            imprimirNodoPre(s.getCondicion(), nivel + 1);
            System.out.println();
            System.out.println(indent + "  entonces: [" + s.getEntonces().size() + " instrucciones]");
            for (NodoAST inst : s.getEntonces()) {
                System.out.print(indent + "    ");
                imprimirNodoPre(inst, nivel + 2);
                System.out.println();
            }
            // sino_si
            List<NodoAST> conds = s.getCondicionesSinoSi();
            List<List<NodoAST>> cuerpos = s.getCuerposSinoSi();
            for (int i = 0; i < conds.size(); i++) {
                System.out.print(indent + "  sino_si: ");
                imprimirNodoPre(conds.get(i), nivel + 1);
                System.out.println();
                for (NodoAST inst : cuerpos.get(i)) {
                    System.out.print(indent + "    ");
                    imprimirNodoPre(inst, nivel + 2);
                    System.out.println();
                }
            }
            if (s.getSino() != null) {
                System.out.println(indent + "  sino: [" + s.getSino().size() + " instrucciones]");
                for (NodoAST inst : s.getSino()) {
                    System.out.print(indent + "    ");
                    imprimirNodoPre(inst, nivel + 2);
                    System.out.println();
                }
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoMientras) {
            NodoMientras m = (NodoMientras) nodo;
            System.out.println("Mientras(");
            System.out.print(indent + "  condicion: ");
            imprimirNodoPre(m.getCondicion(), nivel + 1);
            System.out.println();
            System.out.println(indent + "  cuerpo: [" + m.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : m.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPre(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoPara) {
            NodoPara p = (NodoPara) nodo;
            System.out.println("Para(");
            System.out.print(indent + "  init: ");
            imprimirNodoPre(p.getInicializacion(), nivel + 1);
            System.out.println();
            System.out.print(indent + "  condicion: ");
            imprimirNodoPre(p.getCondicion(), nivel + 1);
            System.out.println();
            System.out.println(indent + "  cuerpo: [" + p.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : p.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPre(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoFuncion) {
            NodoFuncion f = (NodoFuncion) nodo;
            System.out.println("Funcion(" + f.getNombre() + ", params=" + f.getParametros() + ",");
            System.out.println(indent + "  cuerpo: [" + f.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : f.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPre(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoLlamada) {
            NodoLlamada ll = (NodoLlamada) nodo;
            System.out.print("Llamada(" + ll.getNombre() + ", args=[");
            for (int i = 0; i < ll.getArgumentos().size(); i++) {
                if (i > 0) System.out.print(", ");
                imprimirNodoPre(ll.getArgumentos().get(i), nivel);
            }
            System.out.print("])");
        }

        else if (nodo instanceof NodoRetornar) {
            System.out.print("Retornar(");
            imprimirNodoPre(((NodoRetornar) nodo).getValor(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoRomper)    { System.out.print("Romper()"); }
        else if (nodo instanceof NodoContinuar) { System.out.print("Continuar()"); }

        else { System.out.print("[Nodo desconocido]"); }
    }


    // =========================================================
    //  INORDEN  izquierda, raiz, derecha
    // =========================================================
    private void imprimirNodoIn(NodoAST nodo, int nivel) {
        if (nodo == null) { System.out.print("null"); return; }

        String indent = "  ".repeat(nivel);

        if (nodo instanceof NodoNum)            { System.out.print(((NodoNum) nodo).getValor()); }
        else if (nodo instanceof NodoDecimal)   { System.out.print(((NodoDecimal) nodo).getValor()); }
        else if (nodo instanceof NodoCadena)    { System.out.print(((NodoCadena) nodo).getValor()); }
        else if (nodo instanceof NodoBooleano)  { System.out.print(((NodoBooleano) nodo).getValor() ? "verdadero" : "falso"); }
        else if (nodo instanceof NodoNulo)      { System.out.print("nulo"); }
        else if (nodo instanceof NodoId)        { System.out.print(((NodoId) nodo).getNombre()); }

        else if (nodo instanceof NodoBinario) {
            NodoBinario b = (NodoBinario) nodo;
            System.out.print("(");
            imprimirNodoIn(b.getIzquierda(), nivel);
            System.out.print(" " + b.getOperador() + " ");
            imprimirNodoIn(b.getDerecha(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoUnario) {
            NodoUnario u = (NodoUnario) nodo;
            System.out.print("(" + u.getOperador() + " ");
            imprimirNodoIn(u.getOperando(), nivel);
            System.out.print(")");
        }

        else if (nodo instanceof NodoAsignacion) {
            NodoAsignacion a = (NodoAsignacion) nodo;
            String pref = a.isDeclaracion() ? (a.isConstante() ? "const " : "var ") : "";
            imprimirNodoIn(a.getValor(), nivel);
            System.out.print(" = " + pref + a.getNombre());
        }

        else if (nodo instanceof NodoImprimir) {
            imprimirNodoIn(((NodoImprimir) nodo).getValor(), nivel);
            System.out.print(" Imprimir");
        }

        else if (nodo instanceof NodoLeer) {
            System.out.print("Leer(" + ((NodoLeer) nodo).getIdentificador() + ")");
        }

        else if (nodo instanceof NodoLimpiar) {
            System.out.print("LimpiarPantalla()");
        }

        else if (nodo instanceof NodoSi) {
            NodoSi s = (NodoSi) nodo;
            System.out.println("Si(");
            System.out.print(indent + "  entonces: [" + s.getEntonces().size() + " instrucciones]");
            System.out.println();
            for (NodoAST inst : s.getEntonces()) {
                System.out.print(indent + "    ");
                imprimirNodoIn(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoIn(s.getCondicion(), nivel + 1);
            System.out.println();
            List<NodoAST> conds = s.getCondicionesSinoSi();
            List<List<NodoAST>> cuerpos = s.getCuerposSinoSi();
            for (int i = 0; i < conds.size(); i++) {
                System.out.print(indent + "  sino_si: ");
                imprimirNodoIn(conds.get(i), nivel + 1);
                System.out.println();
                for (NodoAST inst : cuerpos.get(i)) {
                    System.out.print(indent + "    ");
                    imprimirNodoIn(inst, nivel + 2);
                    System.out.println();
                }
            }
            if (s.getSino() != null) {
                System.out.println(indent + "  sino: [" + s.getSino().size() + " instrucciones]");
                for (NodoAST inst : s.getSino()) {
                    System.out.print(indent + "    ");
                    imprimirNodoIn(inst, nivel + 2);
                    System.out.println();
                }
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoMientras) {
            NodoMientras m = (NodoMientras) nodo;
            System.out.println("Mientras(");
            System.out.println(indent + "  cuerpo: [" + m.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : m.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoIn(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoIn(m.getCondicion(), nivel + 1);
            System.out.println();
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoPara) {
            NodoPara p = (NodoPara) nodo;
            System.out.println("Para(");
            System.out.println(indent + "  cuerpo: [" + p.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : p.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoIn(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoIn(p.getCondicion(), nivel + 1);
            System.out.println();
            System.out.print(indent + "  init: ");
            imprimirNodoIn(p.getInicializacion(), nivel + 1);
            System.out.println();
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoFuncion) {
            NodoFuncion f = (NodoFuncion) nodo;
            System.out.println("Funcion(" + f.getNombre() + ", params=" + f.getParametros() + ",");
            System.out.println(indent + "  cuerpo: [" + f.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : f.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoIn(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoLlamada) {
            NodoLlamada ll = (NodoLlamada) nodo;
            System.out.print("Llamada(" + ll.getNombre() + ", args=[");
            for (int i = 0; i < ll.getArgumentos().size(); i++) {
                if (i > 0) System.out.print(", ");
                imprimirNodoIn(ll.getArgumentos().get(i), nivel);
            }
            System.out.print("])");
        }

        else if (nodo instanceof NodoRetornar) {
            imprimirNodoIn(((NodoRetornar) nodo).getValor(), nivel);
            System.out.print(" Retornar");
        }

        else if (nodo instanceof NodoRomper)    { System.out.print("Romper()"); }
        else if (nodo instanceof NodoContinuar) { System.out.print("Continuar()"); }
        else { System.out.print("[Nodo desconocido]"); }
    }

    // =========================================================
    //  POSTORDEN  izquierda, derecha, raiz
    // =========================================================
    private void imprimirNodoPost(NodoAST nodo, int nivel) {
        if (nodo == null) { System.out.print("null"); return; }

        String indent = "  ".repeat(nivel);

        if (nodo instanceof NodoNum)            { System.out.print(((NodoNum) nodo).getValor()); }
        else if (nodo instanceof NodoDecimal)   { System.out.print(((NodoDecimal) nodo).getValor()); }
        else if (nodo instanceof NodoCadena)    { System.out.print(((NodoCadena) nodo).getValor()); }
        else if (nodo instanceof NodoBooleano)  { System.out.print(((NodoBooleano) nodo).getValor() ? "verdadero" : "falso"); }
        else if (nodo instanceof NodoNulo)      { System.out.print("nulo"); }
        else if (nodo instanceof NodoId)        { System.out.print(((NodoId) nodo).getNombre()); }

        else if (nodo instanceof NodoBinario) {
            NodoBinario b = (NodoBinario) nodo;
            imprimirNodoPost(b.getIzquierda(), nivel);
            System.out.print(" ");
            imprimirNodoPost(b.getDerecha(), nivel);
            System.out.print(" " + b.getOperador());
        }

        else if (nodo instanceof NodoUnario) {
            NodoUnario u = (NodoUnario) nodo;
            imprimirNodoPost(u.getOperando(), nivel);
            System.out.print(" " + u.getOperador());
        }

        else if (nodo instanceof NodoAsignacion) {
            NodoAsignacion a = (NodoAsignacion) nodo;
            String pref = a.isDeclaracion() ? (a.isConstante() ? "const " : "var ") : "";
            imprimirNodoPost(a.getValor(), nivel);
            System.out.print(" " + pref + a.getNombre() + " =");
        }

        else if (nodo instanceof NodoImprimir) {
            imprimirNodoPost(((NodoImprimir) nodo).getValor(), nivel);
            System.out.print(" Imprimir");
        }

        else if (nodo instanceof NodoLeer) {
            System.out.print("Leer(" + ((NodoLeer) nodo).getIdentificador() + ")");
        }

        else if (nodo instanceof NodoLimpiar) {
            System.out.print("LimpiarPantalla()");
        }

        else if (nodo instanceof NodoSi) {
            NodoSi s = (NodoSi) nodo;
            System.out.println("Si(");
            System.out.println(indent + "  entonces: [" + s.getEntonces().size() + " instrucciones]");
            for (NodoAST inst : s.getEntonces()) {
                System.out.print(indent + "    ");
                imprimirNodoPost(inst, nivel + 2);
                System.out.println();
            }
            List<NodoAST> conds = s.getCondicionesSinoSi();
            List<List<NodoAST>> cuerpos = s.getCuerposSinoSi();
            for (int i = 0; i < conds.size(); i++) {
                System.out.println(indent + "  sino_si cuerpo:");
                for (NodoAST inst : cuerpos.get(i)) {
                    System.out.print(indent + "    ");
                    imprimirNodoPost(inst, nivel + 2);
                    System.out.println();
                }
                System.out.print(indent + "  sino_si condicion: ");
                imprimirNodoPost(conds.get(i), nivel + 1);
                System.out.println();
            }
            if (s.getSino() != null) {
                System.out.println(indent + "  sino: [" + s.getSino().size() + " instrucciones]");
                for (NodoAST inst : s.getSino()) {
                    System.out.print(indent + "    ");
                    imprimirNodoPost(inst, nivel + 2);
                    System.out.println();
                }
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoPost(s.getCondicion(), nivel + 1);
            System.out.println();
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoMientras) {
            NodoMientras m = (NodoMientras) nodo;
            System.out.println("Mientras(");
            System.out.println(indent + "  cuerpo: [" + m.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : m.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPost(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoPost(m.getCondicion(), nivel + 1);
            System.out.println();
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoPara) {
            NodoPara p = (NodoPara) nodo;
            System.out.println("Para(");
            System.out.println(indent + "  cuerpo: [" + p.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : p.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPost(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  condicion: ");
            imprimirNodoPost(p.getCondicion(), nivel + 1);
            System.out.println();
            System.out.print(indent + "  init: ");
            imprimirNodoPost(p.getInicializacion(), nivel + 1);
            System.out.println();
            System.out.print(indent + ")");
        }

        else if (nodo instanceof NodoFuncion) {
            NodoFuncion f = (NodoFuncion) nodo;
            System.out.println("Funcion(");
            System.out.println(indent + "  cuerpo: [" + f.getCuerpo().size() + " instrucciones]");
            for (NodoAST inst : f.getCuerpo()) {
                System.out.print(indent + "    ");
                imprimirNodoPost(inst, nivel + 2);
                System.out.println();
            }
            System.out.print(indent + "  " + f.getNombre() + " params=" + f.getParametros() + ")");
        }

        else if (nodo instanceof NodoLlamada) {
            NodoLlamada ll = (NodoLlamada) nodo;
            System.out.print("Llamada(args=[");
            for (int i = 0; i < ll.getArgumentos().size(); i++) {
                if (i > 0) System.out.print(", ");
                imprimirNodoPost(ll.getArgumentos().get(i), nivel);
            }
            System.out.print("] " + ll.getNombre() + ")");
        }

        else if (nodo instanceof NodoRetornar) {
            imprimirNodoPost(((NodoRetornar) nodo).getValor(), nivel);
            System.out.print(" Retornar");
        }

        else if (nodo instanceof NodoRomper)    { System.out.print("Romper()"); }
        else if (nodo instanceof NodoContinuar) { System.out.print("Continuar()"); }
        else { System.out.print("[Nodo desconocido]"); }
    }

    // =========================================================
    //  CLASE INTERNA DE ERROR
    // =========================================================

    public static class ErrorSintactico extends RuntimeException {
        public ErrorSintactico(String mensaje) { super(mensaje); }
    }
}