import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import constants.Constants;
import graph.WaypointGraph;
import jadex.bridge.service.types.clock.IClockService;
import jadex.commons.SimplePropertyObject;
import jadex.extension.envsupport.environment.IEnvironmentSpace;
import jadex.extension.envsupport.environment.ISpaceProcess;
import jadex.extension.envsupport.environment.space2d.Space2D;
import jadex.extension.envsupport.math.Vector2Int;
import utilities.PeopleInitialPosition;


public class FloorPlanCreationProcess extends SimplePropertyObject implements ISpaceProcess {

	static String grid[][] = new String[Constants.GX][Constants.GY];
    
	private int getWallType(int x, int y){
		int down = y + 1;
		if(down >= Constants.GY) return 1;
		if(grid[x][down].equals("W")){
			return 2;
		} else {
			return 1;
		}
	}
	
    @Override
    public void start(IClockService arg0, IEnvironmentSpace arg1) {

        Space2D space = (Space2D)arg1;

        int maxX = space.getAreaSize().getXAsInteger();
        int maxY = space.getAreaSize().getYAsInteger();
    
        readInputSpace();
        placeObjects(maxX, maxY, space);
        
        PeopleInitialPosition PIP = new PeopleInitialPosition();
        WaypointGraph.grid = grid;
    }

    private void readInputSpace(){
    	String line;
    	int x = 0;
    	int y = 0;
    	try (
    	    InputStream fis = new FileInputStream("space/" + Constants.SPACE_FILE);
    	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
    	    BufferedReader br = new BufferedReader(isr);
    	) {
    	    while ((line = br.readLine()) != null) {
    	    	String sp[] = line.split("\\|");
    	    	x = 0;
    	    	for(int i = 1; i < Constants.GX+1; i++){
    	    		grid[x][y] = sp[i];    	    	
    	    		x++;
    	    	}
    	    	y++;
    	    }
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    private void placeObjects(int maxX, int maxY, Space2D space){
    	for(int y = 0; y < maxY; y++){
        	for(int x = 0; x < maxX; x++){
        		String str = grid[x][y];
        		if(str.equals("W")){
        			Map<String, Object> obstacle = new HashMap<String, Object>();
        			obstacle.put("position", new Vector2Int(x, y));
        			obstacle.put("type", getWallType(x,y));
        			space.createSpaceObject("obstacle", obstacle, null);
        		}
        		else if(str.equals("O") || str.equals("0")){
        			Map<String, Object> obstacle = new HashMap<String, Object>();
        			obstacle.put("position", new Vector2Int(x, y));
        			obstacle.put("type", 0);
        			space.createSpaceObject("obstacle", obstacle, null);
        		} else if(str.equals("E")){
        			Map<String, Object> door = new HashMap<String, Object>();
        			door.put("position", new Vector2Int(x, y));
        			space.createSpaceObject("door", door, null);
        		}
        	}
        }
    }
    
    @Override
    public void shutdown(IEnvironmentSpace iEnvironmentSpace) {

    }

    @Override
    public void execute(IClockService iClockService, IEnvironmentSpace iEnvironmentSpace) {

    }
}
