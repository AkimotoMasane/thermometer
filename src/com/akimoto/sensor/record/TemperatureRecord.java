package com.akimoto.sensor.record;


import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;


public class TemperatureRecord {

	private static final int DEFAULT_CAPACITY = 10;
	
	private ArrayBlockingQueue<Temperature> record;
	
	private int capacity;
	
	private Temperature current;

	private Temperature highest;
	
	private Temperature lowest;


	public TemperatureRecord(int capacity) {

		if (capacity < 0) {

			this.capacity = TemperatureRecord.DEFAULT_CAPACITY;

		} else {
		
			this.capacity = capacity;
		}
		
		this.record = new ArrayBlockingQueue<Temperature>(this.capacity);
	}


	public int getCapacity() {

		return this.capacity;
	}
	

	public synchronized void setTemperature(Temperature temperature) {
		
		if (null != temperature) {

			// �󂫂�����Ƃ�
			if (this.record.size() < this.capacity) {

				// �����ɒǉ�����
				this.record.offer(temperature);

			} else {	// �󂫂��Ȃ��Ƃ�

				// �ł��Â��v�f���폜����
				Temperature delete = this.record.poll();

				delete = null;

				// �����ɒǉ�����
				this.record.offer(temperature);
			}

			this.current = temperature;

			if (null != this.highest) {
				
				float highTemperature = this.highest.getTemperature();
				
				if (highTemperature < temperature.getTemperature()) {

					this.highest = temperature;
				}
				
			} else {

				this.highest = temperature;
			}
			
			if (null != this.lowest) {
				
				float lowTemperature = this.lowest.getTemperature();
				
				if (temperature.getTemperature() < lowTemperature) {

					this.lowest = temperature;
				}
				
			} else {

				this.lowest = temperature;
			}
		}
	}


	public Temperature getCurrentTemperature() {

		return this.current;
	}


	public Temperature getHighestTemperature() {

		return this.highest;
	}


	public Temperature getLowestTemperature() {

		return this.lowest;
	}


	public synchronized Iterator<Temperature> getRecord() {

		ArrayBlockingQueue<Temperature> queue = new ArrayBlockingQueue<Temperature>(this.capacity);
		
		Iterator<Temperature> original = this.record.iterator();
		
		while (true == original.hasNext()) {

			Temperature element = original.next();

			Temperature deepCopy = element.createDeepCopy();

			queue.offer(deepCopy);
		}
		
		Iterator<Temperature> iterator = queue.iterator();

		return iterator;
	}
}
