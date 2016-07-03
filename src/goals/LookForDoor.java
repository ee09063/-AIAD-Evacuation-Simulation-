package goals;
import java.util.ArrayList;

import graph.Node;
import jadex.bdiv3.annotation.Goal;
import jadex.extension.envsupport.math.IVector2;
 
@Goal
public class LookForDoor {
 
	protected IVector2 targetPosition;
	protected ArrayList<Node> path;
	
	public ArrayList<Node> getPath(){return this.path;} 
	
	public LookForDoor(ArrayList<Node> path){
		this.path = path;
	}
}