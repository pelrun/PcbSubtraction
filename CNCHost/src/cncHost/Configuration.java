package cncHost;

public class Configuration{
	//Singleton
	private Configuration() {}
		static private Configuration _instance;
		static public Configuration getInstance() {
		if (_instance == null) 
			_instance = new Configuration();
		return _instance;
	}
	/* global data */
	//branding
	public String name = "PcbSubtraction";
	public String version = "0.1";
	//visualizer window configuration
	public int width = 800;
	public int height = 600;
	public int buffer = 20;
	//path configuration
	public double maxPathLength = 1.4;
	//SuperPoint configuration
	public double superPointRadius = 0.7;
	public int superPointDrawingRadius = 7;
	//GCode configuration
	public double probeHigh = 0.5;
	public double probeDepth = -0.08;
	public int feedrate = 300;
	public int probeFeedrate = 300;
}

