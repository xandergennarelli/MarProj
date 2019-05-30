package application;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class Main extends Application {
	private Image marSprite;
	private Node player;
	private ArrayList<Node> background;
	private ScrollPane view;
	private Map<Integer, String[]> sprNames = new TreeMap<>();
	private boolean mvLeft, mvRight, jumping, ducking, sprinShoo, colR, colL, colU, colD, jumped;
	private double velX, velY, lastVX;
	private final double maxX = 4.0;
	private final double maxY = 5.0;
	private final double accelFac = 0.3;
	private final int winHeight = 480;
	private final int winWidth = 632;
	private int lvlHeight; //number of tiles rows in the level to divide total number of tiles by to create rows
	private int lvlWidth;
	private int score, lastScore;
	
	//rip me. Sorry I didn't finish this in time. I hope once this is over you are still able to play mario with having nightmares.
	
 	@Override
	public void start(Stage primaryStage) {
		try {
			background = createBackground("map");
			ArrayList<Node> enemies = new ArrayList<>();
			ArrayList<Node> foreground = new ArrayList<>();
			ArrayList<Node> nodes = new ArrayList<>();
 			marSprite = new Image(getClass().getResourceAsStream("/smMario.png"));
			player = new ImageView(marSprite);
						
			nodes.addAll(background);
			nodes.addAll(enemies);
			nodes.add(player);
			nodes.addAll(foreground);
			
			Group pBounds = new Group();
			pBounds.getChildren().addAll(nodes);
			relocateBackground();
			
			view = new ScrollPane() { public void requestFocus() {}};
			view.setPrefSize(winWidth, winHeight);
			view.setHbarPolicy(ScrollBarPolicy.NEVER);
			view.setVbarPolicy(ScrollBarPolicy.NEVER);
			view.setContent(pBounds);
			view.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {public void handle(ScrollEvent event) {event.consume();}});
			view.setHmin(0);
			view.setHmax(32);
			
			StackPane layers = new StackPane();
			layers.getChildren().add(view);
			
			Scene scene = new Scene(layers,winWidth,winHeight);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("MarBro");
			primaryStage.setResizable(false);
			primaryStage.show();
			
			view.setVvalue(92);
			player.relocate(40, 288);
			jumped = false;
			score = 0;
			lastScore = score;
						
			//AnimationTimer timer = new AnimationTimer() {
				//@Override
			KeyFrame frame = new KeyFrame(
				Duration.seconds(0.034),
				new EventHandler<ActionEvent>() {
					public void handle(ActionEvent e) {
						double aX = 0.0;
						double aY = 0.0;
						double mX = maxX;
						double mY = maxY;
						
						if(mvRight) aX += accelFac;
						if(mvLeft) aX -= accelFac;
						if(jumping) aY += accelFac;
						if(sprinShoo) mX *= 1.6;
						
						accelerate(aX, aY, mX, mY);
					}
				}
			);
			
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
	            @SuppressWarnings("incomplete-switch")
				@Override
	            public void handle(KeyEvent event) {
	                switch (event.getCode()) {
	                    case UP:    jumping = true; break;
	                    case DOWN:  ducking = true; break;
	                    case LEFT:  mvLeft  = true; break;
	                    case RIGHT: mvRight  = true; break;
	                    case SHIFT: sprinShoo = true; break;
	                }
	            }
	        });
			scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
	            @SuppressWarnings("incomplete-switch")
				@Override
	            public void handle(KeyEvent event) {
	                switch (event.getCode()) {
	                    case UP:    jumping = false; jumped = true; break;
	                    case DOWN:  ducking = false; break;
	                    case LEFT:  mvLeft  = false; break;
	                    case RIGHT: mvRight  = false; break;
	                    case SHIFT: sprinShoo = false; break;
	                }
	            }
	        });
			Timeline gameloop = new Timeline();
			gameloop.setCycleCount(Timeline.INDEFINITE);
			gameloop.getKeyFrames().clear();
			gameloop.getKeyFrames().add(frame);
			gameloop.play();
		} catch(Exception e) {e.printStackTrace();}
	}
 	
 	public void accelerate(double x, double y, double mX, double mY) {
 		if(Math.abs(velX) < accelFac) //prevents weird drifting
 			velX = 0;
 		if(x == 0 && velX != 0) //decelerate the player when not pressing a movement key
 			velX -= Math.copySign(accelFac, velX) * 2.3;
 		if(Math.abs(velX) < mX || Integer.signum((int) x) != Integer.signum((int) velX)) //checks if the player still is under the max speed and can accelerate
 			velX += x;
 		if(Math.abs(velX) > mX) //ensures the player does not exceed max speed
 			velX = Math.copySign(mX, velX);
 		
 		if(velY <= mY) {velY += accelFac;}
 		if(velY > mY) {velY = mY;}
 		if(jumping && !jumped && colD) {velY = -mY * 1.5; jumped = true;}
 		if(Math.abs(velY) < accelFac) {velY = 0;}
 		
 		//face the character sprite in the proper direction
 		if(velX < 0 - accelFac)
 			player.setScaleX(-1);
 		else if(velX > 0 + accelFac)
 			player.setScaleX(1);
 		
 		checkCollision(velX, velY); //check for solid bodies that would stop movement
 		
 		if(colR && velX > 0) velX = 0;
 		if(colL && velX < 0) velX = 0;
 		if(colU && velY < 0) velY = 0;
 		if(colD && velY > 0) velY = 0;
 		if(colD && velY == 0) jumped = false;
 		
 		movePlayer(velX, velY); //applies velocities to the player
 	}
 	
 	public void movePlayer(double x, double y) {
 		player.relocate(x + player.getLayoutX(), y + player.getLayoutY());
 		if(player.getLayoutX() > (view.getHvalue()) + 380)
 			view.setHvalue(view.getHvalue() + (velX / 42));	// not sure the significance of 42 but it is the only way to keep the scroll in sync with mario (it's also the answer to life, the universe, and everything.)
 	}
 	
 	public void checkCollision(double velX, double velY) {
 		double Voffset = player.getLayoutY() % 32.0;	//correct ground clipping after a fast fall
 		double Hoffset = player.getLayoutX() % 32.0;
 		
 		colR=false;colL=false;colU=false;colD=false;
 		final double rightEdge = (player.getLayoutX() + player.getBoundsInLocal().getWidth() + velX);
 		final double leftEdge = (player.getLayoutX() + velX);
 		final double upEdge = (player.getLayoutY() - velY + 1);
 		final double downEdge = (player.getLayoutY() + (player.getBoundsInLocal().getHeight() - 1) - velY);
 		lastVX = velX;
 		
 		int rightTile = (int) (Math.round(rightEdge) / 32);
 		if(rightTile == 0)
 				rightTile += (rightTile / lvlHeight) * lvlWidth;
 		int leftTile = (int) (Math.round(leftEdge) / 32);
 		if(leftTile == 0)
 				leftTile += (leftTile / lvlHeight) * lvlWidth;
 		int upTile = (int) (Math.round(upEdge) / 32);
 		if((upTile % lvlHeight) == 0)
 				upTile += (upTile / lvlHeight) * lvlHeight;
 		int downTile = (int) (Math.ceil(downEdge) / 32);
 		if((downTile % lvlHeight) == 0)
 				downTile += (downTile / lvlHeight) * lvlHeight;
 		 	 		
 		if(((Tile) background.get((int) (((upTile) * lvlWidth) + rightTile))).isCollidable() 
 				|| ((Tile) background.get((int) ((downTile * lvlWidth) + rightTile))).isCollidable())
 			colR = true;
 		
 		if(((Tile) background.get((int) ((upTile * lvlWidth) + leftTile))).isCollidable() 
 				|| ((Tile) background.get((int) ((downTile * lvlWidth) + leftTile))).isCollidable() || leftEdge < accelFac)
 			colL = true;
 		
 		rightTile = (int) (Math.round(rightEdge - 1) / 32);
 		if(rightTile == 0)
 				rightTile += (rightTile / lvlHeight) * lvlWidth;
 		leftTile = (int) (Math.round(leftEdge + 1) / 32);
 		if(leftTile == 0)
 				leftTile += (leftTile / lvlHeight) * lvlWidth;
 		downTile = (int) (Math.ceil(downEdge + 1) / 32);
 		if((downTile % lvlHeight) == 0)
 				downTile += (downTile / lvlHeight) * lvlHeight;
 		if(((Tile) background.get((int) (downTile * lvlWidth) + rightTile)).isCollidable() 
 				|| ((Tile) background.get((int) (downTile * lvlWidth) + leftTile)).isCollidable())
 			colD = true;
 		if(((Tile) background.get((int) (upTile * lvlWidth) + rightTile)).isCollidable() 
 				|| ((Tile) background.get((int) (upTile * lvlWidth) + leftTile)).isCollidable())
 			colU = true;
 		
 		if(colD && Voffset < 16 && Voffset > 2)
 			movePlayer(0.0, -Voffset);
 		if(colR && Hoffset > 16 && Hoffset < 2)
 			movePlayer(Hoffset, 0.0);
 		velX = lastVX;
 	}
 	
 	public ArrayList<Node> createBackground(String file) {
 		sprNames.put(-65536, new String[] {"basicGround", "true", "false", "false", "false"});
 		sprNames.put(-16711681, new String[] {"emptySky", "false", "false", "false", "false"});
 		sprNames.put(-256, new String[] {"coinSky", "false", "false", "false", "true"});
 		ArrayList <Node> bg= new ArrayList<>();
		Image map = crSp(file);
		int h = (int) map.getHeight();
		int w = (int) map.getWidth();
		System.out.println(map.getPixelReader().getArgb(0, 0));
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++) {
				Integer pixel = map.getPixelReader().getArgb(j, i);
				bg.add(new Tile(crSp(sprNames.get(pixel)[0]), Boolean.parseBoolean(sprNames.get(pixel)[1]), Boolean.parseBoolean(sprNames.get(pixel)[2]), 
						Boolean.parseBoolean(sprNames.get(pixel)[3]), Boolean.parseBoolean(sprNames.get(pixel)[4])));
			}
 		lvlHeight = h;
 		return bg;
 	}
 	
 	public void relocateBackground() {
 		lvlWidth = background.size() / lvlHeight;
 		for(int i = 0; i < lvlHeight; i++)
 			for(int j = 0; j < lvlWidth; j++)
 				background.get(j + (lvlWidth * i)).relocate(j*32, i*32);
 	}
 	
 	public Image crSp(String file) {return new Image(getClass().getResourceAsStream("/" + file + ".png"));}
	
	public static void main(String[] args) {launch(args);}
}
