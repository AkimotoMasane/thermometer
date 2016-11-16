package com.akimoto.sensor.pen;


import android.graphics.Color;
import android.graphics.Paint;


public class CirclePen implements Pen {

	// ê¸ÇÃëæÇ≥
	private static final float LINE_THICKNESS = 3.0f;

	private float thickness;

	private int color;


	public CirclePen() {

		this.thickness = CirclePen.LINE_THICKNESS;

		this.color = Color.BLACK;
	}
	
	
	public CirclePen(float thickness) {

		this.thickness = thickness;

		this.color = Color.BLACK;
	}
	
	
	public CirclePen(float thickness, int color) {

		this.thickness = thickness;
		
		this.color = color;
	}


	@Override
	public Paint getPaint() {

		Paint paint = new Paint();

		paint.setStrokeWidth(this.thickness);
		
		paint.setColor(this.color);
		
		return paint;
	}
}
