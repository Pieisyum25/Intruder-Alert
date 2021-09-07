/** 
CGRA-151 Project:
  Name - Intruder Alert
  Developer - John Flynn
  Start - 18/09/20
  Finish - There's always more I could add!
*/

private State state;

public void setup(){
  //fullScreen();
  size(1500, 900);
  colorMode(HSB, 360);
  rectMode(CENTER);
  //noSmooth();
  
  state = new Menu();
}

public void draw(){
  background(0);
  state.runState();
}

public void mousePressed(){ state.mouseP(); }
public void mouseReleased(){ state.mouseR(); }
public void keyPressed(){ state.keyP(); }
public void keyReleased(){ state.keyR(); }

public void setState(State s){ state = s; } // changes state

/** Each implementation of State describes a different game-state:
 * Makes running the different states easier.
 */
public abstract class State {
  
  abstract void mouseP();
  abstract void mouseR();
  abstract void keyP();
  abstract void keyR();
  
  public final void runState(){
    updateState();
    drawState();
  }
  abstract void updateState();
  abstract void drawState();
}
