HColorPool colors;

void setup() {
	size(640, 640);
	H.init(this).background(#111111);
	smooth();

	colors = new HColorPool(#21161c, #251318, #33161c, #221012, #2e1315, #e62d39, #59161a, #711c21, #4e171a, #ffffff);

	int starScale = 800;
	int starOffest = 15;

	for (int i=0; i<53; ++i) {
		HPath d = (HPath) H.add( new HPath().star(5, 5) )
			.size(starScale)
			.noStroke()
			.fill( colors.getColor() )
			.anchorAt(H.CENTER)
			.locAt(H.CENTER)
		;

		new HOscillator()
			.target(d)
			.property(H.ROTATION)
			.range(-20, 20)
			.speed(0.4)
			.freq(8)
			.currentStep(i)
		;

		starScale -= starOffest;
	}
}

void draw() {
	H.drawStage();
}



