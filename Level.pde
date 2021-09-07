
/**
 * Level objects describe a randomly generated level containing rooms, passages, walls, and contents
 * including crates and enemies.
 */
public class Level {
  
  /** Fields: */
  private int levelNum;
  
  // Tile-Map:
  private Tile[][] tiles; // 0 is empty, 1 is floor, 2 is wall
  private int tRows;
  private int tCols;
  
  // Rooms:
  private ArrayList<Room> rooms = new ArrayList<Room>();
  private Room spawnRoom;
  
  // Walls:
  private ArrayList<Wall> walls = new ArrayList<Wall>();
  private ArrayList<Wall>[][] wallMap;
  
  // Player:
  private float playerX, playerY;
  
  // Enemies:
  private ArrayList<Enemy> enemies = new ArrayList<Enemy>();
  
  // Crates:
  private ArrayList<Crate> crates = new ArrayList<Crate>();
  private ArrayList<Crate>[][] crateMap;
  
  // Bullets:
  private ArrayList<Bullet> bullets;
  
  public Level(int num, ArrayList<Bullet> bullets){
    levelNum = num;
    
    // Generate random level layout:
    generateTiles(25, 41); // creats grid of specified size (must be multiple of 4 + 1)
    generateRooms(100); // number of attempts to place rooms
    generatePassages(); // uses maze-generation algorithm to create passages outside rooms
    connectRegions(); // connects passages and rooms until everything is connected
    removeDeadEnds(); // remove passages that lead nowhere
    defineWalls(); // turn walls into big rectangles for efficiency with collision checks
    fillWallMap(); // records the walls near each tile
    
    // Generate enemies, crates and items:
    this.bullets = bullets;
    generateContents(); // generates crates and enemies
  }
  
  public int num(){ return levelNum; }
  public Tile[][] tiles(){ return tiles; }
  public ArrayList<Wall> walls(){ return walls; }
  public ArrayList<Wall>[][] wallMap(){ return wallMap; }
  public ArrayList<Crate> crates(){ return crates; }
  public ArrayList<Crate>[][] crateMap(){ return crateMap; }
  
  public float playerX(){ return playerX; }
  public float playerY(){ return playerY; }
  public void setPlayerPos(float x, float y){
    playerX = x;
    playerY = y; 
  }
  public ArrayList<Enemy> enemies(){ return enemies; }
  
  /** Fills tiles array with empty space (0's): */
  public void generateTiles(int rows, int cols){
    
    if (rows % 2 == 0) rows++; // must both be odd
    if (cols % 2 == 0) cols++;
    tiles = new Tile[rows][cols];
    this.tRows = rows;
    this.tCols = cols;
    for (int row = 0; row < rows; row++) for (int col = 0; col < cols; col++) tiles[row][col] = new Tile(row, col, 0);
  }
  
  /** Adds rectangles of floor tiles (1's): */
  public void generateRooms(int attempts){
    
    // Create 5x5 spawn room:
    int size = 5;
    PVector centre = generateRoomCentre(size*3, size*3);
    spawnRoom = addRoom(centre, size, size);
    PVector spawnPos = spawnRoom.centrePos();
    playerX = spawnPos.x;
    playerY = spawnPos.y;
    
    int passes = 0;
    int fails = 0;
    while (passes < attempts && fails < 500){
      
      // Generate random room size and position:
      int rows = (int)(random(1, 4))*4 + 1;
      if (rows % 2 == 0) rows++;
      int cols = (int)(random(1, 4))*4 + 1;
      if (cols % 2 == 0) cols++;
      
      centre = generateRoomCentre(rows, cols);
      if (!roomOverlapping(centre, rows, cols)){
        addRoom(centre, rows, cols);
        passes++;
      }
      else fails++;
    }
  }
  
  /** Generates a random position within the level with odd coords (within edges of map): */
  public PVector generateRoomCentre(int rows, int cols){
    
    int rowBoundary = (int)(rows/2.0) + 1;
    int colBoundary = (int)(cols/2.0) + 1;
    
    int row = (int)random(rowBoundary, tRows-rowBoundary);
    if (row % 2 == 0) row--;
    int col = (int)random(colBoundary, tCols-colBoundary);
    if (col % 2 == 0) col--;
    
    return new PVector(col, row);
  }
  
  public boolean roomOverlapping(PVector centre, int rows, int cols){
    int centreRow = (int)centre.y;
    int centreCol = (int)centre.x;
    int rowRad = (int)(rows/2.0);
    int colRad = (int)(cols/2.0);
    
    for (int row = centreRow-rowRad; row <= centreRow+rowRad; row++){
      for (int col = centreCol-colRad; col <= centreCol+colRad; col++){
        if (tiles[row][col].type() == 1) return true;
      }
    }
    return false;
  }
  
  public Room addRoom(PVector centre, int rows, int cols){
    int centreRow = (int)centre.y;
    int centreCol = (int)centre.x;
    int rowRad = (int)(rows/2.0);
    int colRad = (int)(cols/2.0);
    
    for (int row = centreRow-rowRad; row <= centreRow+rowRad; row++){
      for (int col = centreCol-colRad; col <= centreCol+colRad; col++){
        tiles[row][col].setType(1);
      }
    }
    
    Room room = new Room(centreRow, centreCol, rowRad, colRad);
    rooms.add(room);
    return room;
  }
  
  public void generatePassages(){
    ArrayList<PVector> nextToUncarved = new ArrayList<PVector>();
    
    PVector start = spawnRoom.centreTile();
    int tries = 0;
    while (tries < 10){
      int row = (int)start.y;
      int col = (int)start.x;
      int dir = (int)random(0, 4);
      switch (dir){
        case 0: col -= 4; break;
        case 1: row -= 4; break;
        case 2: col += 4; break;
        case 3: row += 4; break;
      }
      if (canBeCarved(row, col)){
        nextToUncarved.add(carvePassage((int)start.y, (int)start.x, row, col));
        start.set(col, row);
        break;
      }
      tries++;
    }
    
    while (true){
      int lastDir = -1;
      while (!nextToUncarved.isEmpty()){
        start = nextToUncarved.get(0);   
        tries = 0;
        while (tries < 10){
          int row = (int)start.y;
          int col = (int)start.x;
          int dir;
          dir = (lastDir == -1 || (int)random(0, 2) == 0)? (int)random(0, 4) : lastDir;
          switch (dir){
            case 0: col -= 2; break;
            case 1: row -= 2; break;
            case 2: col += 2; break;
            case 3: row += 2; break;
          }
          lastDir = dir;
          if (canBeCarved(row, col)){
            nextToUncarved.add(carvePassage((int)start.y, (int)start.x, row, col));
            start.set(col, row);
            break;
          }
          lastDir = -1; // direction failed
          tries++;
        }
        if (canBeCarved((int)start.y, (int)start.x)){
          carvePassage((int)start.y, (int)start.x, (int)start.y, (int)start.x);
        }
        if (tries == 10){
          nextToUncarved.remove(0);
        }
      }
      if (filledWithPassages()) break;
      else nextToUncarved.add(findUncarvedPassage());
    }
  }
  
  public PVector carvePassage(int sRow, int sCol, int eRow, int eCol){
    if (eRow-sRow == 0){
      for (int col = (sCol<eCol)? sCol : eCol; col <= ((sCol>eCol)? sCol : eCol); col++){
        tiles[sRow][col].setType(1);
      }
    }
    else {
      for (int row = (sRow<eRow)? sRow : eRow; row <= ((sRow>eRow)? sRow : eRow); row++){
        tiles[row][sCol].setType(1);
      }
    }
    return new PVector(eCol, eRow);
  }
  
  public boolean canBeCarved(int row, int col){
    boolean withinMap = ((0 <= row && row < tRows) && (0 <= col && col < tCols));
    if (!withinMap) return false;
    boolean notCarved = tiles[row][col].type() == 0;
    return notCarved;
  }
  
  public boolean filledWithPassages(){
    for (int row = 1; row < tRows; row = row+2){
      for (int col = 1; col < tCols; col = col+2){
        if (tiles[row][col].type() == 0) return false;
      }
    }
    return true;
  }
  
  public PVector findUncarvedPassage(){
    for (int row = 1; row < tRows; row = row+2){
      for (int col = 1; col < tCols; col = col+2){
        if (tiles[row][col].type() == 0) return new PVector(col, row);
      }
    }
    return null;
  }
  
  public void connectRegions(){
    // Create list of regions (disconnected sections of tilemap).
    // Surround each unconnected region with potential connectors.
    // Apply floodfill algorithm, starting from spawn.
    // When a potential connector is reached, officially connect it.
    // Restart.
    // Loop until all regionss are connected.
    
    ArrayList<Region> regions = new ArrayList<Region>();
    Tile[][] tilesTemp = copyTileMap(); // copy, all turned into 0's as regions found
    createRegions(regions, tilesTemp);
    
    while (regions.size() > 1){
      tilesTemp = copyTileMap(); // fresh copy
      
      for (int i = 1; i < regions.size(); i++){
        Region r = regions.get(i);
        
        // Surround all regions except first with tiles of type 2:
        for (Tile t : r.tiles()){
          for (int dir = 0; dir < 4; dir++){
            int row = t.row();
            int col = t.col();
            switch (dir){
              case 0: col--; break;
              case 1: row--; break;
              case 2: col++; break;
              case 3: row++; break;
            }
            Tile neighbour = tilesTemp[row][col];
            if (neighbour.type() == 0){
              neighbour.setType(2);
            }
          }
        }
      }
      
      // Flood-fill algorithm to connect a region:
      PVector pos = spawnRoom.centreTile();
      Tile startTile = tilesTemp[(int)pos.y][(int)pos.x];
      ArrayList<Tile> nextTiles = new ArrayList<Tile>();
      startTile.setType(0);
      nextTiles.add(startTile);
      while (true){
        boolean done = false;
        //if (nextTiles.size() == 0) break;
        Tile t = nextTiles.remove(0);
        for (int dir = 0; dir < 4; dir++){
          int row = t.row();
          int col = t.col();
          switch (dir){
            case 0: col--; break;
            case 1: row--; break;
            case 2: col++; break;
            case 3: row++; break;
          }
          Tile neighbour = tilesTemp[row][col];
          
          // Connector found:
          if (neighbour.type() == 2){
            tiles[row][col].setType(1); 
            Region r = regionContainingTile(2*row-t.row(), 2*col-t.col(), regions);
            
            // Connect r to first/spawn region:
            Region spawnRegion = regions.get(0);
            spawnRegion.addTile(tiles[row][col]); // add new door/connector that was made
            spawnRegion.addAllTiles(r);
            regions.remove(r);
            
            done = true;
            break;
          }
          else if (neighbour.type() == 1){
            neighbour.setType(0);
            nextTiles.add(neighbour);
          }
        }
        if (done) break;
       // break;
      }
     // break;
    }
    //drawTiles(tilesTemp);
    //drawRegions(regions);
  }
  
  public void createRegions(ArrayList<Region> regions, Tile[][] tilesTemp){
    // Start at spawn, and use floodfill algorithm to add all connected type 1 tiles to new region.
    // While floodfilling, make all those tiles type 0.
    // When done, check if any more type 1 tiles. If no, return.
    // If yes, start at type 1 tile and floodfill to add to new region.
    // Repeat.
    
    while(typeFound(1, tilesTemp)){ // while white squares found
      Tile startTile;
      //println("Starting region: "+regions.size()+", Tiles left: "+numberOfTiles(1, tilesTemp));
      
      if (regions.isEmpty()){ // if no regions made yet, make first region start at player spawn
        PVector startPos = spawnRoom.centreTile();
        int startRow = (int)startPos.y;
        int startCol = (int)startPos.x;
        startTile = tilesTemp[startRow][startCol];
      }
      else { // else if not 1st region, make next region start at random white tile
        startTile = getTile(1, tilesTemp);
      }
      //println("Start pos: "+startTile.row()+", "+startTile.col());
      
      ArrayList<Tile> nextTiles = new ArrayList<Tile>(); // list to store white neighbours in region
      startTile.setType(0); // make starting tile black
      nextTiles.add(startTile); // start with starting tile
      Region region = new Region();
      regions.add(region); // make new region and add it to list
      
      while (!nextTiles.isEmpty()){ // while there are still white tiles to be processed in current region
        
        // Remove first tile from nextTiles and add it to region:
        Tile t = nextTiles.remove(0);
        region.addTile(tiles[t.row()][t.col()]);
        //println("  Tile added: "+t.row()+", "+t.col());
        
        // For each direction from tile, if neighbour is white, make it black and add it to nextTiles:
        for (int dir = 0; dir < 4; dir++){
            int row = t.row();
            int col = t.col();
            switch (dir){
              case 0: col--; break;
              case 1: row--; break;
              case 2: col++; break;
              case 3: row++; break;
            }
            Tile tNext = tilesTemp[row][col];
            if (tNext.type() == 1){
              tNext.setType(0);
              nextTiles.add(tNext);
            }
         }
      }
    }
    //drawRegions(regions);
  }
  
  public int numberOfTiles(int type, Tile[][] tilesTemp){
    int count = 0;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) count++;
      }
    }
    return count;
  }
  
  public boolean typeFound(int type, Tile[][] tilesTemp){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) return true;
      }
    }
    return false;
  }
  
  public Tile getTile(int type, Tile[][] tilesTemp){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        if (tilesTemp[row][col].type() == type) return tilesTemp[row][col];
      }
    }
    return null;
  }
  
  public Tile[][] copyTileMap(){
    Tile[][] tilesTemp = new Tile[tRows][tCols];
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        tilesTemp[row][col] = new Tile(row, col, tiles[row][col].type());
      }
    }
    return tilesTemp;
  }
  
  public Region regionContainingTile(int row, int col, ArrayList<Region> regions){
    for (Region region : regions){
      if (region.containsTile(row, col)) return region;
    }
    return null;
  }
  
  public void removeDeadEnds(){
    boolean done = false;
    while (!done){
      done = true;
      for (int row = 0; row < tRows; row++){
        for (int col = 0; col < tCols; col++){
          if (tiles[row][col].type() == 1){
            
            int exits = 0;
            for (int dir = 0; dir < 4; dir++){
              int r = row;
              int c = col;
              switch (dir){
                case 0: c--; break;
                case 1: r--; break;
                case 2: c++; break;
                case 3: r++; break;
              }
              if (tiles[r][c].type() == 1) exits++;
            }
            
            if (exits <= 1){
              done = false;
              tiles[row][col].setType(0);
            }
          }
        }
      }
    }
  }
  
  public void defineWalls(){
    // Convert every empty space (type 0) touching a floor tile to a wall tile (type 2).
    // Join wall tiles in a line together to form one wall object.
    // This makes it so the game requires less collision checks.
    
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        if ((t.type() == 0) && (tileTouchingType(t, 1))){
          t.setType(2);
        }
      }
    }
    
    // Make copy where wall tiles are removed as wall objects are made:
    Tile[][] tilesTemp = copyTileMap();
    
    // Start at wall tile.
    // If wall tile on left or right, horizontally flood-fill to fill list.
    // Else vertically flood-fill to fill list.
    // Remove wall tiles as added to list.
    // Loop until no more wall tiles (type 2).
    
    while (typeFound(2, tilesTemp)){
      Tile startTile = getTile(2, tilesTemp);
      int startRow = startTile.row();
      int startCol = startTile.col();
      
      boolean horizontal;
      if (startCol-1 >= 0 && (tilesTemp[startRow][startCol-1].type() == 2)) horizontal = true; // if wall tile to left
      else if (startCol+1 < tCols && (tilesTemp[startRow][startCol+1].type() == 2)) horizontal = true; // if wall tile to right
      else horizontal = false; // if neither
      
      ArrayList<Tile> wallTiles = new ArrayList<Tile>(); // holds tiles in wall object
      ArrayList<Tile> nextTiles = new ArrayList<Tile>(); // holds tiles to process
      startTile.setType(0);
      nextTiles.add(startTile);
      
      while (!nextTiles.isEmpty()){
        Tile t = nextTiles.remove(0);
        wallTiles.add(tiles[t.row()][t.col()]);
        
        for (int i = -1; i <= 1; i += 2){
          int r = t.row();
          int c = t.col();
          if (horizontal) c += i;
          else r += i;
          
          if (!((0 <= c && c < tCols) && (0 <= r && r < tRows))) continue; // if not within bounds, skip
          Tile neighbour = tilesTemp[r][c];
          if (neighbour.type() == 2){
            neighbour.setType(0);
            nextTiles.add(neighbour);
          }
        }
      }
      walls.add(new Wall(wallTiles));
    }
    //println(walls.size());
  }
  
  /** Returns true if tile t is touching a tile of type: */
  public boolean tileTouchingType(Tile t, int type){
    for (int dir = 0; dir < 8; dir++){
      int r = t.row();
      int c = t.col();
      switch (dir){
        case 0: r++;
        case 1: c--; break;
        case 2: c--;
        case 3: r--; break;
        case 4: r--;
        case 5: c++; break;
        case 6: c++;
        case 7: r++; break;
      }
      if (r < 0 || r >= tRows || c < 0 || c >= tCols) continue;
      if (tiles[r][c].type() == type) return true;
    }
    return false;
  }
  
  public void fillWallMap(){
    wallMap = new ArrayList[tRows][tCols];
    
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        if (t.type() == 2){
          ArrayList<Wall> nearbyWalls = new ArrayList<Wall>();
          for (Wall w : walls){
            if (w.containsTile(t)){
              nearbyWalls.add(w);
              wallMap[row][col] = nearbyWalls;
              break;
            }
          }
        }
        else if (t.type() == 1 && tileTouchingType(t, 2)){
          ArrayList<Wall> nearbyWalls = new ArrayList<Wall>();
          for (int dir = 0; dir < 8; dir++){
            int r = t.row();
            int c = t.col();
            switch (dir){
              case 0: r++;
              case 1: c--; break;
              case 2: c--;
              case 3: r--; break;
              case 4: r--;
              case 5: c++; break;
              case 6: c++;
              case 7: r++; break;
            }
            if (r < 0 || r >= tRows || c < 0 || c >= tCols) continue;
            Tile tNext = tiles[r][c];
            for (Wall w : walls){
              if (w.containsTile(tNext) && !nearbyWalls.contains(w)){
                nearbyWalls.add(w);
              }
            }
          }
          wallMap[row][col] = nearbyWalls;
        }
      }
    }
  }
  
  /** Draw Methods: */
  
  public void drawTiles(){
    //noStroke();
    stroke(360);
    float tileSize = Game.TILE_SIZE;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        int value = tiles[row][col].type();
        if (value == 0) continue;
        float x = col*tileSize + tileSize/2.0;
        float y = row*tileSize + tileSize/2.0;
        int r = 0;
        switch (value){
          //case 0: fill(0); break;
          case 1: fill(tiles[row][col].brightness()); break;
          case 2: fill(225, 360, 360); break;
        }
        rect(x, y, tileSize, tileSize);
      }
    }
  }
  
  public void drawTiles(Tile[][] tiles){
    noStroke();
    float tileSize = Game.TILE_SIZE;
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        int value = tiles[row][col].type();
        float x = col*tileSize + tileSize/2.0;
        float y = row*tileSize + tileSize/2.0;
        switch (value){
          case 0: fill(0); break;
          case 1: fill(360); break;
          case 2: fill(100, 360, 360); break;
        }
        rect(x, y, tileSize, tileSize);
        /*fill(180);
        textSize(10);
        text(row+" "+col, x, y);*/
      }
    }
  }
  
  public void drawRegions(ArrayList<Region> regions){
    float tileSize = Game.TILE_SIZE;
    for (int i = 0; i < regions.size(); i++){
      Region r = regions.get(i);
      fill(regions.indexOf(r)*(360/regions.size()), 360, 360);
      ArrayList<Tile> tiles = r.tiles();
      println("Region "+i+": "+tiles.size());
      for (Tile t : tiles){
        int value = t.type();
        float x = t.col()*tileSize + tileSize/2.0;
        float y = t.row()*tileSize + tileSize/2.0;
        rect(x, y, tileSize, tileSize);
      }
    }
  }
  
  public void drawWalls(){
    strokeWeight(5);
    stroke(225, 360, 360);
    for (Wall wall : walls){
      float x = (wall.left()+wall.right())/2.0;
      float y = (wall.top()+wall.bottom())/2.0;
      float w = (wall.right()-wall.left());
      float h = (wall.bottom()-wall.top());
      
      int glowFrames = wall.glowFrames();
      if (glowFrames == 0) fill(0);
      else {
        float brightness = 360*glowFrames/wall.maxGlowFrames();
        wall.setGlowFrames(glowFrames-1);
        fill(180, 360, brightness);
      }
      rect(x, y, w, h);
      //fill(180);
      //textSize(15);
      //text(wall.leftCol()+","+wall.rightCol()+" "+wall.topRow()+","+wall.bottomRow(), x, y, w ,h);
    }
  }
  
  public void generateContents(){
    // Get all room floor tiles:
    ArrayList<Tile> floorTiles = new ArrayList<Tile>();
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        Tile t = tiles[row][col];
        
        // If tile is type 1 (floor) and in room:
        if (t.type() != 1) continue;
        boolean inRoom = false;
        for (Room r : rooms){
          if (r == spawnRoom) continue; // so they don't spawn in player spawn room
          if (r.isRoomTile(row, col)){
            inRoom = true;
            break;
          }
        }
        if (inRoom) floorTiles.add(t);
      }
    }
    float tileSize = Game.TILE_SIZE;
    
    // Spawn piles of crates on random room floor tiles:
    crateMap = new ArrayList[tRows][tCols];
    int numOfCratePiles = 15;
    for (int i = 0; i < numOfCratePiles; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float shift = tileSize/4.0;
      float size = tileSize/2.0;
      for (int corner = 0; corner < 4; corner++){
        float x = t.col()*tileSize + tileSize/2.0;
        float y = t.row()*tileSize + tileSize/2.0;
        switch (corner){
          case 0: x -= shift; y -= shift; break;
          case 1: x += shift; y -= shift; break;
          case 2: x += shift; y += shift; break;
          case 3: x -= shift; y += shift; break;
        }
        boolean explosive = (int)random(0, 20) == 0;
        Crate crate = new Crate(new PVector(x, y), size, explosive);
        crates.add(crate);
        // Fill crate map:
        for (int r = -1; r <= 1; r++){
          for (int c = -1; c <= 1; c++){
            int row = t.row()+r;
            int col = t.col()+c;
            if (row < 0 || row >= tRows || col < 0 || col >= tCols) continue;
            if (crateMap[row][col] == null) crateMap[row][col] = new ArrayList<Crate>();
            ArrayList<Crate> cratesOnTile = crateMap[row][col];
            if (!cratesOnTile.contains(crate)) cratesOnTile.add(crate);
          }
        }
      }
    }
    
    // Spawn explosive crates alone:
    int numOfExplosiveCrates = 5 + levelNum;
    for (int i = 0; i < numOfExplosiveCrates; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float shift = tileSize/4.0;
      float size = tileSize/2.0;
      int corner = (int)random(0, 4);
      float x = t.col()*tileSize + tileSize/2.0;
      float y = t.row()*tileSize + tileSize/2.0;
      switch (corner){
        case 0: x -= shift; y -= shift; break;
        case 1: x += shift; y -= shift; break;
        case 2: x += shift; y += shift; break;
        case 3: x -= shift; y += shift; break;
      }
      Crate crate = new Crate(new PVector(x, y), size, true);
      crates.add(crate);
      // Fill crate map:
      for (int r = -1; r <= 1; r++){
        for (int c = -1; c <= 1; c++){
          int row = t.row()+r;
          int col = t.col()+c;
          if (row < 0 || row >= tRows || col < 0 || col >= tCols) continue;
          if (crateMap[row][col] == null) crateMap[row][col] = new ArrayList<Crate>();
          ArrayList<Crate> cratesOnTile = crateMap[row][col];
          if (!cratesOnTile.contains(crate)) cratesOnTile.add(crate);
        }
      }
    }
    
    // Spawn enemies on random room floor tiles:
    int numOfEnemies = 10+(2*levelNum);
    for (int i = 0; i < numOfEnemies; i++){
      int index = (int)random(0, floorTiles.size());
      Tile t = floorTiles.get(index);
      floorTiles.remove(t);
      float x = t.col()*tileSize + tileSize/2.0;
      float y = t.row()*tileSize + tileSize/2.0;
      Gun gun = new Pistol(bullets);
      int random = (int)random(0, 10);
      switch (random){
        case 0: gun = new Shotgun(bullets); break;
        case 1: gun = new AssaultRifle(bullets); break;
        case 2: gun = new SniperRifle(bullets); break;
        case 3: gun = new Bouncer(bullets); break;
        case 4: gun = new FlameThrower(bullets); break;
      }
      //gun = new FlameThrower(bullets);
      enemies.add(new Enemy(new PVector(x, y), 20 + 5*levelNum, tileSize/2.0, 3, 15*tileSize, gun));
      //enemies.add(new Enemy(new PVector(x, y), 1, tileSize/2.0, 3, 15*tileSize, gun)); // for debugging
    }
  }
  
  public void drawCrates(){
    for (Crate c : crates) c.drawCrate();
  }
  
  public void removeFromCrateMap(Crate c){
    for (int row = 0; row < tRows; row++){
      for (int col = 0; col < tCols; col++){
        ArrayList cratesOnTile = crateMap[row][col];
        if (cratesOnTile == null) continue;
        if (cratesOnTile.contains(c)) cratesOnTile.remove(c);
      }
    }
  }
}


public class Room {
  
  /** Fields: */
  private int leftCol, rightCol, topRow, bottomRow;
  
  public Room(int centreRow, int centreCol, int rowRad, int colRad){
    this.topRow = centreRow - rowRad;
    this.bottomRow = centreRow + rowRad;
    this.leftCol = centreCol - colRad;
    this.rightCol = centreCol + colRad;
  }
  
  public int leftCol(){ return leftCol; }
  public int rightCol(){ return rightCol; }
  public int topRow(){ return topRow; }
  public int bottomRow(){ return bottomRow; }
  
  public boolean isRoomTile(int row, int col){
    return ((topRow <= row && row <= bottomRow) && (leftCol <= col && col <= rightCol));
  }
  
  public boolean isInRoom(float x, float y){
    float tileSize = Game.TILE_SIZE;
    float left = leftCol*tileSize;
    float right = (rightCol+1)*tileSize;
    float top = topRow*tileSize;
    float bottom = bottomRow*tileSize;
    return ((left <= x && x <= right) && (top <= y && y <= bottom));
  }
  
  public PVector centrePos(){
    float tileSize = Game.TILE_SIZE;
    float x = (leftCol*tileSize + rightCol*tileSize)/2.0 + tileSize/2.0;
    float y = (topRow*tileSize + bottomRow*tileSize)/2.0 + tileSize/2.0;
    return new PVector(x, y);
  }
  
  public PVector centreTile(){
    int col = (leftCol+rightCol)/2;
    int row = (topRow+bottomRow)/2;
    return new PVector(col, row);
  }
  
}

public class Tile {
  
  /** Fields: */
  private int row, col, type;
  private float brightness = random(300, 350);
  
  public Tile(int row, int col, int type){
    this.row = row;
    this.col = col;
    this.type = type;
  }
  
  public int row(){ return row; }
  public int col(){ return col; }
  public void setType(int type){ this.type = type; }
  public int type(){ return type; }
  public float brightness(){ return brightness; }
}

public class Region {
   
  /** Fields: */
  private ArrayList<Tile> tiles = new ArrayList<Tile>();
  
  public void addTile(Tile tile){ tiles.add(tile); }
  public void addAllTiles(Region other){ tiles.addAll(other.tiles); }
  public boolean containsTile(int row, int col){
    for (Tile tile : tiles){
      if (tile.row() == row && tile.col() == col) return true;
    }
    return false;
  }
  public ArrayList<Tile> tiles(){ return tiles; }
}

public class Wall {
  
  /** Fields: */
  private ArrayList<Tile> tiles;
  private float left, top, right, bottom;
  private int leftCol, rightCol, topRow, bottomRow;
  private int maxGlowFrames = 300;
  private int glowFrames;
  
  public Wall(ArrayList<Tile> tiles){
    this.tiles = tiles;
    
    // Get dimensions of wall:
    int leftCol = Integer.MAX_VALUE;
    int rightCol = Integer.MIN_VALUE;
    int topRow = Integer.MAX_VALUE;
    int bottomRow = Integer.MIN_VALUE;
    for (Tile t : tiles){
      if (t.col() < leftCol) leftCol = t.col();
      if (t.col() > rightCol) rightCol = t.col();
      if (t.row() < topRow) topRow = t.row();
      if (t.row() > bottomRow) bottomRow = t.row();
    }
    this.leftCol = leftCol;
    this.rightCol = rightCol;
    this.topRow = topRow;
    this.bottomRow = bottomRow;
    
    float tileSize = Game.TILE_SIZE;
    left = leftCol * tileSize;
    right = (rightCol+1) * tileSize;
    top = topRow * tileSize;
    bottom = (bottomRow+1) * tileSize;
  }
  
  public boolean containsPoint(float x, float y){
    return (left <= x && x <= right) && (top <= y && y <= bottom);
  }
  public boolean containsTile(Tile t){ return (tiles.contains(t)); }
  
  public void glow(){ glowFrames = maxGlowFrames; }
  public int glowFrames(){ return glowFrames; }
  public void setGlowFrames(int f){ glowFrames = f; }
  public int maxGlowFrames(){ return maxGlowFrames; }
  
  public float left(){ return left; }
  public float right(){ return right; }
  public float top(){ return top; }
  public float bottom(){ return bottom; }
  
  public int topRow(){ return topRow; }
  public int bottomRow(){ return bottomRow; }
  public int leftCol(){ return leftCol; }
  public int rightCol(){ return rightCol; }
}
