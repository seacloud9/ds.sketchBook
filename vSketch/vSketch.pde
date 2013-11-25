import processing.opengl.*;
float a;
PShader bloom;
PShader texlightShader;
int Size=20;
PImage tex;
float rotx = PI/4;
float roty = PI/4;

int[][][][] voxel=new int[Size][Size][Size][4];

void setup(){
  //size(400,400,OPENGL);
  size(800, 800, P3D);
  frameRate(30);
  smooth();
  noStroke();
  bloom = loadShader("bloom.glsl");
  texlightShader = loadShader("pixlightxfrag.glsl", "pixlightxvert.glsl");
  //stroke(color(44,48,32));
  fill(255);
}

void draw(){
  background(255);
  pointLight(51, 102, 126,  width/2, height/2, 10);
  ambientLight(51, 102, 126);
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
          Voxel vx = new Voxel("grass.png");
          vx.display();
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
  a=a+.01;
  camera(mouseY, height/2, (height/2) / tan(PI/2), width/2, height/2, 0, 0, 1, 0);
  PImage test = loadImage("grass.png");
  PImage test2 = loadImage("grass2.png");
  bloom.set("texture", test2);
  shader(texlightShader);
  shader(bloom);
  //pointLight(125, 185, 232, width/2, height, 200);
  
  //bloom.set("origImage", test2);
  //bloom.set("bloomLevel", 1.5);
  //bloom.set("exposure", 2.2);
  
  
  
  

}



/*
void mouseDragged() {
  float rate = 0.01;
  rotx += (pmouseY-mouseY) * rate;
  roty += (mouseX-pmouseX) * rate;
}*/