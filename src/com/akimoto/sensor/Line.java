package com.akimoto.sensor;


import android.graphics.PointF;


public class Line {

	private PointF startPoint;

	private PointF stopPoint;


	public Line(PointF start, PointF stop) {

		this.startPoint = start;

		this.stopPoint = stop;
	}


	/**
	 * 開始ポイントを取得する
	 * @return 開始ポイント
	 */
	public PointF getStartPoint() {

		return this.startPoint;
	}


	/**
	 * 終端ポイントを取得する
	 * @return 終端ポイント
	 */
	public PointF getStopPoint() {

		return this.stopPoint;
	}
}
