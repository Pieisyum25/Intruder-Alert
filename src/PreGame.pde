
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
    shiftSpeed.sub(shift.x/5.0, shift.y/5.0);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75);
  }
  
  public void drawTutorial(){
    // Title at top:
    textAlign(CENTER, CENTER);
    fill(0, 360, 360);
    textFont(titleFont);
    textSize(width/15.0);
    float shift = width/500.0;
    text("Intruder Alert", width/2.0+shift, windowTop/2.0+shift, width, windowTop);
    popMatrix();
    fill(360);
    text("Intruder Alert", width/2.0, windowTop/2.0, width, windowTop);
    
    // Actual tutorial:
    textAlign(LEFT, TOP);
    fill(360);
    textFont(font);
    textSize(width/60);
    String text = "Your ship is under attack! Intruders flood the halls. Take them out by any means necessary.";
    text += "\n\nControls:\n - Move with WASD.\n - Shoot with mouse (left-click) or Arrow Keys.\n - Use SPACE for everything else.\nGood Luck!";
    text += "\n\nPick a player colour to start:\n(Red not recommended)";
    
    float border = width/50.0;
    text(text, width/2, height/2, windowRight-windowLeft-border, windowBottom-windowTop-border);
    
    // Back button:
    textFont(titleFont);
    textSize(width/30.0);
    textAlign(LEFT, BOTTOM);
    if (windowRight <= mouseX && mouseX <= width && 0 <= mouseY && mouseY <= windowTop*2) fill(0, 360, 360);
    else fill(360);
    text("Back", windowRight+(width-windowRight)/2.0 + 10*shift, windowTop - 10*shift, windowLeft, windowTop*2);
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
    windowLeft = width/4.0;
    windowRight = width-windowLeft;
    windowTop = height/6.0;
    windowBottom = height-windowTop;
    
    // Walls:
    strokeWeight(10);
    if (frameCount % 100 < 30) stroke(0, 360, 360);
    else stroke(225, 360, 360);
    fill(0, 200);
    rect(windowLeft/2.0, windowTop, windowLeft, windowTop*2); // top left
    rect(windowRight+(windowLeft/2.0), windowTop, windowLeft, windowTop*2); // top right
    rect(width/2.0, windowTop/2.0, width/2.0, windowTop); // top
    rect(windowLeft/2.0, windowBottom, windowLeft, windowTop*2); // bottom left
    rect(windowRight+(windowLeft/2.0), windowBottom, windowLeft, windowTop*2); // bottom right
    rect(width/2.0, height-(windowTop/2.0), width/2.0, windowTop); // bottom
    
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
    float left = width/3.0;
    float right = width*2.0/3.0;
    int numButtons = 20;
    float bSize = (right-left)/numButtons;
    float y = height*3.0/4.0;
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
