import java.io.FileReader;
import java.util.ArrayList;

public class Lexico {

    /* ==========================
       LISTA DOBLEMENTE ENLAZADA
       ========================== */

    static class Nodo {
        char caracter;
        Nodo siguiente;
        Nodo anterior;

        Nodo(char c){
            caracter = c;
        }
    }

    static Nodo cabeza = null;
    static Nodo cola = null;

    static void insertar(char c){
        Nodo nuevo = new Nodo(c);

        if(cabeza == null){
            cabeza = nuevo;
            cola = nuevo;
        }else{
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            cola = nuevo;
        }
    }

    /* ==========================
       TOKENS
       ========================== */

    enum TipoToken{
        RESERVADA,
        IDENTIFICADOR,
        ENTERO,
        DECIMAL,
        CADENA,
        OPERADOR,
        IMPRIMIR,
        LEER,
        LIMPIAR_PANTALLA,
        ERROR
    }

    static class Token{
        String lexema;
        TipoToken tipo;
        int linea;

        Token(String l, TipoToken t, int ln){
            lexema = l;
            tipo = t;
            linea = ln;
        }
    }

    static ArrayList<Token> tokens = new ArrayList<>();

    /* ==========================
       PALABRAS RESERVADAS
       ========================== */

    static String[] reservadas = {
        "@inicio","@fin","si","sino","sino_si",
        "fin","mientras","para","hacer",
        "verdadero","falso","retornar",
        "funcion","var","const",
        "romper","continuar","nulo","tipo"
    };

    static boolean esReservada(String lex){
        for(String r : reservadas)
            if(r.equals(lex))
                return true;
        return false;
    }

    /* ==========================
       UTILIDADES
       ========================== */

    static boolean esLetra(char c){
        return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }

    static boolean esDigito(char c){
        return (c>='0'&&c<='9');
    }

    static boolean esEspacio(char c){
        return c==' '||c=='\n'||c=='\t'||c=='\r';
    }

    /* ==========================
       CARGAR ARCHIVO
       ========================== */

    static void cargarArchivo(String nombre)throws Exception{
        FileReader fr = new FileReader(nombre);
        int c;

        while((c=fr.read())!=-1)
            insertar((char)c);

        fr.close();
    }

    /* ==========================
       ANALIZADOR LEXICO
       ========================== */

    static void analizar(){

        Nodo actual = cabeza;
        int linea = 1;

        while(actual != null){

            char c = actual.caracter;

            if(c == '\n'){
                linea++;
                actual = actual.siguiente;
                continue;
            }

            if(esEspacio(c)){
                actual = actual.siguiente;
                continue;
            }

            /* =========================
               CADENAS
               ========================= */
            if(c == '"'){
                String lex = "\"";
                actual = actual.siguiente;

                while(actual != null && actual.caracter != '"'){
                    lex += actual.caracter;
                    actual = actual.siguiente;
                }

                if(actual != null){
                    lex += "\"";
                    actual = actual.siguiente;
                    tokens.add(new Token(lex, TipoToken.CADENA, linea));
                }else{
                    tokens.add(new Token(lex, TipoToken.ERROR, linea));
                }
                continue;
            }

            /* =========================
               NUMEROS
               -?[0-9]+(\.[0-9]+)?
               ========================= */
            if(c == '-' || esDigito(c)){

                Nodo temp = actual;
                String lex = "";
                boolean esDecimal = false;

                if(c == '-'){
                    lex += c;
                    temp = temp.siguiente;

                    if(temp == null || !esDigito(temp.caracter)){
                        tokens.add(new Token("-", TipoToken.OPERADOR, linea));
                        actual = actual.siguiente;
                        continue;
                    }
                }

                while(temp != null && esDigito(temp.caracter)){
                    lex += temp.caracter;
                    temp = temp.siguiente;
                }

                if(temp != null && temp.caracter == '.'){
                    Nodo afterDot = temp.siguiente;

                    if(afterDot != null && esDigito(afterDot.caracter)){
                        esDecimal = true;
                        lex += ".";
                        temp = afterDot;

                        while(temp != null && esDigito(temp.caracter)){
                            lex += temp.caracter;
                            temp = temp.siguiente;
                        }
                    }
                }

                if(esDecimal)
                    tokens.add(new Token(lex, TipoToken.DECIMAL, linea));
                else
                    tokens.add(new Token(lex, TipoToken.ENTERO, linea));

                actual = temp;
                continue;
            }

            /* =========================
               IDENTIFICADORES
               ========================= */
            if(esLetra(c)){

                String lex = "";
                Nodo temp = actual;

                while(temp != null &&
                      (esLetra(temp.caracter) || esDigito(temp.caracter))){
                    lex += temp.caracter;
                    temp = temp.siguiente;
                }

                if(esReservada(lex))
                    tokens.add(new Token(lex, TipoToken.RESERVADA, linea));
                else
                    tokens.add(new Token(lex, TipoToken.IDENTIFICADOR, linea));

                actual = temp;
                continue;
            }

            /* =========================
               OPERADORES COMPUESTOS
               ========================= */
            if(actual.siguiente != null){

                String dos = "" + c + actual.siguiente.caracter;

                if(dos.equals("==") ||
                   dos.equals("!=") ||
                   dos.equals(">=") ||
                   dos.equals("<=") ||
                   dos.equals("->") ||
                   dos.equals("<-")){

                    tokens.add(new Token(dos, TipoToken.OPERADOR, linea));
                    actual = actual.siguiente.siguiente;
                    continue;
                }
            }

            /* =========================
               FUNCIONES PRINCIPALES
               ========================= */
            if(c == '!'){
                tokens.add(new Token("!", TipoToken.IMPRIMIR, linea));
                actual = actual.siguiente;
                continue;
            }

            if(c == '?'){
                tokens.add(new Token("?", TipoToken.LEER, linea));
                actual = actual.siguiente;
                continue;
            }

            if(c == '#'){
                tokens.add(new Token("#", TipoToken.LIMPIAR_PANTALLA, linea));
                actual = actual.siguiente;
                continue;
            }

            /* =========================
               OPERADORES SIMPLES
               ========================= */
            if("=+-*/><()@".indexOf(c) >= 0){
                tokens.add(new Token("" + c, TipoToken.OPERADOR, linea));
                actual = actual.siguiente;
                continue;
            }

            /* =========================
               ERROR LEXICO
               ========================= */
            tokens.add(new Token("" + c, TipoToken.ERROR, linea));
            actual = actual.siguiente;
        }
    }

    /* ==========================
       MOSTRAR TOKENS
       ========================== */

    static void mostrarTokens(){

        System.out.println("\n================ TABLA DE TOKENS ================");
        System.out.printf("%-5s %-20s %-20s %-10s\n", 
                        "No.", "LEXEMA", "TIPO", "LINEA");
        System.out.println("------------------------------------------------------------");

        for(int i = 0; i < tokens.size(); i++){

            Token t = tokens.get(i);

            System.out.printf("%-5d %-20s %-20s %-10d\n",
                    (i+1),
                    t.lexema,
                    t.tipo,
                    t.linea);
        }

        System.out.println("============================================================");
    }

    /* ==========================
       MAIN
       ========================== */

    public static void main(String[] args){

        try{
            cargarArchivo("C:\\Users\\chapa\\Documents\\codigo.txt");
            analizar();
            mostrarTokens();
        }
        catch(Exception e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}