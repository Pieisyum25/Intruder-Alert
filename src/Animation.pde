
/** Flying text is the text that flies sideways across the screen:
*/
public class FlyingText {
  
  /** Fields: */
  private PVector pos;
  private PFont font = createFont("Rubik-Bold", height/5.0);
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
    float shift = width/500.0;
    text(text, pos.x+shift, pos.y+shift);
    fill(360);
    text(text, pos.x, pos.y);
    
    float speed = 50.0;
    if (pos.x > width/3.0 && pos.x < width*2.0/3.0) speed = 10.0;
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
  private float alpha = 360.00;
  
  public Splatter(float x, float y){
    int blobsNum = 20;
    int maxSpeed = 2;
    int blobSize = 25;
    for (float i = 0.00; i < blobsNum; i++) blobs.add(new Blob(new PVector(x, y), maxSpeed*((blobsNum-i)/blobsNum), blobSize*(i/blobsNum), color(0, 360, 144)));
  }
  
  public boolean finished(){ return (frameCount > startFrame + lifeSpan); }
  
  public void drawSplatter(){
    long remainingLifeSpan = lifeSpan - (frameCount - startFrame);
    if (remainingLifeSpan <= lifeSpan/5) alpha = 360.00*(remainingLifeSpan/(lifeSpan/5.00));
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
  private color colour;
  
  public Blob(PVector pos, float speed, float size, color colour){
    this.pos = pos;
    float angle = random(0, TWO_PI);
    this.speed = new PVector(speed*cos(angle), speed*sin(angle));
    this.size = size;
    this.colour = colour;
  }
  
  public void setColour(color colour){ this.colour = colour; }
  public void setAlpha(float alpha){ this.colour = color(hue(colour), saturation(colour), brightness(colour), alpha); }
  
  public void drawBlob(){
    // Movement and friction:
    speed.mult(24.0/25.0);
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
        x0 = -size/2.0 + 1;
        direction = random(-PI/2, PI/2);
      }
      else { // right
        x0 = width+size/2.0 - 1;
        direction = random(PI/2, PI*1.5);
      }
    }
    else { // top and bottom
      x0 = random(0, width);
      if (side == 1){ // top
        y0 = -size/2.0 + 1;
        direction = random(0, PI);
      }
      else { // bottom
        y0 = height+size/2.0 - 1;
        direction = random(PI, TWO_PI);
      }
    }
    pos = new PVector(x0, y0);
    float vel = size/5.0;
    speed = new PVector(vel*cos(direction), vel*sin(direction));
  }
  
  public boolean offScreen(){
    float r = size/2.0;
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
    rotate(direction+PI/2.0);
    translate(-pos.x, -pos.y);
    shape(s, pos.x, pos.y, size, size);
    popMatrix();
    pos.add(speed);
  }
}
