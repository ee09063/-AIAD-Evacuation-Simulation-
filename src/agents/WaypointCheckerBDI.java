package agents;

import graph.WaypointGraph;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Body;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Plans;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.Description;
import plans.WaypointPainterPlan;

@Agent
@Description("Only serves to check the waypoint configuration")
@Plans(@Plan(body=@Body(WaypointPainterPlan.class)))
public class WaypointCheckerBDI {
	@Agent
	protected BDIAgent agent;
	
	 @Belief
	 protected ContinuousSpace2D space = (ContinuousSpace2D)agent.getParentAccess().getExtension("2dspace").get();

	 @AgentBody
		public void body() {
		 	System.out.println(" > [WPC] Agent Created");
			agent.adoptPlan(new WaypointPainterPlan(agent, new WaypointGraph(), space));
		}
}