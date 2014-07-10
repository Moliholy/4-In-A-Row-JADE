/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya;

import cuatroenraya.elementos.*;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Ontología de comunicación que para jugar a las Cuatro en Raya
 * 
 * @author pedroj
 */
public class OntologiaCuatroEnRaya extends BeanOntology {
    
    	private static final long serialVersionUID = 1L;

	// NOMBRE
	public static final String ONTOLOGY_NAME = "Ontologia_Cuatro_en_Raya";
        
        //VOCABULARIO
        public static final String REGISTRO_TABLERO = "Tablero Cuatro en Raya";
        public static final String REGISTRO_JUGADOR = "Jugador Cuatro en Raya";
        public static final String TIPO_JUEGO = "Cuatro en Raya";
        public static final int FICHA_AZUL = 1;
        public static final int FICHA_ROJA = 2;
        public static final int LIBRE = 0;

	// The singleton instance of this ontology
	private static Ontology INSTANCE;

	public synchronized final static Ontology getInstance() throws BeanOntologyException {
		if (INSTANCE == null) {
			INSTANCE = new OntologiaCuatroEnRaya();
		}
		return INSTANCE;
	}

	/**
	 * Constructor
	 * 
	 * @throws BeanOntologyException
	 */
	private OntologiaCuatroEnRaya() throws BeanOntologyException {
	
            super(ONTOLOGY_NAME);
        
            add("cuatroenraya.elementos");
	}

    
}
