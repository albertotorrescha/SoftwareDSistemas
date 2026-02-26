import java.io.FileReader;
import java.util.ArrayList;

public class Lexico {

    // Nodo de la lista doblemente enlazada que almacena un caracter del archivo
    static class Nodo {
        char caracter;
        Nodo siguiente;
        Nodo anterior;

        // Constructor que inicializa el nodo con un caracter
        Nodo(char caracterRecibido){
            caracter = caracterRecibido;
        }
    }

    // Punteros principales de la lista
    static Nodo cabeza = null;
    static Nodo cola = null;

    // Inserta un caracter al final de la lista doblemente enlazada
    static void insertarCaracter(char caracter){
        Nodo nuevoNodo = new Nodo(caracter);

        if(cabeza == null){
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        }else{
            cola.siguiente = nuevoNodo;
            nuevoNodo.anterior = cola;
            cola = nuevoNodo;
        }
    }

    // Enumeracion que define los tipos de token del lenguaje
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

    // Clase que representa un token con su lexema, tipo y linea
    static class Token{
        String lexema;
        TipoToken tipo;
        int linea;

        // Constructor que inicializa los atributos del token
        Token(String lexemaRecibido, TipoToken tipoRecibido, int lineaRecibida){
            lexema = lexemaRecibido;
            tipo = tipoRecibido;
            linea = lineaRecibida;
        }
    }

    // Lista que almacena todos los tokens generados por el analizador
    static ArrayList<Token> listaTokens = new ArrayList<>();

    // Arreglo con todas las palabras reservadas del lenguaje
    static String[] palabrasReservadas = {
        "@inicio","@fin","si","sino","sino_si",
        "fin","mientras","para","hacer",
        "verdadero","falso","retornar",
        "funcion","var","const",
        "romper","continuar","nulo","tipo"
    };

    // Verifica si un lexema pertenece al conjunto de palabras reservadas
    static boolean esPalabraReservada(String lexema){
        for(String reservada : palabrasReservadas)
            if(reservada.equals(lexema))
                return true;
        return false;
    }

    // Determina si un caracter es letra
    static boolean esLetra(char caracter){
        return (caracter>='a'&&caracter<='z')||(caracter>='A'&&caracter<='Z');
    }

    // Determina si un caracter es digito
    static boolean esDigito(char caracter){
        return (caracter>='0'&&caracter<='9');
    }

    // Determina si un caracter es espacio en blanco
    static boolean esEspacio(char caracter){
        return caracter==' '||caracter=='\n'||caracter=='\t'||caracter=='\r';
    }

    // Lee el archivo y carga cada caracter en la lista enlazada
    static void cargarArchivo(String nombreArchivo)throws Exception{
        FileReader lectorArchivo = new FileReader(nombreArchivo);
        int codigoCaracter;

        while((codigoCaracter = lectorArchivo.read()) != -1)
            insertarCaracter((char)codigoCaracter);

        lectorArchivo.close();
    }

    // Recorre la lista enlazada y construye los tokens segun las reglas del lenguaje
    static void analizar(){

        Nodo nodoActual = cabeza;
        int numeroLinea = 1;

        while(nodoActual != null){

            char caracterActual = nodoActual.caracter;

            if(caracterActual == '\n'){
                numeroLinea++;
                nodoActual = nodoActual.siguiente;
                continue;
            }

            if(esEspacio(caracterActual)){
                nodoActual = nodoActual.siguiente;
                continue;
            }

            // Reconocimiento de cadenas delimitadas por comillas
            if(caracterActual == '"'){
                String lexema = "\"";
                nodoActual = nodoActual.siguiente;

                while(nodoActual != null && nodoActual.caracter != '"'){
                    lexema += nodoActual.caracter;
                    nodoActual = nodoActual.siguiente;
                }

                if(nodoActual != null){
                    lexema += "\"";
                    nodoActual = nodoActual.siguiente;
                    listaTokens.add(new Token(lexema, TipoToken.CADENA, numeroLinea));
                }else{
                    listaTokens.add(new Token(lexema, TipoToken.ERROR, numeroLinea));
                }
                continue;
            }

            // Reconocimiento de numeros enteros y decimales con signo opcional
            if(caracterActual == '-' || esDigito(caracterActual)){

                Nodo nodoTemporal = nodoActual;
                String lexema = "";
                boolean esDecimal = false;

                if(caracterActual == '-'){
                    lexema += caracterActual;
                    nodoTemporal = nodoTemporal.siguiente;

                    if(nodoTemporal == null || !esDigito(nodoTemporal.caracter)){
                        listaTokens.add(new Token("-", TipoToken.OPERADOR, numeroLinea));
                        nodoActual = nodoActual.siguiente;
                        continue;
                    }
                }

                while(nodoTemporal != null && esDigito(nodoTemporal.caracter)){
                    lexema += nodoTemporal.caracter;
                    nodoTemporal = nodoTemporal.siguiente;
                }

                if(nodoTemporal != null && nodoTemporal.caracter == '.'){
                    Nodo nodoDespuesPunto = nodoTemporal.siguiente;

                    if(nodoDespuesPunto != null && esDigito(nodoDespuesPunto.caracter)){
                        esDecimal = true;
                        lexema += ".";
                        nodoTemporal = nodoDespuesPunto;

                        while(nodoTemporal != null && esDigito(nodoTemporal.caracter)){
                            lexema += nodoTemporal.caracter;
                            nodoTemporal = nodoTemporal.siguiente;
                        }
                    }
                }

                if(esDecimal)
                    listaTokens.add(new Token(lexema, TipoToken.DECIMAL, numeroLinea));
                else
                    listaTokens.add(new Token(lexema, TipoToken.ENTERO, numeroLinea));

                nodoActual = nodoTemporal;
                continue;
            }

            // Reconocimiento de identificadores y palabras reservadas (incluye @inicio y @fin)
            if(caracterActual == '@' || esLetra(caracterActual)){

                String lexema = "";
                Nodo nodoTemporal = nodoActual;

                if(caracterActual == '@'){
                    lexema += "@";
                    nodoTemporal = nodoTemporal.siguiente;
                }

                while(nodoTemporal != null &&
                      (esLetra(nodoTemporal.caracter) || 
                       esDigito(nodoTemporal.caracter))){
                    lexema += nodoTemporal.caracter;
                    nodoTemporal = nodoTemporal.siguiente;
                }

                if(esPalabraReservada(lexema))
                    listaTokens.add(new Token(lexema, TipoToken.RESERVADA, numeroLinea));
                else
                    listaTokens.add(new Token(lexema, TipoToken.IDENTIFICADOR, numeroLinea));

                nodoActual = nodoTemporal;
                continue;
            }

            // Reconocimiento de operadores compuestos
            if(nodoActual.siguiente != null){

                String operadorDoble = "" + caracterActual + nodoActual.siguiente.caracter;

                if(operadorDoble.equals("==") ||
                   operadorDoble.equals("!=") ||
                   operadorDoble.equals(">=") ||
                   operadorDoble.equals("<=") ||
                   operadorDoble.equals("->") ||
                   operadorDoble.equals("<-")){

                    listaTokens.add(new Token(operadorDoble, TipoToken.OPERADOR, numeroLinea));
                    nodoActual = nodoActual.siguiente.siguiente;
                    continue;
                }
            }

            // Reconocimiento de funciones especiales representadas por simbolos
            if(caracterActual == '!'){
                listaTokens.add(new Token("!", TipoToken.IMPRIMIR, numeroLinea));
                nodoActual = nodoActual.siguiente;
                continue;
            }

            if(caracterActual == '?'){
                listaTokens.add(new Token("?", TipoToken.LEER, numeroLinea));
                nodoActual = nodoActual.siguiente;
                continue;
            }

            if(caracterActual == '#'){
                listaTokens.add(new Token("#", TipoToken.LIMPIAR_PANTALLA, numeroLinea));
                nodoActual = nodoActual.siguiente;
                continue;
            }

            // Reconocimiento de operadores simples
            if("=+-*/><()".indexOf(caracterActual) >= 0){
                listaTokens.add(new Token("" + caracterActual, TipoToken.OPERADOR, numeroLinea));
                nodoActual = nodoActual.siguiente;
                continue;
            }

            // Cualquier caracter no reconocido se clasifica como error lexico
            listaTokens.add(new Token("" + caracterActual, TipoToken.ERROR, numeroLinea));
            nodoActual = nodoActual.siguiente;
        }
    }

    // Muestra el codigo fuente numerado linea por linea
    static void mostrarArchivoNumerado(String nombreArchivo){

        try{
            FileReader lector = new FileReader(nombreArchivo);
            String linea = "";
            int numeroLinea = 1;
            int caracter;

            System.out.println("\n=========== CODIGO FUENTE ===========\n");

            while((caracter = lector.read()) != -1){

                if(caracter == '\n'){
                    System.out.printf("%-4d | %s\n", numeroLinea, linea);
                    linea = "";
                    numeroLinea++;
                }else{
                    linea += (char)caracter;
                }
            }

            if(!linea.isEmpty()){
                System.out.printf("%-4d | %s\n", numeroLinea, linea);
            }

            System.out.println("\n======================================\n");

            lector.close();

        }catch(Exception e){
            System.out.println("Error al mostrar archivo: " + e.getMessage());
        }
    }

    // Imprime en formato tabular todos los tokens generados
    static void mostrarTokens(){

        System.out.println("\n================ TABLA DE TOKENS ================");
        System.out.printf("%-5s %-20s %-20s %-10s\n", 
                        "No.", "LEXEMA", "TIPO", "LINEA");
        System.out.println("------------------------------------------------------------");

        for(int indice = 0; indice < listaTokens.size(); indice++){

            Token tokenActual = listaTokens.get(indice);

            System.out.printf("%-5d %-20s %-20s %-10d\n",
                    (indice+1),
                    tokenActual.lexema,
                    tokenActual.tipo,
                    tokenActual.linea);
        }

        System.out.println("============================================================");
    }

    // Metodo principal que ejecuta las fases del analizador lexico
    public static void main(String[] args){

        try{
            String ruta = "C:\\Users\\chapa\\Documents\\codigo.txt";
            mostrarArchivoNumerado(ruta);
            cargarArchivo(ruta);
            analizar();
            mostrarTokens();
        }
        catch(Exception excepcion){
            System.out.println("Error: "+excepcion.getMessage());
        }
    }
}