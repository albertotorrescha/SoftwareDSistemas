package compilador.semantico;

/**
 * Tipos que el analizador puede inferir a partir de las expresiones.
 */
public enum TipoDato {
    ENTERO,
    DECIMAL,
    CADENA,
    BOOLEANO,
    NULO,
    VACIO,
    DESCONOCIDO;

    public boolean esNumerico() {
        return this == ENTERO || this == DECIMAL;
    }
}
