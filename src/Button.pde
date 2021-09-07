
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
  
  abstract void drawButton();
}
