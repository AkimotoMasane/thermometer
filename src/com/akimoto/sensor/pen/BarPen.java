package com.akimoto.sensor.pen;


import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;


public class BarPen implements Pen {

	// 線の太さ
	private static final float LINE_THICKNESS = 2.0f;

	// 点線の間隔
	private static final float DOT_ON_DISTANCE = 40.0f;
	private static final float DOT_OFF_DISTANCE = 20.0f;

	// 線のスタイル
	private Style style;

	// 線の色
	private int color;


	public BarPen() {

		// 実線
		this.style = Style.FILL;

		// 赤色線
		this.color = Color.RED;
	}


	public BarPen(Style style, int color) {

		// 線の種類
		this.style = style;

		// 線の色
		this.color = color;
	}


	@Override
	public Paint getPaint() {

		Paint paint = new Paint();

		// 線の太さを設定する
		paint.setStrokeWidth(BarPen.LINE_THICKNESS);

		// 点線のとき
		if (Style.STROKE == this.style) {

			// 点線の間隔
			float distance[] = new float[] { BarPen.DOT_ON_DISTANCE, BarPen.DOT_OFF_DISTANCE };

			DashPathEffect effect = new DashPathEffect(distance, 0.0f);

			paint.setPathEffect(effect);
		}

		// 線のスタイルを設定する
		paint.setStyle(this.style);

		// 線の色を設定する
		paint.setColor(this.color);

		return paint;
	}
}
