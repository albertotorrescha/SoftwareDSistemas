package compilador.lexico;

public class Token {

    private String lexema;
    private TipoToken tipo;
    private int linea;

    public Token(String lexema, TipoToken tipo, int linea){
        this.lexema = lexema;
        this.tipo = tipo;
        this.linea = linea;
    }

    public String getLexema(){
        return lexema;
    }

    public TipoToken getTipo(){
        return tipo;
    }

    public int getLinea(){
        return linea;
    }
}