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

public class animatedText extends PApplet {

PFont f;
String message = "Each character is not written individually.";

public void setup() {
  size(400, 200);
  f = createFont("Arial",20,true);
}

public void draw() { 
  background(255);
  fill(0);
  textFont(f);         
// The first character is at pixel 10.
int x = 10; 
for (int i = 0; i < message.length(); i++) {
  // Each character is displayed one at a time with the charAt() function.
  text(message.charAt(i),x,height/2);
  // All characters are spaced 10 pixels apart.
  x += 10; 
}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "animatedText" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
