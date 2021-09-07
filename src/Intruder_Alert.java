import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Intruder_Alert extends PApplet {

/** 
CGRA-151 Project:
  Name - Intruder Alert
  Developer - John Flynn
  Start - 18/09/20
  Finish - There's always more I could add!
*/

private State state;

public void setup(){
  //fullScreen();
  
  colorMode(HSB, 360);
  rectMode(CENTER);
  //noSmooth();
  
  state = new Menu();
}

public void draw(){
  background(0);
  state.runState();
}

public void mousePressed(){ state.mouseP(); }
public void mouseReleased(){ state.mouseR(); }
public void keyPressed(){ state.keyP(); }
public void keyReleased(){ state.keyR(); }

public void setState(State s){ state = s; } // changes state

/** Each implementation of State describes a different game-state:
 * Makes running the different states easier.
 */
public abstract class State {
  
  public abstract void mouseP();
  public abstract void mouseR();
  public abstract void keyP();
  public abstract void keyR();
  
  public final void runState(){
    updateState();
    drawState();
  }
  public abstract void updateState();
  public abstract void drawState();
}

/** Flying text is the text that flies sideways across the screen:
*/
public class FlyingText {
  
  /** Fields: */
  private PVector pos;
  private PFont font = createFont("Rubik-Bold", height/5.0f);
  private int side; // 0 for left, 1 for right
  private String text;
  private float hue;
  
  public FlyingText(String text, int side, float hue){
    this.text = text;
    this.side = side;
    this.hue = hue;
    textFont(font);
    if (side == 0){ // from left
      pos = new PVector(-textWidth(text), height*2/5);
    }
    else { // from right
      pos = new PVector(width+textWidth(text), height*3/5);
    }
  }
  
  public boolean finished(){
    textFont(font);
    if (side == 0){
      return (pos.x > width+textWidth(text));
    }
    else {
      return (pos.x < -textWidth(text));
    }
  }
  
  public void drawFlyingText(){
    textFont(font);
    fill(hue, 360, 360);
    float shift = width/500.0f;
    text(text, pos.x+shift, pos.y+shift);
    fill(360);
    text(text, pos.x, pos.y);
    
    float speed = 50.0f;
    if (pos.x > width/3.0f && pos.x < width*2.0f/3.0f) speed = 10.0f;
    if (side == 0){
      pos.set(pos.x+speed, pos.y);
    }
    else {
      pos.set(pos.x-speed, pos.y);
    }
  }
}

/** Splatter describes a blood splatter when an enemy dies:
*/
public class Splatter {
  
  /** Fields: */
  private ArrayList<Blob> blobs = new ArrayList<Blob>();
  private long startFrame = frameCount;
  private int lifeSpan = 600;
  private float alpha = 360.00f;
  
  public Splatter(float x, float y){
    int blobsNum = 20;
    int maxSpeed = 2;
    int blobSize = 25;
    for (float i = 0.00f; i < blobsNum; i++) blobs.add(new Blob(new PVector(x, y), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), color(0, 360, 144)));
  }
  
  public boolean finished(){ return (frameCount > startFrame + lifeSpan); }
  
  public void drawSplatter(){
    long remainingLifeSpan = lifeSpan - (frameCount - startFrame);
    if (remainingLifeSpan <= lifeSpan/5) alpha = 360.00f*(remainingLifeSpan/(lifeSpan/5.00f));
    for (int i = 0; i < blobs.size(); i++){
      Blob b = blobs.get(i);
      b.setColour(color(0, 360, 144, alpha));
      b.drawBlob();
    }
  }
}

/** Used by explosions and splatters.
* Describes a circle with a velocity that fades over time:
*/
public class Blob {
  
  /** Fields: */
  private PVector pos, speed;
  private float size;
  private int colour;
  
  public Blob(PVector pos, float speed, float size, int colour){
    this.pos = pos;
    float angle = random(0, TWO_PI);
    this.speed = new PVector(speed*cos(angle), speed*sin(angle));
    this.size = size;
    this.colour = colour;
  }
  
  public void setColour(int colour){ this.colour = colour; }
  public void setAlpha(float alpha){ this.colour = color(hue(colour), saturation(colour), brightness(colour), alpha); }
  
  public void drawBlob(){
    // Movement and friction:
    speed.mult(24.0f/25.0f);
    pos.add(speed);
    
    noStroke();
    fill(colour);
    ellipse(pos.x, pos.y, size, size);
  }
}

public class Ship {
  
  /** Fields: */
  private PVector pos, speed;
  private float direction, size;
  
  public Ship(){
    size = random(width/100, width/25);
    float x0, y0, x1, y1;
    int side = (int)random(0, 4); // left, top, right, bottom
    if (side % 2 == 0){ // left and right
      y0 = random(0, height);
      if (side == 0){ // left
        x0 = -size/2.0f + 1;
        direction = random(-PI/2, PI/2);
      }
      else { // right
        x0 = width+size/2.0f - 1;
        direction = random(PI/2, PI*1.5f);
      }
    }
    else { // top and bottom
      x0 = random(0, width);
      if (side == 1){ // top
        y0 = -size/2.0f + 1;
        direction = random(0, PI);
      }
      else { // bottom
        y0 = height+size/2.0f - 1;
        direction = random(PI, TWO_PI);
      }
    }
    pos = new PVector(x0, y0);
    float vel = size/5.0f;
    speed = new PVector(vel*cos(direction), vel*sin(direction));
  }
  
  public boolean offScreen(){
    float r = size/2.0f;
    float left = pos.x-r;
    float right = pos.x+r;
    float top = pos.y-r;
    float bottom = pos.y+r;
    return (right <= 0 || left >= width || bottom <= 0 || top >= height);
  }
  
  public void drawShip(PShape s){
    shapeMode(CENTER);
    pushMatrix();
    translate(pos.x, pos.y);
    rotate(direction+PI/2.0f);
    translate(-pos.x, -pos.y);
    shape(s, pos.x, pos.y, size, size);
    popMatrix();
    pos.add(speed);
  }
}

/** Background describes a space background using perlin noise, and star objects:
*/
public class Background {
  
  /** Fields: */
  private int pixelSize;
  private ArrayList<Star> stars;
  private PShape s; // star
  
  public Background(int pixelSize, int starNum){
    this.pixelSize = pixelSize;
    stars = new ArrayList<Star>();
    
    // Make star shape:
    s = createShape();
    double size = 10.00f;
    s.beginShape();
    s.noStroke();
    s.colorMode(HSB, 360);
    s.fill(360);
    for (int i = 0; i < 8; i++){
      float angle = i * PI/4;
      double radius = size/2.00f;
      if (i % 2 == 0) radius /= 5.00f;
      float x = (float)radius*cos(angle);
      float y = (float)radius*sin(angle);
      s.vertex(x, y);
    }
    s.endShape(CLOSE);
    
    // Create stars:
    for (int i = 0; i < starNum; i++){
      stars.add(new Star(random(0, width), random(0, height), random(2, 8), random(0, TWO_PI), random(0, 0.5f)));
    }
  }
  
  public void drawBackground(){
    // Space background using perlin noise:
    int r = pixelSize/2;
    float scale = 0.001f;
    for (int x = r; x <= width+r; x += pixelSize){
      for (int y = r; y <= height+r; y += pixelSize){
        float value = noise(x*scale, y*scale, frameCount*0.005f);
        fill(250+40*value, 360, 200*constrain(0.75f-value, 0, 1));
        stroke(250+40*value, 360, 200*constrain(0.75f-value, 0, 1));
        rect(x, y, pixelSize, pixelSize);
      }
    }
    
    // Update and draw stars:
    for (int i = 0; i < stars.size(); i++){
      Star star = stars.get(i);
      if (star.expired()){
        stars.remove(star);
        i--;
        stars.add(new Star(random(0, width), random(0, height), random(2, 8), random(0, TWO_PI), 0));
      }
      else star.drawStar(s);
    }
  }
}

public class Star {
  
  /** Fields: */
  private float x, y, size, angle, spin, hue;
  private long spawnFrame;
  private int lifeSpan;
  
  public Star(float x, float y, float size, float angle, float currentAge){
    this.x = x;
    this.y = y;
    this.size = size;
    this.angle = angle;
    this.spin = random(-PI/240.0f, PI/240.0f);
    this.lifeSpan = (int)random(600, 1200); // 10 to 20 secs
    this.spawnFrame = frameCount - (int)(lifeSpan*currentAge);
    //this.hue = random(0, 360); // rainbow
  }
  
  public boolean expired(){
    return (frameCount > spawnFrame + lifeSpan);
  }
  
  public void drawStar(PShape s){
    //s.setFill(color(hue, 360, 360)); // rainbow
    float brightness = 360;
    if (frameCount < spawnFrame + 300){
      brightness = 360*(frameCount-spawnFrame)/300;
    }
    else if (frameCount > spawnFrame + lifeSpan - 300){
      brightness = 360*(spawnFrame+lifeSpan-frameCount)/300;
    }
    s.setFill(color(brightness));
    float r = size/2.0f;
    shapeMode(CENTER);
    pushMatrix();
    translate(x-r, y-r);
    rotate(angle);
    translate(-x+r, -y+r);
    shape(s, x, y, size, size);
    popMatrix();
    angle += spin;
  }
}

/** Button describes a rectangle that can be clicked by the user:
*/
public abstract class Button {
  
  protected float x, y, w, h;
  
  public float left(){ return x-w/2; }
  public float right(){ return x+w/2; }
  public float top(){ return y-h/2; }
  public float bottom(){ return y+h/2; }
  
  public boolean underMouse(){
    boolean withinX = (left() <= mouseX && mouseX <= right());
    boolean withinY = (top() <= mouseY && mouseY <= bottom());
    return (withinX && withinY);
  }
  
  public abstract void drawButton();
}

/** 
 * Character describes a player or enemy that collides with walls, bullets, and explosions.
 */
public abstract class Character {
 
  protected float w, h;
  protected PVector pos = new PVector(0, 0);
  protected PVector speed = new PVector(0, 0);
  protected float maxSpeed;
  protected float maxHealth, health;
  protected Gun equippedGun;
  
  protected float realHue, hue;
  protected int hueFramesLeft;
  
  public float x(){ return pos.x; }
  public float y(){ return pos.y; }
  public float w(){ return w; }
  public float h(){ return h; }
  public float left(){ return pos.x-w/2.0f; }
  public float right(){ return pos.x+w/2.0f; }
  public float top(){ return pos.y-h/2.0f; }
  public float bottom(){ return pos.y+h/2.0f; }
  
  public PVector pos(){ return pos; }
  public PVector speed(){ return speed; }
  public void setPos(float x, float y){ pos.set(x, y); }
  public void setSpeed(float x, float y){ speed.set(x, y); }
  
  public void addSpeed(PVector v){ speed.add(v); }
  
  public Gun equippedGun(){ return equippedGun; }
  public void equipGun(Gun gun){ equippedGun = gun; }
  public boolean cooldownOver(){
    if (equippedGun == null) return false;
    return equippedGun.cooldownOver();
  }

  public float realHue(){ return realHue; }
  public void setHue(float hue, int frames){
    this.hue = hue;
    hueFramesLeft = frames;
  }
  
  public float health(){ return health; }
  public void heal(float health){ this.health = constrain(this.health+health, 0, maxHealth); }
  public float maxHealth(){ return maxHealth; }
  public boolean atFullHealth(){ return (health == maxHealth); }
  public void damage(float dmg){ 
    health -= dmg;
    if (this instanceof Player) setHue(0, 10);
  }
  public boolean dead(){ return (health <= 0); }
  
  public void updateCharacter(ArrayList<Wall>[][] wallMap, ArrayList<Crate>[][] crateMap){    
    // Collision checking:
    float tileSize = Game.TILE_SIZE;
    int row = (int)(pos.y/tileSize);
    int col = (int)(pos.x/tileSize);
    ArrayList<Wall> walls = wallMap[row][col];
    ArrayList<Crate> crates = crateMap[row][col];
    
    if (walls != null){
      for (Wall w : walls){
        boolean collidingVertically = (top() < w.bottom() && bottom() > w.top());
        boolean collidingHorizontally = (left() < w.right() && right() > w.left());
        if (!collidingVertically && !collidingHorizontally) continue;
        
        if (collidingVertically){
          if (left()+speed.x <= w.right() && left()-speed.x >= w.right()){ // player hit right of wall
            w.glow();
            pos.set(w.right()+(this.w/2.0f), pos.y);
            speed.set(0, speed.y);
          }
          else if (right()+speed.x >= w.left() && right()-speed.x <= w.left()){ // player hit left of wall
            w.glow();
            pos.set(w.left()-(this.w/2.0f), pos.y);
            speed.set(0, speed.y);
          }
        }
        else if (collidingHorizontally){
          if (top()+speed.y <= w.bottom() && top()-speed.y >= w.bottom()){ // player hit bottom of wall
            w.glow();
            pos.set(pos.x, w.bottom()+(this.h/2.0f));
            speed.set(speed.x, 0);
            //println("Afters speedX: "+speed.x+" speedY: "+speed.y);
          }
          else if (bottom()+speed.y >= w.top() && bottom()-speed.y <= w.top()){ // player hit top of wall
            w.glow();
            pos.set(pos.x, w.top()-(this.h/2.0f));
            speed.set(speed.x, 0);
          }
        }
      }
    }
    if (crates != null){
      for (Crate c : crates){
        boolean collidingVertically = (top() < c.bottom() && bottom() > c.top());
        boolean collidingHorizontally = (left() < c.right() && right() > c.left());
        if (!collidingVertically && !collidingHorizontally) continue;
        
        if (collidingVertically){
          if (left()+speed.x <= c.right() && left()-speed.x >= c.right()){ // player hit right of wall
            pos.set(c.right()+(this.w/2.0f), pos.y);
            speed.set(0, speed.y);
          }
          else if (right()+speed.x >= c.left() && right()-speed.x <= c.left()){ // player hit left of wall
            pos.set(c.left()-(this.w/2.0f), pos.y);
            speed.set(0, speed.y);
          }
        }
        else if (collidingHorizontally){
          if (top()+speed.y <= c.bottom() && top()-speed.y >= c.bottom()){ // player hit bottom of wall
            pos.set(pos.x, c.bottom()+(this.h/2.0f));
            speed.set(speed.x, 0);
          }
          else if (bottom()+speed.y >= c.top() && bottom()-speed.y <= c.top()){ // player hit top of wall
            pos.set(pos.x, c.top()-(this.h/2.0f));
            speed.set(speed.x, 0);
          }
        }
      }
    }
    
    // Friction:
    float speedX = 0.85f * speed.x;
    float speedY = 0.85f * speed.y;
    if (abs(speedX) <= 0.01f) speedX = 0;
    if (abs(speedY) <= 0.01f) speedY = 0;
    speed.set(speedX, speedY);
    
    // Move player:
    pos.add(speed);
    
    // Update hue:
    if (hueFramesLeft == 0) hue = realHue;
    else hueFramesLeft--;
  }
  public abstract void drawCharacter();
}

public class Player extends Character {
  
  /** Additional Fields: */
  
  public Player(float size, float maxSpeed, Gun gun, float hue){
    this.maxHealth = this.health = 100;
    this.w = this.h = size;
    this.realHue = this.hue = hue;
    this.maxSpeed = maxSpeed;
    this.equippedGun = gun;
  }
  
  public void controlPlayer(HashMap<String, Boolean> controls, float direction){
    // Get direction of movement:
    int hMove = 0;
    int vMove = 0;
    if (controls.get("l")) hMove--;
    if (controls.get("r")) hMove++;
    if (controls.get("u")) vMove--;
    if (controls.get("d")) vMove++;
    
    // Update moving speed:
    float speedX = speed.x;
    float speedY = speed.y;
    
    if (hMove == 0 || vMove == 0){ // if only moving in one direction
      speedX += hMove * (maxSpeed-abs(speedX))/4;
      speedY += vMove * (maxSpeed-abs(speedY))/4;
    }
    else { // if moving at an angle
      float angle = atan(vMove/hMove);
      if (hMove < 0) angle += PI;
      float maxSpeedX = maxSpeed * cos(angle);
      float maxSpeedY = maxSpeed * sin(angle);
      speedX += hMove * (abs(maxSpeedX)-abs(speedX))/4;
      speedY += vMove * (abs(maxSpeedY)-abs(speedY))/4;
    }
    
    // Update vector:
    speed.set(speedX, speedY);
    
    // Shooting:
    if (equippedGun != null && cooldownOver()){
      if (controls.get("ml")) equippedGun.shoot(true, pos.copy(), direction, realHue);
      else if (controls.get("0") || controls.get("1") || controls.get("2") || controls.get("3")){
        hMove = 0;
        vMove = 0;
        if (controls.get("2")) hMove--;
        if (controls.get("0")) hMove++;
        if (controls.get("3")) vMove--;
        if (controls.get("1")) vMove++;
        direction = atan2(vMove, hMove);
        if (!(hMove == 0 && vMove == 0)) equippedGun.shoot(true, pos.copy(), direction, realHue);
      }
    }
  }  
  
  public void drawCharacter(){
    stroke(hue, 360, 250);
    strokeWeight(1);
    fill(hue, 360, 360);
    rect(pos.x, pos.y, w, h, 10);
  }
}

public class Enemy extends Character {
  
  /** Fields: */
  private float range;
  
  public Enemy(PVector pos, float health, float size, float maxSpeed, float range, Gun gun){
    this.pos = pos;
    maxHealth = this.health = health;
    w = h = size;
    realHue = hue = 0;
    this.maxSpeed = maxSpeed;
    this.range = range;
    equippedGun = gun;
  }
  
  /** Uses Bresenham's line algorithm to detect if walls or crates are in the way of target: */
  public boolean inLineOfSight(PVector position, ArrayList<Wall> walls, ArrayList<Crate> crates){
    int x0 = (int)pos.x;
    int y0 = (int)pos.y;
    int x1 = (int)position.x;
    int y1 = (int)position.y;
    
    float r = range/2.0f;
    boolean inBoundingBox = (x0-r <= x1 && x1 <= x0+r) && (y0-r <= y1 && y1 <= y0+r);
    if (!inBoundingBox) return false; // not in bounding box
    // Must be in bounding box:
    if (dist(x0, y0, x1, y1) > r) return false; // not in circular range
    
    if (abs(y1 - y0) < abs(x1 - x0)){ // if rise < run, low gradient
      if (x0 > x1) return lowLine(x1, y1, x0, y0, walls, crates); // if x0 is further right, swap points
      else return lowLine(x0, y0, x1, y1, walls, crates);
    }
    else { // else high gradient
      if (y0 > y1) return highLine(x1, y1, x0, y0, walls, crates); // if y0 is further down, swap points
      else return highLine(x0, y0, x1, y1, walls, crates);
    }
  }
  
  public boolean lowLine(int x0, int y0, int x1, int y1, ArrayList<Wall> walls, ArrayList<Crate> crates){
    int dx = x1 - x0; // x distance
    int dy = y1 - y0; // y distance
    int yIncrement = 1; // y moves down with each step
    if (dy < 0){ // if gradient is negative (slopes up as it goes from x0 to x1):
      yIncrement = -1; // y moves up with each step
      dy *= -1; // make dy positive for difference calculation
    }
    int difference = 2*dy - dx; // accumulates error and determines whether to increment y or not
    int x = x0;
    int y = y0;
    while (x <= x1){
      for (Wall w : walls){
        if (w.containsPoint(x, y)) return false;
      }
      for (Crate c : crates){
        if (c.containsPoint(x, y)) return false;
      }
      if (difference > 0){
        y += yIncrement;
        difference -= 2*dx;
      }
      difference += 2*dy;
      x++;
    }
    return true;
  }

  public boolean highLine(int x0, int y0, int x1, int y1, ArrayList<Wall> walls, ArrayList<Crate> crates){
    int dx = x1 - x0; // x distance
    int dy = y1 - y0; // y distance
    int xIncrement = 1; // x moves right with each step
    if (dx < 0){ // if gradient is negative (slopes left as it goes from y0 to y1):
      xIncrement = -1; // x moves left with each step
      dx *= -1; // make dx positive for difference calculation
    }
    int difference = 2*dx - dy; // accumulates error and determines whether to increment x or not
    int x = x0;
    int y = y0;
    while (y <= y1){
      for (Wall w : walls){
        if (w.containsPoint(x, y)) return false;
      }
      for (Crate c : crates){
        if (c.containsPoint(x, y)) return false;
      }
      if (difference > 0){
        x += xIncrement;
        difference -= 2*dy;
      }
      difference += 2*dx;
      y++;
    }
    return true;
  }
  
  public void shoot(float direction){
    if (equippedGun != null) equippedGun.shoot(false, pos.copy(), direction, 0);
  }
  
  public void drawCharacter(){    
    noStroke();
    fill(hue, 360, 360);
    rect(pos.x, pos.y, w, h, 10);
  }
}

/** 
 * This class describes a square destructible box.
 * The are yellow with a red exclamation mark if explosive.
 * Brown if not explosive.
 */
public class Crate {
  
  /** Fields: */
  private PVector pos;
  private float size, durability;
  private boolean explosive;
  
  public Crate(PVector pos, float size, boolean explosive){
    this.pos = pos;
    this.size = size;
    this.explosive = explosive;
    if (explosive) durability = 5;
    else durability = 20;
  }
  
  public PVector pos(){ return pos; }
  public float x(){ return pos.x; }
  public float y(){ return pos.y; }
  public float size(){ return size; }
  public float left(){ return pos.x-size/2.0f; }
  public float right(){ return pos.x+size/2.0f; }
  public float top(){ return pos.y-size/2.0f; }
  public float bottom(){ return pos.y+size/2.0f; }
  
  public boolean containsPoint(float x, float y){
    return (left() <= x && x <= right()) && (top() <= y && y <= bottom());
  }
  
  public void damage(float dmg){ durability -= dmg; }
  public float durability(){ return durability; }
  public boolean destroyed(){ return (durability <= 0); }
  public boolean explosive(){ return explosive; }
  
  public void drawCrate(){
    noStroke();
    fill(200);
    rect(pos.x, pos.y, size, size);
    if (explosive) fill(60, 360, 360);
    else fill(25, 360, 270);
    float innerSize = size*3.0f/4.0f;
    rect(pos.x, pos.y, innerSize, innerSize, innerSize/5.0f);
    if (explosive){
      fill(0, 360, 360);
      rect(pos.x, pos.y-(size*3.0f/16.0f), size/4.0f, size/2.0f);
      rect(pos.x, pos.y+(size/4.0f), size/4.0f, size/4.0f);
    }
  }
}

/** 
 * This class describes a group of blobs moving outwards from a centre point:
 * If not purely decorative, explosions can damage players, enemies, and crates
 * within their radius.
 */
public class Explosion {
  
  /** Fields: */
  private PVector pos;
  private float size, damage;
  private ArrayList<Blob> blobs = new ArrayList<Blob>();
  private long startFrame = frameCount;
  private int lifeSpan = 120;
  private float alpha = 360.0f;
  
  public Explosion(PVector pos, float size, float damage, int inner, int outer, Player player, ArrayList<Enemy> enemies, ArrayList<Crate> crates){
    this.pos = pos;
    this.size = size;
    this.damage = damage;
    int blobsNum = (int)(size/3.0f);
    float maxSpeed = size/40.0f;
    float blobSize = size/5.0f;
    for (float i = 0.00f; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0f))));
    
    // Apply damage:
    damage(player, enemies, crates);
  }
  
  /** Decorative explosions: */
  public Explosion(PVector pos, float size, int inner, int outer){
    this.pos = pos;
    this.size = size;
    int blobsNum = (int)(size/3.0f);
    float maxSpeed = size/40.0f;
    float blobSize = size/5.0f;
    for (float i = 0.00f; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0f))));
  }
  
  /** Decorative with particle number control: */
  public Explosion(PVector pos, float size, int inner, int outer, int blobsNum){
    this.pos = pos;
    this.size = size;
    float maxSpeed = size/40.0f;
    float blobSize = size/5.0f;
    for (float i = 0.00f; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0f))));
  }
  
  public boolean finished(){ return (frameCount > startFrame + lifeSpan); }
  
  public void damage(Player player, ArrayList<Enemy> enemies, ArrayList<Crate> crates){
    if (damage <= 0) return;
    
    // Make arraylist of all characters:
    ArrayList<Character> characters = new ArrayList<Character>(enemies);
    characters.add(player);
    
    float r = size/2.0f;
    float left = pos.x-r;
    float right = pos.x+r;
    float top = pos.y-r;
    float bottom = pos.y+r;
    for (Character c : characters){
      PVector p = c.pos();
      // Bounding box:
      if (left <= p.x && p.x <= right && top <= p.y && p.y <= bottom){
        float dist = dist(p.x, p.y, pos.x, pos.y);
        if (dist <= r){
          c.damage(damage * (r-dist)/r);
          float force = (damage * dist/r)/2.0f + 5.0f;
          // Apply force/knockback:
          float direction = atan2(c.y()-pos.y, c.x()-pos.x);
          float speedX = force*cos(direction);
          float speedY = force*sin(direction);
          c.addSpeed(new PVector(speedX, speedY));
        }
      }
    }
    
    for (Crate c : crates){
      PVector p = c.pos();
      // Bounding box:
      if (left <= p.x && p.x <= right && top <= p.y && p.y <= bottom){
        float dist = dist(p.x, p.y, pos.x, pos.y);
        if (dist <= r){
          c.damage(damage * (r-dist)/r);
        }
      }
    }
  }
  
  public void drawExplosion(){
    long remainingLifeSpan = lifeSpan - (frameCount - startFrame);
    if (remainingLifeSpan <= lifeSpan/5) alpha = 360.00f*(remainingLifeSpan/(lifeSpan/5.00f));
    for (int i = 0; i < blobs.size(); i++){
      Blob b = blobs.get(i);
      b.setAlpha(alpha);
      b.drawBlob();
    }
  }
}

/** 
 * Game describes the game-state, containing levels, the player, a UI, and all the level contents:
 */
public class Game extends State {
  
  /** Constants: */
  public static final float TILE_SIZE = 50; // 10 to see map
  
  /** Fields: */
  private boolean gameOver; // true when player dies
  private boolean levelCompleted; // true when all enemies are killed
  private boolean gameFinished; // true when finished all levels
  private long levelStart; // frame that started level
  private int gracePeriod = 60; // 1 second upon spawning
  
  // Camera/Controls: 
  private PVector cameraPos = new PVector(width/2, height/2);
  private float zoom;
  private HashMap<String, Boolean> controls = new HashMap<String, Boolean>();
  private PVector shift = new PVector(0, 0);
  private PVector shiftSpeed = new PVector(0, 0);
  private float shiftMax = 15;
   
  // Background:
  private Background background;
  
  // Player:
  private Player player;
  
  // Enemies:
  private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
  
  // Splatters / Animations / Explosions:
  private ArrayList<Splatter> splatters = new ArrayList<Splatter>();
  private ArrayList<Explosion> explosions = new ArrayList<Explosion>();
  private ArrayList<Explosion> stationaryExplosions = new ArrayList<Explosion>();
  private ArrayList<FlyingText> flyingText = new ArrayList<FlyingText>();
  
  // Bullets:
  private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
  
  // Items:
  private ArrayList<Item> items = new ArrayList<Item>();
  private GunItem closestGun = null;
  private float closestDist;
  
  // Levels:
  private ArrayList<Level> levels = new ArrayList<Level>();
  private Level level;
  private int numOfLevels = 10;
  
  // UI:
  private UI ui = new UI();
  
  // Endscreen:
  private PFont endFont;
  private float alpha = 0;
  
  public Game(float hue){
    // Set up controls:
    initialiseControls();
    
    // Create background:
    background = new Background((int)(width/3.0f), 25);
    
    // Create player:
    float playerSize = TILE_SIZE/2.0f;
    float playerMaxSpeed = 8;
    player = new Player(playerSize, playerMaxSpeed, new Pistol(bullets, 10, 10, 15), hue);
    
    
    // Create levels:
    for (int i = 0; i < numOfLevels; i++) levels.add(new Level(i, bullets));
    setLevel(0);
    
    // Create endscreen font:
    endFont = createFont("Rubik-Bold", width/10);
  }
  
  public void initialiseControls(){
    controls.put("l", false);
    controls.put("r", false);
    controls.put("u", false);
    controls.put("d", false);
    
    controls.put("0", false);
    controls.put("1", false);
    controls.put("2", false);
    controls.put("3", false);
    
    controls.put("ml", false);
    controls.put("mr", false);
  }
  
  public void setLevel(int i){
    if (level != null) level.setPlayerPos(player.x(), player.y());
    if (i < 0 || i >= levels.size()) return;
    level = levels.get(i);
    levelCompleted = false;
    levelStart = frameCount;
    player.setPos(level.playerX(), level.playerY());
    player.setSpeed(0, 0); // start level stationary
    enemies = level.enemies();
    bullets.clear();
    splatters.clear();
    explosions.clear();
    stationaryExplosions.clear();
    items.clear();
    zoom = 0.5f; // zoom out
    flyingText.clear();
    flyingText.add(new FlyingText("Level", 0, 0));
    flyingText.add(new FlyingText(""+(level.num()+1), 1, 0));
  }
  
  public void mouseP(){
    if (mouseButton == LEFT) controls.put("ml", true);
    else if (mouseButton == RIGHT) controls.put("mr", true);
  }
  public void mouseR(){
    if (mouseButton == LEFT) controls.put("ml", false);
    else if (mouseButton == RIGHT) controls.put("mr", false);
  }
  public void keyP(){
    if (key == 'w' || key == 'W') controls.put("u", true);
    else if (key == 'a' || key == 'A') controls.put("l", true);
    else if (key == 's' || key == 'S') controls.put("d", true);
    else if (key == 'd' || key == 'D') controls.put("r", true);
    else if (keyCode == RIGHT) controls.put("0", true);
    else if (keyCode == DOWN) controls.put("1", true);
    else if (keyCode == LEFT) controls.put("2", true);
    else if (keyCode == UP) controls.put("3", true);
    
    else if (key == 'o' || key == 'O') setLevel(level.num()-1);
    else if (key == 'p' || key == 'P') setLevel(level.num()+1);
    
    else if (key == ' '){
      if (gameOver || gameFinished) setState(new Menu());
      else if (closestGun != null){
        // Drops gun:
        float speed = 3;
        float direction = random(0, TWO_PI);
        items.add(new GunItem(player.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), player.equippedGun()));
        // Equips gun:
        player.equipGun(closestGun.gun());
        items.remove(closestGun);
        closestGun = null;
      }
      else if (levelCompleted) setLevel(level.num()+1);
    }
  }
  public void keyR(){
    if (key == 'w' || key == 'W') controls.put("u", false);
    else if (key == 'a' || key == 'A') controls.put("l", false);
    else if (key == 's' || key == 'S') controls.put("d", false);
    else if (key == 'd' || key == 'D') controls.put("r", false);
    else if (keyCode == RIGHT) controls.put("0", false);
    else if (keyCode == DOWN) controls.put("1", false);
    else if (keyCode == LEFT) controls.put("2", false);
    else if (keyCode == UP) controls.put("3", false);
  }
  
  public void updateState(){
    // Player:
    float direction = atan2(mouseY-height/2.0f, mouseX-width/2.0f);
    if (!gameOver){
      player.controlPlayer(controls, direction);
      player.updateCharacter(level.wallMap(), level.crateMap());
      if (player.dead()){
        shift.add(2*random(-shiftMax, shiftMax), 2*random(-shiftMax, shiftMax));
        splatters.add(new Splatter(player.x(), player.y()));
        gameOver = true;
      }
    }
    
    // Enemies:
    for (int i = 0; i < enemies.size(); i++){
      Enemy e = enemies.get(i);
      if (e.dead()){
        enemies.remove(e);
        i--;
        splatters.add(new Splatter(e.x(), e.y()));
        shift.add(random(-shiftMax, shiftMax), random(-shiftMax, shiftMax));
        int healthOrbs = (int)random(0, 4);
        float speed = 10;
        for (int h = 0; h < healthOrbs; h++){
          direction = random(0, TWO_PI);
          items.add(new HealthOrb(e.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), (int)random(1, 4)));
        }
        speed = 5;
        direction = random(0, TWO_PI);
        items.add(new GunItem(e.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), e.equippedGun()));
      }
      // Enemies shooting:
      else {
        PVector ePos = e.pos();
        PVector pPos = player.pos();
        if (!gameOver && (frameCount > levelStart+gracePeriod) && e.cooldownOver() && e.inLineOfSight(pPos, level.walls(), level.crates())){
          direction = atan2(pPos.y - ePos.y, pPos.x - ePos.x);
          e.shoot(direction);
        }
        // Update:
        e.updateCharacter(level.wallMap(), level.crateMap());
      }
    }
    
    // Crates:
    ArrayList<Crate> crates = level.crates();
    for (int i = 0; i < crates.size(); i++){
      Crate c = crates.get(i);
      if (c.destroyed()){
        if (c.explosive()){
          shift.add(2*random(-shiftMax, shiftMax), 2*random(-shiftMax, shiftMax));
          explosions.add(new Explosion(c.pos().copy(), TILE_SIZE*4, 50, color(60, 360, 360), color(0, 360, 360), player, enemies, crates));
        }
        else {
          shift.add(0.5f*random(-shiftMax, shiftMax), 0.5f*random(-shiftMax, shiftMax));
          explosions.add(new Explosion(c.pos().copy(), TILE_SIZE, color(360), color(250)));
          int random = (int)random(0, 3);
          if (random == 0){ // drop health orb:
            float speed = 5.0f;
            direction = random(0, TWO_PI);
            items.add(new HealthOrb(c.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), (int)random(1, 3)));
          }
        }
        crates.remove(c);
        level.removeFromCrateMap(c);
        i--;
      }
    }
    
    // Check if level completed:
    if (enemies.isEmpty() && !levelCompleted){
      levelCompleted = true;
      if (level.num()+1 == numOfLevels){ // finished all levels
        gameFinished = true;
        stationaryExplosions.add(new Explosion(new PVector(width/2.0f, height/2.0f), width/2.0f, color(45, 100, 360), color(45, 360, 360), 50));
        flyingText.clear();
        flyingText.add(new FlyingText("Congratulations", 0, 45));
        flyingText.add(new FlyingText("You Win!", 1, 45));
      }
      else {
        stationaryExplosions.add(new Explosion(new PVector(width/2.0f, height/2.0f), width/2.0f, color(player.realHue(), 100, 360), color(player.realHue(), 360, 360), 50));
        flyingText.clear();
        flyingText.add(new FlyingText("Level "+(level.num()+1), 0, 225));
        flyingText.add(new FlyingText("Complete", 1, 225));
      }
    }
    
    // Make list of all characters (player + enemies):
    ArrayList<Character> characters = new ArrayList<Character>();
    if (!gameOver) characters.add(player);
    characters.addAll(enemies);
    
    // Bullets:
    for (int i = 0; i < bullets.size(); i++){
      Bullet b = bullets.get(i);
      if (b.expired()){
        bullets.remove(b);
        i--;
      }
      else if (b.hitWall(level.wallMap()) && !b.bounces()){
        explosions.add(new Explosion(b.pos().copy(), TILE_SIZE*0.75f, color(b.hue(), 100, 360), color(b.hue(), 360, 360)));
        bullets.remove(b);
        i--;
      }
      else if (b.hitCharacter(characters)){
        explosions.add(new Explosion(b.pos().copy(), TILE_SIZE, color(0, 360, 360), color(0, 360, 144)));
        bullets.remove(b);
        i--;
        shift.add(0.25f*random(-shiftMax, shiftMax), 0.25f*random(-shiftMax, shiftMax));
      }
      else {
        if (b.hitCrate(level.crateMap()) && b.destroyed()){
          explosions.add(new Explosion(b.pos().copy(), TILE_SIZE*0.75f, color(b.hue(), 100, 360), color(b.hue(), 360, 360)));
          bullets.remove(b);
          i--;
          continue;
        }
        b.updateBullet();
      }
    }
  }
  
  public void drawState(){
    
    // Space background:
    background.drawBackground();
    
    // Camera start:
    float defaultZoom = 1.5f;
    float cameraEasing = 0.07f;
    zoom += (defaultZoom-zoom)/100.0f;
    
    pushMatrix();
    translate(player.x(), player.y());
    scale(zoom, zoom);
    translate(-player.x(), -player.y());
    cameraPos.add(shift);
    float x = cameraPos.x;
    float y = cameraPos.y;
    float dx = player.x() - x;
    float dy = player.y() - y;
    x += dx * cameraEasing;
    y += dy * cameraEasing;
    translate((width/2 - x)/zoom, (height/2 - y)/zoom);
    cameraPos.sub(shift);
    cameraPos.set(x, y);
    
    // Draw tiles:
    level.drawTiles();
    
    // Draw blood splatters:
    for (int i = 0; i < splatters.size(); i++){
      Splatter splatter = splatters.get(i);
      if (splatter.finished()){
        splatters.remove(splatter);
        i--;
      }
      else splatter.drawSplatter();
    }
    
    // Draw walls and crates:
    level.drawCrates();
    level.drawWalls();
    
    // Draw items:
    for (int i = 0; i < items.size(); i++){
      Item item = items.get(i);
      item.updateItem();
      if (item.follows()){
        item.follow(player.pos());
        if (item.touchingCharacter(player)){
          if (item instanceof HealthOrb && !player.atFullHealth()){
            HealthOrb orb = (HealthOrb)item;
            player.heal(orb.health());
            items.remove(item);
            i--;
            continue;
          }
        }
      }
      item.drawItem();
    }
    // Get gun closest to player:
    closestGun = null;
    for (Item i : items){
      if (i instanceof GunItem && i.inRange(player.pos())){
        // Is in range to be picked up:
        float dist = dist(player.pos().x, player.pos().y, i.pos().x, i.pos().y);
        if (closestGun == null || dist <= closestDist){
          closestGun = (GunItem)i;
          closestDist = dist;
        }
      }
    }
    
    // Draw characters:
    if (!gameOver) player.drawCharacter();
    for (Enemy e : enemies) e.drawCharacter();
    
    // Draw projectiles:
    for (Bullet b : bullets) b.drawBullet();
    
    // Draw explosions:
    for (int i = 0; i < explosions.size(); i++){
      Explosion e = explosions.get(i);
      if (e.finished()){
        explosions.remove(e);
        i--;
      }
      else e.drawExplosion();
    }
    
    // Camera end:
    popMatrix();
    
    // Update screen shift:
    shiftSpeed.sub(shift.x/5.0f, shift.y/5.0f);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75f);
    
    // Draw UI:
    ui.drawHealthBar(player.health(), player.maxHealth());
    ui.drawMinimap(level, player.pos, gameOver);
    ui.drawGun(player.equippedGun());
    if (closestGun != null){
      String text = "Press SPACE to pick up "+closestGun.gun().name()+".";
      ui.drawPrompt(text);
    }
    else if (levelCompleted){
      String text;
      if (gameFinished) text = "Congratulations! You have won! Press SPACE to continue.";
      else text = "Level completed: Press SPACE to continue...";
      ui.drawPrompt(text);
    }
    
    // Draw stationary explosions:
    for (int i = 0; i < stationaryExplosions.size(); i++){
      Explosion e = stationaryExplosions.get(i);
      if (e.finished()){
        stationaryExplosions.remove(e);
        i--;
      }
      else e.drawExplosion();
    }
    
    // Flying text animations:
    for (int i = 0; i < flyingText.size(); i++){
      FlyingText ft = flyingText.get(i);
      if (ft.finished()){
        flyingText.remove(ft);
        i--;
      }
      else ft.drawFlyingText();
    }
    
    // End-screen if gameOver:
    if (gameOver) endScreen();
  }
  
  public void endScreen(){
    // Fade screen to black:
    noStroke();
    fill(0, alpha);
    rect(width/2.0f, height/2.0f, width, height);
    if (alpha < 200) alpha += 2;
    
    // Print GAME OVER and instructions:
    textAlign(CENTER, CENTER);
    textFont(endFont);
    String text = "GAME OVER";
    String instructions = "Press SPACE to exit to menu...";
    
    // Coloured "GAME OVER"
    fill(0, 360, 360);
    pushMatrix();
    float offset = width/500.0f;
    translate(offset, offset);
    textSize(width/10.0f); 
    text(text, width/2, height/2);
    textSize(width/30.0f); 
    text(instructions, width/2, height*3/4);
    popMatrix();
    
    // White "GAME OVER"
    fill(360);
    pushMatrix();
    //translate(shiftX, shiftY);
    textSize(width/10.0f); 
    text(text, width/2, height/2);
    textSize(width/30.0f); 
    text(instructions, width/2, height*3/4);
    popMatrix();
  }
}

/** 
 * Gun describes a gun object with specified characteristics.
 * Guns shoot bullet objects (below):
 * Weapons are equipped by players and enemies:
 */
public abstract class Gun {
  
  /** Constants: */
  protected final float TPS = Game.TILE_SIZE/60.0f; // tiles per second
  
  /** Fields: */
  protected ArrayList<Bullet> bullets;
  protected String name;
  protected float damage, speed, inaccuracy, size, force, durability, crateDamage;
  protected float cooldown, lifespan;
  protected long lastShotFrame;
  protected boolean bounces;
  
  protected float fpt(){ // frames per tile
    if (speed == 0) return 0;
    return (60.0f/speed);
  }
  
  public String name(){ return name; }
  public ArrayList<Bullet> bullets(){ return bullets; }
  public abstract void shoot(boolean player, PVector pos, float direction, float hue);
  public boolean cooldownOver(){ return (lastShotFrame + cooldown < frameCount); }
}

public class Pistol extends Gun {
  
  public Pistol(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Pistol";
    //bounces = true;
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/120.0f;
    size = 15;
    force = 3.0f;
    this.cooldown = cooldown;
    lifespan = (int)(6.0f*fpt());
    durability = 1;
    crateDamage = 7;
  }
  
  public Pistol(ArrayList<Bullet> bullets){
    name = "Pistol";
    this.bullets = bullets;
    damage = 10;
    speed = 2;
    inaccuracy = TWO_PI/120.0f;
    size = 15;
    force = 3.0f;
    cooldown = 25;
    lifespan = (int)(6.0f*fpt());
    durability = 1;
    crateDamage = 7;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    float speedX = speed*cos(direction);
    float speedY = speed*sin(direction);
    //vel.add(speedX, speedY);
    PVector vel = new PVector(speedX, speedY);
    bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, hue, lifespan, crateDamage, durability));
  }
}

public class Shotgun extends Gun {
  
  /** Fields: */
  private int streams = 5; // pellets shot
  private float angle = PI/18.0f; // angle between pellets
  
  public Shotgun(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Shotgun";
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/120.0f;
    size = 15;
    force = 5.0f;
    this.cooldown = cooldown;
    lifespan = (int)(4.0f*fpt());
    durability = 3;
    crateDamage = 20;
  }
  
  public Shotgun(ArrayList<Bullet> bullets){
    name = "Shotgun";
    this.bullets = bullets;
    damage = 5;
    speed = 3;
    inaccuracy = TWO_PI/120.0f;
    size = 15;
    force = 5.0f;
    cooldown = 100;
    lifespan = (int)(4.0f*fpt());
    durability = 3;
    crateDamage = 20;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    direction -= ((streams-1)/2.0f)*angle;
    
    for (int i = 0; i < streams; i++){
      float newDirection = direction + i*angle;
      float speedX = speed*cos(newDirection);
      float speedY = speed*sin(newDirection);
      //vel.add(speedX, speedY);
      PVector vel = new PVector(speedX, speedY);
      bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, hue, lifespan, crateDamage, durability));
    }
  }
}

public class AssaultRifle extends Gun {
  
  public AssaultRifle(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Assault Rifle";
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/100.0f;
    size = 10;
    force = 3.0f;
    this.cooldown = cooldown;
    lifespan = (int)(9.0f*fpt());
    durability = 1;
    crateDamage = 5;
  }
  
  public AssaultRifle(ArrayList<Bullet> bullets){
    name = "Assault Rifle";
    this.bullets = bullets;
    damage = 3;
    speed = 5;
    inaccuracy = TWO_PI/100.0f;
    size = 10;
    force = 3.0f;
    cooldown = 8;
    lifespan = (int)(9.0f*fpt());
    durability = 1;
    crateDamage = 5;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    float speedX = speed*cos(direction);
    float speedY = speed*sin(direction);
    //vel.add(speedX, speedY);
    PVector vel = new PVector(speedX, speedY);
    bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, hue, lifespan, crateDamage, durability));
  }
}

public class SniperRifle extends Gun {
  
  public SniperRifle(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Sniper Rifle";
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = 0;
    size = 10;
    force = 10.0f;
    this.cooldown = cooldown;
    lifespan = (int)(15.0f*fpt());
    durability = 10;
    crateDamage = 40;
  }
  
  public SniperRifle(ArrayList<Bullet> bullets){
    name = "Sniper Rifle";
    this.bullets = bullets;
    damage = 25;
    speed = 15;
    inaccuracy = TWO_PI/120.0f;
    size = 10;
    force = 10.0f;
    cooldown = 100;
    lifespan = (int)(15.0f*fpt());
    durability = 10;
    crateDamage = 40;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    float speedX = speed*cos(direction);
    float speedY = speed*sin(direction);
    //vel.add(speedX, speedY);
    PVector vel = new PVector(speedX, speedY);
    bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, hue, lifespan, crateDamage, durability));
  }
}

public class Bouncer extends Gun {
  
  public Bouncer(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Bouncer";
    bounces = true;
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/60.0f;
    size = 10;
    force = 3.0f;
    this.cooldown = cooldown;
    lifespan = (int)(15.0f*fpt());
    durability = 3;
    crateDamage = 4;
  }
  
  public Bouncer(ArrayList<Bullet> bullets){
    name = "Bouncer";
    bounces = true;
    this.bullets = bullets;
    damage = 2;
    speed = 5;
    inaccuracy = TWO_PI/60.0f;
    size = 10;
    force = 3.0f;
    cooldown = 6;
    lifespan = (int)(15.0f*fpt());
    durability = 3;
    crateDamage = 4;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    float speedX = speed*cos(direction);
    float speedY = speed*sin(direction);
    //vel.add(speedX, speedY);
    PVector vel = new PVector(speedX, speedY);
    bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, hue, lifespan, crateDamage, durability));
  }
}

public class FlameThrower extends Gun {
  
  public FlameThrower(ArrayList<Bullet> bullets, float damage, float speed, float cooldown){
    name = "Flame Thrower";
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/10.0f;
    size = 15;
    force = 1.0f;
    this.cooldown = cooldown;
    lifespan = (int)(3.0f*fpt());
    durability = 1;
    crateDamage = 2;
  }
  
  public FlameThrower(ArrayList<Bullet> bullets){
    name = "Flame Thrower";
    this.bullets = bullets;
    damage = 1;
    speed = 7;
    inaccuracy = TWO_PI/10.0f;
    size = 15;
    force = 1.0f;
    cooldown = 0.5f;
    lifespan = (int)(2.0f*fpt());
    durability = 1;
    crateDamage = 2;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    float speedX = speed*cos(direction);
    float speedY = speed*sin(direction);
    //vel.add(speedX, speedY);
    PVector vel = new PVector(speedX, speedY);
    bullets.add(new Bullet(player, bounces, pos.copy(), vel, damage, size, force, 35, lifespan, crateDamage, durability));
  }
}

/**
 * Bullets describe a moving circle that inflicts damage upon hitting a character or crate:
 */
public class Bullet {
  
  /** Fields: */
  private boolean player, bounces;
  private PVector pos, speed;
  private float damage, size, force, hue, crateDamage, durability, lifespan;
  private long spawnFrame;
  private PVector[] trail = new PVector[10];
  
  public Bullet(boolean player, boolean bounces, PVector pos, PVector speed, float damage, float size, float force, float hue, float lifespan, float crateDamage, float durability){
    this.player = player;
    this.bounces = bounces;
    this.pos = pos;
    this.speed = speed;
    this.damage = damage;
    this.size = size;
    this.force = force;
    this.hue = hue;
    this.lifespan = lifespan;
    this.crateDamage = crateDamage;
    this.durability = durability;
    spawnFrame = frameCount;
    for (int i = 0; i < trail.length; i++){
      trail[i] = pos.copy();
    }
  }
  
  public PVector pos(){ return pos; }
  public float left(){ return pos.x-(size/2.0f); }
  public float top(){ return pos.y-(size/2.0f); }
  public float right(){ return pos.x+(size/2.0f); }
  public float bottom(){ return pos.y+(size/2.0f); }
  public float hue(){ return hue; }
  
  public boolean expired(){ return (frameCount > spawnFrame + lifespan); }
  public void damage(float dmg){ durability -= dmg; }
  public float durability(){ return durability; }
  public boolean destroyed(){ return (durability <= 0); }
  public boolean bounces(){ return bounces; }
  public boolean hitWall(ArrayList<Wall>[][] wallMap){
    float tileSize = Game.TILE_SIZE;
    int row = (int)(pos.y/tileSize);
    int col = (int)(pos.x/tileSize);
    ArrayList<Wall> walls = wallMap[row][col];
    
    if (walls != null){
      for (Wall w : walls){
        // Bounding box (efficient):
        boolean collidingVertically = (top() < w.bottom()) && (bottom() > w.top());
        boolean collidingHorizontally = (left() < w.right()) && (right() > w.left());
        if (bounces){
          float r = size/2.0f;
          if (collidingVertically){
            if (left()+speed.x <= w.right() && left()-speed.x >= w.right()){ // bullet hit right of wall
              w.glow();
              pos.set(w.right()+(r), pos.y);
              speed.set(-speed.x, speed.y);
            }
            else if (right()+speed.x >= w.left() && right()-speed.x <= w.left()){ // bullet hit left of wall
              w.glow();
              pos.set(w.left()-(r), pos.y);
              speed.set(-speed.x, speed.y);
            }
          }
          else if (collidingHorizontally){
            if (top()+speed.y <= w.bottom() && top()-speed.y >= w.bottom()){ // bullet hit bottom of wall
              w.glow();
              pos.set(pos.x, w.bottom()+(r));
              speed.set(speed.x, -speed.y);
            }
            else if (bottom()+speed.y >= w.top() && bottom()-speed.y <= w.top()){ // bullet hit top of wall
              w.glow();
              pos.set(pos.x, w.top()-(r));
              speed.set(speed.x, -speed.y);
            }
          }
        }
        if (collidingVertically && collidingHorizontally) return true;
      }
    }
    return false;
  }
  
  public boolean hitCrate(ArrayList<Crate>[][] crateMap){
    float tileSize = Game.TILE_SIZE;
    int row = (int)(pos.y/tileSize);
    int col = (int)(pos.x/tileSize);
    ArrayList<Crate> crates = crateMap[row][col];
    
    if (crates != null){
      for (Crate c : crates){
        // Bounding box (efficient):
        boolean collidingVertically = (top() < c.bottom()) && (bottom() > c.top());
        boolean collidingHorizontally = (left() < c.right()) && (right() > c.left());
        if (bounces){
          float r = size/2.0f;
          if (collidingVertically){
            if (left()+speed.x <= c.right() && left()-speed.x >= c.right()){ // hit right of crate
              pos.set(c.right()+(r), pos.y);
              speed.set(-speed.x, speed.y);
              c.damage(crateDamage);
              durability--;
            }
            else if (right()+speed.x >= c.left() && right()-speed.x <= c.left()){ // hit left of crate
              pos.set(c.left()-(r), pos.y);
              speed.set(-speed.x, speed.y);
              c.damage(crateDamage);
              durability--;
            }
          }
          else if (collidingHorizontally){
            if (top()+speed.y <= c.bottom() && top()-speed.y >= c.bottom()){ // hit bottom of crate
              pos.set(pos.x, c.bottom()+(r));
              speed.set(speed.x, -speed.y);
              c.damage(crateDamage);
              durability--;
            }
            else if (bottom()+speed.y >= c.top() && bottom()-speed.y <= c.top()){// hit top of crate
              pos.set(pos.x, c.top()-(r));
              speed.set(speed.x, -speed.y);
              c.damage(crateDamage);
              durability--;
            }
          }
        }
        else if (collidingVertically && collidingHorizontally){
          c.damage(crateDamage);
          durability--;
        }
        if (collidingVertically && collidingHorizontally) return true;
      }
    }
    return false;
  }
  
  public boolean hitCharacter(ArrayList<Character> chars){
    for (Character c : chars){
      boolean collidingVertically = (top() < c.bottom()) && (bottom() > c.top());
      boolean collidingHorizontally = (left() < c.right()) && (right() > c.left());
      if (collidingVertically && collidingHorizontally){
        if (player != (c instanceof Player)){ // player can't hit self and enemies can't hit enemies
          c.damage(damage);
          // Apply force/knockback:
          float direction = atan2(c.y()-pos.y, c.x()-pos.x);
          float speedX = force*cos(direction);
          float speedY = force*sin(direction);
          c.addSpeed(new PVector(speedX, speedY));
          return true;
        }
      }
    }
    return false;
  }
  
  public void updateBullet(){ pos.add(speed); }
  public void drawBullet(){
    this.drawTrail();
    strokeWeight(1);
    stroke(hue, 360, 250);
    fill(hue, 360, 360);
    ellipse(pos.x, pos.y, size, size);
    noStroke();
    fill(hue, 100, 360);
    ellipse(pos.x, pos.y, size/2.0f, size/2.0f);
  }
  
  public void drawTrail(){
    noStroke();
    fill(hue, 100, 360);
    int lastIndex = trail.length-1;
    
    // Draws and updates trail position:
    for (int i = 0; i < lastIndex-1; i++){
      float size = this.size*i/trail.length;
      ellipse(trail[i].x, trail[i].y, size, size);
      if (lastIndex-1 % 2 == 0) trail[i].set(trail[i+2].copy());
      else trail[i].set(PVector.lerp(trail[i+1], trail[i+2], 0.5f));
    }
    float size = this.size*(lastIndex-1)/trail.length;
    ellipse(trail[lastIndex-1].x, trail[lastIndex-1].y, size, size);
    trail[lastIndex-1].set(PVector.lerp(pos, trail[lastIndex], 0.5f));
    
    size = this.size*lastIndex/trail.length;
    ellipse(trail[lastIndex].x, trail[lastIndex].y, size, size);
    trail[lastIndex].set(pos.copy());
  }
}

/**
 * Items are objects that drop from certain events.
 * HealthOrbs follow the player and add health.
 * WeaponItems drop on the ground and can be picked up to replace the Player's current weapon.
 */
public abstract class Item {
  
  /** Fields: */
  protected PVector pos, speed;
  protected float range, size, hue;
  protected boolean follows;
  
  
  public PVector pos(){ return pos; }
  public boolean follows(){ return follows; }
  public boolean inRange(PVector p){
    boolean inBoundingBox = (pos.x-range <= p.x && p.x <= pos.x+range) && (pos.y-range <= p.y && p.y <= pos.y+range);
    if (!inBoundingBox) return false;
    return (dist(pos.x, pos.y, p.x, p.y) <= range);
  }
  public boolean touchingCharacter(Character c){
    return (c.left() <= pos.x && pos.x <= c.right()) && (c.top() <= pos.y && pos.y <= c.bottom());
  }
  public void follow(PVector p){
    float direction = atan2(p.y-pos.y, p.x-pos.x);
    float magnitude = 1-dist(pos.x, pos.y, p.x, p.y)/range;
    magnitude *= 4;
    if (magnitude <= 0) return;
    pos.add(magnitude*cos(direction), magnitude*sin(direction));
  }
  public void updateItem(){ 
    pos.add(speed);
    // Apply friction:
    speed.mult(0.9f);
    if (speed.x < 0.1f) speed.set(0, speed.y);
    if (speed.y < 0.1f) speed.set(speed.x, 0);
  }
  public abstract void drawItem();
}

public class HealthOrb extends Item {
  
  /** Fields: */
  private float health;
  
  public HealthOrb(PVector pos, PVector speed, float health){
    float tileSize = Game.TILE_SIZE;
    this.pos = pos;
    this.speed = speed;
    range = tileSize*3;
    size = tileSize/5.0f + health*(tileSize/25.0f);
    hue = 100;
    this.health = health;
    follows = true;
  }
  
  public float health(){ return health; }
  
  public void drawItem(){
    noStroke();
    fill(hue, 360, 360);
    if (health > 2) rect(pos.x, pos.y, size, size, size/4.0f);
    else ellipse(pos.x, pos.y, size, size);
  }
}

public class GunItem extends Item {
  
  /** Fields: */
  private Gun gun;
  
  public GunItem(PVector pos, PVector speed, Gun gun){
    float tileSize = Game.TILE_SIZE;
    this.pos = pos;
    this.speed = speed;
    range = tileSize;
    size = tileSize/4.0f;
    hue = 35;
    follows = false;
    
    String gunName = gun.name();
    switch(gunName){
      case "Pistol": this.gun = new Pistol(gun.bullets(), 10, 10, 15); break;
      case "Shotgun": this.gun = new Shotgun(gun.bullets(), 10, 10, 60); break;
      case "Assault Rifle": this.gun = new AssaultRifle(gun.bullets(), 5, 12, 7); break;
      case "Sniper Rifle": this.gun = new SniperRifle(gun.bullets(), 50, 15, 100); break;
      case "Bouncer": this.gun = new Bouncer(gun.bullets(), 3, 12, 5); break;
      case "Flame Thrower": this.gun = new FlameThrower(gun.bullets(), 1.5f, 7, 0.5f); break;
    }
  }
  
  public Gun gun(){ return gun; }
  
  public void drawItem(){
    noStroke();
    fill(hue, 360, 360);
    rect(pos.x, pos.y, size*1.5f, size);
  }
}

/**
 * Level objects describe a randomly generated level containing rooms, passages, walls, and contents
 * including crates and enemies.
 */
public class Level {
  
  /** Fields: */
  private int levelNum;
  
  // Tile-Map:
  private Tile[][] tiles; // 0 is empty, 1 is floor, 2 is wall
  private int tRows;
  private int tCols;
  
  // Rooms:
  private ArrayList<Room> rooms = new ArrayList<Room>();
  private Room spawnRoom;
  
  // Walls:
  private ArrayList<Wall> walls = new ArrayList<Wall>();
  private ArrayList<Wall>[][] wallMap;
  
  // Player:
  private float playerX, playerY;
  
  // Enemies:
  private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
  
  // Crates:
  private ArrayList<Crate> crates = new ArrayList<Crate>();
  private ArrayList<Crate>[][] crateMap;
  
  // Bullets:
  private ArrayList<Bullet> bullets;
  
  public Level(int num, ArrayList<Bullet> bullets){
    levelNum = num;
    
    // Generate random level layout:
    generateTiles(25, 41); // creats grid of specified size (must be multiple of 4 + 1)
    generateRooms(100); // number of attempts to place rooms
    generatePassages(); // uses maze-generation algorithm to create passages outside rooms
    connectRegions(); // connects passages and rooms until everything is connected
    removeDeadEnds(); // remove passages that lead nowhere
    defineWalls(); // turn walls into big rectangles for efficiency with collision checks
    fillWallMap(); // records the walls near each tile
    
    // Generate enemies, crates and items:
    this.bullets = bullets;
    generateContents(); // generates crates and enemies
  }
  
  public int num(){ return levelNum; }
  public Tile[][] tiles(){ return tiles; }
  public ArrayList<Wall> walls(){ return walls; }
  public ArrayList<Wall>[][] wallMap(){ return wallMap; }
  public ArrayList<Crate> crates(){ return crates; }
  public ArrayList<Crate>[][] crateMap(){ return crateMap; }
  
  public float playerX(){ return playerX; }
  public float playerY(){ return playerY; }
  public void setPlayerPos(float x, float y){
    playerX = x;
    playerY = y; 
  }
  public ArrayList<Enemy> enemies(){ return enemies; }
  
  /** Fills tiles array with empty space (0's): */
  public void generateTiles(int rows, int cols){
    
    if (rows % 2 == 0) rows++; // must both be odd
    if (cols % 2 == 0) cols++;
    tiles = new Tile[rows][cols];
    this.tRows = rows;
    this.tCols = cols;
    for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) tiles[row][col] = new Tile(row, col, 0);
  }
  
  /** Adds rectangles of floor tiles (1's): */
  public void generateRooms(int attempts){
    
    // Create 5x5 spawn room:
    int size = 5;
    PVector centre = generateRoomCentre(size*3, size*3);
    spawnRoom = addRoom(centre, size, size);
    PVector spawnPos = spawnRoom.centrePos();
    playerX = spawnPos.x;
    playerY = spawnPos.y;
    
    int passes = 0;
    int fails = 0;
    while (passes < attempts && fails < 500){
      
      // Generate random room size and position:
      int rows = (int)(random(1, 4))*4 + 1;
      if (rows % 2 == 0) rows++;
      int cols = (int)(random(1, 4))*4 + 1;
      if (cols % 2 == 0) cols++;
      
      centre = generateRoomCentre(rows, cols);
      if (!roomOverlapping(centre, rows, cols)){
        addRoom(centre, rows, cols);
        passes++;
      }
      else fails++;
    }
  }
  
  /** Generates a random position within the level with odd coords (within edges of map): */
  public PVector generateRoomCentre(int rows, int cols){
    
    int rowBoundary = (int)(rows/2.0f) + 1;
    int colBoundary = (int)(cols/2.0f) + 1;
    
    int row = (int)random(rowBoundary, tRows-rowBoundary);
    if (row % 2 == 0) row--;
    int col = (int)random(colBoundary, tCols-colBoundary);
    if (col % 2 == 0) col--;
    
    return new PVector(col, row);
  }
  
  public boolean roomOverlapping(PVector centre, int rows, int cols){
    int centreRow = (int)centre.y;
    int centreCol = (int)centre.x;
    int rowRad = (int)(rows/2.0f);
    int colRad = (int)(cols/2.0f);
    
    for (int row = centreRow-rowRad; row <= centreRow+rowRad; row++){
      for (int col = centreCol-colRad; col <= centreCol+colRad; col++){
        if (tiles[row][col].type() == 1) return true;
      }
    }
    return false;
  }
  
  public Room addRoom(PVector centre, int rows, int cols){
    int centreRow = (int)centre.y;
    int centreCol = (int)centre.x;
    int rowRad = (int)(rows/2.0f);
    int colRad = (int)(cols/2.0f);
    
    for (int row = centreRow-rowRad; row <= centreRow+rowRad; row++){
      for (int col = centreCol-colRad; col <= centreCol+colRad; col++){
        tiles[row][col].setType(1);
      }
    }
    
    Room room = new Room(centreRow, centreCol, rowRad, colRad);
    rooms.add(room);
    return room;
  }
  
  public void generatePassages(){
    ArrayList<PVector> nextToUncarved = new ArrayList<PVector>();
    
    PVector start = spawnRoom.centreTile();
    int tries = 0;
    while (tries < 10){
      int row = (int)start.y;
      int col = (int)start.x;
      int dir = (int)random(0, 4);
      switch (dir){
        case 0: col -= 4; break;
        case 1: row -= 4; break;
        case 2: col += 4; break;
        case 3: row += 4; break;
      }
      if (canBeCarved(row, col)){
        nextToUncarved.add(carvePassage((int)start.y, (int)start.x, row, col));
        start.set(col, row);
        break;
      }
      tries++;
    }
    
    while (true){
      int lastDir = -1;
      while (!nextToUncarved.isEmpty()){
        start = nextToUncarved.get(0);   
        tries = 0;
        while (tries < 10){
          int row = (int)start.y;
          int col = (int)start.x;
          int dir;
          dir = (lastDir == -1 || (int)random(0, 2) == 0)? (int)random(0, 4) : lastDir;
          switch (dir){
            case 0: col -= 2; break;
            case 1: row -= 2; break;
            case 2: col += 2; break;
            case 3: row += 2; break;
          }
          lastDir = dir;
          if (canBeCarved(row, col)){
            nextToUncarved.add(carvePassage((int)start.y, (int)start.x, row, col));
            start.set(col, row);
            break;
          }
          lastDir = -1; // direction failed
          tries++;
        }
        if (canBeCarved((int)start.y, (int)start.x)){
          carvePassage((int)start.y, (int)start.x, (int)start.y, (int)start.x);
        }
        if (tries == 10){
          nextToUncarved.remove(0);
        }
      }
      if (filledWithPassages()) break;
      else nextToUncarved.add(findUncarvedPassage());
    }
  }
  
  public PVector carvePassage(int sRow, int sCol, int eRow, int eCol){
    if (eRow-sRow == 0){
      for (int col = (sCol<eCol)? sCol : eCol; col <= ((sCol>eCol)? sCol : eCol); col++){
        tiles[sRow][col].setType(1);
      }
    }
    else {
      for (int row = (sRow<eRow)? sRow : eRow; row <= ((sRow>eRow)? sRow : eRow); row++){
        tiles[row][sCol].setType(1);
      }
    }
    return new PVector(eCol, eRow);
  }
  
  public boolean canBeCarved(int row, int col){
    boolean withinMap = ((0 <= row && row < tRows) && (0 <= col && col < tCols));
    if (!withinMap) return false;
    boolean notCarved = tiles[row][col].type() == 0;
    return notCarved;
  }
  
  public boolean filledWithPassages(){
    for (int row = 1; row < tRows; row = row+2){
      for (int col = 1; col < tCols; col = col+2){
        if (tiles[row][col].type() == 0) return false;
      }
    }
    return true;
  }
  
  public PVector findUncarvedPassage(){
    for (int row = 1; row < tRows; row = row+2){
      for (int col = 1; col < tCols; col = col+2){
        if (tiles[row][col].type() == 0) return new PVector(col, row);
      }
    }
    return null;
  }
  
  public void connectRegions(){
    // Create list of regions (disconnected sections of tilemap).
    // Surround each unconnected region with potential connectors.
    // Apply floodfill algorithm, starting from spawn.
    // When a potential connector is reached, officially connect it.
    // Restart.
    // Loop until all regionss are connected.
    
    ArrayList<Region> regions = new ArrayList<Region>();
    Tile[][] tilesTemp = copyTileMap(); // copy, all turned into 0's as regions found
    createRegions(regions, tilesTemp);
    
    while (regions.size() > 1){
      tilesTemp = copyTileMap(); // fresh copy
      
      for (int i = 1; i < regions.size(); i++){
        Region r = regions.get(i);
        
        // Surround all regions except first with tiles of type 2:
        for (Tile t : r.tiles()){
          for (int dir = 0; dir < 4; dir++){
            int row = t.row();
            int col = t.col();
            switch (dir){
              case 0: col--; break;
              case 1: row--; break;
              case 2: col++; break;
              case 3: row++; break;
            }
            Tile neighbour = tilesTemp[row][col];
            if (neighbour.type() == 0){
              neighbour.setType(2);
            }
          }
        }
      }
      
      // Flood-fill algorithm to connect a region:
      PVector pos = spawnRoom.centreTile();
      Tile startTile = tilesTemp[(int)pos.y][(int)pos.x];
      ArrayList<Tile> nextTiles = new ArrayList<Tile>();
      startTile.setType(0);
      nextTiles.add(startTile);
      while (true){
        boolean done = false;
        //if (nextTiles.size() == 0) break;
        Tile t = nextTiles.remove(0);
        for (int dir = 0; dir < 4; dir++){
          int row = t.row();
          int col = t.col();
          switch (dir){
            case 0: col--; break;
            case 1: row--; break;
            case 2: col++; break;
            case 3: row++; break;
          }
          Tile neighbour = tilesTemp[row][col];
          
          // Connector found:
          if (neighbour.type() == 2){
            tiles[row][col].setType(1); 
            Region r = regionContainingTile(2*row-t.row(), 2*col-t.col(), regions);
            
            // Connect r to first/spawn region:
            Region spawnRegion = regions.get(0);
            spawnRegion.addTile(tiles[row][col]); // add new door/connector that was made
            spawnRegion.addAllTiles(r);
            regions.remove(r);
            
            done = true;
            break;
          }
          else if (neighbour.type() == 1){
            neighbour.setType(0);
            nextTiles.add(neighbour);
          }
        }
        if (done) break;
       // break;
      }
     // break;
    }
    //drawTiles(tilesTemp);
    //drawRegions(regions);
  }
  
  public void createRegions(ArrayList<Region> regions, Tile[][] tilesTemp){
    // Start at spawn, and use floodfill algorithm to add all connected type 1 tiles to new region.
    // While floodfilling, make all those tiles type 0.
    // When done, check if any more type 1 tiles. If no, return.
    // If yes, start at type 1 tile and floodfill to add to new region.
    // Repeat.
    
    while(typeFound(1, tilesTemp)){ // while white squares found
      Tile startTile;
      //println("Starting region: "+regions.size()+", Tiles left: "+numberOfTiles(1, tilesTemp));
      
      if (regions.isEmpty()){ // if no regions made yet, make first region start at player spawn
        PVector startPos = spawnRoom.centreTile();
        int startRow = (int)startPos.y;
        int startCol = (int)startPos.x;
        startTile = tilesTemp[startRow][startCol];
      }
      else { // else if not 1st region, make next region start at random white tile
        startTile = getTile(1, tilesTemp);
      }
      //println("Start pos: "+startTile.row()+", "+startTile.col());
      
      ArrayList<Tile> nextTiles = new ArrayList<Tile>(); // list to store white neighbours in region
      startTile.setType(0); // make starting tile black
      nextTiles.add(startTile); // start with starting tile
      Region region = new Region();
      regions.add(region); // make new region and add it to list
      
      while (!nextTiles.isEmpty()){ // while there are still white tiles to be processed in current region
        
        // Remove first tile from nextTiles and add it to region:
        Tile t = nextTiles.remove(0);
        region.addTile(tiles[t.row()][t.col()]);
        //println("  Tile added: "+t.row()+", "+t.col());
        
        // For each direction from tile, if neighbour is white, make it black and add it to nextTiles:
        for (int dir = 0; dir < 4; dir++){
            int row = t.row();
            int col = t.col();
            switch (dir){
              case 0: col--; break;
              case 1: row--; break;
              case 2: col++; break;
              case 3: row++; break;
            }
            Tile tNext = tilesTemp[row][col];
            if (tNext.type() == 1){
              tNext.setType(0);
              nextTiles.add(tNext);
            }
         }
      }
    }
    //drawRegions(regions);
  }
  
  public int numberOfTiles(int type, Tile[][] tilesTemp){
    int count = 0;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) count++;
      }
    }
    return count;
  }
  
  public boolean typeFound(int type, Tile[][] tilesTemp){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) return true;
      }
    }
    return false;
  }
  
  public Tile getTile(int type, Tile[][] tilesTemp){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) return tilesTemp[row][col];
      }
    }
    return null;
  }
  
  public Tile[][] copyTileMap(){
    Tile[][] tilesTemp = new Tile[tRows][tCols];
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        tilesTemp[row][col] = new Tile(row, col, tiles[row][col].type());
      }
    }
    return tilesTemp;
  }
  
  public Region regionContainingTile(int row, int col, ArrayList<Region> regions){
    for (Region region : regions){
      if (region.containsTile(row, col)) return region;
    }
    return null;
  }
  
  public void removeDeadEnds(){
    boolean done = false;
    while (!done){
      done = true;
      for (int row = 0; row < tRows; row++){
        for (int col = 0; col < tCols; col++){
          if (tiles[row][col].type() == 1){
            
            int exits = 0;
            for (int dir = 0; dir < 4; dir++){
              int r = row;
              int c = col;
              switch (dir){
                case 0: c--; break;
                case 1: r--; break;
                case 2: c++; break;
                case 3: r++; break;
              }
              if (tiles[r][c].type() == 1) exits++;
            }
            
            if (exits <= 1){
              done = false;
              tiles[row][col].setType(0);
            }
          }
        }
      }
    }
  }
  
  public void defineWalls(){
    // Convert every empty space (type 0) touching a floor tile to a wall tile (type 2).
    // Join wall tiles in a line together to form one wall object.
    // This makes it so the game requires less collision checks.
    
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        if ((t.type() == 0) && (tileTouchingType(t, 1))){
          t.setType(2);
        }
      }
    }
    
    // Make copy where wall tiles are removed as wall objects are made:
    Tile[][] tilesTemp = copyTileMap();
    
    // Start at wall tile.
    // If wall tile on left or right, horizontally flood-fill to fill list.
    // Else vertically flood-fill to fill list.
    // Remove wall tiles as added to list.
    // Loop until no more wall tiles (type 2).
    
    while (typeFound(2, tilesTemp)){
      Tile startTile = getTile(2, tilesTemp);
      int startRow = startTile.row();
      int startCol = startTile.col();
      
      boolean horizontal;
      if (startCol-1 >= 0 && (tilesTemp[startRow][startCol-1].type() == 2)) horizontal = true; // if wall tile to left
      else if (startCol+1 < tCols && (tilesTemp[startRow][startCol+1].type() == 2)) horizontal = true; // if wall tile to right
      else horizontal = false; // if neither
      
      ArrayList<Tile> wallTiles = new ArrayList<Tile>(); // holds tiles in wall object
      ArrayList<Tile> nextTiles = new ArrayList<Tile>(); // holds tiles to process
      startTile.setType(0);
      nextTiles.add(startTile);
      
      while (!nextTiles.isEmpty()){
        Tile t = nextTiles.remove(0);
        wallTiles.add(tiles[t.row()][t.col()]);
        
        for (int i = -1; i <= 1; i += 2){
          int r = t.row();
          int c = t.col();
          if (horizontal) c += i;
          else r += i;
          
          if (!((0 <= c && c < tCols) && (0 <= r && r < tRows))) continue; // if not within bounds, skip
          Tile neighbour = tilesTemp[r][c];
          if (neighbour.type() == 2){
            neighbour.setType(0);
            nextTiles.add(neighbour);
          }
        }
      }
      walls.add(new Wall(wallTiles));
    }
    //println(walls.size());
  }
  
  /** Returns true if tile t is touching a tile of type: */
  public boolean tileTouchingType(Tile t, int type){
    for (int dir = 0; dir < 8; dir++){
      int r = t.row();
      int c = t.col();
      switch (dir){
        case 0: r++;
        case 1: c--; break;
        case 2: c--;
        case 3: r--; break;
        case 4: r--;
        case 5: c++; break;
        case 6: c++;
        case 7: r++; break;
      }
      if (r < 0 || r >= tRows || c < 0 || c >= tCols) continue;
      if (tiles[r][c].type() == type) return true;
    }
    return false;
  }
  
  public void fillWallMap(){
    wallMap = new ArrayList[tRows][tCols];
    
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        if (t.type() == 2){
          ArrayList<Wall> nearbyWalls = new ArrayList<Wall>();
          for (Wall w : walls){
            if (w.containsTile(t)){
              nearbyWalls.add(w);
              wallMap[row][col] = nearbyWalls;
              break;
            }
          }
        }
        else if (t.type() == 1 && tileTouchingType(t, 2)){
          ArrayList<Wall> nearbyWalls = new ArrayList<Wall>();
          for (int dir = 0; dir < 8; dir++){
            int r = t.row();
            int c = t.col();
            switch (dir){
              case 0: r++;
              case 1: c--; break;
              case 2: c--;
              case 3: r--; break;
              case 4: r--;
              case 5: c++; break;
              case 6: c++;
              case 7: r++; break;
            }
            if (r < 0 || r >= tRows || c < 0 || c >= tCols) continue;
            Tile tNext = tiles[r][c];
            for (Wall w : walls){
              if (w.containsTile(tNext) && !nearbyWalls.contains(w)){
                nearbyWalls.add(w);
              }
            }
          }
          wallMap[row][col] = nearbyWalls;
        }
      }
    }
  }
  
  /** Draw Methods: */
  
  public void drawTiles(){
    //noStroke();
    stroke(360);
    float tileSize = Game.TILE_SIZE;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        int value = tiles[row][col].type();
        if (value == 0) continue;
        float x = col*tileSize + tileSize/2.0f;
        float y = row*tileSize + tileSize/2.0f;
        int r = 0;
        switch (value){
          //case 0: fill(0); break;
          case 1: fill(tiles[row][col].brightness()); break;
          case 2: fill(225, 360, 360); break;
        }
        rect(x, y, tileSize, tileSize);
      }
    }
  }
  
  public void drawTiles(Tile[][] tiles){
    noStroke();
    float tileSize = Game.TILE_SIZE;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        int value = tiles[row][col].type();
        float x = col*tileSize + tileSize/2.0f;
        float y = row*tileSize + tileSize/2.0f;
        switch (value){
          case 0: fill(0); break;
          case 1: fill(360); break;
          case 2: fill(100, 360, 360); break;
        }
        rect(x, y, tileSize, tileSize);
        /*fill(180);
        textSize(10);
        text(row+" "+col, x, y);*/
      }
    }
  }
  
  public void drawRegions(ArrayList<Region> regions){
    float tileSize = Game.TILE_SIZE;
    for (int i = 0; i < regions.size(); i++){
      Region r = regions.get(i);
      fill(regions.indexOf(r)*(360/regions.size()), 360, 360);
      ArrayList<Tile> tiles = r.tiles();
      println("Region "+i+": "+tiles.size());
      for (Tile t : tiles){
        int value = t.type();
        float x = t.col()*tileSize + tileSize/2.0f;
        float y = t.row()*tileSize + tileSize/2.0f;
        rect(x, y, tileSize, tileSize);
      }
    }
  }
  
  public void drawWalls(){
    strokeWeight(5);
    stroke(225, 360, 360);
    for (Wall wall : walls){
      float x = (wall.left()+wall.right())/2.0f;
      float y = (wall.top()+wall.bottom())/2.0f;
      float w = (wall.right()-wall.left());
      float h = (wall.bottom()-wall.top());
      
      int glowFrames = wall.glowFrames();
      if (glowFrames == 0) fill(0);
      else {
        float brightness = 360*glowFrames/wall.maxGlowFrames();
        wall.setGlowFrames(glowFrames-1);
        fill(180, 360, brightness);
      }
      rect(x, y, w, h);
      //fill(180);
      //textSize(15);
      //text(wall.leftCol()+","+wall.rightCol()+" "+wall.topRow()+","+wall.bottomRow(), x, y, w ,h);
    }
  }
  
  public void generateContents(){
    // Get all room floor tiles:
    ArrayList<Tile> floorTiles = new ArrayList<Tile>();
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        
        // If tile is type 1 (floor) and in room:
        if (t.type() != 1) continue;
        boolean inRoom = false;
        for (Room r : rooms){
          if (r == spawnRoom) continue; // so they don't spawn in player spawn room
          if (r.isRoomTile(row, col)){
            inRoom = true;
            break;
          }
        }
        if (inRoom) floorTiles.add(t);
      }
    }
    float tileSize = Game.TILE_SIZE;
    
    // Spawn piles of crates on random room floor tiles:
    crateMap = new ArrayList[tRows][tCols];
    int numOfCratePiles = 15;
    for (int i = 0; i < numOfCratePiles; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float shift = tileSize/4.0f;
      float size = tileSize/2.0f;
      for (int corner = 0; corner < 4; corner++){
        float x = t.col()*tileSize + tileSize/2.0f;
        float y = t.row()*tileSize + tileSize/2.0f;
        switch (corner){
          case 0: x -= shift; y -= shift; break;
          case 1: x += shift; y -= shift; break;
          case 2: x += shift; y += shift; break;
          case 3: x -= shift; y += shift; break;
        }
        boolean explosive = (int)random(0, 20) == 0;
        Crate crate = new Crate(new PVector(x, y), size, explosive);
        crates.add(crate);
        // Fill crate map:
        for (int r = -1; r <= 1; r++){
          for (int c = -1; c <= 1; c++){
            int row = t.row()+r;
            int col = t.col()+c;
            if (row < 0 || row >= tRows || col < 0 || col >= tCols) continue;
            if (crateMap[row][col] == null) crateMap[row][col] = new ArrayList<Crate>();
            ArrayList<Crate> cratesOnTile = crateMap[row][col];
            if (!cratesOnTile.contains(crate)) cratesOnTile.add(crate);
          }
        }
      }
    }
    
    // Spawn explosive crates alone:
    int numOfExplosiveCrates = 5 + levelNum;
    for (int i = 0; i < numOfExplosiveCrates; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float shift = tileSize/4.0f;
      float size = tileSize/2.0f;
      int corner = (int)random(0, 4);
      float x = t.col()*tileSize + tileSize/2.0f;
      float y = t.row()*tileSize + tileSize/2.0f;
      switch (corner){
        case 0: x -= shift; y -= shift; break;
        case 1: x += shift; y -= shift; break;
        case 2: x += shift; y += shift; break;
        case 3: x -= shift; y += shift; break;
      }
      Crate crate = new Crate(new PVector(x, y), size, true);
      crates.add(crate);
      // Fill crate map:
      for (int r = -1; r <= 1; r++){
        for (int c = -1; c <= 1; c++){
          int row = t.row()+r;
          int col = t.col()+c;
          if (row < 0 || row >= tRows || col < 0 || col >= tCols) continue;
          if (crateMap[row][col] == null) crateMap[row][col] = new ArrayList<Crate>();
          ArrayList<Crate> cratesOnTile = crateMap[row][col];
          if (!cratesOnTile.contains(crate)) cratesOnTile.add(crate);
        }
      }
    }
    
    // Spawn enemies on random room floor tiles:
    int numOfEnemies = 10+(2*levelNum);
    for (int i = 0; i < numOfEnemies; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float x = t.col()*tileSize + tileSize/2.0f;
      float y = t.row()*tileSize + tileSize/2.0f;
      Gun gun = new Pistol(bullets);
      int random = (int)random(0, 10);
      switch (random){
        case 0: gun = new Shotgun(bullets); break;
        case 1: gun = new AssaultRifle(bullets); break;
        case 2: gun = new SniperRifle(bullets); break;
        case 3: gun = new Bouncer(bullets); break;
        case 4: gun = new FlameThrower(bullets); break;
      }
      //gun = new FlameThrower(bullets);
      enemies.add(new Enemy(new PVector(x, y), 20 + 5*levelNum, tileSize/2.0f, 3, 15*tileSize, gun));
      //enemies.add(new Enemy(new PVector(x, y), 1, tileSize/2.0, 3, 15*tileSize, gun)); // for debugging
    }
  }
  
  public void drawCrates(){
    for (Crate c : crates) c.drawCrate();
  }
  
  public void removeFromCrateMap(Crate c){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        ArrayList cratesOnTile = crateMap[row][col];
        if (cratesOnTile == null) continue;
        if (cratesOnTile.contains(c)) cratesOnTile.remove(c);
      }
    }
  }
}


public class Room {
  
  /** Fields: */
  private int leftCol, rightCol, topRow, bottomRow;
  
  public Room(int centreRow, int centreCol, int rowRad, int colRad){
    this.topRow = centreRow - rowRad;
    this.bottomRow = centreRow + rowRad;
    this.leftCol = centreCol - colRad;
    this.rightCol = centreCol + colRad;
  }
  
  public int leftCol(){ return leftCol; }
  public int rightCol(){ return rightCol; }
  public int topRow(){ return topRow; }
  public int bottomRow(){ return bottomRow; }
  
  public boolean isRoomTile(int row, int col){
    return ((topRow <= row && row <= bottomRow) && (leftCol <= col && col <= rightCol));
  }
  
  public boolean isInRoom(float x, float y){
    float tileSize = Game.TILE_SIZE;
    float left = leftCol*tileSize;
    float right = (rightCol+1)*tileSize;
    float top = topRow*tileSize;
    float bottom = bottomRow*tileSize;
    return ((left <= x && x <= right) && (top <= y && y <= bottom));
  }
  
  public PVector centrePos(){
    float tileSize = Game.TILE_SIZE;
    float x = (leftCol*tileSize + rightCol*tileSize)/2.0f + tileSize/2.0f;
    float y = (topRow*tileSize + bottomRow*tileSize)/2.0f + tileSize/2.0f;
    return new PVector(x, y);
  }
  
  public PVector centreTile(){
    int col = (leftCol+rightCol)/2;
    int row = (topRow+bottomRow)/2;
    return new PVector(col, row);
  }
  
}

public class Tile {
  
  /** Fields: */
  private int row, col, type;
  private float brightness = random(300, 350);
  
  public Tile(int row, int col, int type){
    this.row = row;
    this.col = col;
    this.type = type;
  }
  
  public int row(){ return row; }
  public int col(){ return col; }
  public void setType(int type){ this.type = type; }
  public int type(){ return type; }
  public float brightness(){ return brightness; }
}

public class Region {
   
  /** Fields: */
  private ArrayList<Tile> tiles = new ArrayList<Tile>();
  
  public void addTile(Tile tile){ tiles.add(tile); }
  public void addAllTiles(Region other){ tiles.addAll(other.tiles); }
  public boolean containsTile(int row, int col){
    for (Tile tile : tiles){
      if (tile.row() == row && tile.col() == col) return true;
    }
    return false;
  }
  public ArrayList<Tile> tiles(){ return tiles; }
}

public class Wall {
  
  /** Fields: */
  private ArrayList<Tile> tiles;
  private float left, top, right, bottom;
  private int leftCol, rightCol, topRow, bottomRow;
  private int maxGlowFrames = 300;
  private int glowFrames;
  
  public Wall(ArrayList<Tile> tiles){
    this.tiles = tiles;
    
    // Get dimensions of wall:
    int leftCol = Integer.MAX_VALUE;
    int rightCol = Integer.MIN_VALUE;
    int topRow = Integer.MAX_VALUE;
    int bottomRow = Integer.MIN_VALUE;
    for (Tile t : tiles){
      if (t.col() < leftCol) leftCol = t.col();
      if (t.col() > rightCol) rightCol = t.col();
      if (t.row() < topRow) topRow = t.row();
      if (t.row() > bottomRow) bottomRow = t.row();
    }
    this.leftCol = leftCol;
    this.rightCol = rightCol;
    this.topRow = topRow;
    this.bottomRow = bottomRow;
    
    float tileSize = Game.TILE_SIZE;
    left = leftCol * tileSize;
    right = (rightCol+1) * tileSize;
    top = topRow * tileSize;
    bottom = (bottomRow+1) * tileSize;
  }
  
  public boolean containsPoint(float x, float y){
    return (left <= x && x <= right) && (top <= y && y <= bottom);
  }
  public boolean containsTile(Tile t){ return (tiles.contains(t)); }
  
  public void glow(){ glowFrames = maxGlowFrames; }
  public int glowFrames(){ return glowFrames; }
  public void setGlowFrames(int f){ glowFrames = f; }
  public int maxGlowFrames(){ return maxGlowFrames; }
  
  public float left(){ return left; }
  public float right(){ return right; }
  public float top(){ return top; }
  public float bottom(){ return bottom; }
  
  public int topRow(){ return topRow; }
  public int bottomRow(){ return bottomRow; }
  public int leftCol(){ return leftCol; }
  public int rightCol(){ return rightCol; }
}

/** Menu describes the title screen with the space background, art, and menu buttons:
*/
public class Menu extends State {
  
  /** Fields: */
  private PFont titleFont;
  private PFont buttonFont;
  private ArrayList<MenuButton> buttons = new ArrayList<MenuButton>();
  private Background background = new Background((int)(width/25.0f), 50);
  private PShape ship;
  private ArrayList<Ship> ships = new ArrayList<Ship>();
  
  // Screen-shaking:
  private PVector shift = new PVector(0, 0);
  private PVector shiftSpeed = new PVector(0, 0);
  private float shiftMax = 20;
  
  // Get methods:
  public PFont titleFont(){ return titleFont; }
  public PFont font(){ return buttonFont; }
  public Background background(){ return background; }
  public PShape ship(){ return ship; }
  public ArrayList<Ship> ships(){ return ships; }
  public PVector shift(){ return shift; }
  public PVector shiftSpeed(){ return shiftSpeed; }
  public float shiftMax(){ return shiftMax; }
  
  public Menu(){
    titleFont = createFont("Impact", width/15);
    //titleFont = createFont("Dialoginput.bold", width/15);
    buttonFont = createFont("Dialoginput.bold", width/50);
    buttons.add(new MenuButton(buttonFont, "New Game", height*2/3));
    
    // Create ship shape:
    ship = createShape();
    float wingWidth = 3.5f;
    
    ship.beginShape();
    ship.fill(360);
    ship.noStroke();
    ship.vertex(0, 4);
    ship.vertex(wingWidth, 0);
    ship.vertex(wingWidth, 4);
    ship.vertex(10-wingWidth, 4);
    ship.vertex(10-wingWidth, 0);
    ship.vertex(10, 4);
    ship.vertex(9, 10);
    ship.vertex(10-wingWidth, 8);
    ship.vertex(wingWidth, 8);
    ship.vertex(1, 10);
    ship.endShape(CLOSE);
  }
  
  public void mouseP(){}
  public void mouseR(){
    for (MenuButton b : buttons){
      if (b.underMouse() && b.getText().equals("New Game")){
        setState(new PreGame(this));
      }
    }
  }
  public void keyP(){}
  public void keyR(){}
  
  public void updateState(){}
  
  public void drawState(){
    pushMatrix();
    translate(shift.x, shift.y);
    drawArt();
    drawUI();
    
    // Update screen shift:
    shiftSpeed.sub(shift.x/5.0f, shift.y/5.0f);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75f);
  }
  
  public void drawArt(){
    // Space and stars:
    background.drawBackground();
    
    // Ships:
    int random = (int)random(0, 60);
    if (random == 0){
      ships.add(new Ship());
    }
    else if (random == 59){
      shift.add(random(-shiftMax, shiftMax), random(-shiftMax, shiftMax));
    }
    for (int i = 0; i < ships.size(); i++){
      Ship s = ships.get(i);
      if (s.offScreen()){
        ships.remove(s);
        i--;
      }
      else s.drawShip(ship);
    }
    
    // Walls and window:
    float windowLeft = width/4.0f;
    float windowRight = width-windowLeft;
    float windowTop = height/6.0f;
    float windowBottom = height-windowTop;
    
    // Walls:
    strokeWeight(10);
    if (frameCount % 100 < 30) stroke(0, 360, 360);
    else stroke(225, 360, 360);
    fill(0, 200);
    rect(windowLeft/2.0f, windowTop, windowLeft, windowTop*2); // top left
    rect(windowRight+(windowLeft/2.0f), windowTop, windowLeft, windowTop*2); // top right
    rect(width/2.0f, windowTop/2.0f, width/2.0f, windowTop); // top
    rect(windowLeft/2.0f, windowBottom, windowLeft, windowTop*2); // bottom left
    rect(windowRight+(windowLeft/2.0f), windowBottom, windowLeft, windowTop*2); // bottom right
    rect(width/2.0f, height-(windowTop/2.0f), width/2.0f, windowTop); // bottom
    
    // Artwork:
    float sirenX = width/8;
    float sirenY = height/6;
    float sirenSize = width/10;
    float lightSize = (sirenSize/3) * sin((TWO_PI/50)*frameCount) + (sirenSize/2);
    float lightLength = width+height;
    float lightWidth = width/3;
    
    // Centre siren light:
    fill(20, 360, 360);
    noStroke();
    ellipse(sirenX, sirenY, lightSize, lightSize);
    
    // Light beams:
    pushMatrix();
    translate(sirenX, sirenY);
    rotate((TWO_PI/200)*frameCount);
    translate(-sirenX, -sirenY);
    beginShape();
    vertex(sirenX, sirenY);
    vertex(sirenX-lightWidth/2, sirenY-lightLength);
    vertex(sirenX+lightWidth/2, sirenY-lightLength);
    vertex(sirenX, sirenY);
    vertex(sirenX+lightWidth/2, sirenY+lightLength);
    vertex(sirenX-lightWidth/2, sirenY+lightLength);
    endShape(CLOSE);
    popMatrix();
    
    // Siren shell:
    fill(0, 360, 360, 300);
    noStroke();
    ellipse(sirenX, sirenY, sirenSize, sirenSize);
  }
  
  public void drawUI(){
    // Title and buttons:
    textAlign(CENTER, CENTER);
    fill(0, 360, 360);
    textFont(titleFont);
    text("Intruder Alert", width/2+width/500, height/3+width/500);
    popMatrix();
    fill(360);
    text("Intruder Alert", width/2, height/3);
    for (Button b : buttons) b.drawButton();
  }
}

public class MenuButton extends Button {
  
  private PFont font;
  private String text;
  
  public MenuButton(PFont font, String text, float y){
    this.font = font;
    this.text = text;
    this.x = width/2;
    this.y = y;
    this.w = width/4;
    this.h = height/15;
  }
  
  public String getText(){ return text; }
  
  public void drawButton(){
    stroke(0, 360, 360);
    strokeWeight(5);
    fill(360, 50);
    if (underMouse() && mousePressed) fill(0, 360, 300, 180);
    rect(x, y, w, h, 10);
    
    textAlign(CENTER, CENTER);
    textFont(font);
    fill(360);
    if (underMouse()) fill(0, 360, 360);
    text(text, x, y-h/10, w, h);
  }
}

/** PreGame describes the tutorial screen with controls and buttons letting player choose their colour
* that change state to Game.
*/
public class PreGame extends State {
  
  /** Fields: */
  private Menu menu;
  private PFont titleFont;
  private PFont font;
  private Background background;
  private PShape ship;
  private ArrayList<Ship> ships;
  private ArrayList<PreGameButton> buttons = new ArrayList<PreGameButton>();
  
  // Screen-shaking:
  private PVector shift;
  private PVector shiftSpeed;
  private float shiftMax;
  
  // Window:
  private float windowLeft, windowRight, windowTop, windowBottom;
  
  public PreGame(Menu menu){
    this.menu = menu;
    
    // Copy art and font:
    titleFont = menu.titleFont();
    font = menu.font();
    background = menu.background();
    ship = menu.ship();
    ships = menu.ships();
    shift = menu.shift();
    shiftSpeed = menu.shiftSpeed();
    shiftMax = menu.shiftMax();
    
    addButtons();
  }
  
  public void mouseP(){}
  public void mouseR(){
    for (PreGameButton b : buttons){
      if (b.underMouse()){
        setState(new Game(b.hue()));
      }
    }
    if (windowRight <= mouseX && mouseX <= width && 0 <= mouseY && mouseY <= windowTop*2){ // back button
      setState(this.menu);
    }
  }
  public void keyP(){}
  public void keyR(){}
  
  public void updateState(){}
  
  public void drawState(){
    drawArt();
    drawTutorial();
    
    // Update screen shift:
    shiftSpeed.sub(shift.x/5.0f, shift.y/5.0f);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75f);
  }
  
  public void drawTutorial(){
    // Title at top:
    textAlign(CENTER, CENTER);
    fill(0, 360, 360);
    textFont(titleFont);
    textSize(width/15.0f);
    float shift = width/500.0f;
    text("Intruder Alert", width/2.0f+shift, windowTop/2.0f+shift, width, windowTop);
    popMatrix();
    fill(360);
    text("Intruder Alert", width/2.0f, windowTop/2.0f, width, windowTop);
    
    // Actual tutorial:
    textAlign(LEFT, TOP);
    fill(360);
    textFont(font);
    textSize(width/60);
    String text = "Your ship is under attack! Intruders flood the halls. Take them out by any means necessary.";
    text += "\n\nControls:\n - Move with WASD.\n - Shoot with mouse (left-click) or Arrow Keys.\n - Use SPACE for everything else.\nGood Luck!";
    text += "\n\nPick a player colour to start:\n(Red not recommended)";
    
    float border = width/50.0f;
    text(text, width/2, height/2, windowRight-windowLeft-border, windowBottom-windowTop-border);
    
    // Back button:
    textFont(titleFont);
    textSize(width/30.0f);
    textAlign(LEFT, BOTTOM);
    if (windowRight <= mouseX && mouseX <= width && 0 <= mouseY && mouseY <= windowTop*2) fill(0, 360, 360);
    else fill(360);
    text("Back", windowRight+(width-windowRight)/2.0f + 10*shift, windowTop - 10*shift, windowLeft, windowTop*2);
  }
  
  public void drawArt(){
    // Shift:
    pushMatrix();
    translate(shift.x, shift.y);
    
    // Space and stars:
    background.drawBackground();
    
    // Ships:
    int random = (int)random(0, 60);
    if (random == 0){
      ships.add(new Ship());
    }
    else if (random == 59){
      shift.add(random(-shiftMax, shiftMax), random(-shiftMax, shiftMax));
    }
    for (int i = 0; i < ships.size(); i++){
      Ship s = ships.get(i);
      if (s.offScreen()){
        ships.remove(s);
        i--;
      }
      else s.drawShip(ship);
    }
    
    // Walls and window:
    windowLeft = width/4.0f;
    windowRight = width-windowLeft;
    windowTop = height/6.0f;
    windowBottom = height-windowTop;
    
    // Walls:
    strokeWeight(10);
    if (frameCount % 100 < 30) stroke(0, 360, 360);
    else stroke(225, 360, 360);
    fill(0, 200);
    rect(windowLeft/2.0f, windowTop, windowLeft, windowTop*2); // top left
    rect(windowRight+(windowLeft/2.0f), windowTop, windowLeft, windowTop*2); // top right
    rect(width/2.0f, windowTop/2.0f, width/2.0f, windowTop); // top
    rect(windowLeft/2.0f, windowBottom, windowLeft, windowTop*2); // bottom left
    rect(windowRight+(windowLeft/2.0f), windowBottom, windowLeft, windowTop*2); // bottom right
    rect(width/2.0f, height-(windowTop/2.0f), width/2.0f, windowTop); // bottom
    
    // Artwork:
    float sirenX = width/8;
    float sirenY = height/6;
    float sirenSize = width/10;
    float lightSize = (sirenSize/3) * sin((TWO_PI/50)*frameCount) + (sirenSize/2);
    float lightLength = width+height;
    float lightWidth = width/3;
    
    // Centre siren light:
    fill(20, 360, 360);
    noStroke();
    ellipse(sirenX, sirenY, lightSize, lightSize);
    
    // Light beams:
    pushMatrix();
    translate(sirenX, sirenY);
    rotate((TWO_PI/200)*frameCount);
    translate(-sirenX, -sirenY);
    beginShape();
    vertex(sirenX, sirenY);
    vertex(sirenX-lightWidth/2, sirenY-lightLength);
    vertex(sirenX+lightWidth/2, sirenY-lightLength);
    vertex(sirenX, sirenY);
    vertex(sirenX+lightWidth/2, sirenY+lightLength);
    vertex(sirenX-lightWidth/2, sirenY+lightLength);
    endShape(CLOSE);
    popMatrix();
    
    // Siren shell:
    fill(0, 360, 360, 300);
    noStroke();
    ellipse(sirenX, sirenY, sirenSize, sirenSize);
    
    // Colour buttons:
    for (Button b : buttons) b.drawButton();
  }
  
  public void addButtons(){
    float left = width/3.0f;
    float right = width*2.0f/3.0f;
    int numButtons = 20;
    float bSize = (right-left)/numButtons;
    float y = height*3.0f/4.0f;
    for (int i = 0; i <= numButtons; i++){
      float x = left+i*bSize;
      float hue = i*360/(numButtons+1);
      buttons.add(new PreGameButton(x, y, bSize, hue));
    }
  }
}



public class PreGameButton extends Button {
  
  /** Fields: */
  float hue;
  
  public PreGameButton(float x, float y, float size, float hue){
    this.x = x;
    this.y = y;
    w = h = size;
    this.hue = hue;
  }
  
  public float hue(){ return hue; }
  
  public void drawButton(){
    fill(hue, 360, 360);
    if (underMouse() && mousePressed) fill(hue, 360, 200);
    rect(x, y, w, h, w/10);
  }
}

/** UI describes all the fancy displays during the game, like health-bar, minimap, equipped gun and the prompt
* that tells you to press space.
*/
public class UI {
  
  /** Fields: */
  private PShape tri; // triangle
  private PFont logFont; // text
  
  public UI(){
    // Make triangle shape:
    tri = createShape();
    tri.beginShape();
    tri.colorMode(HSB, 360);
    tri.fill(360);
    tri.strokeWeight(3);
    tri.stroke(180, 360, 360);
    float r = 10.0f;
    for (int i = 0; i < 3; i++){
      float angle = (-PI/2.0f) + i*(TWO_PI/3.0f);
      float x = r*cos(angle);
      float y = r*sin(angle);
      tri.vertex(x, y);
    }
    tri.endShape(CLOSE);
    
    logFont = createFont("Dialoginput.bold", width/90.0f);
  }
  
  /** Draws text-box at bottom of screen: */
  public void drawPrompt(String text){
    float x = width/2.0f;
    float y = height;
    float w = width*3.0f/4.0f;
    float h = height/5.0f;
    float r = width/50.0f;
    stroke(0);
    strokeWeight(5);
    fill(0, 150);
    rect(x, y, w, h, r);
    stroke(180, 360, 360);
    strokeWeight(3);
    rect(x, y, w, h, r);
    textAlign(CENTER, CENTER);
    textFont(logFont);
    textSize(width/50.0f);
    fill(360);
    text(text, x, y-h/4.0f);
  }
  
  /** Draws rectangle containing gun at bottom left: */
  public void drawGun(Gun equipped){
    // Draw rectangle:
    fill(0, 150);
    stroke(0);
    strokeWeight(5);
    float x = 0; 
    float y = height;
    float w = width/4.0f;
    float h = width/4.0f;
    float r = width/50.0f;
    rect(x, y, w, h, r);
    noFill();
    stroke(180, 360, 360);
    strokeWeight(3);
    rect(x, y, w, h, r);
    
    // Draw text:
    x += w/4.0f;
    y -= h/4.0f;
    textAlign(CENTER, CENTER);
    textFont(logFont);
    textSize(width/50.0f);
    float shift = w/300.0f;
    fill(180, 360, 360);
    text(equipped.name(), x+shift, y+shift, w/2.0f, w/2.0f);
    fill(360);
    text(equipped.name(), x, y, w/2.0f, w/2.0f);
    
    
    textAlign(CENTER, TOP);
    text("Equipped:", x, y+h/50.0f, w/2.0f, h/2.0f);
    
    // Divider:
    noStroke();
    fill(360);
    rect(w/4.0f, y-h/10.0f, w*5.0f/12.0f, h/100.0f);
  }
  
  /** Draws health bar made of equilateral triangles: */
  public void drawHealthBar(float health, float maxHealth){
    int triNum = 10;
    float healthPerTri = maxHealth/triNum;
    float triLength = width/40.0f;
    float gap = triLength/5.0f;
    float left = width/20.0f;
    float top = left;
    
    shapeMode(CORNER);
    for (int i = 0; i < triNum; i++){
      float x = left + (triLength+gap)*i;
      float y = (i % 2 == 0)? top-(2.5f*gap) : top;
      y += gap*sin((TWO_PI/(70.0f*(health/maxHealth) + 30))*(frameCount+10*i));
      float healthInTri = constrain(health-(healthPerTri*i), 0, healthPerTri);
      
      // Black triangle behind:
      tri.setFill(color(0, 180));
      tri.setStroke(color(0));
      
      pushMatrix();
      translate(x, y);
      if (i % 2 == 0) rotate(PI);
      scale(triLength/12.0f);
      translate(-x, -y);
      shape(tri, x, y);
      popMatrix();
      
      // Coloured triangle in front:
      //tri.setFill(color(180*(health/maxHealth), 360, 360 * (healthInTri/healthPerTri)));
      tri.setFill(color(360));
      if (health == maxHealth) tri.setStroke(color(180, 360, 360));
      else tri.setStroke(color(100*(health/maxHealth), 360, 360));
      
      pushMatrix();
      translate(x, y);
      if (i % 2 == 0) rotate(PI);
      scale((healthInTri/healthPerTri)*(triLength/12.0f));
      translate(-x, -y);
      shape(tri, x, y);
      popMatrix();
    }
  }
  
  public void drawMinimap(Level level, PVector pos, boolean gameOver){
    Tile[][] tiles = level.tiles();
    ArrayList<Enemy> enemies = level.enemies();
    
    // Draw minimap:
    float left = width-(width*9.0f/40.0f);
    float right = width-(width/100.0f);
    float top = width/100.0f;
    float w = right-left;
    float tileSize = w/tiles[0].length;
    float h = tileSize*tiles.length;
    
    // Transparent gray rectangle behind map:
    float mapX = left+(w/2.0f);
    float mapY = top+(h/2.0f);
    float extension = width/100.0f;
    float fontSize = width/50.0f;
    stroke(0);
    strokeWeight(5);
    fill(0, 150);
    rect(mapX, mapY+fontSize, w+extension, h+extension+(2*fontSize), 30);
    
    stroke(180, 360, 360);
    strokeWeight(3);
    noFill();
    rect(mapX, mapY+fontSize, w+extension, h+extension+(2*fontSize), 30);
    
    // Text (level num and enemies left):
    String text = "----Level "+(level.num()+1)+"----\nEnemies remaining: "+enemies.size();
    textAlign(CENTER, CENTER);
    fill(360);
    textFont(logFont);
    textSize(width/90.0f);
    text(text, mapX, top+h+fontSize-(extension/2.0f));
    
    // White minimap tiles:
    stroke(350);
    strokeWeight(1);
    fill(350);
    for (int row = 0; row < tiles.length; row++){
      for (int col = 0; col < tiles[row].length; col++){
        int value = tiles[row][col].type();
        if (value != 1) continue;
        float x = left + (tileSize*col) + (tileSize/2.0f);
        float y = top + (tileSize*row) + (tileSize/2.0f);
        rect(x, y, tileSize, tileSize);
      }
    }
    
    // Draw small red squares for enemies:
    for (Enemy e : enemies){
      float eCol = e.pos.x/Game.TILE_SIZE;
      float eRow = e.pos.y/Game.TILE_SIZE;
      float eX = eCol*tileSize + left;
      float eY = eRow*tileSize + top;
      noStroke();
      fill(0, 360, 360);
      rect(eX, eY, tileSize/1.5f, tileSize/1.5f, tileSize/4.0f);
    }
    
    // Draw blue square for player:
    float pCol = pos.x/Game.TILE_SIZE;
    float pRow = pos.y/Game.TILE_SIZE;
    float pX = pCol*tileSize + left;
    float pY = pRow*tileSize + top;
    noStroke();
    if (gameOver) fill(0); // black if dead
    else fill(200, 360, 360);
    rect(pX, pY, tileSize*1.5f, tileSize*1.5f, tileSize/2.0f);
  }
}
  public void settings() {  size(1500, 900); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Intruder_Alert" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
