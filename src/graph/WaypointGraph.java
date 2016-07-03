package graph;

import java.util.ArrayList;
import java.util.Collections;

import constants.Constants;
import utilities.Bresenham;
import utilities.Pair;


public class WaypointGraph {

	public ArrayList<Node> nodes;
	
	public static String grid[][] = new String[Constants.GX][Constants.GY];
	
	public WaypointGraph(){
		nodes = new ArrayList<Node>();
		createNodes();
		linkNodes();
	}
	
	public ArrayList<Node> getPath(Node target){
		ArrayList<Node> path = new ArrayList<Node>();
		Node n = target;
		while(n != null){
			path.add(n);
			n = n.parent;
		}
		Collections.reverse(path);
		return path;
	}
	
	private void createNodes(){
		for(int y = 0; y < Constants.GY; y++){
        	for(int x = 0; x < Constants.GX; x++){
        		String o = grid[x][y];
        		if(o.equals("W") || o.equals("O") || o.equals("E")){
        			createWaypoint(x,y,o);
        		}
        	}
		}
		linkNodes();
		ArrayList<Node> rnodes = new ArrayList<Node>();
		boolean repeated = false;
		for(Node n : nodes){
			repeated = false;
			if(!(grid[n.getX()][n.getY()].equals("W") || grid[n.getX()][n.getY()].equals("O") || grid[n.getX()][n.getY()].equals("0"))){
				for(Node nr : rnodes){
					if(nr.getX() == n.getX() && nr.getY() == n.getY()){
						repeated = true;
					}
				}
				if(!repeated){
					rnodes.add(n);
				}
			}
		}
		nodes = rnodes;
	}
	
	private void linkNodes(){
		Bresenham br = new Bresenham();	
		for(Node n1 : nodes){
			for(Node n2: nodes){
				if(!n1.equals(n2)){
					ArrayList<Pair<Integer,Integer>> line = br.BresenhamPlotting(n1,n2);
					if(!intersectsWithObs(line)){
						n1.neighbours.put(n2, distance(n1,n2));
					}
				}
			}
		}
	}
	
	public void addNewNode(int x, int y){
    	Node node = new Node(x, y, "waypoint",false);
    	nodes.add(node);
    	Bresenham br = new Bresenham();
    	for(Node n : nodes){
    		ArrayList<Pair<Integer,Integer>> line = br.BresenhamPlotting(node, n);
    		if(!intersectsWithObs(line)){
    			double distance = distance(n,node);
    			n.neighbours.put(node, distance);
    			node.neighbours.put(n, distance);
    		}
    	}
    }
	
	public boolean intersectsWithObs(ArrayList<Pair<Integer,Integer>> line){
		for(Pair<Integer,Integer> square : line){
			String str = grid[square.getFirst()][square.getSecond()];
			if(str.equals("W") || str.equals("O")){
				return true;
			}
		}
		return false;
	}
	
	private double distance(Node n1, Node n2){
		int x1 = n1.getX();
		int x2 = n2.getX();
		int y1 = n1.getY();
		int y2 = n2.getY();
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	private void createWaypoint(int x, int y, String obj){
		int up = y-1;
		int down = y+1;
		int left = x-1;
		int right = x+1;
		
		if(obj.equals("E")){
			if(up < 0){
				if(grid[x][down].equals("0") || grid[x][down].equals("W")){
					this.nodes.add(new Node(x,y,"door",true));
					return;
				}
			} else if(down >= Constants.GY){
				if(grid[x][up].equals("0") || grid[x][up].equals("W")){
					this.nodes.add(new Node(x,y,"door",true));
					return;
				}
			} else if(right >= Constants.GX){
				if(grid[left][y].equals("0") || grid[left][y].equals("W")){
					this.nodes.add(new Node(x,y,"door",true));
					return;
				}
			} else if(left < 0){
				if(grid[right][y].equals("0") || grid[right][y].equals("W")){
					this.nodes.add(new Node(x,y,"door",true));
					return;
				}
			}
			this.nodes.add(new Node(x,y,"door",false));
			return;
		}
		
		String u = "filler";
		String d = "filler";
		String l = "filler";
		String r = "filler";
		
		if(up >= 0) u = grid[x][up];
		if(down < Constants.GY) d = grid[x][down];
		if(left >= 0) l = grid[left][y];
		if(right < Constants.GX) r = grid[right][y];
		
		//Q1
		if(!u.equals("filler") && u.equals(" ") && !r.equals("filler") && r.equals(" ")){
			nodes.add(new Node(right,up,"waypoint",false));
		}
		//Q2
		if(!u.equals("filler") && u.equals(" ") && !l.equals("filler") && l.equals(" ")){
			nodes.add(new Node(left,up,"waypoint",false));
		}
		//Q3
		if(!l.equals("filler") && l.equals(" ") && !d.equals("filler") && d.equals(" ")){
			nodes.add(new Node(left,down,"waypoint",false));
		}
		//Q4
		if(!d.equals("filler") && d.equals(" ") && !r.equals("filler") && r.equals(" ")){
			nodes.add(new Node(right,down,"waypoint",false));
		}
	}
	
	public Node closestDoor(){
    	Node closest = null;
    	double maxDist = Double.MAX_VALUE;
    	for(Node n : nodes){
    		if(n.dist < maxDist && n.getT().equals("door")){
    			closest = n;
    			maxDist = n.dist;
    		}
    	}
    	return closest;
    }
	
	public Node getNode(int x, int y){
		for(Node n : nodes){
			if(n.getX() == x && n.getY() == y){
				return n;
			}
		}
		return null;
	}
}