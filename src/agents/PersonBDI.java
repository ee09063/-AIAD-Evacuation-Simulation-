package agents;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import org.jcodec.common.ArrayUtil;

import constants.Constants;
import goals.GetOut;
import goals.RandomWalk;
import graph.Node;
import graph.WaypointGraph;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.service.RequiredServiceInfo;
import jadex.bridge.service.annotation.Service;
import jadex.bridge.service.search.SServiceProvider;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.IVector2;
import jadex.extension.envsupport.math.Vector2Double;
import jadex.extension.envsupport.math.Vector2Int;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import pathfinding.Dijkstra;
import services.IChatService;
import services.WorldInformService;
import utilities.Pair;
import utilities.PeopleInfo;
import utilities.PeopleInitialPosition;

@Agent
@Service
@ProvidedServices(@ProvidedService(type=IChatService.class))
@Description("Agent Person")
public class PersonBDI  implements IChatService{
    @Agent
    public BDIAgent agent;

    @Belief
    public ContinuousSpace2D space = (ContinuousSpace2D)agent.getParentAccess().getExtension("2dspace").get();

    @Belief
    public ISpaceObject myself = space.getAvatar(agent.getComponentDescription(), agent.getModel().getFullName());
    
    @Belief
    public double speed;
    
    @Belief
    public int physicalCondition;
    
    @Belief
    public double panicLevel;
    
    @Belief
    public int levelheadedness;
    
    @Belief
    public boolean knowledge;
    
    @Belief
    public boolean down;
    
    @Belief
    public boolean stunned;
    
    @Belief
    public Pair<Integer,Integer> door;
    
    public int aggressiveness;
    
    private WaypointGraph wgraph;
    private RandomWalk rwGoal;
    private Dijkstra pathfinder;
    private boolean changeCourse = false;
    private ArrayList<ISpaceObject> collisionFilter;
    
    @AgentCreated
    public void init(){
    	myself.setProperty("id",myself.getId());
		PeopleInfo IF = PeopleInitialPosition.getNextPosition();
		myself.setProperty("position", new Vector2Int(IF.x,IF.y));
		myself.setProperty("alive", true);
		myself.setProperty("wounded", false);
		myself.setProperty("direction", 1);
		down = false;
		myself.setProperty("down",false);
		stunned = false;
		myself.setProperty("stunned",false);
		physicalCondition = IF.pc;
		panicLevel = IF.pl;
		levelheadedness = IF.lh;
		myself.setProperty("physicalCondition", physicalCondition);
		wgraph = new WaypointGraph();
		wgraph.addNewNode(IF.x, IF.y);
		knowledge = IF.kn;
		pathfinder = new Dijkstra(wgraph);
		collisionFilter = new ArrayList<ISpaceObject>();
	}
    
    @AgentBody
    public void body(){
        IVector2 myPos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);
        
        if(knowledge){
	       goToClosestDoor(false, wgraph.getNode(myPos.getXAsInteger(),myPos.getYAsInteger()));
        }
        else{
        	rwGoal = new RandomWalk(wgraph.getNode(myPos.getXAsInteger(), myPos.getYAsInteger()));
        	agent.dispatchTopLevelGoal(rwGoal).get();
        }
        
        if((boolean)myself.getProperty("alive")){
        	space.destroySpaceObject(myself.getId());
        	informWorldAlive();
        	agent.killAgent();
        }
    }
    
    public void goToClosestDoor(boolean random, Node dj){
		pathfinder.dijkstra(dj);    
		Node CD = null;
		ArrayList<Node> path = null;
		if(!random){
			CD = wgraph.closestDoor();
		} else {
			CD = wgraph.getNode(door.getFirst(), door.getSecond());
		}
		path = wgraph.getPath(CD);
		
		if(path.size() == 0){
			System.out.println(" > [PERSON] WHO BUILT THIS?!");
			System.out.println(" > [DEPRESSING] AGENT [PERSON " + myself.getId() + "] Commited suicide");
			space.destroySpaceObject(myself.getId());
			agent.killAgent();
		} else {
			//path.remove(0);
			agent.dispatchTopLevelGoal(new GetOut(path)).get();
		}
    }
    
    @Plan(trigger=@Trigger(goals=GetOut.class))
    public void getOut(GetOut goal){
    	ArrayList<Node> path = goal.getPath();
    	for(Node n : path){
    		if(n.getT().equals("door") && n.blocked){
    			System.out.println(" > [PERSON " + myself.getId() + "] The door is blocked!");
    			wgraph.nodes.remove(n);
    			IVector2 myPos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);
    			goToClosestDoor(false, wgraph.getNode(myPos.getXAsInteger(),myPos.getYAsInteger()));
    		} else { 
    			moveToTarget(n);
    		}
    	}
    }
    
    @Plan(trigger=@Trigger(goals=RandomWalk.class))
    public void randomWalk(RandomWalk goal){
    	Node current = goal.current;
    	while((!current.getT().equals("door") && !current.blocked) && !rwGoal.stop){
    		Set<Node> near = current.neighbours.keySet();
    		for(Node n : near){
    			if(n.getT().equals("door") && !n.blocked){
    				knowledge = true;
    				moveToTarget(n);
    				current = n;
    				return;
    			}
    		}
    		ArrayList<Node> list = new ArrayList<Node>();
        	list.addAll(near);
        	Node nn = list.get(new Random().nextInt(near.size()-1));
        	moveToTarget(nn);
        	current = nn;
    	}
    	if(rwGoal.stop){
    		changeCourse = false;
    		IVector2 myPos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);  		
    		Node dj = addARNode(myPos);	
    		goToClosestDoor(true,dj);
    	}
    }
    
    public void moveToTarget(Node n){
	    	IVector2 destination = new Vector2Double(n.getX(), n.getY());
			IVector2 location = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);
			
			if(destination.getYAsInteger() > location.getYAsInteger()){ // down
				myself.setProperty("direction", 2);
			} else if(destination.getYAsInteger() < location.getYAsInteger()){
				myself.setProperty("direction", 1);
			} else if(destination.getYAsInteger() == location.getYAsInteger()){
				if(destination.getXAsInteger() > location.getXAsInteger()){
					myself.setProperty("direction", 4);
				} else {
					myself.setProperty("direction", 3);
				}
			}
				
			long nextTick = System.currentTimeMillis();
			
	    	while(true && !changeCourse && (boolean)myself.getProperty("alive")){
	    		destination = new Vector2Double(n.getX(), n.getY());
				location = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);
		    	
				double maxdist = speed*Constants.MOVEMENT_MULTIPLIER;
				double dist = ((Space2D)space).getDistance(location, destination).getAsDouble();
				IVector2 newloc = dist <= maxdist ? destination :
					                                destination.copy().subtract(location).normalize().multiply(maxdist).add(location);
				myself.setProperty(Space2D.PROPERTY_POSITION, newloc);
				
				increasePanicLevel();
				
				updateCollisionFilter();
				
				ISpaceObject colPerson = collision();
				
				if(colPerson != null){
					collisionFilter.add(colPerson);
	    			collisionManagement(colPerson);
	    		}
				
				if(location.getXAsDouble() == destination.getXAsDouble() && location.getYAsDouble() == destination.getYAsDouble())
					break;
				
				nextTick += Constants.SKIP_TICKS;
				long waitTime = nextTick - System.currentTimeMillis();
				if(waitTime > 0){
					agent.waitForDelay(waitTime).get();
				} else {
					agent.waitForDelay(40).get();
				}
	    	}
	}
    
    public ISpaceObject collision(){
    	IVector2 pos = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);
    	ISpaceObject[] people = (ISpaceObject[]) ArrayUtil.addAll(space.getSpaceObjectsByType("person"),space.getSpaceObjectsByType("security"));
    	for(ISpaceObject person : people){
    		IVector2 pos2 = (IVector2) person.getProperty(Space2D.PROPERTY_POSITION);
    		if(pos2.equals(pos)){//Found himself
    			continue;
    		}
    		if(collisionFilter.contains(person)){// Found someone he collided with in the very recent past
    			continue;
    		}
    		if(intersection(pos.getXAsDouble() + 0.5, pos.getYAsDouble() + 0.5, 0.5, pos2.getXAsDouble() + 0.5, pos2.getYAsDouble() + 0.5, 0.5)){
    			return person;
    		}
    	}
    	return null;
    }
    
    public void collisionManagement(ISpaceObject person){
    	boolean agroAction = takeAgressiveAction();

    	myself.setProperty("agroAction", agroAction);
    	int agroValue;
    	if(agroAction){
    		agroValue = this.aggressiveness;
    	} else {
    		agroValue = this.physicalCondition;
    	}
    	
    	myself.setProperty("agroValue", agroValue);
    	agent.waitForDelay((long) Constants.REACTION_TIME).get();
    	
    	try{
	    	if(person != null){
		    	boolean oaa = (boolean) person.getProperty("agroAction");
		    	int oav = (int) person.getProperty("agroValue");
		    	boolean od = (boolean) person.getProperty("down");
		    	
		    	confrontationResolution(agroAction, agroValue, oaa, oav, od, person.getType());
	    	}
    	}catch(NullPointerException e) {
    		agent.waitForDelay(Constants.REACTION_TIME);
    		stun();
    	}
    }
    
    void confrontationResolution(boolean aa, int av, boolean oaa, int oav, boolean od, String type){
    	if(type.equals("security") && this.physicalCondition>=30){
    		stun();
    	} else if(type.equals("security") && this.physicalCondition<30){
    		System.out.println(" > [PERSON " + myself.getId() + "] I was helped");
    		updatePC(50);
    		stun();
    	} else if(this.down){ // I'm down
    		if(oaa){ // The other person was aggressive
    			updatePC(this.physicalCondition - Constants.COLLISION_TRAMPLE);
    			fallDown();
    		} else { // The other person was not aggressive -> HELP
    			System.out.println(" > [PERSON " + myself.getId() + "] I was helped");
    			if(this.physicalCondition < 30){
    				updatePC(35);
    			}
    			myself.setProperty("wounded", false);
    			getUp();
    			agent.waitForDelay(Constants.STUNNED_TIME).get();
    		}
    	} else if(!this.down && !od){ // We are both up
    		if(aa && oaa){ // We are both aggressive
    			if(av < oav){ // I lose
    				updatePC(this.physicalCondition - Constants.COLLISION_VALUE);
    				fallDown();
    			}
    		} else if (!aa && !oaa){ // We are both non aggressive
    			if(av < oav){ // I lose
    				stun();
    			}
    		} else { // One of us was agressive
    			if(!aa && oaa){ // The other was aggressive and I was not -> I lose
    				updatePC(this.physicalCondition - Constants.COLLISION_VALUE);
    				fallDown();
    			}
    		}
    	}
    }
    
    @Plan(trigger=@Trigger(factchangeds="physicalCondition"))
    public void updateSpeed(ChangeEvent event) {
		int pc = (int) event.getValue();
		myself.setProperty("physicalCondition", pc);
		double speed = 0;
		if(pc >= 30 && pc <= 60){
			speed = Constants.SLOW_SPEED;
		} else if(pc <= 0){
			speed = 0;
			die();
		} else if(pc > 60){
			speed = Constants.NORMAL_SPEED;
		} 
		myself.setProperty("speed", speed);
		this.speed = speed;
    }
    
    public boolean takeAgressiveAction(){
    	Random rand = new Random();
    	int panicRand = rand.nextInt(100);
    	int agroRand = rand.nextInt(100);
    	this.aggressiveness = agroRand;
    	
    	if(this.panicLevel > Constants.PANIC_THRESHOLD)
    		return true;
    	else if(this.panicLevel < Constants.PANIC_THRESHOLD - 50)
    		return false;
    	
    	if(panicRand >= Constants.PANIC_THRESHOLD){
    		if(agroRand <= Constants.AGRO_THRESHOLD){
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		if(agroRand >= (Constants.AGRO_THRESHOLD*2.5)){
    			return true;
    		} else {
    			return false;
    		}
    	}
    }
    
    void fallDown(){
    	System.out.println(" > [PERSON " + myself.getId() + "] has fallen down!");
    	this.down = true;
    	myself.setProperty("down", true);
    	if(this.physicalCondition < 30){
			speed = 0;
			myself.setProperty("wounded",true);
			if(this.stunned){
				stunned = false;
				myself.setProperty("stunned", false);
			}
			informWorldWounded();
			while(this.physicalCondition < 30){
				updateCollisionFilter();
				
				ISpaceObject colPerson = collision();
				
				if(colPerson != null){
					collisionFilter.add(colPerson);
	    			collisionManagement(colPerson);
	    		}
			}
    	} else {
    		long dftc = 0;
        	while(dftc < Constants.FALL_DOWN_TIME){
        		long start = System.currentTimeMillis();
        		
        		updateCollisionFilter();
				
				ISpaceObject colPerson = collision();
				
				if(colPerson != null){
					collisionFilter.add(colPerson);
	    			collisionManagement(colPerson);
	    		}
        		long dt = System.currentTimeMillis() - start;
        		dftc+=dt;
        	}
        	getUp();
    	}
    }
    
    void getUp(){
    	this.down = false;
    	myself.setProperty("down", false);
    }
    
    void updatePC(int pc){
    	this.physicalCondition = pc;
    	myself.setProperty("physicalCondition", pc);
    }
    
    void stun(){
    	System.out.println(" > [PERSON " + myself.getId() + "] Is stunned!");
    	this.stunned = true;
    	myself.setProperty("stunned", true);
    	long dftc = 0;
    	while(dftc < Constants.STUNNED_TIME){
    		long start = System.currentTimeMillis();
    		
    		updateCollisionFilter();
			
			ISpaceObject colPerson = collision();
			
			if(colPerson != null){
				collisionFilter.add(colPerson);
    			collisionManagement(colPerson);
    		}
			
    		long dt = System.currentTimeMillis() - start;
    		dftc+=dt;
    	}
    	this.stunned = false;
    	myself.setProperty("stunned", false);
    }
    
    void die(){
    	System.out.println(" > [PERSON " + myself.getId() + "] Has died!");
    	myself.setProperty("alive",false);
    }
    
    @Override
   	public void informPeople(int x, int y) {
   		door = new Pair<Integer, Integer>(x,y);
   		if(!knowledge){
   			System.out.println(" > [PERSON " + myself.getId() + "] I was lost but not anymore");
   			rwGoal.stop = true;
   			changeCourse = true;
   		}
   	}
       
    public void informWorldAlive(){
    	WorldInformService wis = SServiceProvider.getService(agent.getServiceProvider(), WorldInformService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
    	wis.gotOutAlive();
    }
   
    public void informWorldWounded(){
    	WorldInformService wis = SServiceProvider.getService(agent.getServiceProvider(), WorldInformService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
    	wis.woundedEvent();
    }
  
    public void increasePanicLevel(){
    	this.panicLevel += (Constants.PANIC_LEVEL_INCREASE * this.levelheadedness);
    	if(this.panicLevel > 100)
    		this.panicLevel = 100;
    }
   
    private void updateCollisionFilter(){
    	IVector2 pos = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);
    	ArrayList<ISpaceObject> newCollisionFilter = new ArrayList<ISpaceObject>();
    	for(ISpaceObject person : collisionFilter){
    		IVector2 pos2 = (IVector2) person.getProperty(Space2D.PROPERTY_POSITION);
    		if(intersection(pos.getXAsDouble() + 0.5, pos.getYAsDouble() + 0.5, 0.5, pos2.getXAsDouble() + 0.5, pos2.getYAsDouble() + 0.5, 0.5)){
    			newCollisionFilter.add(person);
    		}
    	}
    	collisionFilter = newCollisionFilter;
    }
   
    public boolean intersection(double x1, double y1, double r1, double x2, double y2, double r2){
    	double dx = x1 - x2; 
    	double dy = y1 - y2;
   	
    	double d = Math.sqrt((dx*dx) + (dy*dy)); 

    	double r = r1 + r2;
   	
    	if(d < r){ 
    		return true;
    	}
    	return false;
    }
    
    private Node addARNode(IVector2 pos){
    	ISpaceObject obs[] = space.getSpaceObjectsByType("obstacle");
    	for(ISpaceObject o : obs){
    		IVector2 opos = (IVector2) o.getProperty(Space2D.PROPERTY_POSITION);
    		if(opos.getXAsInteger() == pos.getXAsInteger() && opos.getYAsInteger() == pos.getYAsInteger()){
    			double fx = pos.getXAsDouble() - (long)pos.getXAsDouble();
    	    	double fy = pos.getYAsDouble() - (long)pos.getYAsDouble();
    	    	if(fx >= 0.5 && fy >= 0.5){
    	    		wgraph.addNewNode(pos.getXAsInteger()+1,pos.getYAsInteger()+1);
    	    		return wgraph.getNode(pos.getXAsInteger()+1,pos.getYAsInteger()+1);
    	    	} else if(fx > 0.5 && fy < 0.5){
    	    		wgraph.addNewNode(pos.getXAsInteger()+1,pos.getYAsInteger()-1);
    	    		return wgraph.getNode(pos.getXAsInteger()+1,pos.getYAsInteger()+1);
    	    	} else if(fx < 0.5 && fy < 0.5){
    	    		wgraph.addNewNode(pos.getXAsInteger()-1,pos.getYAsInteger()-1);
    	    		return wgraph.getNode(pos.getXAsInteger()+1,pos.getYAsInteger()+1);
    	    	} else {
    	    		wgraph.addNewNode(pos.getXAsInteger()-1,pos.getYAsInteger()-1);
    	    		return wgraph.getNode(pos.getXAsInteger()+1,pos.getYAsInteger()+1);
    	    	}
    		}
    	}
    	
    	wgraph.addNewNode(pos.getXAsInteger(),pos.getYAsInteger());
		return wgraph.getNode(pos.getXAsInteger(),pos.getYAsInteger());
    }
}
