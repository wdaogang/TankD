package com.echoeight.tankd.levels;

import java.util.ArrayList;

import org.lwjgl.Sys;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.CursorLoader;

import com.echoeight.bison.entity.Entity;
import com.echoeight.bison.entity.EntityManager;
import com.echoeight.bison.states.BaseState;
import com.echoeight.tankd.Game;
import com.echoeight.tankd.entity.AmmoIcon;
import com.echoeight.tankd.entity.BG;
import com.echoeight.tankd.entity.PlayerTank;
import com.echoeight.tankd.entity.Shell;
import com.echoeight.tankd.entity.Tank;
import com.echoeight.tankd.entity.Tread;
import com.echoeight.tankd.entity.Turret;
import com.echoeight.tankd.entity.Wall;

public class Camp1State extends BaseState {
	
	protected Game game;
	
	private long lastFrame;
	
	EntityManager em;
	CampInput ui;
	
	int WIDTH, HEIGHT;
	
	ArrayList<Tank> tanks = new ArrayList<Tank>();
	ArrayList<Wall> walls = new ArrayList<Wall>();
	ArrayList<AmmoIcon> ammoIcons = new ArrayList<AmmoIcon>();
	
	PlayerTank player;
	BG bg;
	
    public Camp1State(Game game, int id) {
		super(id);
		this.game = game;
		WIDTH = game.dm.getWidth();
		HEIGHT = game.dm.getHeight();
		em = new EntityManager();
	}
    
    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    public int getDelta() {
        long currentTime = getTime();
        int delta = (int) (currentTime - lastFrame);
        lastFrame = getTime();
        return delta;
    }
    
	public void drawLevel(){
		for(int i=55;i<HEIGHT;i+=20){
			walls.add(new Wall(em,10,i,20,20));
		}
		for(int i=45;i<HEIGHT;i+=20){
			walls.add(new Wall(em,WIDTH-10,i+10,20,20));
		}
		for(int i=0;i<WIDTH;i+=20){
			walls.add(new Wall(em,i,55,20,20));
		}
		for(int i=0;i<WIDTH;i+=20){
			walls.add(new Wall(em,i,HEIGHT-10,20,20));
		}
	}
	
	@Override
	public void init() {
		initGL();
        try {
            Cursor cursor = (CursorLoader.get()).getCursor("res/cursor.png",8,8);
			Mouse.setNativeCursor(cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}
        drawLevel();
        ui = new CampInput(game, em);
        player = new PlayerTank(em, 400, 100, 34, 34, 0);
        bg = new BG(em, 0, 0, 600, 800);
        tanks.add(player);
        //for(int i=1;i<=player.getAmmo();i++){
        	ammoIcons.add(new AmmoIcon(em,200,100,32,14,0));
        //}
	}
	
	@Override
	public void update() {
		Color.white.bind();
		int delta = getDelta();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		bg.draw();
		for(Wall wall : walls){
			wall.draw();
		}
		
		handleWalls();
		ui.handleInput(em, player);
		
		for(Entity ent : em.getMovingEntities()){
			if(!(ent instanceof Tank) && !(ent instanceof Turret) && !(ent instanceof Shell) && !(ent instanceof Tread)){
				ent.draw();
				ent.update(delta);
			}
		}
		
		for(Tank t : tanks){
			if(t.getTreads() != null){
				for(Tread tread : t.getTreads()){
					tread.rotate(tread.getAngle());	
				}
			}
			for(Shell shell : t.getShells()){
				shell.rotate(shell.getAngle());				
			}
			t.rotate(t.getAngle());
			t.update(delta);
			
			t.setDX(0);
			t.setDY(0);
			
			
		}
		em.flush();
	    Display.update();
	    Display.sync(60);
	}
	
	public void handleWalls(){
		for(Entity entity : em.getMovingEntities()){
			for(Wall wall : walls){
				if(entity.intersects(wall)){
					if(entity instanceof Shell){
						handleShell((Shell) entity);
					}else if(!(entity instanceof Tread)){
						em.markForDelete(entity);
					}
				}
			}
		}
		em.flush();
	}
	
	private void handleShell(Shell shell){
		if(shell.hasBounced()){
			em.markForDelete(shell);
		}else{
			shell.bounce();
		}
	}
	
	private void initGL(){
//		try {
//			Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
//			Display.create();
//			Display.setTitle(game.dm.getTitle());
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
//
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glShadeModel(GL11.GL_SMOOTH);        
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING); 
		
		GL11.glClearColor(255f, 255f, 255f, 0f);                
		GL11.glClearDepth(1);
		
		GL11.glEnable(GL11.GL_BLEND);
      	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      	
      	GL11.glViewport(0,0,WIDTH,HEIGHT);
      
      	GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
      	GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);  
	}
	
}