
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
  public float left(){ return pos.x-w/2.0; }
  public float right(){ return pos.x+w/2.0; }
  public float top(){ return pos.y-h/2.0; }
  public float bottom(){ return pos.y+h/2.0; }
  
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
            pos.set(w.right()+(this.w/2.0), pos.y);
            speed.set(0, speed.y);
          }
          else if (right()+speed.x >= w.left() && right()-speed.x <= w.left()){ // player hit left of wall
            w.glow();
            pos.set(w.left()-(this.w/2.0), pos.y);
            speed.set(0, speed.y);
          }
        }
        else if (collidingHorizontally){
          if (top()+speed.y <= w.bottom() && top()-speed.y >= w.bottom()){ // player hit bottom of wall
            w.glow();
            pos.set(pos.x, w.bottom()+(this.h/2.0));
            speed.set(speed.x, 0);
            //println("Afters speedX: "+speed.x+" speedY: "+speed.y);
          }
          else if (bottom()+speed.y >= w.top() && bottom()-speed.y <= w.top()){ // player hit top of wall
            w.glow();
            pos.set(pos.x, w.top()-(this.h/2.0));
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
            pos.set(c.right()+(this.w/2.0), pos.y);
            speed.set(0, speed.y);
          }
          else if (right()+speed.x >= c.left() && right()-speed.x <= c.left()){ // player hit left of wall
            pos.set(c.left()-(this.w/2.0), pos.y);
            speed.set(0, speed.y);
          }
        }
        else if (collidingHorizontally){
          if (top()+speed.y <= c.bottom() && top()-speed.y >= c.bottom()){ // player hit bottom of wall
            pos.set(pos.x, c.bottom()+(this.h/2.0));
            speed.set(speed.x, 0);
          }
          else if (bottom()+speed.y >= c.top() && bottom()-speed.y <= c.top()){ // player hit top of wall
            pos.set(pos.x, c.top()-(this.h/2.0));
            speed.set(speed.x, 0);
          }
        }
      }
    }
    
    // Friction:
    float speedX = 0.85 * speed.x;
    float speedY = 0.85 * speed.y;
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
    
    float r = range/2.0;
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
