package application;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class Main extends Application {
	private Image marSprite;
	private Node player;
	private ArrayList<Node> background;
	private Map<Integer, String[]> sprNames = new TreeMap<>();
	private boolean mvLeft, mvRight, jumping, ducking, sprinShoo, colR, colL, colU, colD, jumped;
	private double velX, velY, lastVX;
	private final double maxX = 2.0;
	private final double maxY = 3.0;
	private final double accelFac = 0.07;
	private final int winHeight = 480;
	private final int winWidth = 632;
	private int lvlHeight; //number of tiles rows in the level to divide total number of tiles by to create rows
	private int lvlWidth;
	
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
			
			ScrollPane view = new ScrollPane() { public void requestFocus() {}};
			view.setPrefSize(winWidth, winHeight);
			view.setHbarPolicy(ScrollBarPolicy.NEVER);
			view.setVbarPolicy(ScrollBarPolicy.NEVER);
			view.setContent(pBounds);
			view.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {public void handle(ScrollEvent event) {event.consume();}});
			
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
						
			AnimationTimer timer = new AnimationTimer() {
				@Override
				public void handle(long now) {
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
			};
			
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
			
			timer.start();
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
 		
 		//jump logic
 		if(velY <= mY * -1 || jumped) {
 			y = 0;
 			jumped = true;
 		}
 		if(y == 0)
 			velY += accelFac * 1.5;
 		else
 			velY += accelFac;
 		if(colD || velY < 0)
 			velY -= y * 2;
 		
 		//face the character sprite in the proper direction
 		if(velX < 0 - accelFac)
 			player.setScaleX(-1);
 		else if(velX > 0 + accelFac)
 			player.setScaleX(1);
 		
 		//check for solid bodies that would stop movement
 		int offset;
 		if((colR || colL) && colD && (offset = (int) (player.getLayoutY() % 32)) < 16) {
 			movePlayer(0.0, -offset - 1);
 			velX = lastVX;
 		}
 		checkCollision(velX, velY);
 		if(colR && velX > 0) velX = 0;
 		if(colL && velX < 0) velX = 0;
 		if(colU && velY < 0) velY = 0;
 		if(colD && velY > 0) velY = 0;
 		if(colD && velY == 0) jumped = false;
 		
 		movePlayer(velX, velY);
 	}
 	
 	public void movePlayer(double x, double y) {player.relocate(x + player.getLayoutX(), y + player.getLayoutY());}
 	
 	public void checkCollision(double velX, double velY) {	//TODO finish this method after completing background creation
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
 		 	 		
 		if(((Tile) background.get((int) (((upTile) * lvlWidth) + rightTile))).getCollidable() 
 				|| ((Tile) background.get((int) ((downTile * lvlWidth) + rightTile))).getCollidable())
 			colR = true;
 		
 		if(((Tile) background.get((int) ((upTile * lvlWidth) + leftTile))).getCollidable() 
 				|| ((Tile) background.get((int) ((downTile * lvlWidth) + leftTile))).getCollidable() || leftEdge < accelFac)
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
 		if(((Tile) background.get((int) (downTile * lvlWidth) + rightTile)).getCollidable() 
 				|| ((Tile) background.get((int) (downTile * lvlWidth) + leftTile)).getCollidable())
 			colD = true;
 		if(((Tile) background.get((int) (upTile * lvlWidth) + rightTile)).getCollidable() 
 				|| ((Tile) background.get((int) (upTile * lvlWidth) + leftTile)).getCollidable())
 			colU = true;
 	}
 	
 	public ArrayList<Node> createBackground(String file) {
 		sprNames.put(-65536, new String[] {"basicGround", "true"});
 		sprNames.put(-16711681, new String[] {"emptySky", "false"});
 		ArrayList <Node> bg= new ArrayList<>();
		Image map = crSp(file);
		int h = (int) map.getHeight();
		int w = (int) map.getWidth();
		
		for(int i = 0; i < h; i++)
			for(int j = 0; j < w; j++) {
				Integer pixel = map.getPixelReader().getArgb(j, i);
				bg.add(new Tile(crSp(sprNames.get(pixel)[0]), Boolean.parseBoolean(sprNames.get(pixel)[1])));
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
