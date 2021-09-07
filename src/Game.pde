
/** 
 * Game describes the game-state, containing levels, the player, a UI, and all the level contents:
 */
public class Game extends State {
  
  /** Constants: */
  public static final float TILE_SIZE = 50; // 10 to see map
  
  /** Fields: */
  private boolean gameOver; // true when player dies
  private boolean levelCompleted; // true when all enemies are killed
  private boolean gameFinished; // true when finished all levels
  private long levelStart; // frame that started level
  private int gracePeriod = 60; // 1 second upon spawning
  
  // Camera/Controls: 
  private PVector cameraPos = new PVector(width/2, height/2);
  private float zoom;
  private HashMap<String, Boolean> controls = new HashMap<String, Boolean>();
  private PVector shift = new PVector(0, 0);
  private PVector shiftSpeed = new PVector(0, 0);
  private float shiftMax = 15;
   
  // Background:
  private Background background;
  
  // Player:
  private Player player;
  
  // Enemies:
  private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
  
  // Splatters / Animations / Explosions:
  private ArrayList<Splatter> splatters = new ArrayList<Splatter>();
  private ArrayList<Explosion> explosions = new ArrayList<Explosion>();
  private ArrayList<Explosion> stationaryExplosions = new ArrayList<Explosion>();
  private ArrayList<FlyingText> flyingText = new ArrayList<FlyingText>();
  
  // Bullets:
  private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
  
  // Items:
  private ArrayList<Item> items = new ArrayList<Item>();
  private GunItem closestGun = null;
  private float closestDist;
  
  // Levels:
  private ArrayList<Level> levels = new ArrayList<Level>();
  private Level level;
  private int numOfLevels = 10;
  
  // UI:
  private UI ui = new UI();
  
  // Endscreen:
  private PFont endFont;
  private float alpha = 0;
  
  public Game(float hue){
    // Set up controls:
    initialiseControls();
    
    // Create background:
    background = new Background((int)(width/3.0), 25);
    
    // Create player:
    float playerSize = TILE_SIZE/2.0;
    float playerMaxSpeed = 8;
    player = new Player(playerSize, playerMaxSpeed, new Pistol(bullets, 10, 10, 15), hue);
    
    
    // Create levels:
    for (int i = 0; i < numOfLevels; i++) levels.add(new Level(i, bullets));
    setLevel(0);
    
    // Create endscreen font:
    endFont = createFont("Rubik-Bold", width/10);
  }
  
  public void initialiseControls(){
    controls.put("l", false);
    controls.put("r", false);
    controls.put("u", false);
    controls.put("d", false);
    
    controls.put("0", false);
    controls.put("1", false);
    controls.put("2", false);
    controls.put("3", false);
    
    controls.put("ml", false);
    controls.put("mr", false);
  }
  
  public void setLevel(int i){
    if (level != null) level.setPlayerPos(player.x(), player.y());
    if (i < 0 || i >= levels.size()) return;
    level = levels.get(i);
    levelCompleted = false;
    levelStart = frameCount;
    player.setPos(level.playerX(), level.playerY());
    player.setSpeed(0, 0); // start level stationary
    enemies = level.enemies();
    bullets.clear();
    splatters.clear();
    explosions.clear();
    stationaryExplosions.clear();
    items.clear();
    zoom = 0.5f; // zoom out
    flyingText.clear();
    flyingText.add(new FlyingText("Level", 0, 0));
    flyingText.add(new FlyingText(""+(level.num()+1), 1, 0));
  }
  
  public void mouseP(){
    if (mouseButton == LEFT) controls.put("ml", true);
    else if (mouseButton == RIGHT) controls.put("mr", true);
  }
  public void mouseR(){
    if (mouseButton == LEFT) controls.put("ml", false);
    else if (mouseButton == RIGHT) controls.put("mr", false);
  }
  public void keyP(){
    if (key == 'w' || key == 'W') controls.put("u", true);
    else if (key == 'a' || key == 'A') controls.put("l", true);
    else if (key == 's' || key == 'S') controls.put("d", true);
    else if (key == 'd' || key == 'D') controls.put("r", true);
    else if (keyCode == RIGHT) controls.put("0", true);
    else if (keyCode == DOWN) controls.put("1", true);
    else if (keyCode == LEFT) controls.put("2", true);
    else if (keyCode == UP) controls.put("3", true);
    
    else if (key == 'o' || key == 'O') setLevel(level.num()-1);
    else if (key == 'p' || key == 'P') setLevel(level.num()+1);
    
    else if (key == ' '){
      if (gameOver || gameFinished) setState(new Menu());
      else if (closestGun != null){
        // Drops gun:
        float speed = 3;
        float direction = random(0, TWO_PI);
        items.add(new GunItem(player.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), player.equippedGun()));
        // Equips gun:
        player.equipGun(closestGun.gun());
        items.remove(closestGun);
        closestGun = null;
      }
      else if (levelCompleted) setLevel(level.num()+1);
    }
  }
  public void keyR(){
    if (key == 'w' || key == 'W') controls.put("u", false);
    else if (key == 'a' || key == 'A') controls.put("l", false);
    else if (key == 's' || key == 'S') controls.put("d", false);
    else if (key == 'd' || key == 'D') controls.put("r", false);
    else if (keyCode == RIGHT) controls.put("0", false);
    else if (keyCode == DOWN) controls.put("1", false);
    else if (keyCode == LEFT) controls.put("2", false);
    else if (keyCode == UP) controls.put("3", false);
  }
  
  public void updateState(){
    // Player:
    float direction = atan2(mouseY-height/2.0, mouseX-width/2.0);
    if (!gameOver){
      player.controlPlayer(controls, direction);
      player.updateCharacter(level.wallMap(), level.crateMap());
      if (player.dead()){
        shift.add(2*random(-shiftMax, shiftMax), 2*random(-shiftMax, shiftMax));
        splatters.add(new Splatter(player.x(), player.y()));
        gameOver = true;
      }
    }
    
    // Enemies:
    for (int i = 0; i < enemies.size(); i++){
      Enemy e = enemies.get(i);
      if (e.dead()){
        enemies.remove(e);
        i--;
        splatters.add(new Splatter(e.x(), e.y()));
        shift.add(random(-shiftMax, shiftMax), random(-shiftMax, shiftMax));
        int healthOrbs = (int)random(0, 4);
        float speed = 10;
        for (int h = 0; h < healthOrbs; h++){
          direction = random(0, TWO_PI);
          items.add(new HealthOrb(e.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), (int)random(1, 4)));
        }
        speed = 5;
        direction = random(0, TWO_PI);
        items.add(new GunItem(e.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), e.equippedGun()));
      }
      // Enemies shooting:
      else {
        PVector ePos = e.pos();
        PVector pPos = player.pos();
        if (!gameOver && (frameCount > levelStart+gracePeriod) && e.cooldownOver() && e.inLineOfSight(pPos, level.walls(), level.crates())){
          direction = atan2(pPos.y - ePos.y, pPos.x - ePos.x);
          e.shoot(direction);
        }
        // Update:
        e.updateCharacter(level.wallMap(), level.crateMap());
      }
    }
    
    // Crates:
    ArrayList<Crate> crates = level.crates();
    for (int i = 0; i < crates.size(); i++){
      Crate c = crates.get(i);
      if (c.destroyed()){
        if (c.explosive()){
          shift.add(2*random(-shiftMax, shiftMax), 2*random(-shiftMax, shiftMax));
          explosions.add(new Explosion(c.pos().copy(), TILE_SIZE*4, 50, color(60, 360, 360), color(0, 360, 360), player, enemies, crates));
        }
        else {
          shift.add(0.5*random(-shiftMax, shiftMax), 0.5*random(-shiftMax, shiftMax));
          explosions.add(new Explosion(c.pos().copy(), TILE_SIZE, color(360), color(250)));
          int random = (int)random(0, 3);
          if (random == 0){ // drop health orb:
            float speed = 5.0;
            direction = random(0, TWO_PI);
            items.add(new HealthOrb(c.pos().copy(), new PVector(speed*cos(direction), speed*sin(direction)), (int)random(1, 3)));
          }
        }
        crates.remove(c);
        level.removeFromCrateMap(c);
        i--;
      }
    }
    
    // Check if level completed:
    if (enemies.isEmpty() && !levelCompleted){
      levelCompleted = true;
      if (level.num()+1 == numOfLevels){ // finished all levels
        gameFinished = true;
        stationaryExplosions.add(new Explosion(new PVector(width/2.0, height/2.0), width/2.0, color(45, 100, 360), color(45, 360, 360), 50));
        flyingText.clear();
        flyingText.add(new FlyingText("Congratulations", 0, 45));
        flyingText.add(new FlyingText("You Win!", 1, 45));
      }
      else {
        stationaryExplosions.add(new Explosion(new PVector(width/2.0, height/2.0), width/2.0, color(player.realHue(), 100, 360), color(player.realHue(), 360, 360), 50));
        flyingText.clear();
        flyingText.add(new FlyingText("Level "+(level.num()+1), 0, 225));
        flyingText.add(new FlyingText("Complete", 1, 225));
      }
    }
    
    // Make list of all characters (player + enemies):
    ArrayList<Character> characters = new ArrayList<Character>();
    if (!gameOver) characters.add(player);
    characters.addAll(enemies);
    
    // Bullets:
    for (int i = 0; i < bullets.size(); i++){
      Bullet b = bullets.get(i);
      if (b.expired()){
        bullets.remove(b);
        i--;
      }
      else if (b.hitWall(level.wallMap()) && !b.bounces()){
        explosions.add(new Explosion(b.pos().copy(), TILE_SIZE*0.75, color(b.hue(), 100, 360), color(b.hue(), 360, 360)));
        bullets.remove(b);
        i--;
      }
      else if (b.hitCharacter(characters)){
        explosions.add(new Explosion(b.pos().copy(), TILE_SIZE, color(0, 360, 360), color(0, 360, 144)));
        bullets.remove(b);
        i--;
        shift.add(0.25*random(-shiftMax, shiftMax), 0.25*random(-shiftMax, shiftMax));
      }
      else {
        if (b.hitCrate(level.crateMap()) && b.destroyed()){
          explosions.add(new Explosion(b.pos().copy(), TILE_SIZE*0.75, color(b.hue(), 100, 360), color(b.hue(), 360, 360)));
          bullets.remove(b);
          i--;
          continue;
        }
        b.updateBullet();
      }
    }
  }
  
  public void drawState(){
    
    // Space background:
    background.drawBackground();
    
    // Camera start:
    float defaultZoom = 1.5f;
    float cameraEasing = 0.07f;
    zoom += (defaultZoom-zoom)/100.0;
    
    pushMatrix();
    translate(player.x(), player.y());
    scale(zoom, zoom);
    translate(-player.x(), -player.y());
    cameraPos.add(shift);
    float x = cameraPos.x;
    float y = cameraPos.y;
    float dx = player.x() - x;
    float dy = player.y() - y;
    x += dx * cameraEasing;
    y += dy * cameraEasing;
    translate((width/2 - x)/zoom, (height/2 - y)/zoom);
    cameraPos.sub(shift);
    cameraPos.set(x, y);
    
    // Draw tiles:
    level.drawTiles();
    
    // Draw blood splatters:
    for (int i = 0; i < splatters.size(); i++){
      Splatter splatter = splatters.get(i);
      if (splatter.finished()){
        splatters.remove(splatter);
        i--;
      }
      else splatter.drawSplatter();
    }
    
    // Draw walls and crates:
    level.drawCrates();
    level.drawWalls();
    
    // Draw items:
    for (int i = 0; i < items.size(); i++){
      Item item = items.get(i);
      item.updateItem();
      if (item.follows()){
        item.follow(player.pos());
        if (item.touchingCharacter(player)){
          if (item instanceof HealthOrb && !player.atFullHealth()){
            HealthOrb orb = (HealthOrb)item;
            player.heal(orb.health());
            items.remove(item);
            i--;
            continue;
          }
        }
      }
      item.drawItem();
    }
    // Get gun closest to player:
    closestGun = null;
    for (Item i : items){
      if (i instanceof GunItem && i.inRange(player.pos())){
        // Is in range to be picked up:
        float dist = dist(player.pos().x, player.pos().y, i.pos().x, i.pos().y);
        if (closestGun == null || dist <= closestDist){
          closestGun = (GunItem)i;
          closestDist = dist;
        }
      }
    }
    
    // Draw characters:
    if (!gameOver) player.drawCharacter();
    for (Enemy e : enemies) e.drawCharacter();
    
    // Draw projectiles:
    for (Bullet b : bullets) b.drawBullet();
    
    // Draw explosions:
    for (int i = 0; i < explosions.size(); i++){
      Explosion e = explosions.get(i);
      if (e.finished()){
        explosions.remove(e);
        i--;
      }
      else e.drawExplosion();
    }
    
    // Camera end:
    popMatrix();
    
    // Update screen shift:
    shiftSpeed.sub(shift.x/5.0, shift.y/5.0);
    shift.add(shiftSpeed);
    shiftSpeed.mult(0.75);
    
    // Draw UI:
    ui.drawHealthBar(player.health(), player.maxHealth());
    ui.drawMinimap(level, player.pos, gameOver);
    ui.drawGun(player.equippedGun());
    if (closestGun != null){
      String text = "Press SPACE to pick up "+closestGun.gun().name()+".";
      ui.drawPrompt(text);
    }
    else if (levelCompleted){
      String text;
      if (gameFinished) text = "Congratulations! You have won! Press SPACE to continue.";
      else text = "Level completed: Press SPACE to continue...";
      ui.drawPrompt(text);
    }
    
    // Draw stationary explosions:
    for (int i = 0; i < stationaryExplosions.size(); i++){
      Explosion e = stationaryExplosions.get(i);
      if (e.finished()){
        stationaryExplosions.remove(e);
        i--;
      }
      else e.drawExplosion();
    }
    
    // Flying text animations:
    for (int i = 0; i < flyingText.size(); i++){
      FlyingText ft = flyingText.get(i);
      if (ft.finished()){
        flyingText.remove(ft);
        i--;
      }
      else ft.drawFlyingText();
    }
    
    // End-screen if gameOver:
    if (gameOver) endScreen();
  }
  
  public void endScreen(){
    // Fade screen to black:
    noStroke();
    fill(0, alpha);
    rect(width/2.0, height/2.0, width, height);
    if (alpha < 200) alpha += 2;
    
    // Print GAME OVER and instructions:
    textAlign(CENTER, CENTER);
    textFont(endFont);
    String text = "GAME OVER";
    String instructions = "Press SPACE to exit to menu...";
    
    // Coloured "GAME OVER"
    fill(0, 360, 360);
    pushMatrix();
    float offset = width/500.0;
    translate(offset, offset);
    textSize(width/10.0); 
    text(text, width/2, height/2);
    textSize(width/30.0); 
    text(instructions, width/2, height*3/4);
    popMatrix();
    
    // White "GAME OVER"
    fill(360);
    pushMatrix();
    //translate(shiftX, shiftY);
    textSize(width/10.0); 
    text(text, width/2, height/2);
    textSize(width/30.0); 
    text(instructions, width/2, height*3/4);
    popMatrix();
  }
}
