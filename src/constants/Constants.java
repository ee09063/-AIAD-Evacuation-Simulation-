package constants;

public class Constants {

	public static int GX = 30;
	public static int GY = 30;
	public static  int height = 200;
	public static int width = 300;
	
	public static double MOVEMENT_MULTIPLIER = 0.5;
	public static int FRAMES_PER_SECOND = 30;
	public static int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
	public static double NORMAL_SPEED = 0.5;
	public static double SLOW_SPEED = 0.3;
	
	public static double PANIC_LEVEL_INCREASE = 0.05;
	public static long FALL_DOWN_TIME = 3500;
	public static long REACTION_TIME = 200;
	public static long WORLD_TIME_CICLE = 100;
	public static long STUNNED_TIME = 2000;
	public static int SECURITY_WAIT_TIME = 5000;
	public static int WORLD_SAFETY_TIME = 2500;
	
	public static int PANIC_THRESHOLD = 75;
	public static int AGRO_THRESHOLD = 30;
	
	public static int COLLISION_VALUE = 10;
	public static int COLLISION_TRAMPLE = 20;
	
	public static String SPACE_FILE = "space5.txt";
	public static String POSITION_FILE = "initialPositions5.txt";
}
