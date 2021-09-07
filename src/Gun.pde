
/** 
 * Gun describes a gun object with specified characteristics.
 * Guns shoot bullet objects (below):
 * Weapons are equipped by players and enemies:
 */
public abstract class Gun {
  
  /** Constants: */
  protected final float TPS = Game.TILE_SIZE/60.0; // tiles per second
  
  /** Fields: */
  protected ArrayList<Bullet> bullets;
  protected String name;
  protected float damage, speed, inaccuracy, size, force, durability, crateDamage;
  protected float cooldown, lifespan;
  protected long lastShotFrame;
  protected boolean bounces;
  
  protected float fpt(){ // frames per tile
    if (speed == 0) return 0;
    return (60.0/speed);
  }
  
  public String name(){ return name; }
  public ArrayList<Bullet> bullets(){ return bullets; }
  abstract void shoot(boolean player, PVector pos, float direction, float hue);
  public boolean cooldownOver(){ return (lastShotFrame + cooldown < frameCount); }
}

public class Pistol extends Gun {
  
  public Pistol(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Pistol";
    //bounces = true;
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/120.0;
    size = 15;
    force = 3.0;
    this.cooldown = cooldown;
    lifespan = (int)(6.0*fpt());
    durability = 1;
    crateDamage = 7;
  }
  
  public Pistol(ArrayList<Bullet> bullets){
    name = "Pistol";
    this.bullets = bullets;
    damage = 10;
    speed = 2;
    inaccuracy = TWO_PI/120.0;
    size = 15;
    force = 3.0;
    cooldown = 25;
    lifespan = (int)(6.0*fpt());
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
  private float angle = PI/18.0; // angle between pellets
  
  public Shotgun(ArrayList<Bullet> bullets, float damage, float speed, int cooldown){
    name = "Shotgun";
    this.bullets = bullets;
    this.damage = damage;
    this.speed = speed;
    inaccuracy = TWO_PI/120.0;
    size = 15;
    force = 5.0;
    this.cooldown = cooldown;
    lifespan = (int)(4.0*fpt());
    durability = 3;
    crateDamage = 20;
  }
  
  public Shotgun(ArrayList<Bullet> bullets){
    name = "Shotgun";
    this.bullets = bullets;
    damage = 5;
    speed = 3;
    inaccuracy = TWO_PI/120.0;
    size = 15;
    force = 5.0;
    cooldown = 100;
    lifespan = (int)(4.0*fpt());
    durability = 3;
    crateDamage = 20;
  }
  
  public void shoot(boolean player, PVector pos, float direction, float hue){
    if (!cooldownOver()) return; // if cooldown not over
    lastShotFrame = frameCount;
    
    direction += random(-inaccuracy, inaccuracy);
    direction -= ((streams-1)/2.0)*angle;
    
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
    inaccuracy = TWO_PI/100.0;
    size = 10;
    force = 3.0;
    this.cooldown = cooldown;
    lifespan = (int)(9.0*fpt());
    durability = 1;
    crateDamage = 5;
  }
  
  public AssaultRifle(ArrayList<Bullet> bullets){
    name = "Assault Rifle";
    this.bullets = bullets;
    damage = 3;
    speed = 5;
    inaccuracy = TWO_PI/100.0;
    size = 10;
    force = 3.0;
    cooldown = 8;
    lifespan = (int)(9.0*fpt());
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
    force = 10.0;
    this.cooldown = cooldown;
    lifespan = (int)(15.0*fpt());
    durability = 10;
    crateDamage = 40;
  }
  
  public SniperRifle(ArrayList<Bullet> bullets){
    name = "Sniper Rifle";
    this.bullets = bullets;
    damage = 25;
    speed = 15;
    inaccuracy = TWO_PI/120.0;
    size = 10;
    force = 10.0;
    cooldown = 100;
    lifespan = (int)(15.0*fpt());
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
    inaccuracy = TWO_PI/60.0;
    size = 10;
    force = 3.0;
    this.cooldown = cooldown;
    lifespan = (int)(15.0*fpt());
    durability = 3;
    crateDamage = 4;
  }
  
  public Bouncer(ArrayList<Bullet> bullets){
    name = "Bouncer";
    bounces = true;
    this.bullets = bullets;
    damage = 2;
    speed = 5;
    inaccuracy = TWO_PI/60.0;
    size = 10;
    force = 3.0;
    cooldown = 6;
    lifespan = (int)(15.0*fpt());
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
    inaccuracy = TWO_PI/10.0;
    size = 15;
    force = 1.0;
    this.cooldown = cooldown;
    lifespan = (int)(3.0*fpt());
    durability = 1;
    crateDamage = 2;
  }
  
  public FlameThrower(ArrayList<Bullet> bullets){
    name = "Flame Thrower";
    this.bullets = bullets;
    damage = 1;
    speed = 7;
    inaccuracy = TWO_PI/10.0;
    size = 15;
    force = 1.0;
    cooldown = 0.5;
    lifespan = (int)(2.0*fpt());
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
  public float left(){ return pos.x-(size/2.0); }
  public float top(){ return pos.y-(size/2.0); }
  public float right(){ return pos.x+(size/2.0); }
  public float bottom(){ return pos.y+(size/2.0); }
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
          float r = size/2.0;
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
          float r = size/2.0;
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
    ellipse(pos.x, pos.y, size/2.0, size/2.0);
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
      else trail[i].set(PVector.lerp(trail[i+1], trail[i+2], 0.5));
    }
    float size = this.size*(lastIndex-1)/trail.length;
    ellipse(trail[lastIndex-1].x, trail[lastIndex-1].y, size, size);
    trail[lastIndex-1].set(PVector.lerp(pos, trail[lastIndex], 0.5));
    
    size = this.size*lastIndex/trail.length;
    ellipse(trail[lastIndex].x, trail[lastIndex].y, size, size);
    trail[lastIndex].set(pos.copy());
  }
}
