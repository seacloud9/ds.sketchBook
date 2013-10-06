// As seen here, we need to preload Images and Fonts.
//
// See http://processingjs.org/reference/preload/
// and http://processingjs.org/reference/font/
// for more information.

/*
@pjs preload="sintra.jpg";
*/

HDrawablePool pool;
HSwarm swarm;
HPixelColorist colors;
HTimer timer;
boolean record = false;

void setup() {
	size(2000,1323);
	H.init(this).background(#ffffff).autoClear(false);
	smooth();

	PImage img = loadImage("investigator.png");
	colors = new HPixelColorist(img)
	    .fillOnly()
	     .strokeOnly()
	     .fillAndStroke()
    ;

	swarm = new HSwarm()
		.goal(width/2,height/2)
		.speed(5)
		.turnEase(0.05f)
		.twitch(20)
	;

	pool = new HDrawablePool(100);
	pool.autoAddToStage()
		.add (
			new HTriangle().type(H.EQUILATERAL)
		)
		.onCreate (
		    new HCallback() {
		    	public void run(Object obj) {
		    		HDrawable d = (HDrawable) obj;
					d
						.size((int)random(6,60), (int)random(6,60) )
						.noStroke()
						.fill( #000000 )
						.loc( width/2, height/2 )
						.anchorAt( H.CENTER )
					;
					colors.applyColor( d );
					swarm.addTarget(d);
				}
			}
		)
	;

	timer = new HTimer()
		.numCycles( pool.numActive() )
		.interval(250)
		.callback(
			new HCallback() { 
				public void run(Object obj) {
					pool.request();
				}
			}
		);
}

void draw() {
	if (record) {
			saveFrame("MyRender-####.png");
		}
		if (record) {
			endRecord();
			record = false;
	}
	HIterator<HDrawable> it = pool.iterator();
	while(it.hasNext()) {
		HDrawable d = it.next();
		colors.applyColor(d);
		d.alpha(50);
	}

    if(H.mouseStarted()) {
      swarm.goal(mouseX,mouseY);
    }

	H.drawStage();
}
