import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class vSketch extends PApplet {


float a;
int Size=20;
PImage tex;
float rotx = PI/4;
float roty = PI/4;

int[][][][] voxel=new int[Size][Size][Size][4];

public void setup(){
  //size(400,400,OPENGL);
  size(800, 800, P3D);
  frameRate(30);
  tex = loadImage("grass.png");
  textureMode(NORMAL);
  smooth();
  noStroke();
  //stroke(color(44,48,32));
  fill(255);
}

public void draw(){
  background(255);
  for(int x=0;x<Size/2;x++){
    for(int y=0;y<Size/2;y++){
	for(int z=0;z<Size/2;z++){
	  if(voxel[x][y][z][0]==1){
	    fill(voxel[x][y][z][1],voxel[x][y][z][2],voxel[x][y][z][3]);
	    pushMatrix();
	    translate(width/2,height/2);
	    rotateY(a);
	    rotateX(a);
	    translate((x-Size/4)*30,(y-Size/4)*30,(z-Size/4)*30);
	    scale(10);
	    TexturedCube(tex);
	    popMatrix();
	    if(random(0,100)>99){
		voxel[x][y][z][0]=0;
	    }
	  }
	  else{
	    if(random(0,100)>99){
		voxel[x][y][z][0]=1;
		voxel[x][y][z][1]=10*x+150;
		voxel[x][y][z][2]=10*y+150;
		voxel[x][y][z][3]=10*z+150;

	    }
	  }

	}
    }
  }
  a=a+.01f;
  camera(mouseY, height/2, (height/2) / tan(PI/2), width/2, height/2, 0, 0, 1, 0);
}





public void TexturedCube(PImage tex) {
  beginShape(QUADS);
  texture(tex);

  // Given one texture and six faces, we can easily set up the uv coordinates
  // such that four of the faces tile "perfectly" along either u or v, but the other
  // two faces cannot be so aligned.  This code tiles "along" u, "around" the X/Z faces
  // and fudges the Y faces - the Y faces are arbitrarily aligned such that a
  // rotation along the X axis will put the "top" of either texture at the "top"
  // of the screen, but is not otherwised aligned with the X/Z faces. (This
  // just affects what type of symmetry is required if you need seamless
  // tiling all the way around the cube)
  
  // +Z "front" face
  vertex(-1, -1,  1, 0, 0);
  vertex( 1, -1,  1, 1, 0);
  vertex( 1,  1,  1, 1, 1);
  vertex(-1,  1,  1, 0, 1);

  // -Z "back" face
  vertex( 1, -1, -1, 0, 0);
  vertex(-1, -1, -1, 1, 0);
  vertex(-1,  1, -1, 1, 1);
  vertex( 1,  1, -1, 0, 1);

  // +Y "bottom" face
  vertex(-1,  1,  1, 0, 0);
  vertex( 1,  1,  1, 1, 0);
  vertex( 1,  1, -1, 1, 1);
  vertex(-1,  1, -1, 0, 1);

  // -Y "top" face
  vertex(-1, -1, -1, 0, 0);
  vertex( 1, -1, -1, 1, 0);
  vertex( 1, -1,  1, 1, 1);
  vertex(-1, -1,  1, 0, 1);

  // +X "right" face
  vertex( 1, -1,  1, 0, 0);
  vertex( 1, -1, -1, 1, 0);
  vertex( 1,  1, -1, 1, 1);
  vertex( 1,  1,  1, 0, 1);

  // -X "left" face
  vertex(-1, -1, -1, 0, 0);
  vertex(-1, -1,  1, 1, 0);
  vertex(-1,  1,  1, 1, 1);
  vertex(-1,  1, -1, 0, 1);

  endShape();
}
/*
void mouseDragged() {
  float rate = 0.01;
  rotx += (pmouseY-mouseY) * rate;
  roty += (mouseX-pmouseX) * rate;
}*/
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "vSketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
