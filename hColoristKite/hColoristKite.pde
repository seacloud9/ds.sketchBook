// As seen here, we need to preload Images and Fonts.
//
// See http://processingjs.org/reference/preload/
// and http://processingjs.org/reference/font/
// for more information.

/* @pjs preload="sintra.jpg"; */

HDrawablePool pool;
HTimer timer;
int cellSize = 20;
boolean record = false;
PImage img;
boolean hasRun = false;

void pixelateImage(){
	if(cellSize <= 20 && cellSize >= 0){
				cellSize -= 1;
			}else{
				cellSize = 10;
			}
	final HPixelColorist colors = new HPixelColorist(img)
		.fillOnly();

		pool = new HDrawablePool(23328);
		pool.autoAddToStage()
		.add (
			new HRect()
			.rounding(4)
			)
		.layout (
			new HGridLayout()
			.startX(0)
			.startY(0)
			.spacing(cellSize+1,cellSize+1)
			.cols(216)
			)
		.onCreate (
			new HCallback(){
				public void run(Object obj) {
					HDrawable d = (HDrawable) obj;
					d
					.noStroke()
					.anchorAt(H.CENTER)
					.size(cellSize)
					;

					colors.applyColor(d);
				}
			}
			)
		.requestAll();

		H.drawStage();
}

void setup() {
	size(2160,1080);
	H.init(this).background(255);
	smooth();
	img = loadImage("kitetest3.png");
	timer = new HTimer()
		.numCycles( 999 )
		.interval(25)
		.callback(
			new HCallback() { 
				public void run(Object obj) {
					pixelateImage();
				}
			}
		);
		pixelateImage();
}

void draw() {
	if(!hasRun){
		pixelateImage();
	}
		if (record) {
			saveFrame("MyRender-####.png");
		}
		if (record) {
			endRecord();
			record = false;
		}
	}

