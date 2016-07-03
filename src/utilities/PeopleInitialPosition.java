package utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import constants.Constants;

public class PeopleInitialPosition{

	private static ArrayList<PeopleInfo> positions;
	private static int currentPosition;
	
	public PeopleInitialPosition() {
		positions = new ArrayList<PeopleInfo>();
		currentPosition = 0;
		readPositionsInput();
	}
	
	private void readPositionsInput(){
		String line = null;
    	try (
    	    InputStream fis = new FileInputStream("initialPositions/" + Constants.POSITION_FILE);
    	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
    	    BufferedReader br = new BufferedReader(isr);
    	) {
    	    while ((line = br.readLine()) != null) {
    	    	if(line.trim().indexOf('X') == 0)
    	    	    continue;
    	    	
    	    	String sp[] = line.split("\\|");
    	    	int x = Integer.parseInt(sp[0].trim());
    	    	int y = Integer.parseInt(sp[1].trim());
    	    	int pc = Integer.parseInt(sp[2].trim());
    	    	double pl = Double.parseDouble(sp[3].trim());
    	    	int lh = Integer.parseInt(sp[4].trim());
    	    	boolean kn = Boolean.parseBoolean(sp[5].trim());
    	    	positions.add(new PeopleInfo(x, y, pc, pl, lh, kn));
    	    }
    	} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static PeopleInfo getNextPosition(){
		if(currentPosition < positions.size()){
			PeopleInfo pos = positions.get(currentPosition);
			currentPosition++;
			return pos;
		} else {
			return null;
		}
	}
}
