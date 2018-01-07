package cat.urv.imas.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import cat.urv.imas.behaviour.digger.DiggerBehaviour;
import cat.urv.imas.behaviour.prospector.ProspectorBehaviour;
import cat.urv.imas.onthology.DiggerInfoAgent;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InformAgentAction;
import cat.urv.imas.onthology.MobileAgentAction;
import cat.urv.imas.onthology.MoveAction;
import cat.urv.imas.onthology.RoundStart;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.map.FieldCell;

public class ProspectorAgent extends ImasAgent implements MovingAgentInterface {

    private int currentX;
    private int currentY;
    private AID prospectorCoordinator;
    private GameSettings game;
    private long roundEnd;
    private ArrayList<FieldCell> detectedMetals = new ArrayList<FieldCell>(); 
    private MobileAgentAction currentAction;
    
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }

    @Override
    public void setup() {
        super.setup();

        //Find prospector coordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        
        
        // TODO implement and add behaviours
        // set starting position
        String[] args = (String[]) getArguments();
        currentX = Integer.parseInt(args[0]);
        currentY = Integer.parseInt(args[1]);

        log("I am at ("+ currentX +","+ currentY +")!");

        // add behaviours
        addBehaviour(new ProspectorBehaviour(this));
    }

    @Override
    public int stepsToPosition(int row, int col) {
        // easy approach: euclidean distance
        int yDistance = Math.abs (row - currentY);
        int xDistance = Math.abs (col - currentX);
        double distance = Math.sqrt((yDistance)*(yDistance) +(xDistance)*(xDistance));
        return (int) Math.ceil(distance);
    }
    
    public void startRound(RoundStart rs) {
        setCurrentPosition(rs.getX(),rs.getY());
        roundEnd = rs.getRoundEnd();
    }
    
    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }
    
    public void setCurrentPosition(int x, int y) {
        currentX = x;
        currentY = y;
    }
    
    public void moveNextCell() {
    	//Make a random move.
    	int inc_x = ThreadLocalRandom.current().nextInt(-1, 2);
    	int inc_y = ThreadLocalRandom.current().nextInt(-1, 2);
    	currentAction = new MoveAction(currentX + inc_x, currentY + inc_y);
    	log("I want to move to ("+ currentX + "," + currentY +")!");
    	
    	//TODO: Implementation of an intelligent movement to efficiently explore the map.
    }
    
    public void examine() {
    	detectedMetals = game.detectFieldsWithMetal(currentY, currentX);
    	log("Hey fella, I found "+ detectedMetals.size() + " new metals!");
    }
    
    public void informCoordinator() {
    	//TODO: Add detected metals to the message
    	ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(prospectorCoordinator);
        try {
        	getContentManager().fillContent(message, new InformAgentAction(currentAction));
            log("Sending msg with my current action: " + message.getContent());
            send(message);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Some error while sending?");
        }       
    }
}