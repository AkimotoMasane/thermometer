package com.akimoto.sensor.sound;


import com.akimoto.sensor.R;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;


public class SoundEffect {

	private static final int MAX_STREAMS = 2;

	private static final float VOLUME = 0.6f;
	private static final float RATE = 1.0f;

	private AudioAttributes audioAttributes;

	private SoundPool soundPool;
	
	private int idTouch;
	
	private int idWarning;

	
	public SoundEffect() {
		
		this.audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
		
		this.soundPool = new SoundPool.Builder().setAudioAttributes(this.audioAttributes).setMaxStreams(SoundEffect.MAX_STREAMS).build();
	}


	public void loadSoundFile(Context context) {

		this.idTouch = this.soundPool.load(context, R.raw.se_touch, 1);
		
		this.idWarning = this.soundPool.load(context, R.raw.se_warning, 1);
	}


	public void releaseSoundFile() {

		this.soundPool.release();
	}


	public void playSoundEffectTouch() {
		
		this.soundPool.play(this.idTouch, SoundEffect.VOLUME, SoundEffect.VOLUME, 0, 0, SoundEffect.RATE);
	}


	public void playSoundEffectWarning() {

		this.soundPool.play(this.idWarning, SoundEffect.VOLUME, SoundEffect.VOLUME, 0, 0, SoundEffect.RATE);
	}
}
