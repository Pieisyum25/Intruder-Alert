
/** UI describes all the fancy displays during the game, like health-bar, minimap, equipped gun and the prompt
* that tells you to press space.
*/
public class UI {
  
  /** Fields: */
  private PShape tri; // triangle
  private PFont logFont; // text
  
  public UI(){
    // Make triangle shape:
    tri = createShape();
    tri.beginShape();
    tri.colorMode(HSB, 360);
    tri.fill(360);
    tri.strokeWeight(3);
    tri.stroke(180, 360, 360);
    float r = 10.0;
    for (int i = 0; i < 3; i++){
      float angle = (-PI/2.0) + i*(TWO_PI/3.0);
      float x = r*cos(angle);
      float y = r*sin(angle);
      tri.vertex(x, y);
    }
    tri.endShape(CLOSE);
    
    logFont = createFont("Dialoginput.bold", width/90.0);
  }
  
  /** Draws text-box at bottom of screen: */
  public void drawPrompt(String text){
    float x = width/2.0;
    float y = height;
    float w = width*3.0/4.0;
    float h = height/5.0;
    float r = width/50.0;
    stroke(0);
    strokeWeight(5);
    fill(0, 150);
    rect(x, y, w, h, r);
    stroke(180, 360, 360);
    strokeWeight(3);
    rect(x, y, w, h, r);
    textAlign(CENTER, CENTER);
    textFont(logFont);
    textSize(width/50.0);
    fill(360);
    text(text, x, y-h/4.0);
  }
  
  /** Draws rectangle containing gun at bottom left: */
  public void drawGun(Gun equipped){
    // Draw rectangle:
    fill(0, 150);
    stroke(0);
    strokeWeight(5);
    float x = 0; 
    float y = height;
    float w = width/4.0;
    float h = width/4.0;
    float r = width/50.0;
    rect(x, y, w, h, r);
    noFill();
    stroke(180, 360, 360);
    strokeWeight(3);
    rect(x, y, w, h, r);
    
    // Draw text:
    x += w/4.0;
    y -= h/4.0;
    textAlign(CENTER, CENTER);
    textFont(logFont);
    textSize(width/50.0);
    float shift = w/300.0;
    fill(180, 360, 360);
    text(equipped.name(), x+shift, y+shift, w/2.0, w/2.0);
    fill(360);
    text(equipped.name(), x, y, w/2.0, w/2.0);
    
    
    textAlign(CENTER, TOP);
    text("Equipped:", x, y+h/50.0, w/2.0, h/2.0);
    
    // Divider:
    noStroke();
    fill(360);
    rect(w/4.0, y-h/10.0, w*5.0/12.0, h/100.0);
  }
  
  /** Draws health bar made of equilateral triangles: */
  public void drawHealthBar(float health, float maxHealth){
    int triNum = 10;
    float healthPerTri = maxHealth/triNum;
    float triLength = width/40.0;
    float gap = triLength/5.0;
    float left = width/20.0;
    float top = left;
    
    shapeMode(CORNER);
    for (int i = 0; i < triNum; i++){
      float x = left + (triLength+gap)*i;
      float y = (i % 2 == 0)? top-(2.5*gap) : top;
      y += gap*sin((TWO_PI/(70.0*(health/maxHealth) + 30))*(frameCount+10*i));
      float healthInTri = constrain(health-(healthPerTri*i), 0, healthPerTri);
      
      // Black triangle behind:
      tri.setFill(color(0, 180));
      tri.setStroke(color(0));
      
      pushMatrix();
      translate(x, y);
      if (i % 2 == 0) rotate(PI);
      scale(triLength/12.0);
      translate(-x, -y);
      shape(tri, x, y);
      popMatrix();
      
      // Coloured triangle in front:
      //tri.setFill(color(180*(health/maxHealth), 360, 360 * (healthInTri/healthPerTri)));
      tri.setFill(color(360));
      if (health == maxHealth) tri.setStroke(color(180, 360, 360));
      else tri.setStroke(color(100*(health/maxHealth), 360, 360));
      
      pushMatrix();
      translate(x, y);
      if (i % 2 == 0) rotate(PI);
      scale((healthInTri/healthPerTri)*(triLength/12.0));
      translate(-x, -y);
      shape(tri, x, y);
      popMatrix();
    }
  }
  
  public void drawMinimap(Level level, PVector pos, boolean gameOver){
    Tile[][] tiles = level.tiles();
    ArrayList<Enemy> enemies = level.enemies();
    
    // Draw minimap:
    float left = width-(width*9.0/40.0);
    float right = width-(width/100.0);
    float top = width/100.0;
    float w = right-left;
    float tileSize = w/tiles[0].length;
    float h = tileSize*tiles.length;
    
    // Transparent gray rectangle behind map:
    float mapX = left+(w/2.0);
    float mapY = top+(h/2.0);
    float extension = width/100.0;
    float fontSize = width/50.0;
    stroke(0);
    strokeWeight(5);
    fill(0, 150);
    rect(mapX, mapY+fontSize, w+extension, h+extension+(2*fontSize), 30);
    
    stroke(180, 360, 360);
    strokeWeight(3);
    noFill();
    rect(mapX, mapY+fontSize, w+extension, h+extension+(2*fontSize), 30);
    
    // Text (level num and enemies left):
    String text = "----Level "+(level.num()+1)+"----\nEnemies remaining: "+enemies.size();
    textAlign(CENTER, CENTER);
    fill(360);
    textFont(logFont);
    textSize(width/90.0);
    text(text, mapX, top+h+fontSize-(extension/2.0));
    
    // White minimap tiles:
    stroke(350);
    strokeWeight(1);
    fill(350);
    for (int row = 0; row < tiles.length; row++){
      for (int col = 0; col < tiles[row].length; col++){
        int value = tiles[row][col].type();
        if (value != 1) continue;
        float x = left + (tileSize*col) + (tileSize/2.0);
        float y = top + (tileSize*row) + (tileSize/2.0);
        rect(x, y, tileSize, tileSize);
      }
    }
    
    // Draw small red squares for enemies:
    for (Enemy e : enemies){
      float eCol = e.pos.x/Game.TILE_SIZE;
      float eRow = e.pos.y/Game.TILE_SIZE;
      float eX = eCol*tileSize + left;
      float eY = eRow*tileSize + top;
      noStroke();
      fill(0, 360, 360);
      rect(eX, eY, tileSize/1.5, tileSize/1.5, tileSize/4.0);
    }
    
    // Draw blue square for player:
    float pCol = pos.x/Game.TILE_SIZE;
    float pRow = pos.y/Game.TILE_SIZE;
    float pX = pCol*tileSize + left;
    float pY = pRow*tileSize + top;
    noStroke();
    if (gameOver) fill(0); // black if dead
    else fill(200, 360, 360);
    rect(pX, pY, tileSize*1.5f, tileSize*1.5f, tileSize/2.0);
  }
}
