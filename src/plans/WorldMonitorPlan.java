package plans;
import org.jcodec.common.ArrayUtil;

import agents.WorldBDI;
import constants.Constants;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.extension.envsupport.environment.ISpaceObject;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.micro.annotation.Agent;
 
@Plan
public class WorldMonitorPlan{
 
	@Agent
	BDIAgent agent;
	
	public long waitTime;
	public ContinuousSpace2D space;
	public int alive;
	public int dead;
	public int initialPeople;
	private long simulationTime = 0;
	
	public WorldMonitorPlan(ContinuousSpace2D space, BDIAgent agent) {
		this.space = space;
		this.agent = agent;
	}
	
	@PlanBody
	public void WorldMonitorPlanBody() {
		System.out.println(" > [WORLD] Monitoring Active");
		WorldBDI.noaf = 0;
		boolean done = false;
		ISpaceObject[] agents = (ISpaceObject[]) ArrayUtil.addAll(space.getSpaceObjectsByType("person"),space.getSpaceObjectsByType("security"));
		initialPeople = agents.length;
		
		int noa = 0;
		
		int nopa = 0;
		
		while(!done){
			agents = (ISpaceObject[]) ArrayUtil.addAll(space.getSpaceObjectsByType("person"),space.getSpaceObjectsByType("security"));
			nopa = agents.length;
			noa = 0;
			for(int i = 0; i < agents.length; i++){
				ISpaceObject a = agents[i];
				
				boolean alive = (boolean) a.getProperty("alive");
				boolean wounded = (boolean) a.getProperty("wounded");
				
				if(alive && !wounded){
					noa++;
				}
			}
			agent.waitForDelay(Constants.WORLD_TIME_CICLE).get();
			simulationTime+=Constants.WORLD_TIME_CICLE;
			if(noa == 0){ // No more agents capable of action
				done = true;
			}
		}
		if(nopa > 0) agent.waitForDelay(Constants.WORLD_SAFETY_TIME).get();
		printStats();
		agent.killAgent();
	}
	
	public void printStats(){
		System.out.println("\n =========== SIMULATION OVER ===========");
		System.out.println(" > [STATS] Number of dead: " + (initialPeople - WorldBDI.noaf));
		System.out.println(" > [STATS] Number of living: " + WorldBDI.noaf);
		System.out.println(" > [STATS] Number of wounded events: " + WorldBDI.nowe);
		System.out.println(" > [STATS] Simulation time: " + simulationTime/1000.0 + " seconds");
		System.out.println(" =========== SIMULATION OVER =========== \n");
		WorldBDI.noaf = 0;
		WorldBDI.nowe = 0;
	}
 
}