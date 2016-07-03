package agents;

import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.bridge.service.annotation.Service;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;
import plans.WorldMonitorPlan;
import services.WorldInformService;

@Agent
@Description("The World")
@Service
@ProvidedServices(@ProvidedService(type=WorldInformService.class))
@Plans(@Plan(body=@Body(WorldMonitorPlan.class)))
public class WorldBDI implements WorldInformService{
	@Agent
	protected BDIAgent agent;

	@Belief
	protected ContinuousSpace2D space = (ContinuousSpace2D)agent.getParentAccess().getExtension("2dspace").get();
	
	WorldMonitorPlan plan;
	
	public static int noaf = 0;
	public static int nowe = 0;
	
	@AgentBody
		public void body() {
		 	plan = new WorldMonitorPlan(space,agent);
			agent.adoptPlan(plan);
		}

	@Override
	public void gotOutAlive() {
		noaf++;
	}

	@Override
	public void woundedEvent() {
		System.out.println(" > [WORLD] A person got injured");
		nowe++;
	}
	
}
