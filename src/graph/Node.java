package graph;

import java.util.HashMap;
import java.util.Map;

public class Node implements Comparable<Node>{
	private String type;
	private int x;
	private int y;
	public Node parent = null;
	public double dist = Double.MAX_VALUE; // MAX_VALUE assumed to be infinity
	public boolean blocked;
	
	public final Map<Node, Double> neighbours = new HashMap<>();
	
	public Node(int x, int y, String type, boolean blocked){
		this.x = x;
		this.y = y;
		this.type = type;
		this.blocked = blocked;
	}
	
	@Override
	public int compareTo(Node other) {
		return Double.compare(dist, other.dist);
    }
	     
	@Override
	public String toString(){
		return this.getX() + " " + this.getY() + " " + blocked;
	}
	
	public int getX(){return this.x;}
	
	public int getY(){return this.y;}
	
	public String getT(){return this.type;}
	
	public void setParent(Node parent){ this.parent = parent;}
	
	public Node getParent(){ return this.parent;}
}
