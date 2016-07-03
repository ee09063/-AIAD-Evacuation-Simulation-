package utilities;

import java.util.ArrayList;

import graph.Node;

public class Bresenham{
	
	public Bresenham(){}
	
	public static ArrayList<Pair<Integer,Integer>> sqs = new ArrayList<Pair<Integer,Integer>>();
	
	public ArrayList<Pair<Integer,Integer>> BresenhamPlotting(Node n1, Node n2){
		ArrayList<Pair<Integer,Integer>> result = new ArrayList<Pair<Integer,Integer>>();
		int x1 = n1.getX();
        int y1 = n1.getY();
        int x2 = n2.getX();
        int y2 = n2.getY();
		int dx = Math.abs(x2 - x1);   
	    int dy = Math.abs(y2 - y1);
	    int ix;
	    int iy;
	    
	    if (x1 < x2)
	        ix = 1;           
	    else
	        ix = -1;         

	    if (y1 < y2)
	        iy = 1;        
	    else
	        iy = -1;          

	    int e = 0;               

	    for(int i = 0; i < dx + dy; i++){
	    	result.add(new Pair<Integer,Integer>(x1,y1));
	        int e1 = e + dy;
	        int e2 = e - dx;
	        if (Math.abs(e1) < Math.abs(e2)){
	            x1 += ix;
	            e = e1;
	        }
	        else{
	            y1 += iy;
	            e = e2;
	        }
	    }
	    return result;
	}
	/*
    public ArrayList<Pair<Integer,Integer>> BresenhamPlotting(Node n1, Node n2) {
    	ArrayList<Pair<Integer,Integer>> result = new ArrayList<Pair<Integer,Integer>>();
        int x1 = n1.getX();
        int y1 = n1.getY();
        int x2 = n2.getX();
        int y2 = n2.getY();
    	
    	int d = 0;
 
        int dy = Math.abs(y2 - y1);
        int dx = Math.abs(x2 - x1);
 
        int dy2 = (dy << 1); // slope scaling factors to avoid floating
        int dx2 = (dx << 1); // point
 
        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;
 
        if (dy <= dx) {
            for (;;) {
                //plot(g, x1, y1);
            	result.add(new Pair<Integer,Integer>(x1,y1));
            	sqs.add(new Pair<Integer,Integer>(x1,y1));
                if (x1 == x2)
                    break;
                x1 += ix;
                d += dy2;
                if (d > dx) {
                    y1 += iy;
                    d -= dx2;
                }
            }
        } else {
            for (;;) {
                //plot(g, x1, y1);
            	result.add(new Pair<Integer,Integer>(x1,y1));
            	sqs.add(new Pair<Integer,Integer>(x1,y1));
                if (y1 == y2)
                    break;
                y1 += iy;
                d += dx2;
                if (d > dy) {
                    x1 += ix;
                    d -= dy2;
                }
            }
        }
        return result;
    }*/
}














