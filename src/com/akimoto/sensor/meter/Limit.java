package com.akimoto.sensor.meter;

public class Limit {

	private static float DEFAULT_HIGHEST_LIMIT = 10.0f;
	private static float DEFAULT_LOWEST_LIMIT = -10.0f;

	private Position highest;

	private Position lowest;


	public Limit(float max, float min) {

		if (min < max) {

			this.highest = new Position(max);

			this.lowest = new Position(min);

		} else {

			this.highest = new Position(Limit.DEFAULT_HIGHEST_LIMIT);

			this.lowest = new Position(Limit.DEFAULT_LOWEST_LIMIT);
		}
	}


	public void setHighestLimit(float value) {

		if (this.lowest.getPosition() < value) {

			this.highest.setPosition(value);

		} else {

			float limit = this.lowest.getPosition() + 1.0f;

			this.highest.setPosition(limit);
		}
	}


	public float getHighestLimit() {

		return this.highest.getPosition();
	}


	public void setLowestLimit(float value) {

		if (value < this.highest.getPosition()) {

			this.lowest.setPosition(value);

		} else {

			float limit = this.highest.getPosition() - 1.0f;

			this.lowest.setPosition(limit);
		}
	}


	public float getLowestLimit() {

		return this.lowest.getPosition();
	}
}
