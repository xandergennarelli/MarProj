package application;
	
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class Main extends Application {
	private Image marSprite;
	private Node player;
	private ArrayList<Node> background;
	private final String[] sprNames = new String[] {"emptySky", "basicGround"};
	private final Image[] sprites = new Image[] {};
	private boolean mvLeft, mvRight, jumping, ducking, sprinShoo, colR, colL, colU, colD, jumped;
	private double velX, velY, lastVX;
	private final double maxX = 3.0;
	private final double maxY = 4.0;
	private final double accelFac = 0.07;
	private final int winHeight = 192;
	private final int winWidth = 576;
	private int lvlHeight; //number of tiles rows in the level to divide total number of tiles by to create rows
	private int lvlWidth;
	
 	@Override
	public void start(Stage primaryStage) {
		try {
			background = createBackground("sprites/map.txt");
			ArrayList<Node> enemies = new ArrayList<>();
			ArrayList<Node> foreground = new ArrayList<>();
			ArrayList<Node> nodes = new ArrayList<>();
 			marSprite = new Image(getClass().getResourceAsStream("/smMario.png"));
			player = new ImageView(marSprite);
			
			Group pBounds = new Group();
			
			nodes.addAll(background);
			nodes.addAll(enemies);
			nodes.add(player);
			nodes.addAll(foreground);
			
			pBounds.getChildren().addAll(nodes);
			relocateBackground();
			
			ScrollPane view = new ScrollPane();
			view.setPrefSize(winWidth, winHeight);
			view.setHbarPolicy(ScrollBarPolicy.NEVER);
			view.setVbarPolicy(ScrollBarPolicy.NEVER);
			view.setContent(pBounds);
			
			
			Scene scene = new Scene(view,winWidth,winHeight);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("MarBro");
			primaryStage.show();
			
			view.setVvalue(92);
			player.relocate(40, 192);
			jumped = false;
						
			AnimationTimer timer = new AnimationTimer() {
				private int frameCount = 0;
				@Override
				public void handle(long now) {
					if(frameCount % 3 != 0) {
						frameCount++;
						return;
					}
					frameCount = 0;
					
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
 			velY += accelFac * 3;
 		else
 			velY += accelFac * 2;
 		if(colD || velY < 0)
 			velY -= y * 4;
 		
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
 		if(colD) jumped = false;
 		
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
 		ArrayList <Node> bg= new ArrayList<>();
		int rowCount = 0;
 		try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {  
			String line = bufferedReader.readLine();
    	    while(line != null) {
    	    	rowCount++;
    	    	Scanner item = new Scanner(line);
    	    	item.useDelimiter(",");
    	    	while(item.hasNext()) {
        	    	String block = item.next();
    	    		String name = sprNames[Integer.parseInt(block.substring(0, block.indexOf("|")))];
    	    		boolean col = Boolean.parseBoolean(block.substring(block.indexOf("|") + 1));
    	    		bg.add(new Tile(crSp(name), col));
    	    	}
    	    	line = bufferedReader.readLine();
    	    }
    	} catch (FileNotFoundException e) {
    	    // exception handling
    		System.out.println("WHAT DID YOU DO");
    	} catch (IOException e) {
    	    // exception handling
    		System.out.println("you should literally never see this");
    	}
 		lvlHeight = rowCount;
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
