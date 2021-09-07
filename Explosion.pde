
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
  private float alpha = 360.0;
  
  public Explosion(PVector pos, float size, float damage, color inner, color outer, Player player, ArrayList<Enemy> enemies, ArrayList<Crate> crates){
    this.pos = pos;
    this.size = size;
    this.damage = damage;
    int blobsNum = (int)(size/3.0);
    float maxSpeed = size/40.0;
    float blobSize = size/5.0;
    for (float i = 0.00; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0))));
    
    // Apply damage:
    damage(player, enemies, crates);
  }
  
  /** Decorative explosions: */
  public Explosion(PVector pos, float size, color inner, color outer){
    this.pos = pos;
    this.size = size;
    int blobsNum = (int)(size/3.0);
    float maxSpeed = size/40.0;
    float blobSize = size/5.0;
    for (float i = 0.00; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0))));
  }
  
  /** Decorative with particle number control: */
  public Explosion(PVector pos, float size, color inner, color outer, int blobsNum){
    this.pos = pos;
    this.size = size;
    float maxSpeed = size/40.0;
    float blobSize = size/5.0;
    for (float i = 0.00; i < blobsNum; i++) blobs.add(new Blob(pos.copy(), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), lerpColor(outer, inner, i/(blobsNum-1.0))));
  }
  
  public boolean finished(){ return (frameCount > startFrame + lifeSpan); }
  
  public void damage(Player player, ArrayList<Enemy> enemies, ArrayList<Crate> crates){
    if (damage <= 0) return;
    
    // Make arraylist of all characters:
    ArrayList<Character> characters = new ArrayList<Character>(enemies);
    characters.add(player);
    
    float r = size/2.0;
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
          float force = (damage * dist/r)/2.0 + 5.0;
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
    if (remainingLifeSpan <= lifeSpan/5) alpha = 360.00*(remainingLifeSpan/(lifeSpan/5.00));
    for (int i = 0; i < blobs.size(); i++){
      Blob b = blobs.get(i);
      b.setAlpha(alpha);
      b.drawBlob();
    }
  }
}
