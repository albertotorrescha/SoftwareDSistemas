package compilador.lexico;

import java.io.FileReader;
import java.util.ArrayList;

public class AnalizadorLexico {

    private Nodo cabeza = null;
    private Nodo cola = null;
    private ArrayList<Token> listaTokens = new ArrayList<>();

    private static final String[] PALABRAS_RESERVADAS = {
        "@inicio","@fin","si","sino","sino_si",
        "fin","mientras","para","hacer",
        "verdadero","falso","retornar",
        "funcion","var","const",
        "romper","continuar","nulo","tipo"
    };

    private void insertarCaracter(char c){
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

    public void cargarArchivo(String ruta) throws Exception{
        FileReader fr = new FileReader(ruta);
        int c;

        while((c = fr.read()) != -1){
            insertarCaracter((char)c);
        }

        fr.close();
    }

    public void analizar(){

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

            if(c == '"'){
                actual = automataCadena(actual, linea);
            }
            else if(c == '-' || esDigito(c)){
                actual = automataNumero(actual, linea);
            }
            else if(c == '@' || esLetra(c)){
                actual = automataIdentificador(actual, linea);
            }
            else{
                actual = automataSimbolos(actual, linea);
            }
        }
    }

    private Nodo automataCadena(Nodo inicio, int linea){

    String lexema = "";
    Nodo temp = inicio.siguiente;

    while(temp != null &&
          temp.caracter != '"' &&
          temp.caracter != '\n'){
        lexema += temp.caracter;
        temp = temp.siguiente;
    }

    if(temp != null && temp.caracter == '"'){
        listaTokens.add(
            new Token("\"" + lexema + "\"",
                      TipoToken.CADENA,
                      linea)
        );
        return temp.siguiente;
    }

    listaTokens.add(
        new Token("\"" + lexema,
                  TipoToken.ERROR,
                  linea)
    );

    return temp;
}


    private Nodo automataNumero(Nodo inicio, int linea){

        Nodo temp = inicio;
        String lexema = "";
        boolean decimal = false;

        if(temp.caracter == '-'){
            lexema += "-";
            temp = temp.siguiente;

            if(temp == null || !esDigito(temp.caracter)){
                listaTokens.add(new Token("-", TipoToken.ARITMETICO, linea));
                return inicio.siguiente;
            }
        }

        while(temp != null && esDigito(temp.caracter)){
            lexema += temp.caracter;
            temp = temp.siguiente;
        }

        if(temp != null && temp.caracter == '.'){
            if(temp.siguiente != null && esDigito(temp.siguiente.caracter)){
                decimal = true;
                lexema += ".";
                temp = temp.siguiente;

                while(temp != null && esDigito(temp.caracter)){
                    lexema += temp.caracter;
                    temp = temp.siguiente;
                }
            }
        }

        if(decimal)
            listaTokens.add(new Token(lexema, TipoToken.DECIMAL, linea));
        else
            listaTokens.add(new Token(lexema, TipoToken.ENTERO, linea));

        return temp;
    }

    private Nodo automataIdentificador(Nodo inicio, int linea){
        String lexema = "";
        Nodo temp = inicio;

        if(temp.caracter == '@'){
            lexema += "@";
            temp = temp.siguiente;
        }

        while(temp != null &&
              (esLetra(temp.caracter) || esDigito(temp.caracter) || temp.caracter == '_')){
            lexema += temp.caracter;
            temp = temp.siguiente;
        }

        if(esPalabraReservada(lexema))
            listaTokens.add(new Token(lexema, TipoToken.RESERVADA, linea));
        else
            listaTokens.add(new Token(lexema, TipoToken.IDENTIFICADOR, linea));

        return temp;
    }

    private Nodo automataSimbolos(Nodo inicio, int linea){

        char c = inicio.caracter;

        if(inicio.siguiente != null){
            String doble = "" + c + inicio.siguiente.caracter;

            if(doble.equals("->") || doble.equals("<-")){
                listaTokens.add(new Token(doble, TipoToken.ASIGNACION, linea));
                return inicio.siguiente.siguiente;
            }

            if(doble.equals("==") || doble.equals("!=") ||
               doble.equals(">=") || doble.equals("<=")){
                listaTokens.add(new Token(doble, TipoToken.RELACIONAL, linea));
                return inicio.siguiente.siguiente;
            }
        }

        if("+-*/".indexOf(c) >= 0){
            listaTokens.add(new Token(""+c, TipoToken.ARITMETICO, linea));
            return inicio.siguiente;
        }

        if("><".indexOf(c) >= 0){
            listaTokens.add(new Token(""+c, TipoToken.RELACIONAL, linea));
            return inicio.siguiente;
        }

        if(c == '='){
            listaTokens.add(new Token("=", TipoToken.ASIGNACION, linea));
            return inicio.siguiente;
        }

        if("()".indexOf(c) >= 0){
            listaTokens.add(new Token(""+c, TipoToken.PARENTESIS, linea));
            return inicio.siguiente;
        }

        if(c == '!'){
            listaTokens.add(new Token("!", TipoToken.IMPRIMIR, linea));
            return inicio.siguiente;
        }

        if(c == '?'){
            listaTokens.add(new Token("?", TipoToken.LEER, linea));
            return inicio.siguiente;
        }

        if(c == '#'){
            listaTokens.add(new Token("#", TipoToken.LIMPIAR_PANTALLA, linea));
            return inicio.siguiente;
        }

        listaTokens.add(new Token(""+c, TipoToken.ERROR, linea));
        return inicio.siguiente;
    }


    private boolean esLetra(char c){
        return (c>='a'&&c<='z')||(c>='A'&&c<='Z');
    }

    private boolean esDigito(char c){
        return (c>='0'&&c<='9');
    }

    private boolean esEspacio(char c){
        return c==' '||c=='\t'||c=='\r';
    }

    private boolean esPalabraReservada(String lex){
        for(String r : PALABRAS_RESERVADAS)
            if(r.equals(lex))
                return true;
        return false;
    }


    public ArrayList<Token> getTokens(){
        return listaTokens;
    }

    public void mostrarTokens(){

    System.out.println("\n================ TABLA DE TOKENS ================");
    System.out.printf("%-5s %-20s %-20s %-10s\n",
            "No.", "LEXEMA", "TIPO", "LINEA");
    System.out.println("------------------------------------------------------------");

    for(int i = 0; i < listaTokens.size(); i++){

        Token t = listaTokens.get(i);

        String lexemaVisible = t.getLexema()
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace("\t", "\\t");

        System.out.printf("%-5d %-20s %-20s %-10d\n",
                (i + 1),
                lexemaVisible,
                t.getTipo(),
                t.getLinea());
    }

    System.out.println("============================================================");
}
}