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
	size(3262,638);
	H.init(this).background(#ffffff).autoClear(false);
	smooth();

	PImage img = loadImage("palaceoffinearts.jpg");
	colors = new HPixelColorist(img)
	    .fillOnly()
	     .strokeOnly()
	     .fillAndStroke()
    ;

	swarm = new HSwarm()
		.goal(width/2,height/2)
		.speed(10)
		.turnEase(0.05f)
		.twitch(20)
	;

	pool = new HDrawablePool(100);
	PShape svg1 = loadShape("splat1.svg");
	PShape svg2 = loadShape("splat2.svg");
	PShape svg3 = loadShape("splat3.svg");
	PShape svg4 = loadShape("splat4.svg");
	PShape svg5 = loadShape("splat5.svg");
	PShape svg6 = loadShape("splat6.svg");
	PShape svg7 = loadShape("splat7.svg");
	PShape svg8 = loadShape("splat8.svg");
	PShape svg9 = loadShape("splat9.svg");
	PShape svg10 = loadShape("splat10.svg");
	PShape svg11 = loadShape("splat11.svg");
	PShape svg12 = loadShape("splat12.svg");
	PShape svg13 = loadShape("splat13.svg");
	pool.autoAddToStage()
		.add (
			new HShape(svg1).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg2).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg3).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg4).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg5).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg6).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg7).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg8).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg9).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg10).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg11).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg12).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.add (
			new HShape(svg13).enableStyle(false)
			.strokeJoin(CENTER)
			.strokeCap(CENTER)
			.anchorAt(H.CENTER)
		)
		.onCreate (
		    new HCallback() {
		    	public void run(Object obj) {
		    		HDrawable d = (HDrawable) obj;
					d
						.size((int)random(50,100), (int)random(50,100) )
						.noStroke()
						.fill( #000000 )
						.loc( width/2, height/2 )
						.anchorAt( H.CENTER )
					;
					colors.applyColor( d );

					// Add "d" to swarm's list of targets
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
		)
	;
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
