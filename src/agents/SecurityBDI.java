package agents;

import java.util.ArrayList;
import java.util.Collection;

import constants.Constants;
import goals.GetOut;
import goals.MoveToTargetGoal;
import graph.Node;
import graph.WaypointGraph;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bridge.service.RequiredServiceInfo;
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
import pathfinding.Dijkstra;
import services.IChatService;
import services.WorldInformService;
import utilities.Pair;
import utilities.PeopleInfo;
import utilities.PeopleInitialPosition;

@Agent
@Description("<h1>Agent Security</h1>")
public class SecurityBDI{
    @Agent
    public BDIAgent agent;

    @Belief
    public ContinuousSpace2D space = (ContinuousSpace2D)agent.getParentAccess().getExtension("2dspace").get();

    @Belief
    public ISpaceObject myself = space.getAvatar(agent.getComponentDescription(), agent.getModel().getFullName());
    
    @Belief
    public double speed;
 
    @Belief
    public Pair<Integer,Integer> door;
    
    public WaypointGraph wgraph;
    
    MoveToTargetGoal mttg;
    
    Dijkstra pathfinder;
    
    private ArrayList<ISpaceObject> collisionFilter;
    
     @AgentCreated
     public void init(){
    	 PeopleInfo IF = PeopleInitialPosition.getNextPosition();
    	 myself.setProperty("position", new Vector2Int(IF.x,IF.y));
    	 myself.setProperty("alive", true);
    	 myself.setProperty("wounded", false);
    	 myself.setProperty("direction", 1);
    	 speed = Constants.NORMAL_SPEED;
    	 wgraph = new WaypointGraph();
    	 wgraph.addNewNode(IF.x,IF.y);
    	 pathfinder = new Dijkstra(wgraph);
    	 collisionFilter = new ArrayList<ISpaceObject>();
     }
    
    @AgentBody
    public void body(){
        goToClosestDoor();
        space.destroySpaceObject(myself.getId());
        informWorld();
        agent.killAgent();
    }
    
    public void goToClosestDoor(){
		IVector2 myPos = (IVector2)myself.getProperty(Space2D.PROPERTY_POSITION);
		pathfinder.dijkstra(wgraph.getNode(myPos.getXAsInteger(),myPos.getYAsInteger()));    
		Node CD = wgraph.closestDoor();
		
		ArrayList<Node> path = wgraph.getPath(CD); 
		if(path.size() == 0){
			System.out.println(" > [SECURITY] WHO BUILT THIS?!");
			System.out.println(" > [DEPRESSING] AGENT [SECURITY " + myself.getId() + "] Commited suicide");
			agent.killAgent();
		} else {
			agent.dispatchTopLevelGoal(new GetOut(path)).get();
		}
    }
    
    @Plan(trigger=@Trigger(goals=GetOut.class))
    public void getOut(GetOut goal){
    	ArrayList<Node> path = goal.getPath();
    	int size = goal.getPath().size();
    	for(int i = 0; i < size - 1; i++){
    		Node n = goal.getPath().get(i);
    		moveToTarget(n);
    	}
    	Node exit = path.get(size-1);
    	if(exit.blocked){
    		System.out.println(" > [SECURITY] Oh noes! The exit is blocked!");
    		wgraph.nodes.remove(exit);
    		goToClosestDoor();
    	} else {
	    	door = new Pair<Integer,Integer>(exit.getX(),exit.getY());
	    	informPeopleOfExit(exit.getX(),exit.getY());
	    	//agent.waitForDelay(Constants.SECURITY_WAIT_TIME).get();
	    	moveToTarget(exit);
    	}
    }
    
    void informPeopleOfExit(int x, int y){
    	Collection<IChatService> ss = SServiceProvider.getServices(agent.getServiceProvider(), IChatService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
		for(IChatService cs : ss){
	    	cs.informPeople(x,y);
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
		
    	while(true){
    		destination = new Vector2Double(n.getX(), n.getY());
			location = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);

			double maxdist = speed*Constants.MOVEMENT_MULTIPLIER;
			double dist = ((Space2D)space).getDistance(location, destination).getAsDouble();
			IVector2 newloc = dist <= maxdist ? destination :
				                                destination.copy().subtract(location).normalize().multiply(maxdist).add(location);
			myself.setProperty(Space2D.PROPERTY_POSITION, newloc);
			
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
			agent.waitForDelay(waitTime).get();
    	}
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
    
    public ISpaceObject collision(){
    	IVector2 pos = (IVector2) myself.getProperty(Space2D.PROPERTY_POSITION);
    	ISpaceObject[] people = space.getSpaceObjectsByType("person");
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
    	agent.waitForDelay((long) Constants.REACTION_TIME).get();
    
    	confrontationResolution(person);
    }
    
    void confrontationResolution(ISpaceObject person){
    	if((boolean)person.getProperty("down")){
    		System.out.println(" > [SECURITY] Don't worry, everything is going to be ok");
    	}
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
    
    public void informWorld(){
    	WorldInformService wis = SServiceProvider.getService(agent.getServiceProvider(), WorldInformService.class, RequiredServiceInfo.SCOPE_PLATFORM).get();
    	wis.gotOutAlive();
    }
}