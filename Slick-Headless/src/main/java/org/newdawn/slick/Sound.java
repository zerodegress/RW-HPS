package org.newdawn.slick;

import java.io.InputStream;
import java.net.URL;

/**
 * A single sound effect loaded from either OGG or XM/MOD file. Sounds are allocated to 
 * channels dynamically - if not channel is available the sound will not play. 
 *
 * @author kevin
 */
public class Sound {

	/**
	 * Create a new Sound 
	 * 
	 * @param in The location of the OGG or MOD/XM to load
	 * @param ref The name to associate this stream
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound(InputStream in, String ref) throws SlickException {
	}
	
	/**
	 * Create a new Sound 
	 * 
	 * @param url The location of the OGG or MOD/XM to load
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound(URL url) throws SlickException {
	}
	
	/**
	 * Create a new Sound 
	 * 
	 * @param ref The location of the OGG or MOD/XM to load
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound(String ref) throws SlickException {
	}
	
	/**
	 * Play this sound effect at default volume and pitch
	 */
	public void play() {
	}
	
	/**
	 * Play this sound effect at a given volume and pitch
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 */
	public void play(float pitch, float volume) {
	}

	/**
	 * Play a sound effect from a particular location
	 * 
	 * @param x The x position of the source of the effect
 	 * @param y The y position of the source of the effect
	 * @param z The z position of the source of the effect
	 */
	public void playAt(float x, float y, float z) {
	}
	
	/**
	 * Play a sound effect from a particular location
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 * @param x The x position of the source of the effect
 	 * @param y The y position of the source of the effect
	 * @param z The z position of the source of the effect
	 */
	public void playAt(float pitch, float volume, float x, float y, float z) {
	}
	/**
	 * Loop this sound effect at default volume and pitch
	 */
	public void loop() {
	}
	
	/**
	 * Loop this sound effect at a given volume and pitch
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 */
	public void loop(float pitch, float volume) {
	}
	
	/**
	 * Check if the sound is currently playing
	 * 
	 * @return True if the sound is playing
	 */
	public boolean playing() {
		return true;
	}
	
	/**
	 * Stop the sound being played
	 */
	public void stop() {
	}
}
