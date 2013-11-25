import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import org.philhosoft.p8g.svg.P8gGraphicsSVG; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class fracInvaders extends PApplet {


P8gGraphicsSVG svg;
int sz = 100;
int step = sz/5; // = 4
//int step = 1;
int padding = PApplet.parseInt(sz/2);
int mx,my;
ArrayList invaders;

public void setup() {
  svg = (P8gGraphicsSVG) createGraphics(width, height, P8gGraphicsSVG.SVG, random(1234)+".svg");
  beginRecord(svg);
  svg.clear(); // Discard previous frame
  svg.beginDraw(); // And record this one
  mx = PApplet.parseInt(900/(sz+padding));
  my = PApplet.parseInt(600/(sz+padding));
  size(padding+mx*(sz+padding), padding+my*(sz+padding));
  background(255);
  stroke(0);

  invaders = new ArrayList();

  for (float x=padding; x<width-2*padding; x += (sz+padding)) {
    //println(width-2*padding);
    for (float y=padding; y<height-2*padding; y += (sz+padding)) {
      //println(height-2*padding);
      Invader invader = new Invader(x, y);
      invaders.add(invader);
    }
  }
  for (int i=0; i<invaders.size();i++) {
    Invader inv = (Invader) invaders.get(i);
    inv.display();
  }
}

public void draw() {
  
}


public void keyPressed() {
  if (key == 's') // Save the current image (and overwrite the previous one)
  {
    svg.endRecord();
    save(random(1234)+".gif");
    println("Saved.");
  }
  else if (key == 'r') // Record the current image to a new numbered file
  {
    svg.recordFrame("SineOnBezier-###.svg");
    println("Saved #" + svg.savedFrameCount);
  }
  else if (key == 'q')
  {
    // Don't overwrite the last saved frame!
    svg.clear();
    exit();
  }
  
}
public void mouseClicked() {
  setup();
}

class Invader {
  float x, y;
  int c, m ;
  int[][] col = new int[sz][sz];

  Invader(float _x, float _y) {
    x = _x;
    y = _y;
    //c = 155;
    
    for (int j=0;j<sz;j+=step) {
      m = 1;
      
      for (int i=0;i<sz/2;i+=step) {
        c = (random(1) > .5f)? 255:0; //black or white?
        col[j][i]= c;
        col[j][i+(sz-step)/m] = c;
        m++;
        //println(col);
      }
    }

  }

  public void display() {
    fill(c);
    stroke(c);
    for (int j=0;j<sz; j+=step) {
      for (int i=0;i<sz; i+= step) {
        fill(col[j][i]);
        //println(j);
        noStroke();
        rect(x+i, y+j, step, step);
      }
    }
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "fracInvaders" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
