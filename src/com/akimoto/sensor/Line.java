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
	 * �J�n�|�C���g���擾����
	 * @return �J�n�|�C���g
	 */
	public PointF getStartPoint() {

		return this.startPoint;
	}


	/**
	 * �I�[�|�C���g���擾����
	 * @return �I�[�|�C���g
	 */
	public PointF getStopPoint() {

		return this.stopPoint;
	}
}
