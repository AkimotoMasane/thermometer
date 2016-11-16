package com.akimoto.sensor.meter;

public class Gauge {

	private static float HIGHEST_LIMIT = 125.0f;
	private static float LOWEST_LIMIT = -40.0f;

	private Limit limit;

	private Bar bar;


	/**
	 * 
	 * @param highest 表示する温度の上限値
	 * @param lowest 表示する温度の下限値
	 * @param highPosition 高温バー
	 * @param lowPosition 低温バー
	 */
	public Gauge(float highest, float lowest, float highPosition, float lowPosition) {

		if (Gauge.HIGHEST_LIMIT < highest) {

			highest = Gauge.HIGHEST_LIMIT;
		}

		if (lowest < Gauge.LOWEST_LIMIT) {

			lowest = Gauge.LOWEST_LIMIT;
		}

		this.limit = new Limit(highest, lowest);

		if (highest < highPosition) {

			highPosition = highest;
		}

		if (lowPosition < lowest) {

			lowPosition = lowest;
		}

		this.bar = new Bar(highPosition, lowPosition);
	}


	/**
	 * 温度の上限値を設定する
	 * @param value 温度の上限値
	 */
	public void setHighestLimit(float value) {

		if (value <= Gauge.HIGHEST_LIMIT) {

			this.limit.setHighestLimit(value);
		}
	}


	public float getHighestLimit() {

		float limit = this.limit.getHighestLimit();

		if (Gauge.HIGHEST_LIMIT < limit) {

			limit = Gauge.HIGHEST_LIMIT;
		}

		return limit;
	}


	public void setLowestLimit(float value) {

		if (Gauge.LOWEST_LIMIT <= value) {

			this.limit.setLowestLimit(value);
		}
	}


	public float getLowestLimit() {

		float limit = this.limit.getLowestLimit();

		if (limit < Gauge.LOWEST_LIMIT) {

			limit = Gauge.LOWEST_LIMIT;
		}

		return limit;
	}


	public void setHighBarPosition(float value) {

		float limit = this.limit.getHighestLimit();
		
		if (value < limit) {

			this.bar.setHighBarPosition(value);
		}
	}


	public float getHighBarPosition() {

		float position = this.bar.getHighBarPosition();

		float limit = this.limit.getHighestLimit();
		
		if (limit < position) {

			position = limit;
		}

		return position;
	}


	public void setLowBarPosition(float value) {

		float limit = this.limit.getLowestLimit();
		
		if (limit < value) {

			this.bar.setLowBarPosition(value);
		}
	}


	public float getLowBarPosition() {

		float position = this.bar.getLowBarPosition();

		float limit = this.limit.getLowestLimit();
		
		if (position < limit) {

			position = limit;
		}

		return position;
	}


	public float getMeasure() {

		float highest = this.limit.getHighestLimit();

		float lowest = this.limit.getLowestLimit();

		float measure;

		if ( (0.0f < highest) && (0.0f < lowest) ) {

			measure = highest - lowest;

		} else if ( (0.0f < highest) && (lowest < 0.0f) ) {

			float lower = Math.abs(lowest);

			measure = highest + lower;

		} else {

			float upper = Math.abs(highest);

			float lower = Math.abs(lowest);

			measure = lower - upper;
		}

		return measure;
	}


	public boolean isHighestWarning(float current) {

		float highest = this.bar.getHighBarPosition();

		boolean warning = (highest < current) ? true : false;

		return warning;
	}


	public boolean isLowestWarning(float current) {

		float lowest = this.bar.getLowBarPosition();

		boolean warning = (current < lowest) ? true : false;

		return warning;
	}
}
