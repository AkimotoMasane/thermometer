package com.akimoto.sensor.pen;


import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;


public class BarPen implements Pen {

	// ���̑���
	private static final float LINE_THICKNESS = 2.0f;

	// �_���̊Ԋu
	private static final float DOT_ON_DISTANCE = 40.0f;
	private static final float DOT_OFF_DISTANCE = 20.0f;

	// ���̃X�^�C��
	private Style style;

	// ���̐F
	private int color;


	public BarPen() {

		// ����
		this.style = Style.FILL;

		// �ԐF��
		this.color = Color.RED;
	}


	public BarPen(Style style, int color) {

		// ���̎��
		this.style = style;

		// ���̐F
		this.color = color;
	}


	@Override
	public Paint getPaint() {

		Paint paint = new Paint();

		// ���̑�����ݒ肷��
		paint.setStrokeWidth(BarPen.LINE_THICKNESS);

		// �_���̂Ƃ�
		if (Style.STROKE == this.style) {

			// �_���̊Ԋu
			float distance[] = new float[] { BarPen.DOT_ON_DISTANCE, BarPen.DOT_OFF_DISTANCE };

			DashPathEffect effect = new DashPathEffect(distance, 0.0f);

			paint.setPathEffect(effect);
		}

		// ���̃X�^�C����ݒ肷��
		paint.setStyle(this.style);

		// ���̐F��ݒ肷��
		paint.setColor(this.color);

		return paint;
	}
}
