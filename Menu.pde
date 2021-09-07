
/** Menu describes the title screen with the space background, art, and menu buttons:
*/
public class Menu extends State {
  
  /** Fields: */
  private PFont titleFont;
  private PFont buttonFont;
  private ArrayList<MenuButton> buttons = new ArrayList<MenuButton>();
  private Background background = new Background((int)(width/25.0), 50);
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
    float wingWidth = 3.5;
    
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
    shiftSpeed.sub(shift.x/5.0, shift.y/5.0);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75);
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
    float windowLeft = width/4.0;
    float windowRight = width-windowLeft;
    float windowTop = height/6.0;
    float windowBottom = height-windowTop;
    
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
