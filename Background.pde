
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
    double size = 10.00;
    s.beginShape();
    s.noStroke();
    s.colorMode(HSB, 360);
    s.fill(360);
    for (int i = 0; i < 8; i++){
      float angle = i * PI/4;
      double radius = size/2.00;
      if (i % 2 == 0) radius /= 5.00;
      float x = (float)radius*cos(angle);
      float y = (float)radius*sin(angle);
      s.vertex(x, y);
    }
    s.endShape(CLOSE);
    
    // Create stars:
    for (int i = 0; i < starNum; i++){
      stars.add(new Star(random(0, width), random(0, height), random(2, 8), random(0, TWO_PI), random(0, 0.5)));
    }
  }
  
  public void drawBackground(){
    // Space background using perlin noise:
    int r = pixelSize/2;
    float scale = 0.001;
    for (int x = r; x <= width+r; x += pixelSize){
      for (int y = r; y <= height+r; y += pixelSize){
        float value = noise(x*scale, y*scale, frameCount*0.005);
        fill(250+40*value, 360, 200*constrain(0.75-value, 0, 1));
        stroke(250+40*value, 360, 200*constrain(0.75-value, 0, 1));
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
    this.spin = random(-PI/240.0, PI/240.0);
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
    float r = size/2.0;
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
