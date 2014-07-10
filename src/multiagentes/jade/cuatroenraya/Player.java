/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagentes.jade.cuatroenraya;

import cuatroenraya.OntologiaCuatroEnRaya;
import cuatroenraya.elementos.Ficha;
import cuatroenraya.elementos.Ganador;
import cuatroenraya.elementos.Jugador;
import cuatroenraya.elementos.Jugar;
import cuatroenraya.elementos.Movimiento;
import cuatroenraya.elementos.MovimientoRealizado;
import cuatroenraya.elementos.Posicion;
import cuatroenraya.elementos.PosicionarFicha;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.ContractNetResponder;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import multiagentes.jade.utils.AgentHelper;

/**
 *
 * @author Molina
 */
public class Player extends Agent {

    public static final String SERVICE_NAME = "player";
    public static final String SERVICE_TYPE = "4EnRayaPlayer";
    protected HashMap<AID, Board> boards;//almacenamos las partidas con clave el AID del agent tablero
    protected int wins;
    protected int draws;
    protected int loses;
    protected Codec codec;
    protected ContentManager manager;

    /**
     * Calls the AI to make a movement
     *
     * @param board game status' representation
     * @return the movement to be made expressed as an integer
     */
    protected int play(Board board) throws Exception {
        int maxColumn = Board.COLUMNS;
        int result = -1;
        Random random = new Random();

        do {
            result = board.doMovement(random.nextInt(maxColumn), SquareStatus.FRIENDLY);
        } while (result <= -1);

        return result;
    }

    /**
     * Revome the existing connection with the finished game
     *
     * @param game the game to be removed
     */
    protected void finishGame(AID game) {
        boards.remove(game);
    }

    /**
     *
     * @return the number of winned games
     */
    public int getWins() {
        return wins;
    }

    /**
     *
     * @return the number of drawed games
     */
    public int getDraws() {
        return draws;
    }

    /**
     *
     * @return the number of lost games
     */
    public int getLoses() {
        return loses;
    }

    /**
     *
     * @return the total number of games played
     */
    public int getPlayerGames() {
        return wins + loses + draws;
    }

    @Override
    protected void setup() {
        super.setup();
        wins = draws = loses = 0;
        codec = new SLCodec();
        boards = new HashMap<>();
        Ontology ontology = null;
        try {
            ontology = OntologiaCuatroEnRaya.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager = getContentManager();
        manager.registerLanguage(codec);
        manager.registerOntology(ontology);
        try {
            AgentHelper.registerYellowPages(this, SERVICE_NAME, SERVICE_TYPE,
                    OntologiaCuatroEnRaya.ONTOLOGY_NAME, FIPANames.ContentLanguage.FIPA_SL);
        } catch (FIPAException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
        AgentHelper.log(this, "Connected and successfully registered in yellow pages");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        //adding behaviours
        addBehaviour(new ListenToEndGameNotification(this));
        addBehaviour(new ManageGameEvents(this, template));
    }

    /**
     * Sends an action to a game
     *
     * @param game the game as a message's receiver
     * @param content the message to be sent
     */
    protected void sendAction(AID game, String content) {
        AgentHelper.sendMessage(this, game, ACLMessage.INFORM, content);
    }

    /////////////////////////////////////////////////////////////////
    //////////////////////////Extra Classes//////////////////////////
    /////////////////////////////////////////////////////////////////
    /**
     * Esta clase se encarga de manejar todos los mensajes que se intercambian
     * entre el tablero y el jugador. Aquellos eventos de relevancia son
     * tratados convenientemente actualizando los atributos del jugador.
     */
    protected class ManageGameEvents extends ContractNetResponder {

        public ManageGameEvents(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
            //ha recibido un call for propose. Vamos, que el tablero le informa de que está disponible
            //el jugador SIEMPRE va a aceptar todas las solicitudes de los tableros mediante  un PROPOSE
            ACLMessage propose = cfp.createReply();
            try {
                Action action = (Action) manager.extractContent(cfp);
                Jugar jugar = (Jugar) action.getAction();
                Jugador jugador = new Jugador();
                jugador.setJugador(getAID());
                jugar.setOponente(jugador);
                //Rellenamos el campo oponente con el agente
                //que acepta la propuesta de juego

                action.setAction(jugar); //La incluimos en la acción
                manager.fillContent(propose, action); // Y en el mensaje
            } catch (Codec.CodecException | OntologyException ex) {
                ex.printStackTrace();
            }


            //mandamos finalmente el mensaje al tablero proponiéndole jugar
            propose.setPerformative(ACLMessage.PROPOSE);
            return propose;
        }

        @Override
        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            ACLMessage inform = accept.createReply();
            inform.setPerformative(ACLMessage.INFORM);
            //ponemos como AID identificativa la del tablero con el que vamos a jugar
            AID tablero = accept.getSender();
            try {
                Action action = (Action) manager.extractContent(accept);
                Jugar jugar = (Jugar) action.getAction();
                System.out.println("El oponente del agente: " + getLocalName() + " es el agente: " + jugar.getOponente().getJugador().getLocalName());

                //añadimos al jugador a la lista de partidas que almacena la clase
                boards.put(tablero, new Board());

                //Informamos que estamos conformes en realizar la acción
                manager.fillContent(inform, action);
            } catch (Codec.CodecException | OntologyException ex) {
                ex.printStackTrace();
            }

            //creamos el mensaje de filtro y de primer envío
            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

            //añadimos el comportamiento de recepción de mensajes REQUEST de estado del juego
            addBehaviour(new ListenToGameMovements(myAgent, template));

            AgentHelper.log(myAgent, "Comportamiento de recepción de movimientos añadido");
            //enviamos finalmente el mensaje de aceptación de juego
            return inform;
        }

        @Override
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            super.handleRejectProposal(cfp, propose, reject);
            //esto no puede pasar, pero por si acaso
            System.out.println("El juego " + getLocalName() + ": Proposición rechazada");
        }
    }

    protected class ListenToGameMovements extends AchieveREResponder {

        public ListenToGameMovements(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
            AgentHelper.log(myAgent, "Entrando en el prepareResponse y mandando un AGREE");
            ACLMessage agree = request.createReply();
            agree.setPerformative(ACLMessage.AGREE);
            return agree;
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
            AgentHelper.log(myAgent, "Entrando en el prepareResultNotification y mandando un INFORM con el contenido de la jugada que se va a realizar");
            //TODO hacer la jugada
            try {
                //nos llega un mensaje con la actualización del tablero.
                //tenemos que tratarlo convenientemente y enviar la respuesta al tablero.
                ACLMessage toSendBack = request.createReply();
                toSendBack.setPerformative(ACLMessage.INFORM);

                //gestionamos el movimiento que hay que hacer y rellenamos el mensaje con el contenido apropiado
                Action contenidoMensaje = (Action)manager.extractContent(request);
                PosicionarFicha pf = (PosicionarFicha) contenidoMensaje.getAction();
                Movimiento mov = pf.getAnterior();


                int colorFichaRecibido = mov.getFicha().getColor();
                int miColorFicha = OntologiaCuatroEnRaya.FICHA_AZUL; //ponemos esta por defecto, por si acaso
                switch (colorFichaRecibido) {
                    case OntologiaCuatroEnRaya.LIBRE:
                        //estamos en el primer movimiento
                        miColorFicha = OntologiaCuatroEnRaya.FICHA_ROJA;
                        break;
                    case OntologiaCuatroEnRaya.FICHA_AZUL:
                        miColorFicha = OntologiaCuatroEnRaya.FICHA_ROJA;
                        break;
                    case OntologiaCuatroEnRaya.FICHA_ROJA:
                        miColorFicha = OntologiaCuatroEnRaya.FICHA_AZUL;
                        break;

                }

                Ficha ficha = new Ficha(miColorFicha);
                Board board = boards.get(request.getSender());
                int movement = play(board);
                int row = movement / Board.COLUMNS;
                int column = movement % Board.COLUMNS;
                Movimiento movToSend = new Movimiento(ficha, new Posicion(row, column));
                Jugador yoMismo = new Jugador(getAID(), ficha);
                MovimientoRealizado mrToSend = new MovimientoRealizado(yoMismo, movToSend);
                manager.fillContent(toSendBack, mrToSend);

                //finalmente enviamos el mensaje de vuelta al tablero para que evalue nuestro movimiento
                return toSendBack;

            } catch (Codec.CodecException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UngroundedException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    protected class ListenToEndGameNotification extends CyclicBehaviour {

        public ListenToEndGameNotification(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            //está pendiente de escuchar notificaciones de final de juego del tablero
            //una vez recibida ajustará los atributos convenientemente y buscará de nuevo un tablero
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE));
            if (msg != null) {
                try {
                    System.out.println("\nJUEGO TERMINADO");
                    System.out.println(msg);
                    Ganador ganador = (Ganador) getContentManager().extractContent(msg);
                    Jugador winner = ganador.getJugador();
                    if (winner != null) { //no ha sido empate
                        if (winner.getJugador().getName().equals(getAID().getName())) {
                            wins++;
                        } else {
                            loses++;
                        }
                    } else { //ha sido empate
                        draws++;
                    }
                    finishGame(msg.getSender());
                    //y ahora simplemente sigue recibiendo solicitudes de partidas
                } catch (Codec.CodecException | OntologyException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }
}