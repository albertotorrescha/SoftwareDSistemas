package compilador.lexico;

public class Nodo {

    public char caracter;
    public Nodo siguiente;
    public Nodo anterior;

    public Nodo(char caracter){
        this.caracter = caracter;
    }
}