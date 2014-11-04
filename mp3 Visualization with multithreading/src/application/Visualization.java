package application;

import javafx.scene.Node;

public abstract class Visualization implements Runnable {
	
	
	protected int maxFrequency;
	
	public abstract void Update(double timestamp, double duration, float[] magnitudes, float[] phases);
	
	public abstract Node getNode();
}
