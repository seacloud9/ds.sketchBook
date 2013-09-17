import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class starFieldSketch extends PApplet {

/// 3D Starfield

int numstars=400;
final int SPREAD=64;
int CX,CY;
final float SPEED=1.9f;

Star[] s = new Star[numstars];

public void setup(){
  size(512,512);
  colorMode(RGB,255);
  noStroke();
  frameRate(60);
  CX=width/2 ; CY=height/2;
 /// s = new Star[numstars];
  for(int i=0;i<numstars;i++){
    s[i]=new Star();
    s[i].SetPosition();
  }
}

public void draw(){
  background(0,0,0);
  for(int i=0;i<numstars;i++){
    s[i].DrawStar();
  }
}

class Star { 
  float x=0,y=0,z=0,sx=0,sy=0;
  public void SetPosition(){
    z=(float) random(200,255);
    x=(float) random(-1000,1000);
    y=(float) random(-1000,1000);
  }
  public void DrawStar(){
    if (z<SPEED){
	this.SetPosition();
    }
    z-=SPEED;
    sx=(x*SPREAD)/(z)+CX;
    sy=(y*SPREAD)/(4+z)+CY;
    if (sx<0 | sx>width){
	this.SetPosition();
    }
    if (sy<0 | sy>height){
	this.SetPosition();
    }
    fill(color(255 - (int) z,255 - (int) z,255 - (int) z));
    rect( (int) sx,(int) sy,2,3);
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "starFieldSketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
