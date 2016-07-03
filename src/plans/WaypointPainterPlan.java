package plans;

import java.util.HashMap;
import java.util.Map;

import graph.Node;
import graph.WaypointGraph;
import jadex.bdiv3.BDIAgent;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.PlanBody;
import jadex.extension.envsupport.environment.space2d.ContinuousSpace2D;
import jadex.extension.envsupport.math.Vector2Int;
import jadex.micro.annotation.Agent;

@Plan
public class WaypointPainterPlan {

	@Agent
	BDIAgent agent;
	
	protected WaypointGraph g;
	protected ContinuousSpace2D space;
	
	public WaypointPainterPlan(BDIAgent agent, WaypointGraph g, ContinuousSpace2D space) {
		this.agent = agent;
		this. g = g;
		this.space = space;
	}
	
	@PlanBody
	public void WaypointPainterPlanBody() {
		for(Node n : g.nodes){
			Map<String, Vector2Int> wp = new HashMap<String, Vector2Int>();
			wp.put("position", new Vector2Int(n.getX(), n.getY()));
			space.createSpaceObject("waypoint", wp, null);
		}
	}
	
}
