package compilador;

import compilador.lexico.AnalizadorLexico;
import compilador.sintactico.AnalizadorSintactico;
import compilador.sintactico.nodos.NodoAST;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {
            //Análisis Léxico ──────────────────────────
            AnalizadorLexico lexico = new AnalizadorLexico();
            lexico.cargarArchivo("codigo.txt");
            lexico.analizar();
            lexico.mostrarTokens();

            //Análisis Sintáctico ──────────────────────
            AnalizadorSintactico sintactico =
                new AnalizadorSintactico(lexico.getTokens());

            List<NodoAST> arbol = sintactico.parsear();

            if (sintactico.tieneErrores()) {
                System.out.println("\n====== ERRORES SINTACTICOS ======");
                for (String err : sintactico.getErrores()) {
                    System.out.println("  X " + err);
                }
                System.out.println("=================================");
            } else {
                System.out.println("\nAnalisis sintactico completado sin errores.");
            }

            sintactico.mostrarAST(arbol);

        } catch (AnalizadorSintactico.ErrorSintactico e) {
            System.out.println("\n[ERROR SINTACTICO FATAL] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}