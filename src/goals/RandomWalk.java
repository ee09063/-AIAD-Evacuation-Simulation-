package goals;
import graph.Node;
import jadex.bdiv3.annotation.Goal;
 
@Goal
public class RandomWalk {
	public Node current;
	public boolean stop = false;
	public RandomWalk(Node current){
		this.current = current;
	}	
}