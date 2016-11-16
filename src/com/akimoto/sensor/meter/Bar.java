package com.akimoto.sensor.meter;

public class Bar {

	private static float DEFAULT_HIGH_POSITION = 10.0f;
	private static float DEFAULT_LOW_POSITION = -10.0f;

	private Position highBar;

	private Position lowBar;


	public Bar() {

		this.highBar = new Position(Bar.DEFAULT_HIGH_POSITION);

		this.lowBar = new Position(Bar.DEFAULT_LOW_POSITION);
	}


	public Bar(float high, float low) {

		if (low < high) {

			this.highBar = new Position(high);

			this.lowBar = new Position(low);

		} else {

			this.highBar = new Position(Bar.DEFAULT_HIGH_POSITION);

			this.lowBar = new Position(Bar.DEFAULT_LOW_POSITION);
		}
	}


	public void setHighBarPosition(float value) {

		float low = this.lowBar.getPosition();

		if (low < value) {

			this.highBar.setPosition(value);
		}
	}


	public float getHighBarPosition() {

		return this.highBar.getPosition();
	}


	public void setLowBarPosition(float value) {

		float high = this.highBar.getPosition();

		if (value < high) {

			this.lowBar.setPosition(value);
		}
	}


	public float getLowBarPosition() {

		return this.lowBar.getPosition();
	}
}
