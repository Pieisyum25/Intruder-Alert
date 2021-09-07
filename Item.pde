
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
    speed.mult(0.9);
    if (speed.x < 0.1) speed.set(0, speed.y);
    if (speed.y < 0.1) speed.set(speed.x, 0);
  }
  abstract void drawItem();
}

public class HealthOrb extends Item {
  
  /** Fields: */
  private float health;
  
  public HealthOrb(PVector pos, PVector speed, float health){
    float tileSize = Game.TILE_SIZE;
    this.pos = pos;
    this.speed = speed;
    range = tileSize*3;
    size = tileSize/5.0 + health*(tileSize/25.0);
    hue = 100;
    this.health = health;
    follows = true;
  }
  
  public float health(){ return health; }
  
  public void drawItem(){
    noStroke();
    fill(hue, 360, 360);
    if (health > 2) rect(pos.x, pos.y, size, size, size/4.0);
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
    size = tileSize/4.0;
    hue = 35;
    follows = false;
    
    String gunName = gun.name();
    switch(gunName){
      case "Pistol": this.gun = new Pistol(gun.bullets(), 10, 10, 15); break;
      case "Shotgun": this.gun = new Shotgun(gun.bullets(), 10, 10, 60); break;
      case "Assault Rifle": this.gun = new AssaultRifle(gun.bullets(), 5, 12, 7); break;
      case "Sniper Rifle": this.gun = new SniperRifle(gun.bullets(), 50, 15, 100); break;
      case "Bouncer": this.gun = new Bouncer(gun.bullets(), 3, 12, 5); break;
      case "Flame Thrower": this.gun = new FlameThrower(gun.bullets(), 1.5, 7, 0.5); break;
    }
  }
  
  public Gun gun(){ return gun; }
  
  public void drawItem(){
    noStroke();
    fill(hue, 360, 360);
    rect(pos.x, pos.y, size*1.5, size);
  }
}
