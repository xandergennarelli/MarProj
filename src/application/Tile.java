package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Tile extends ImageView{
	private final boolean collidable, powerup, hazard;
	private boolean coin;
	public Tile(Image i, boolean c, boolean p, boolean h, boolean s) {
		super(i);
		collidable = c;
		powerup = p;
		hazard = h;
		coin = s;
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
	public boolean isCoin() {
		return this.coin;
	}
	public void setCoin(boolean coin) {
		this.coin = coin;
	}
}
