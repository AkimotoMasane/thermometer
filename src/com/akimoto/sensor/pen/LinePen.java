package com.akimoto.sensor.pen;


import android.graphics.Color;
import android.graphics.Paint;


public class LinePen implements Pen {

	// ü‚Ì‘¾‚³
	private static final float LINE_THICKNESS = 5.0f;

	private float thickness;
	
	private int color;


	public LinePen() {

		this.thickness = LinePen.LINE_THICKNESS;
		
		this.color = Color.BLACK;
	}
	

	public LinePen(float thickness) {

		this.thickness = thickness;
		
		this.color = Color.BLACK;
	}


	public LinePen(float thickness, int color) {

		this.thickness = thickness;
		
		this.color = color;
	}


	@Override
	public Paint getPaint() {

		Paint paint = new Paint();

		paint.setStrokeWidth(this.thickness);

		// ü‚ÌF‚ğİ’è‚·‚é
		paint.setColor(this.color);
		
		return paint;
	}	
}
