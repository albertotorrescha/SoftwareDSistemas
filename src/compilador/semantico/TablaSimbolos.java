package compilador.semantico;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabla de simbolos con ambitos anidados para variables y funciones.
 */
public class TablaSimbolos {
    private static class Ambito {
        private final String nombre;
        private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

        Ambito(String nombre) {
            this.nombre = nombre;
        }
    }

    private final Deque<Ambito> ambitos = new ArrayDeque<>();
    private final List<Simbolo> historial = new ArrayList<>();

    public TablaSimbolos() {
        abrirAmbito("global");
    }

    public void abrirAmbito(String nombre) {
        ambitos.push(new Ambito(nombre));
    }

    public void cerrarAmbito() {
        if (ambitos.size() > 1) {
            ambitos.pop();
        }
    }

    public String getAmbitoActual() {
        return ambitos.peek().nombre;
    }

    public boolean declarar(Simbolo simbolo) {
        Ambito actual = ambitos.peek();
        if (actual.simbolos.containsKey(simbolo.getNombre())) {
            return false;
        }
        actual.simbolos.put(simbolo.getNombre(), simbolo);
        historial.add(simbolo);
        return true;
    }

    public Simbolo buscar(String nombre) {
        for (Ambito ambito : ambitos) {
            Simbolo simbolo = ambito.simbolos.get(nombre);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }

    public List<Simbolo> getSimbolos() {
        return new ArrayList<>(historial);
    }

    public void mostrar() {
        System.out.println("\n================ TABLA DE SIMBOLOS ================");
        System.out.printf("%-18s %-12s %-14s %-18s %-8s%n",
                "NOMBRE", "TIPO", "CATEGORIA", "AMBITO", "LINEA");
        System.out.println("------------------------------------------------------------------");
        for (Simbolo simbolo : historial) {
            System.out.printf("%-18s %-12s %-14s %-18s %-8d%n",
                    simbolo.getNombre(), simbolo.getTipo(),
                    simbolo.getCategoria(), simbolo.getAmbito(),
                    simbolo.getLinea());
        }
        System.out.println("==================================================================");
    }
}
