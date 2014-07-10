/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya.elementos;

import cuatroenraya.OntologiaCuatroEnRaya;
import jade.content.Concept;

/**
 *
 * @author pedroj
 */
public class Ficha implements Concept{
    
    private int color;

    public Ficha () {
  
    }
    
    public Ficha(int tipo) {
        this.color = tipo;
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public int getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return color == OntologiaCuatroEnRaya.FICHA_AZUL ? "FICHA_AZUL" : "FICHA_ROJA";
    }
}
