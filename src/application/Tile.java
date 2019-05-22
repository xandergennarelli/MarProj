package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Tile extends ImageView{
	private final boolean collidable, powerup, hazard;
	private final int score;
	public Tile(Image i, boolean c, boolean p, boolean h, int s) {
		super(i);
		collidable = c;
		powerup = p;
		hazard = h;
		score = s;
	}
	public boolean isCollidable() {
		return this.collidable;
	}
	public boolean isPowerup() {
		return this.powerup;
	}
	public boolean isHazard() {
		return this.hazard;
	}
	public int getScore() {
		return this.score;
	}
}
