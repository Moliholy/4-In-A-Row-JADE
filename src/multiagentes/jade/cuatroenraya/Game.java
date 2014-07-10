/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multiagentes.jade.cuatroenraya;

import cuatroenraya.OntologiaCuatroEnRaya;
import cuatroenraya.elementos.Ficha;
import cuatroenraya.elementos.Ganador;
import cuatroenraya.elementos.Juego;
import cuatroenraya.elementos.Jugador;
import cuatroenraya.elementos.Jugar;
import cuatroenraya.elementos.Movimiento;
import cuatroenraya.elementos.MovimientoRealizado;
import cuatroenraya.elementos.Partida;
import cuatroenraya.elementos.Posicion;
import cuatroenraya.elementos.PosicionarFicha;
import cuatroenraya.elementos.Tablero;
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
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import static multiagentes.jade.cuatroenraya.GameStatus.DRAW;
import static multiagentes.jade.cuatroenraya.GameStatus.PLAYER1_WINS;
import static multiagentes.jade.cuatroenraya.GameStatus.PLAYER2_WINS;
import static multiagentes.jade.cuatroenraya.GameStatus.TURN_PLAYER1;
import static multiagentes.jade.cuatroenraya.GameStatus.TURN_PLAYER2;
import static multiagentes.jade.cuatroenraya.GameStatus.UNBEGUN;
import multiagentes.jade.utils.AgentHelper;

/**
 * Controlador del juego.
 *
 * @author Molina
 */
public class Game extends Agent {

    public static final String LOSE = "lose";
    public static final String WIN = "win";
    public static final String DRAW = "draw";
    public static final int REQUIRED_PLAYERS = 2;
    protected final int WIN_COUNT = 4;//numero de fichas necesarias para ganar (hay que conectar 4 en el cuatro en raya, obviamente)
    protected int playersInGame;
    protected int playersReadyToBegin;
    protected int rows;
    protected int columns;
    protected int matchCount;
    protected static final char EMPTY = '-';
    protected static final char PLAYER1 = 'O';
    protected static final char PLAYER2 = 'X';
    protected char[][] board;
    protected GameStatus status;
    protected Jugador players[];
    // protected Window wnd;
    protected ManageActions manageActions;
    protected ReceiveGameNotifications receiveGameNotifications;
    protected Behaviour findPlayers;
    protected Codec codec;
    protected Partida partida;

    /**
     * Acceso al tablero de juego.
     *
     * @return Tablero de juego actual.
     */
    public char[][] getBoard() {
        return board;
    }

    /**
     * Acceso al estado de juego actual.
     *
     * @return Estado actual del juego.
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Envía una invitación a un jugador para que se una al juego.
     *
     * @param player Jugador al que invitar.
     */
    protected void invitePlayer(AID player) {
        AgentHelper.sendMessage(this, player, ACLMessage.PROPOSE, null);
    }

    /**
     * Envía un mensaje del tipo INFORM a un jugador.
     *
     * @param player Jugador al que enviar el mensaje.
     * @param content Contenido del mensaje.
     */
    protected void sendMessageToPlayer(AID player, String content) {
        AgentHelper.sendMessage(this, player, ACLMessage.INFORM, content);
    }

    /**
     * Makes the game wait.
     *
     * @param ms Time in miliseconds.
     */
    private synchronized void makeWait(long ms) {
        try {
            wait(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if there're enough players to begin a new game
     *
     * @return true if there are two players ready to play, or false otherwise
     */
    protected boolean playersReady() {
        return playersInGame == REQUIRED_PLAYERS;
    }

    /**
     * Prints the current board in the console
     */
    protected void printBoard() {
        System.out.println();
        for (int i = 0; i < rows; i++) {
            System.out.println();
            for (int j = 0; j < columns; j++) {
                System.out.print(board[i][j] + " ");
            }
        }
        System.out.println("\n");
    }

    /**
     * Initializes all class' parameters
     */
    protected void init() {
        rows = Board.ROWS;
        columns = Board.COLUMNS;
        playersInGame = 0;
        playersReadyToBegin = 0;
        status = GameStatus.UNBEGUN;

        players = new Jugador[REQUIRED_PLAYERS];

        players[0] = new Jugador();
        players[0].setFicha(new Ficha(OntologiaCuatroEnRaya.FICHA_ROJA));

        players[1] = new Jugador();
        players[1].setFicha(new Ficha(OntologiaCuatroEnRaya.FICHA_AZUL));

        matchCount++;
        board = new char[rows][columns];
        findPlayers = new FindPlayers(this);
        manageActions = null;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = EMPTY;
            }
        }
        //buscamos jugadores para empezar la partida
        findPlayers();
    }

    /**
     * Finds players to be added in order to be able to begin a new game
     */
    protected void findPlayers() {
        status = GameStatus.UNBEGUN;
        addBehaviour(findPlayers);
        if (manageActions != null) {
            removeBehaviour(manageActions);
        }
    }

    /**
     * Changes the behaviour so that it stops attending game resposes and begins
     * managing protocol's conversations
     */
    protected void manageConversations() {
        status = GameStatus.UNBEGUN;
        addBehaviour(manageActions);
        removeBehaviour(findPlayers);
    }

    protected ACLMessage createMovementRequest(AID receiver, Movimiento movement) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        try {
            //creamos el mensaje de filtro y de primer envío
            Ontology ontology = OntologiaCuatroEnRaya.getInstance();
            message.setSender(getAID());
            message.addReceiver(receiver);
            message.setLanguage(codec.getName());
            message.setOntology(ontology.getName());
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            message.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

            //la acción será un PosicionarFicha, tal como pide el guión
            Action action = new Action(getAID(), new PosicionarFicha(partida, movement));
            //llenamos el contenido con la acción
            ContentManager manager = getContentManager();
            manager.fillContent(message, action);
        } catch (Codec.CodecException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OntologyException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        return message;
    }

    /**
     * Lets the show begin
     */
    protected void manageGame() {
        status = GameStatus.TURN_PLAYER1;
        addBehaviour(receiveGameNotifications);
        removeBehaviour(manageActions);
    }

    /**
     * Sends messages to all players whose content specifies the obtained result
     * in the game. It also reinitializes game parameters so that a new game can
     * be begun
     */
    protected void finishGame() {
        final GameStatus st = status;
        Ganador winner = new Ganador();
        winner.setPartida(partida);
        switch (st) {
            case DRAW:
                winner.setJugador(null);
                break;
            case PLAYER1_WINS:
                winner.setJugador(players[0]);
                break;
            case PLAYER2_WINS:
                winner.setJugador(players[1]);
                break;
            default:
                return;
        }
        if (winner.getJugador() != null) {
            addBehaviour(new NotifyWinner(this, winner));

            init();
            findPlayers();
        }
    }

    /**
     * Checks if the current board has at least one square to be filled
     *
     * @return true i the board is not completely filled, or false otherwise
     */
    protected boolean boardFilled() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (board[i][j] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if any player has alredy won
     *
     * @param typeSquare square type that represents the player to be checked
     * @return the game status after the checkout
     */
    protected GameStatus checkWinner(char typeSquare) {
        if (typeSquare == PLAYER1) {
            return GameStatus.PLAYER1_WINS;
        } else if (typeSquare == PLAYER2) {
            return GameStatus.PLAYER2_WINS;
        }
        return null;
    }

    /**
     * Checks the game status after a player's movement
     *
     * @return the new game status
     */
    protected GameStatus checkGameStatus() {
        //ROWS
        for (int i = 0; i < rows; i++) {
            int count = 0;
            char current = EMPTY;
            for (int j = 0; j < columns; j++) {
                char found = board[i][j];
                if (found == current) {
                    count++;
                    if (count == WIN_COUNT && current != EMPTY) {
                        return status = checkWinner(current);
                    }
                } else {
                    count = 0;
                    current = found;
                }
            }
        }

        //COLUMNS
        for (int i = 0; i < columns; i++) {
            int count = 0;
            char current = EMPTY;
            for (int j = 0; j < rows; j++) {
                char found = board[j][i];
                if (found == current) {
                    count++;
                    if (count == WIN_COUNT && current != EMPTY) {
                        return status = checkWinner(current);
                    }
                } else {
                    count = 0;
                    current = found;
                }
            }
        }






        //Once at this point we know no one has won. Now we check the rest of possibilities
        if (boardFilled()) {
            return status = GameStatus.DRAW;
        }

        // if it wasn't draw, then we only change the player's turn
        return status = status == GameStatus.TURN_PLAYER1 ? GameStatus.TURN_PLAYER2 : GameStatus.TURN_PLAYER1;
    }

    /**
     * Controls the actions so that it allows players to move and waits for its
     * response
     *
     * @param lastMovement last movement made on the board
     */
    protected void manageActions(int lastMovement) {
        switch (status) {
            case UNBEGUN:
                //do nothing
                break;
            case TURN_PLAYER1:
                sendMessageToPlayer(players[0].getJugador(), Integer.toString(lastMovement));
                break;
            case TURN_PLAYER2:
                sendMessageToPlayer(players[1].getJugador(), Integer.toString(lastMovement));
                break;
            default:
                finishGame();
        }
    }

    @Override
    protected void setup() {
        super.setup();
        matchCount = 0;
        ContentManager manager = getContentManager();
        codec = new SLCodec();
        manager.registerLanguage(codec);
        Ontology ontology = null;
        try {
            ontology = OntologiaCuatroEnRaya.getInstance();
        } catch (BeanOntologyException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        manager.registerOntology(ontology);
        init();
        AgentHelper.log(this, "Juego preparado y buscando jugadores...");
        // Create and show game window
        //TODO LADIS, esto para ti
        // wnd = new Window();
        // wnd.setVisible(true);
    }

    /////////////////////////////////////////////////////////////////
    //////////////////////////Extra Classes//////////////////////////
    /////////////////////////////////////////////////////////////////
    protected class ReceiveGameNotifications extends AchieveREInitiator {

        public ReceiveGameNotifications(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        /**
         * Aquí se trata el recibo de las jugadas de los jugadores.
         *
         * @param agree mensaje recibido
         */
        @Override
        protected void handleAgree(ACLMessage agree) {
            super.handleAgree(agree);
            AgentHelper.log(myAgent, "Recibido AGREE de " + agree.getSender().getName());
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            super.handleInform(inform);
            AgentHelper.log(myAgent, "Estamos dentro de HandleInform del agente tablero");
            try {
                //tenemos que hacer tres cosas:
                //1º) manejar el mensaje que ha llegado y actualizar el tablero en consecuencia.
                MovimientoRealizado mr = (MovimientoRealizado) getContentManager().extractContent(inform);
                Movimiento mov = mr.getMovimiento();
                Posicion pos = mov.getPosicion();
                int row = pos.getFila();
                int column = pos.getColumna();
                AID player = status == GameStatus.TURN_PLAYER1
                        ? players[0].getJugador() : players[1].getJugador();
                if (board[row][column] == EMPTY) {
                    board[row][column] = player == players[0].getJugador() ? PLAYER1 : PLAYER2;
                }
                //TODO LADIS, ESTA PARTE ES TUYA. MODIFICA ESTO CONVENIENTEMENTE
                /*
                 if (player == players[0].getJugador()) {
                 wnd.addMovement(row, column, 1);
                 } else {
                 wnd.addMovement(row, column, 2);
                 }
                 * */
                printBoard();

                //2º) comprobar si alguien ha ganado y actuar en consecuencia
                checkGameStatus();
                Jugador jugadorActual = null;
                switch (status) {
                    case DRAW:
                    case PLAYER1_WINS:
                    case PLAYER2_WINS:
                        AgentHelper.log(myAgent, "Alguien ha ganado!  " + status.toString());
                        finishGame();
                        return;
                    case TURN_PLAYER1:
                        jugadorActual = players[0];
                        break;
                    case TURN_PLAYER2:
                        jugadorActual = players[1];
                        break;
                    case UNBEGUN://esto no debería pasar
                    default:
                        return;
                }

                //3º) si nadie ha ganado enviar al otro jugador el estado del tablero
                ACLMessage message = createMovementRequest(jugadorActual.getJugador(), mov);

                //mandamos el mensaje al otro jugador para que sepa el movimiento mediante REQUEST
                receiveGameNotifications = new ReceiveGameNotifications(myAgent, message);
                addBehaviour(receiveGameNotifications);
            } catch (Codec.CodecException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UngroundedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            } catch (OntologyException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected class ManageActions extends ContractNetInitiator {

        public ManageActions(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector v) {
            //nos ha respondido afirmativamente un jugador
            System.out.println("El agente " + propose.getSender().getName() + " propone " + propose.getContent());
            if (playersInGame < REQUIRED_PLAYERS) {
                //Los dos primeros en responder se almacenan para jugar
                players[playersInGame].setJugador(propose.getSender());
                playersInGame++;
                System.out.println("Jugador " + propose.getSender().getName()
                        + " agregado exitosamente a la lista de jugadores");
            }
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("El agente " + refuse.getSender().getName() + " reusa");
        }

        @Override
        protected void handleFailure(ACLMessage failure) {

            if (failure.getSender().equals(myAgent.getAMS())) // Fallo en el envío al no encontrar al agente participante
            {
                System.out.println("No se ha encontrado el agente participante");
            } else {
                System.out.println("El agente " + failure.getSender().getName() + " falla");
            }
        }

        /**
         * Método que gestiona las respuestas a los call for proposes que
         * invitan a los jugadores a entrar al tablero de juego.
         *
         * @param responses
         * @param acceptances
         */
        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            // Seleccionamos a los dos primeros que hayan contestado
            int i = 0;
            int j = 1; //Para localizar al oponente
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                ACLMessage reply = msg.createReply();

                if (i < REQUIRED_PLAYERS) {
                    try {
                        ContentManager manager = getContentManager();
                        Action a = (Action) manager.extractContent(msg);
                        Jugar jugar = (Jugar) a.getAction();
                        jugar.setOponente(players[j]); //Incluimos el oponente

                        a.setAction(jugar); //La incluimos en la acción
                        manager.fillContent(reply, a); // Y en el mensaja


                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        acceptances.addElement(reply);

                        j--;
                    } catch (Codec.CodecException | OntologyException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    //Rechazamos si ya tenemos a dos
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                }
                i++;
            }
        }

        /**
         * Método para gestionar el envio de jugadas por parte de los jugadores.
         *
         * @param inform mensaje recibido.
         */
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("El agente " + inform.getSender().getName()
                    + " responde afirmativamente a la realización del juego");
            //System.out.println(inform);
            playersReadyToBegin++;
        }

        @Override
        protected void handleAllResultNotifications(Vector resultNotifications) {
            //Simulación para la comuniciación del ganador
            //Cuando todas las notificaciones hayan sido recibidas
            AgentHelper.log(myAgent, "Entrando en handleAllResultNotifications del tablero");
            if (playersReadyToBegin == REQUIRED_PLAYERS) {
                //comenzamos la partida!
                System.out.println("\n\n¡Que comience la partida!");
                Movimiento firstMovement = new Movimiento(new Ficha(OntologiaCuatroEnRaya.LIBRE),
                        new Posicion(0, 0));
                AID receiver = players[0].getJugador();
                ACLMessage firstMessage = createMovementRequest(receiver, firstMovement);
                AgentHelper.log(myAgent, "\n\nEnviando el mensaje...\n" + firstMessage.getContent());
                receiveGameNotifications = new ReceiveGameNotifications(myAgent, firstMessage);
                //mandado el primer mensaje sólo tenemos que cambiar de comportamiento para recibir respuestas
                addBehaviour(receiveGameNotifications);
            }
        }
    }

    protected class FindPlayers extends CyclicBehaviour {

        public FindPlayers(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            //Buscamos a los jugadores en las páginas amarillas
            try {
                //Buscamos a los agentes jugadores
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription templateSd = new ServiceDescription();
                templateSd.setType(OntologiaCuatroEnRaya.REGISTRO_JUGADOR);
                template.addServices(templateSd);

                //buscamos a los agentes jugadores haciendo uso de la clase propia
                DFAgentDescription[] results = AgentHelper.lookForAvailableAgents(myAgent,
                        Player.SERVICE_TYPE, OntologiaCuatroEnRaya.ONTOLOGY_NAME);

                if (results.length > 1) {
                    // Creamos el mensaje a enviar en el CFP
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    for (DFAgentDescription elm : results) {
                        msg.addReceiver(elm.getName());
                    }

                    msg.setSender(getAID());
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    // Esperamos respuesta por 10 seg.
                    msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

                    Tablero tablero = new Tablero(Board.ROWS, Board.COLUMNS);
                    Juego juego = new Juego(OntologiaCuatroEnRaya.TIPO_JUEGO); // creamos el juego
                    partida = new Partida(Integer.toString(matchCount), juego); // creamos la partida        
                    Jugar jugar = new Jugar(partida, tablero); // Creamos el cuerpo de la acción
                    Ontology ontology = OntologiaCuatroEnRaya.getInstance();
                    ContentManager manager = getContentManager();

                    msg.setLanguage(codec.getName());
                    msg.setOntology(ontology.getName());

                    //Creación de la acción Jugar que se enviará
                    Action a = new Action(getAID(), jugar);
                    manager.fillContent(msg, a);

                    //Implementación del protocolo ContractNet
                    manageActions = new ManageActions(myAgent, msg);
                    manageConversations();
                }
            } catch (FIPAException | Codec.CodecException | OntologyException ex) {
                ex.printStackTrace();
            }
        }
    }

    protected class NotifyWinner extends OneShotBehaviour {

        Ganador ganador;

        public NotifyWinner(Agent a, Ganador ganador) {
            super(a);
            this.ganador = ganador;
        }

        @Override
        public void action() {
            try {
                System.out.println("\nGANADOR: envío comunicación del ganador");

                // Crear el mensaje
                ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                Ontology ontology = OntologiaCuatroEnRaya.getInstance();

                msg.setSender(getAID());
                msg.addReceiver(players[0].getJugador());
                msg.addReceiver(players[1].getJugador());
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());

                ContentManager manager = getContentManager();
                manager.fillContent(msg, ganador);

                //mandamos el mensaje del ganador
                send(msg);

                System.out.println("\nMensaje enviado al ganador y al perdedor...");
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }

            //COMENZAMOS DESDE EL PRINCIPIO TODO!!
            init();
        }
    }
}
