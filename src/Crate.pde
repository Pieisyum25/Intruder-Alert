
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
  public float left(){ return pos.x-size/2.0; }
  public float right(){ return pos.x+size/2.0; }
  public float top(){ return pos.y-size/2.0; }
  public float bottom(){ return pos.y+size/2.0; }
  
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
    float innerSize = size*3.0/4.0;
    rect(pos.x, pos.y, innerSize, innerSize, innerSize/5.0);
    if (explosive){
      fill(0, 360, 360);
      rect(pos.x, pos.y-(size*3.0/16.0), size/4.0, size/2.0);
      rect(pos.x, pos.y+(size/4.0), size/4.0, size/4.0);
    }
  }
}
