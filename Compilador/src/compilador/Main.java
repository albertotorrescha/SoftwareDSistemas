package compilador;

import compilador.lexico.AnalizadorLexico;

public class Main {

    public static void main(String[] args) {

        try {
            AnalizadorLexico lexico = new AnalizadorLexico();

            lexico.cargarArchivo("codigo.txt");
            lexico.analizar();
            lexico.mostrarTokens();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}