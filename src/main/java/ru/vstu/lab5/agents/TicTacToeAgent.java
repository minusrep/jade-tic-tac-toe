package ru.vstu.lab5.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ru.vstu.lab5.game.GameBoard;
import ru.vstu.lab5.game.MoveSelector;

/**
 * JADE-агент, играющий в крестики-нолики.
 *
 * Взаимодействие агентов построено через ACL-сообщения с conversation-id = tic-tac-toe.
 * Один агент получает аргумент "starter" и начинает партию крестиками.
 */
public class TicTacToeAgent extends Agent {
    private static final long serialVersionUID = 1L;

    private static final String SERVICE_TYPE = "tic-tac-toe-player";
    private static final String SERVICE_NAME = "tic-tac-toe-service";
    private static final String CONVERSATION_ID = "tic-tac-toe";
    private static final String MESSAGE_MOVE = "MOVE";

    private boolean starter;
    private boolean gameStarted;
    private boolean finished;

    private AID opponent;
    private GameBoard board;
    private char myMark = '?';
    private char opponentMark = '?';

    @Override
    protected void setup() {
        this.board = new GameBoard();
        this.starter = hasStarterArgument();

        registerInDirectoryFacilitator();

        System.out.println(getLocalName() + " is ready. Starter = " + starter);

        addBehaviour(new FindOpponentBehaviour(this, 1000));
        addBehaviour(new ReceiveGameMessagesBehaviour());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException ignored) {
            // Агент мог быть не зарегистрирован при аварийном завершении.
        }
        System.out.println(getLocalName() + " finished work.");
    }

    private boolean hasStarterArgument() {
        Object[] args = getArguments();
        if (args == null) {
            return false;
        }
        for (Object arg : args) {
            if (arg != null && "starter".equalsIgnoreCase(arg.toString())) {
                return true;
            }
        }
        return false;
    }

    private void registerInDirectoryFacilitator() {
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        ServiceDescription service = new ServiceDescription();
        service.setType(SERVICE_TYPE);
        service.setName(SERVICE_NAME + "-" + getLocalName());

        agentDescription.addServices(service);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException e) {
            throw new IllegalStateException("Cannot register agent in DF", e);
        }
    }

    private void searchOpponent() {
        if (opponent != null) {
            return;
        }

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceTemplate = new ServiceDescription();
        serviceTemplate.setType(SERVICE_TYPE);
        template.addServices(serviceTemplate);

        try {
            DFAgentDescription[] results = DFService.search(this, template);
            for (DFAgentDescription result : results) {
                AID candidate = result.getName();
                if (!candidate.equals(getAID())) {
                    opponent = candidate;
                    System.out.println(getLocalName() + " found opponent: " + opponent.getLocalName());
                    return;
                }
            }
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " cannot search opponent: " + e.getMessage());
        }
    }

    private void startGameIfPossible() {
        if (!starter || gameStarted || finished || opponent == null) {
            return;
        }

        myMark = GameBoard.X;
        opponentMark = GameBoard.O;
        gameStarted = true;

        System.out.println(getLocalName() + " starts the game as X.");
        makeMoveAndSend();
    }

    private void handleIncomingMove(ACLMessage message) {
        try {
            String[] parts = message.getContent().split("\\|");
            if (parts.length != 4 || !MESSAGE_MOVE.equals(parts[0])) {
                System.err.println(getLocalName() + " received unsupported message: " + message.getContent());
                return;
            }

            if (opponent == null) {
                opponent = message.getSender();
            }

            char senderMark = parts[1].charAt(0);
            int cell = Integer.parseInt(parts[2]);
            board = GameBoard.fromString(parts[3]);

            configureMarksAfterIncomingMove(senderMark);
            gameStarted = true;

            System.out.println(getLocalName() + " received move from "
                    + message.getSender().getLocalName()
                    + ": " + senderMark + " -> cell " + cell
                    + board.pretty());

            if (finishIfGameEnded()) {
                return;
            }

            makeMoveAndSend();
        } catch (RuntimeException ex) {
            System.err.println(getLocalName() + " cannot process message: " + message.getContent());
            ex.printStackTrace();
        }
    }

    private void configureMarksAfterIncomingMove(char senderMark) {
        if (myMark != '?') {
            return;
        }

        opponentMark = senderMark;
        myMark = senderMark == GameBoard.X ? GameBoard.O : GameBoard.X;

        System.out.println(getLocalName() + " plays as " + myMark + ".");
    }

    private void makeMoveAndSend() {
        if (finished || opponent == null || myMark == '?') {
            return;
        }

        int move = MoveSelector.chooseBestMove(board, myMark, opponentMark);
        if (move < 0) {
            finishIfGameEnded();
            return;
        }

        boolean applied = board.makeMove(move, myMark);
        if (!applied) {
            throw new IllegalStateException("Selected occupied cell: " + move);
        }

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(opponent);
        message.setConversationId(CONVERSATION_ID);
        message.setContent(MESSAGE_MOVE + "|" + myMark + "|" + move + "|" + board.serialize());
        send(message);

        System.out.println(getLocalName() + " sent move: " + myMark + " -> cell " + move + board.pretty());

        finishIfGameEnded();
    }

    private boolean finishIfGameEnded() {
        char winner = board.getWinner();
        if (winner != GameBoard.EMPTY) {
            finished = true;
            System.out.println(getLocalName() + ": game over. Winner = " + winner + board.pretty());
            System.out.println(getLocalName() + " remains active in JADE GUI. Stop it manually in RMA if needed.");
            return true;
        }

        if (board.isFull()) {
            finished = true;
            System.out.println(getLocalName() + ": game over. Draw." + board.pretty());
            System.out.println(getLocalName() + " remains active in JADE GUI. Stop it manually in RMA if needed.");
            return true;
        }

        return false;
    }

    private final class FindOpponentBehaviour extends TickerBehaviour {
        private static final long serialVersionUID = 1L;

        private FindOpponentBehaviour(Agent agent, long period) {
            super(agent, period);
        }

        @Override
        protected void onTick() {
            if (finished) {
                stop();
                return;
            }

            searchOpponent();
            startGameIfPossible();

            if (opponent != null && gameStarted) {
                stop();
            }
        }
    }

    private final class ReceiveGameMessagesBehaviour extends CyclicBehaviour {
        private static final long serialVersionUID = 1L;

        private final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchConversationId(CONVERSATION_ID),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM)
        );

        @Override
        public void action() {
            if (finished) {
                block();
                return;
            }

            ACLMessage message = myAgent.receive(template);
            if (message == null) {
                block();
                return;
            }

            handleIncomingMove(message);
        }
    }
}
