package goals;

import graph.Node;
import jadex.bdiv3.annotation.Goal;

@Goal
public class MoveToTargetGoal {
	public Node target;
	
	public MoveToTargetGoal(Node target){
		this.target = target;
	}
}
