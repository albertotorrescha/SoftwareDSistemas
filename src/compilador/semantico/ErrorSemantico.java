package compilador.semantico;

public class ErrorSemantico {
    private final int linea;
    private final String mensaje;

    public ErrorSemantico(int linea, String mensaje) {
        this.linea = linea;
        this.mensaje = mensaje;
    }

    public int getLinea() { return linea; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        return "Linea " + linea + ": " + mensaje;
    }
}
