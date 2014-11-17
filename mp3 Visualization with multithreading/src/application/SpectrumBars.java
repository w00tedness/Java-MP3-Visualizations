package application;

import java.util.Random;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class SpectrumBars extends Visualization {
	
	private int numBars, barHeight, rectangleWidth, rectangleHeight, horizontalGap, verticalGap, colorShiftIndex;
	private Color startColor, endColor, backgroundColor;
	private final int VERTICAL_PADDING = 6;
	private final int HORIZONTAL_PADDING = 8;
	private Color[] colorShiftVals;
	private boolean isStartColorShifting;
	private final int COLOR_SHIFT_SPEED = 3;
	Canvas display;
	private double timestamp, duration;
	private float[] magnitudes, phases;
	private boolean updated;
	
	SpectrumBars(int freqMax) {
		isStartColorShifting = false;
		maxFrequency = freqMax;
		numBars = 8;
		barHeight = 12;
		rectangleWidth = 40;
		rectangleHeight = 15;
		horizontalGap = 7;
		verticalGap = 3;
		display = new Canvas(rectangleWidth*numBars+horizontalGap*(numBars+1)+HORIZONTAL_PADDING,
							 rectangleHeight*barHeight+verticalGap*(barHeight+1)+VERTICAL_PADDING);
		startColor = Color.GOLD;
		endColor = Color.BLUE;
		backgroundColor = Color.BLACK;
		updateColorShiftVals();
	}
	

	
	private Color randomColor() {
		int startDifference = 0, endDifference = 0;
		int r = 0, g = 0, b = 0;
		Random randGen = new Random(System.currentTimeMillis());
		while(startDifference < 100 || endDifference < 100) {
			r = Math.abs(randGen.nextInt()%256);
			g = Math.abs(randGen.nextInt()%256);
			b = Math.abs(randGen.nextInt()%256);
			startDifference = Math.abs((int) ( (double) startColor.getRed()*255.0) - r) +
			  		  		  Math.abs((int) ( (double) startColor.getGreen()*255.0) - g) +
			  		  		  Math.abs((int) ( (double) startColor.getBlue()*255.0) - b);
			endDifference = Math.abs((int) ( (double) endColor.getRed()*255.0) - r) +
			  		  		Math.abs((int) ( (double) endColor.getGreen()*255.0) - g) +
			  		  		Math.abs((int) ( (double) endColor.getBlue()*255.0) - b);
		}
		return new Color((double)r/255.0, (double)g/255.0, (double)b/255.0, 1.0);
	}
	
	private void updateColorShiftVals() {
		Color currentColor, finalColor;
		colorShiftIndex = 0;
		if(isStartColorShifting) {
			isStartColorShifting = false;
			currentColor = endColor;
		}
		else {
			isStartColorShifting = true;
			currentColor = startColor;
		}
		finalColor = randomColor();
		double rStart = currentColor.getRed();
		double gStart = currentColor.getGreen();
		double bStart = currentColor.getBlue();
		double rDifference = finalColor.getRed()-rStart;
		double gDifference = finalColor.getGreen()-gStart;
		double bDifference = finalColor.getBlue()-bStart;
		
		double maxDifference = Math.abs(rDifference);
		if(maxDifference < Math.abs(gDifference))
			maxDifference = Math.abs(gDifference);
		if(maxDifference < Math.abs(bDifference))
			maxDifference = Math.abs(bDifference);
		
		int numSteps = (int)(((maxDifference*255.0)/COLOR_SHIFT_SPEED)+.5);
		colorShiftVals = new Color[numSteps];
		
		for(int i = 0; i < numSteps; ++i) {
			colorShiftVals[i] = new Color(rStart+rDifference/(double)numSteps*i,
					 				 gStart+gDifference/(double)numSteps*i,
					 				 bStart+bDifference/(double)numSteps*i,
									 1.0);
		}
	}
	
	private void incrementColors() {
		if(isStartColorShifting)
			startColor = colorShiftVals[colorShiftIndex];
		else
			endColor = colorShiftVals[colorShiftIndex];
		colorShiftIndex += 1;
		if(colorShiftIndex == colorShiftVals.length)
			updateColorShiftVals();
	}
	
	private Color[] getColorVals() {
		Color[] colorVals = new Color[barHeight];
		double rStart = startColor.getRed();
		double gStart = startColor.getGreen();
		double bStart = startColor.getBlue();
		double rEnd = endColor.getRed();
		double gEnd = endColor.getGreen();
		double bEnd = endColor.getBlue();
		for(int i = 0; i < barHeight; ++i) {
			colorVals[i] = new Color(rStart+(rEnd-rStart)/barHeight*i,
									 gStart+(gEnd-gStart)/barHeight*i,
									 bStart+(bEnd-bStart)/barHeight*i,
									 1.0);
		}
		return colorVals;
	}
	
	private int[] processHeights(float[] magnitudes) {

		float sum = 0;
		int bar_count = 0;
		int[] heights = new int[numBars];
		double max = ((((double) magnitudes.length/(double) numBars)*maxFrequency)-600);
		int maxHeight = barHeight;
		for(int i = 0; i < magnitudes.length; ++i) {
			if(i+1 >= (bar_count+1)*(magnitudes.length/numBars)) {
				double val = (double) (numBars-bar_count-1)/numBars*.8;
				sum = -1*sum-600;
				sum += (int)(max-sum)*val;
				heights[bar_count] = (int) ( (double) Math.pow(sum/(max), 6)*maxHeight);
				bar_count+=1;
				sum = magnitudes[i];
			}
			else sum += magnitudes[i];
		}
		if(bar_count != numBars)
			heights[bar_count] = -1*((int) ( (sum/(max-(numBars-bar_count)*20+300))*((double) barHeight)));
		return heights;
	}
	
	public void Update(double timestamp, double duration, float[] magnitudes, float[] phases) {
		updated = true;
		this.timestamp = timestamp;
		this.duration = duration;
		this.magnitudes = magnitudes;
		this.phases = phases;
	}



	@Override
	public void run() {
		if(updated) {
			GraphicsContext context = display.getGraphicsContext2D();
			display.setWidth(rectangleWidth*numBars+horizontalGap*(numBars+1)+HORIZONTAL_PADDING);
			display.setHeight(rectangleHeight*barHeight+verticalGap*(barHeight+1)+VERTICAL_PADDING);
			incrementColors();
			Color[] colorVals = getColorVals();
			int[] heights = processHeights(magnitudes);
			context.setFill(backgroundColor);
			context.fillRect(0, 0, display.getWidth(), display.getHeight());
			for(int bar_index = 0; bar_index < numBars; ++bar_index) {
				if(heights[bar_index] >= barHeight)
					heights[bar_index] = barHeight-1;
				int x_val = rectangleWidth*bar_index+horizontalGap*(bar_index+1)+HORIZONTAL_PADDING/2;
				for(int rectangle_index = 1; rectangle_index <= heights[bar_index]; ++rectangle_index) {
					int y_val = (int) display.getHeight() - (rectangleHeight*rectangle_index+verticalGap*(rectangle_index+1)+VERTICAL_PADDING/2);
					context.setFill(colorVals[rectangle_index]);
					context.fillRect(x_val, y_val, rectangleWidth, rectangleHeight);
				}
			}
		}
		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



	@Override
	public Node getNode() {
		return display;
	}



	@Override
	protected Object call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}