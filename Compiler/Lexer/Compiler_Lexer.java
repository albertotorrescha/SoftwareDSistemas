import java.io.FileReader;
import java.util.ArrayList;

public class Compiler_Lexer {

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
        ERROR
    }

    static class Token{
        String lexema;
        TipoToken tipo;
        int linea;

        Token(String l, TipoToken t, int ln){
            lexema=l;
            tipo=t;
            linea=ln;
        }
    }

    static ArrayList<Token> tokens = new ArrayList<>();

    /* ==========================
       PALABRAS RESERVADAS (PDF)
       ========================== */

    static String[] reservadas = {
        "@inicio","@fin","si","sino","sino_si",
        "fin","mientras","para","hacer",
        "verdadero","falso","retornar",
        "funcion","var","const",
        "romper","continuar","nulo","tipo"
    };

    static boolean esReservada(String lex){
        for(int i=0;i<reservadas.length;i++)
            if(reservadas[i].equals(lex))
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

    static boolean esOperador(char c){
        return "=+-*/><!?#()@".indexOf(c)>=0;
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

        while(actual!=null){

            char c = actual.caracter;

            if(c=='\n') linea++;

            /* ---- ESPACIOS ---- */
            if(esEspacio(c)){
                actual=actual.siguiente;
                continue;
            }

            /* ---- CADENAS ---- */
            if(c=='"'){
                String lex="\"";
                actual=actual.siguiente;

                while(actual!=null && actual.caracter!='"'){
                    lex+=actual.caracter;
                    actual=actual.siguiente;
                }

                if(actual!=null){
                    lex+="\"";
                    actual=actual.siguiente;
                    tokens.add(new Token(lex,TipoToken.CADENA,linea));
                }else{
                    tokens.add(new Token(lex,TipoToken.ERROR,linea));
                }
                continue;
            }

            /* ---- NUMEROS ---- */
            if(esDigito(c) || c=='-'){
                String lex="";
                boolean decimal=false;

                lex+=c;
                actual=actual.siguiente;

                while(actual!=null && esDigito(actual.caracter)){
                    lex+=actual.caracter;
                    actual=actual.siguiente;
                }

                if(actual!=null && actual.caracter=='.'){
                    decimal=true;
                    lex+='.';
                    actual=actual.siguiente;

                    while(actual!=null && esDigito(actual.caracter)){
                        lex+=actual.caracter;
                        actual=actual.siguiente;
                    }
                }

                if(decimal)
                    tokens.add(new Token(lex,TipoToken.DECIMAL,linea));
                else
                    tokens.add(new Token(lex,TipoToken.ENTERO,linea));

                continue;
            }

            /* ---- IDENTIFICADOR / RESERVADA ---- */
            if(esLetra(c) || c=='@'){
                String lex="";

                while(actual!=null &&
                     (esLetra(actual.caracter)
                     ||esDigito(actual.caracter)
                     ||actual.caracter=='_'
                     ||actual.caracter=='@')){
                    lex+=actual.caracter;
                    actual=actual.siguiente;
                }

                if(esReservada(lex))
                    tokens.add(new Token(lex,TipoToken.RESERVADA,linea));
                else
                    tokens.add(new Token(lex,TipoToken.IDENTIFICADOR,linea));

                continue;
            }

            /* ---- OPERADORES ---- */
            if(esOperador(c)){
                tokens.add(new Token(""+c,TipoToken.OPERADOR,linea));
                actual=actual.siguiente;
                continue;
            }

            /* ---- ERROR LEXICO ---- */
            tokens.add(new Token(""+c,TipoToken.ERROR,linea));
            actual=actual.siguiente;
        }
    }

    /* ==========================
       MOSTRAR TOKENS
       ========================== */

    static void mostrarTokens(){

        System.out.println("===== TOKENS =====");

        for(int i=0;i<tokens.size();i++){
            Token t = tokens.get(i);
            System.out.println(
                (i+1)+") "+t.lexema+
                " -> "+t.tipo+
                " (linea "+t.linea+")");
        }
    }

    /* ==========================
       MAIN
       ========================== */

    public static void main(String[] args){

        try{
            cargarArchivo("codigo.txt");
            analizar();
            mostrarTokens();
        }
        catch(Exception e){
            System.out.println("Error: "+e.getMessage());
        }
    }
}
