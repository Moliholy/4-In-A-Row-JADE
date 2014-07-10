/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import jade.content.Concept;

/**
 *
 * @author pedroj
 */
public class Juego implements Concept {
    
    private String tipo;
    
    public Juego (){
        
    }

    public Juego(String tipo) {
        this.tipo = tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }
    
}
