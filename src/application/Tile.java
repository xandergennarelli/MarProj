package application;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Tile extends ImageView{
	private final boolean collidable;
	public Tile(Image i, boolean c) {
		super(i);
		collidable = c;
	}
	public boolean getCollidable() {
		return this.collidable;
	}
}
