package com.akimoto.sensor.record;


import java.io.Serializable;


public class BarPosition implements Serializable {

	private float high;

	private float low;


	public BarPosition() {
	}

	
	public BarPosition(float high, float low) {

		this.high = high;

		this.low = low;
	}
	

	public float getHigh() {

		return this.high;
	}


	public void setHigh(float value) {

		this.high = value;
	}


	public float getLow() {

		return this.low;
	}


	public void setLow(float value) {

		this.low = value;
	}
}
