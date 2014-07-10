/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cuatroenraya;

import cuatroenraya.elementos.*;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedroj
 */
public class PruebaOntologia extends Agent {
    
    private ContentManager manager = (ContentManager) getContentManager();
	
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec = new SLCodec();

    // La ontología que utilizará el agente
    private Ontology ontology;
    
    @Override
    protected void setup() {
        try {
            ontology = OntologiaCuatroEnRaya.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(PruebaOntologia.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerLanguage(codec);
	manager.registerOntology(ontology);
        
        Tablero tablero = new Tablero(6,7); // prueba del tablero
        Ficha ficha = new Ficha(OntologiaCuatroEnRaya.FICHA_ROJA); //Ficha de prueba
        Jugador jugador = new Jugador(getAID(), ficha); //nos añadimos como jugador
        Juego juego = new Juego(OntologiaCuatroEnRaya.TIPO_JUEGO); // creamos el juego
        Partida partida = new Partida("Partida de prueba", juego); // creamos la partida
        
        Jugar jugar = new Jugar(partida,tablero,jugador); // Creamos el cuerpo de la acción
        
        addBehaviour(new PruebaEnvioAccionJuego(this, jugar));
        addBehaviour(new PruebaRecepcionAccionJuego(this));
    }
    
    @Override
    protected void takeDown() {
        System.out.println("\n" + getName() + " Terminadas las tareas ...");
    }
    
    class PruebaEnvioAccionJuego extends OneShotBehaviour {
        
        Jugar jugar;

	public PruebaEnvioAccionJuego(Agent a, Jugar jugar) {
            super(a);
            this.jugar = jugar;
	}

        @Override
        public void action() {
            try {
                System.out.println("\nAGENTE PRUEBA: envío acción Jugar");

		// Crear el mensaje
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		AID receiver = getAID(); // Send the message to myself

		msg.setSender(getAID());
		msg.addReceiver(receiver);
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());

		//Creación de la acción que se enviará
		Action a = new Action(getAID(), jugar);
		manager.fillContent(msg, a);

                System.out.println(msg);
		
                send(msg);
                
                System.out.println("\nMensaje enviado....");
            } catch (Exception e) {
		e.printStackTrace();
            }  
        }
    } 
    
    class PruebaRecepcionAccionJuego extends CyclicBehaviour {

        public PruebaRecepcionAccionJuego(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    try {
                        System.out.println("\nAGENTE PRUEBA: recepción acción jugar");
                        System.out.println(msg);
                        Action a = (Action) manager.extractContent(msg);
                        Jugar jugar = (Jugar) a.getAction();

			System.out.println("\nEl identificador de la partida es: " + jugar.getPartida().getId());
			System.out.println("\nEl juego es: " + jugar.getPartida().getJuego().getTipo());
			System.out.println("\nLas dimensiones del tablero son... " + jugar.getTablero());
                        System.out.println("\nEl jugador es: " + jugar.getOponente().getJugador().getName());
                        System.out.println("\nEl color de la ficha es: " + jugar.getOponente().getFicha());

			// Presentación del cuerpo de la acción

			doDelete();
                    } catch (Exception e) {
			e.printStackTrace();
                    }
		} else {
                    block();
		}
        }
        
    }
}
