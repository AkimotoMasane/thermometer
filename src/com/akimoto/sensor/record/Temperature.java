package com.akimoto.sensor.record;


public class Temperature {

	private int year;
	private int month;
	private int day;
	private int hour;
	private int minute;
	private int second;
	
	private float temperature;
	
	
	public Temperature(int year, int month, int day, int hour, int minute, int second, float temprature) {

		this.year = year;

		this.month = month;

		this.day = day;
		
		this.hour = hour;

		this.minute = minute;

		this.second = second;

		this.temperature = temprature;
	}


	public int getYear() {

		return this.year;
	}


	public int getMonth() {

		return this.month;
	}


	public int getDay() {

		return this.day;
	}


	public int getHour() {

		return this.hour;
	}


	public int getMinute() {

		return this.minute;
	}


	public int getSecond() {

		return this.second;
	}


	public float getTemperature() {

		return this.temperature;
	}


	public Temperature createDeepCopy() {

		Temperature deepCopy = new Temperature(this.year, this.month, this.day, this.hour, this.minute, this.second, this.temperature);

		return deepCopy;
	}
}
