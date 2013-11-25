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

public class hColoristPixelator extends PApplet {

// As seen here, we need to preload Images and Fonts.
//
// See http://processingjs.org/reference/preload/
// and http://processingjs.org/reference/font/
// for more information.

/* @pjs preload="kitetest3.png"; */
/* @pjs transparent="true"; */

HDrawablePool pool;
HTimer timer;
int cellSize = 20;
boolean record = false;
PImage img;
boolean hasRun = false;
int hSize = 680;
int wSize = 2632;
String imgName = "northbeach.png";

public void pixelateImage(){
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
			.rounding(2)
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

public void setup() {
	size(wSize,hSize);
	H.init(this);
	smooth();
	img = loadImage(imgName);
	timer = new HTimer()
		.numCycles( 999 )
		.interval(250)
		.callback(
			new HCallback() { 
				public void run(Object obj) {
					pixelateImage();
				}
			}
		);
		pixelateImage();
}

public void draw() {
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

public static abstract class HBehavior extends HNode < HBehavior > {
    protected HBehaviorRegistry _registry;
    public HBehavior register() {
        H.behaviors().register(this);
        return this;
    }
    public HBehavior unregister() {
        H.behaviors().unregister(this);
        return this;
    }
    public boolean poppedOut() {
        return _registry == null;
    }
    public void popOut() {
        super.popOut();
        _registry = null;
    }
    public void swapLeft() {
        if (_prev._prev == null) return;
        super.swapLeft();
    }
    public void putAfter(HBehavior dest) {
        if (dest._registry == null) return;
        super.putAfter(dest);
        _registry = dest._registry;
    }
    public void putBefore(HBehavior dest) {
        if (dest._registry == null) return;
        super.putBefore(dest);
        _registry = dest._registry;
    }
    public void replaceNode(HBehavior target) {
        super.replaceNode(target);
        _registry = target._registry;
        target._registry = null;
    }
    public abstract void runBehavior(PApplet app);
}
public static class HBehaviorRegistry {
    private HBehaviorSentinel _firstSentinel;
    public HBehaviorRegistry() {
        _firstSentinel = new HBehaviorSentinel(this);
    }
    public boolean isRegistered(HBehavior b) {
        return (b._registry != null && b._registry.equals(this));
    }
    public void register(HBehavior b) {
        if (b.poppedOut()) b.putAfter(_firstSentinel);
    }
    public void unregister(HBehavior b) {
        if (isRegistered(b)) b.popOut();
    }
    public void runAll(PApplet app) {
        HBehavior n = _firstSentinel.next();
        while (n != null) {
            n.runBehavior(app);
            n = n.next();
        }
    }
    public static class HBehaviorSentinel extends HBehavior {
        public HBehaviorSentinel(HBehaviorRegistry r) {
            _registry = r;
        }
        public void runBehavior(PApplet app) {}
    }
}
public static class HFollow extends HBehavior {
    protected float _ease, _spring, _dx, _dy;
    protected HFollowable _goal;
    protected HMovable _follower;
    public HFollow() {
        this(1);
    }
    public HFollow(float ease) {
        this(ease, 0);
    }
    public HFollow(float ease, float spring) {
        this(ease, spring, H.mouse());
    }
    public HFollow(float ease, float spring, HFollowable goal) {
        _ease = ease;
        _spring = spring;
        _goal = goal;
    }
    public HFollow ease(float f) {
        _ease = f;
        return this;
    }
    public float ease() {
        return _ease;
    }
    public HFollow spring(float f) {
        _spring = f;
        return this;
    }
    public float spring() {
        return _spring;
    }
    public HFollow goal(HFollowable g) {
        _goal = g;
        return this;
    }
    public HFollowable goal() {
        return _goal;
    }
    public HFollow followMouse() {
        _goal = H.mouse();
        return this;
    }
    public HFollow target(HMovable f) {
        if (f == null) unregister();
        else register();
        _follower = f;
        return this;
    }
    public HMovable target() {
        return _follower;
    }
    public void runBehavior(PApplet app) {
        if (_follower == null || !H.mouse().started()) return;
        _dx = _dx * _spring + (_goal.followableX() - _follower.x()) * _ease;
        _dy = _dy * _spring + (_goal.followableY() - _follower.y()) * _ease;
        _follower.move(_dx, _dy);
    }
    public HFollow register() {
        return (HFollow) super.register();
    }
    public HFollow unregister() {
        return (HFollow) super.unregister();
    }
}
public static class HMagneticField extends HBehavior {
    protected ArrayList < HMagnet > _magnets;
    protected HLinkedHashSet < HDrawable > _targets;
    public HMagneticField() {
        _magnets = new ArrayList < HMagneticField.HMagnet > ();
        _targets = new HLinkedHashSet < HDrawable > ();
    }
    public HMagneticField addMagnet(float sx, float sy, float nx, float ny) {
        HMagnet m = new HMagnet();
        m.southx = sx;
        m.southy = sy;
        m.northx = nx;
        m.northy = ny;
        _magnets.add(m);
        return this;
    }
    public HMagnet magnet(int index) {
        return _magnets.get(index);
    }
    public HMagneticField removeMagnet(int index) {
        _magnets.remove(index);
        return this;
    }
    public HMagneticField addTarget(HDrawable d) {
        if (_targets.size() <= 0) register();
        _targets.add(d);
        return this;
    }
    public HMagneticField removeTarget(HDrawable d) {
        _targets.remove(d);
        if (_targets.size() <= 0) unregister();
        return this;
    }
    public float getRotation(float x, float y) {
        float northRot = 0;
        float southRot = 0;
        int numMagnets = _magnets.size();
        for (int i = 0; i < numMagnets; i++) {
            HMagnet m = _magnets.get(i);
            northRot += HMath.xAxisAngle(x, y, m.northx, m.northy);
            southRot += HMath.xAxisAngle(x, y, m.southx, m.southy);
        }
        return (northRot + southRot) / numMagnets;
    }
    public void runBehavior(PApplet app) {
        for (HIterator < HDrawable > it = _targets.iterator(); it.hasNext();) {
            HDrawable d = it.next();
            d.rotationRad(getRotation(d.x(), d.y()));
        }
    }
    public HMagneticField register() {
        return (HMagneticField) super.register();
    }
    public HMagneticField unregister() {
        return (HMagneticField) super.unregister();
    }
    public static class HMagnet {
        public float southx, southy, northx, northy;
    }
}
public static class HOscillator extends HBehavior {
    protected HDrawable _target;
    protected float _stepDeg, _speed, _min, _max, _freq, _relValue, _origW, _origH;
    protected int _propertyId, _waveform;
    public HOscillator() {
        _speed = 1;
        _min = -1;
        _max = 1;
        _freq = 1;
        _propertyId = HConstants.Y;
        _waveform = HConstants.SINE;
    }
    public HOscillator(HDrawable newTarget) {
        this();
        target(newTarget);
    }
    public HOscillator createCopy() {
        HOscillator osc = new HOscillator().currentStep(_stepDeg).speed(_speed).range(_min, _max).freq(_freq).relativeVal(_relValue).property(_propertyId).waveform(_waveform);
        return osc;
    }
    public HOscillator target(HDrawable newTarget) {
        if (newTarget == null) unregister();
        else register();
        _target = newTarget;
        if (_target != null) {
            _origW = _target.width();
            _origH = _target.height();
        }
        return this;
    }
    public HDrawable target() {
        return _target;
    }
    public HOscillator currentStep(float stepDegrees) {
        _stepDeg = stepDegrees;
        return this;
    }
    public float currentStep() {
        return _stepDeg;
    }
    public HOscillator speed(float spd) {
        _speed = spd;
        return this;
    }
    public float speed() {
        return _speed;
    }
    public HOscillator range(float minimum, float maximum) {
        _min = minimum;
        _max = maximum;
        return this;
    }
    public HOscillator min(float minimum) {
        _min = minimum;
        return this;
    }
    public float min() {
        return _min;
    }
    public HOscillator max(float maximum) {
        _max = maximum;
        return this;
    }
    public float max() {
        return _max;
    }
    public HOscillator freq(float frequency) {
        _freq = frequency;
        return this;
    }
    public float freq() {
        return _freq;
    }
    public HOscillator relativeVal(float relativeValue) {
        _relValue = relativeValue;
        return this;
    }
    public float relativeVal() {
        return _relValue;
    }
    public HOscillator property(int propertyId) {
        _propertyId = propertyId;
        return this;
    }
    public int property() {
        return _propertyId;
    }
    public HOscillator waveform(int form) {
        _waveform = form;
        return this;
    }
    public int waveform() {
        return _waveform;
    }
    public float nextVal() {
        float currentDeg = _stepDeg * _freq;
        float outVal = 0;
        switch (_waveform) {
        case HConstants.SINE:
            outVal = HMath.sineWave(currentDeg);
            break;
        case HConstants.TRIANGLE:
            outVal = HMath.triangleWave(currentDeg);
            break;
        case HConstants.SAW:
            outVal = HMath.sawWave(currentDeg);
            break;
        case HConstants.SQUARE:
            outVal = HMath.squareWave(currentDeg);
            break;
        }
        outVal = H.app().map(outVal, -1, 1, _min, _max) + _relValue;
        _stepDeg += speed();
        return outVal;
    }
    public void runBehavior(PApplet app) {
        if (_target == null) return;
        float val = nextVal();
        switch (_propertyId) {
        case HConstants.WIDTH:
            _target.width(val);
            break;
        case HConstants.HEIGHT:
            _target.height(val);
            break;
        case HConstants.SIZE:
            _target.size(val);
            break;
        case HConstants.ALPHA:
            _target.alpha(H.app().round(val));
            break;
        case HConstants.X:
            _target.x(val);
            break;
        case HConstants.Y:
            _target.y(val);
            break;
        case HConstants.LOCATION:
            _target.loc(val, val);
            break;
        case HConstants.ROTATION:
            _target.rotation(val);
            break;
        case HConstants.DROTATION:
            _target.rotate(val);
            break;
        case HConstants.DX:
            _target.move(val, 0);
            break;
        case HConstants.DY:
            _target.move(0, val);
            break;
        case HConstants.DLOC:
            _target.move(val, val);
            break;
        case HConstants.SCALE:
            _target.size(_origW * val, _origH * val);
            break;
        default:
            break;
        }
    }
    public HOscillator register() {
        return (HOscillator) super.register();
    }
    public HOscillator unregister() {
        return (HOscillator) super.unregister();
    }
}
public static class HRotate extends HBehavior {
    protected HRotatable _target;
    protected float _speedRad;
    public HRotate() {}
    public HRotate(HRotatable newTarget, float dDeg) {
        target(newTarget);
        _speedRad = dDeg * HConstants.D2R;
    }
    public HRotate target(HRotatable r) {
        if (r == null) unregister();
        else register();
        _target = r;
        return this;
    }
    public HRotatable target() {
        return _target;
    }
    public HRotate speed(float dDeg) {
        _speedRad = dDeg * HConstants.D2R;
        return this;
    }
    public float speed() {
        return _speedRad * HConstants.R2D;
    }
    public HRotate speedRad(float dRad) {
        _speedRad = dRad;
        return this;
    }
    public float speedRad() {
        return _speedRad;
    }
    public void runBehavior(PApplet app) {
        float rot = _target.rotationRad() + _speedRad;
        _target.rotationRad(rot);
    }
    public HRotate register() {
        return (HRotate) super.register();
    }
    public HRotate unregister() {
        return (HRotate) super.unregister();
    }
}
public static class HSwarm extends HBehavior implements HMovable, HFollowable {
    protected float _goalX, _goalY, _speed, _turnEase, _twitch;
    protected HLinkedHashSet < HSwarmer > _swarmers;
    public HSwarm() {
        _speed = 1;
        _turnEase = 1;
        _twitch = 16;
        _swarmers = new HLinkedHashSet < HSwarmer > ();
    }
    public HSwarm addTarget(HSwarmer d) {
        if (_swarmers.size() <= 0) register();
        _swarmers.add(d);
        return this;
    }
    public HSwarm removeTarget(HSwarmer d) {
        _swarmers.remove(d);
        if (_swarmers.size() <= 0) unregister();
        return this;
    }
    public HSwarm goal(float x, float y) {
        _goalX = x;
        _goalY = y;
        return this;
    }
    public PVector goal() {
        return new PVector(_goalX, _goalY);
    }
    public HSwarm goalX(float x) {
        _goalX = x;
        return this;
    }
    public float goalX() {
        return _goalX;
    }
    public HSwarm goalY(float y) {
        _goalY = y;
        return this;
    }
    public float goalY() {
        return _goalY;
    }
    public HSwarm speed(float s) {
        _speed = s;
        return this;
    }
    public float speed() {
        return _speed;
    }
    public HSwarm turnEase(float e) {
        _turnEase = e;
        return this;
    }
    public float turnEase() {
        return _turnEase;
    }
    public HSwarm twitch(float deg) {
        _twitch = deg * HConstants.D2R;
        return this;
    }
    public HSwarm twitchRad(float rad) {
        _twitch = rad;
        return this;
    }
    public float twitch() {
        return _twitch * HConstants.R2D;
    }
    public float twitchRad() {
        return _twitch;
    }
    public float x() {
        return _goalX;
    }
    public float y() {
        return _goalY;
    }
    public float followableX() {
        return _goalX;
    }
    public float followableY() {
        return _goalY;
    }
    public HSwarm move(float dx, float dy) {
        _goalX += dx;
        _goalY += dy;
        return this;
    }
    public void runBehavior(PApplet app) {
        int numSwarmers = _swarmers.size();
        HIterator < HSwarmer > it = _swarmers.iterator();
        for (int i = 0; i < numSwarmers; ++i) {
            HSwarmer swarmer = it.next();
            float rot = swarmer.rotationRad();
            float tx = swarmer.x();
            float ty = swarmer.y();
            float tmp = HMath.xAxisAngle(tx, ty, _goalX, _goalY) - rot;
            float dRot = app.atan2(app.sin(tmp), app.cos(tmp)) * _turnEase;
            rot += dRot;
            float noise = app.noise(i * numSwarmers + app.frameCount / 8f);
            rot += app.map(noise, 0, 1, -_twitch, _twitch);
            swarmer.rotationRad(rot);
            swarmer.move(app.cos(rot) * _speed, app.sin(rot) * _speed);
        }
    }
    public HSwarm register() {
        return (HSwarm) super.register();
    }
    public HSwarm unregister() {
        return (HSwarm) super.unregister();
    }
}
public static class HVelocity extends HBehavior {
    protected boolean _autoRegisters;
    protected float _velocityX, _velocityY, _accelX, _accelY;
    protected HMovable _target;
    public HVelocity() {
        _autoRegisters = true;
    }
    public HVelocity(boolean isAutoRegister) {
        _autoRegisters = isAutoRegister;
    }
    public HVelocity autoRegisters(boolean b) {
        _autoRegisters = b;
        return this;
    }
    public boolean autoRegisters() {
        return _autoRegisters;
    }
    public HVelocity target(HMovable t) {
        if (_autoRegisters) {
            if (t == null) unregister();
            else register();
        }
        _target = t;
        return this;
    }
    public HMovable target() {
        return _target;
    }
    public HVelocity velocity(float velocity, float deg) {
        return velocityRad(velocity, deg * HConstants.D2R);
    }
    public HVelocity velocityRad(float velocity, float rad) {
        PApplet app = H.app();
        _velocityX = velocity * app.cos(rad);
        _velocityY = velocity * app.sin(rad);
        return this;
    }
    public HVelocity velocityX(float dx) {
        _velocityX = dx;
        return this;
    }
    public float velocityX() {
        return _velocityX;
    }
    public HVelocity velocityY(float dy) {
        _velocityY = dy;
        return this;
    }
    public float velocityY() {
        return _velocityY;
    }
    public HVelocity launchTo(float goalX, float goalY, float time) {
        if (_target == null) {
            HWarnings.warn("Null Target", "HVelocity.launchTo()", HWarnings.NULL_TARGET);
        } else {
            float numFrames = time * 60 / 1000;
            float nfsq = numFrames * numFrames;
            _velocityX = (goalX - _target.x() - _accelX * nfsq / 2) / numFrames;
            _velocityY = (goalY - _target.y() - _accelY * nfsq / 2) / numFrames;
        }
        return this;
    }
    public HVelocity accel(float acceleration, float deg) {
        return accelRad(acceleration, deg * HConstants.D2R);
    }
    public HVelocity accelRad(float acceleration, float rad) {
        PApplet app = H.app();
        _accelX = acceleration * app.cos(rad);
        _accelY = acceleration * app.sin(rad);
        return this;
    }
    public HVelocity accelX(float ddx) {
        _accelX = ddx;
        return this;
    }
    public float accelX() {
        return _accelX;
    }
    public HVelocity accelY(float ddy) {
        _accelY = ddy;
        return this;
    }
    public float accelY() {
        return _accelY;
    }
    public void runBehavior(PApplet app) {
        _target.move(_velocityX, _velocityY);
        _velocityX += _accelX;
        _velocityY += _accelY;
    }
    public HVelocity register() {
        return (HVelocity) super.register();
    }
    public HVelocity unregister() {
        return (HVelocity) super.unregister();
    }
}
public static interface HIterator < U > {
    public boolean hasNext();
    public U next();
    public void remove();
}
public static class HLinkedHashSet < T > extends HLinkedList < T > {
    protected HashMap < T, HLinkedListNode < T >> nodeMap;
    public HLinkedHashSet() {
        nodeMap = new HashMap < T, HLinkedListNode < T >> ();
    }
    public boolean remove(T content) {
        HLinkedListNode < T > node = nodeMap.get(content);
        if (node == null) return false;
        unregister(content);
        node.popOut();
        --_size;
        return true;
    }
    public boolean add(T content) {
        return contains(content) ? false : super.add(content);
    }
    public boolean push(T content) {
        return contains(content) ? false : super.push(content);
    }
    public boolean insert(T content, int index) {
        return contains(content) ? false : super.insert(content, index);
    }
    public T pull() {
        return unregister(super.pull());
    }
    public T pop() {
        return unregister(super.pop());
    }
    public T removeAt(int index) {
        return unregister(super.removeAt(index));
    }
    public void removeAll() {
        while (_size > 0) pop();
    }
    public boolean contains(T obj) {
        return nodeMap.get(obj) != null;
    }
    protected HLinkedListNode < T > register(T obj) {
        HLinkedListNode < T > node = new HLinkedListNode < T > (obj);
        nodeMap.put(obj, node);
        return node;
    }
    protected T unregister(T obj) {
        nodeMap.remove(obj);
        return obj;
    }
}
public static class HLinkedList < T > {
    protected HLinkedListNode < T > _firstSentinel, _lastSentinel;
    protected int _size;
    public HLinkedList() {
        _firstSentinel = new HLinkedListNode < T > (null);
        _lastSentinel = new HLinkedListNode < T > (null);
        _lastSentinel.putAfter(_firstSentinel);
    }
    public T first() {
        return _firstSentinel._next._content;
    }
    public T last() {
        return _lastSentinel._prev._content;
    }
    public T get(int index) {
        HLinkedListNode < T > n = nodeAt(index);
        return (n == null) ? null : n._content;
    }
    public boolean push(T content) {
        if (content == null) return false;
        register(content).putAfter(_firstSentinel);
        ++_size;
        return true;
    }
    public boolean add(T content) {
        if (content == null) return false;
        register(content).putBefore(_lastSentinel);
        ++_size;
        return true;
    }
    public boolean insert(T content, int index) {
        if (content == null) return false;
        HLinkedListNode < T > n = (index == _size) ? _lastSentinel : nodeAt(index);
        if (n == null) return false;
        register(content).putBefore(n);
        ++_size;
        return true;
    }
    public T pop() {
        HLinkedListNode < T > firstNode = _firstSentinel._next;
        if (firstNode._content != null) {
            firstNode.popOut();
            --_size;
        }
        return firstNode._content;
    }
    public T pull() {
        HLinkedListNode < T > lastNode = _lastSentinel._prev;
        if (lastNode._content != null) {
            lastNode.popOut();
            --_size;
        }
        return lastNode._content;
    }
    public T removeAt(int index) {
        HLinkedListNode < T > n = nodeAt(index);
        if (n == null) return null;
        n.popOut();
        --_size;
        return n._content;
    }
    public void removeAll() {
        _lastSentinel.putAfter(_firstSentinel);
        _size = 0;
    }
    public int size() {
        return _size;
    }
    public boolean inRange(int index) {
        return (0 <= index) && (index < _size);
    }
    public HLinkedListIterator < T > iterator() {
        return new HLinkedListIterator < T > (this);
    }
    protected HLinkedListNode < T > nodeAt(int i) {
        int ri;
        if (i < 0) {
            ri = -i;
            i += _size;
        } else {
            ri = _size - i;
        } if (!inRange(i)) {
            HWarnings.warn("Out of Range: " + i, "HLinkedList.nodeAt()", null);
            return null;
        }
        HLinkedListNode < T > node;
        if (ri < i) {
            node = _lastSentinel._prev;
            while (--ri > 0) node = node._prev;
        } else {
            node = _firstSentinel._next;
            while (i-- > 0) node = node._next;
        }
        return node;
    }
    protected HLinkedListNode < T > register(T obj) {
        return new HLinkedListNode < T > (obj);
    }
    public static class HLinkedListNode < U > extends HNode < HLinkedListNode < U >> {
        private U _content;
        public HLinkedListNode(U nodeContent) {
            _content = nodeContent;
        }
        public U content() {
            return _content;
        }
    }
    public static class HLinkedListIterator < U > implements HIterator < U > {
        private HLinkedList < U > list;
        private HLinkedListNode < U > n1, n2;
        public HLinkedListIterator(HLinkedList < U > parent) {
            list = parent;
            n1 = list._firstSentinel._next;
            if (n1 != null) n2 = n1._next;
        }
        public boolean hasNext() {
            return (n1._content != null);
        }
        public U next() {
            U content = n1._content;
            n1 = n2;
            if (n2 != null) n2 = n2._next;
            return content;
        }
        public void remove() {
            if (n1._content != null) {
                n1.popOut();
                --list._size;
            }
        }
    }
}
public static abstract class HNode < T extends HNode < T >> {
    protected T _prev, _next;
    public T prev() {
        return _prev;
    }
    public T next() {
        return _next;
    }
    public boolean poppedOut() {
        return (_prev == null) && (_next == null);
    }
    public void popOut() {
        if (_prev != null) _prev._next = _next;
        if (_next != null) _next._prev = _prev;
        _prev = _next = null;
    }
    public void putBefore(T dest) {
        if (dest == null || dest.equals(this)) return;
        if (!poppedOut()) popOut();
        T p = dest._prev;
        if (p != null) p._next = (T) this;
        _prev = p;
        _next = dest;
        dest._prev = (T) this;
    }
    public void putAfter(T dest) {
        if (dest == null || dest.equals(this)) return;
        if (!poppedOut()) popOut();
        T n = dest.next();
        dest._next = (T) this;
        _prev = dest;
        _next = n;
        if (n != null) n._prev = (T) this;
    }
    public void replaceNode(T dest) {
        if (dest == null || dest.equals(this)) return;
        if (!poppedOut()) popOut();
        T p = dest._prev;
        T n = dest._next;
        dest._prev = dest._next = null;
        _prev = p;
        _next = n;
    }
    public void swapLeft() {
        if (_prev == null) return;
        T pairPrev = _prev._prev;
        T pairNext = _next;
        _next = _prev;
        _prev._prev = (T) this;
        _prev._next = pairNext;
        if (pairNext != null) pairNext._prev = _prev;
        _prev = pairPrev;
        if (pairPrev != null) pairPrev._next = (T) this;
    }
    public void swapRight() {
        if (_next == null) return;
        T pairPrev = _prev;
        T pairNext = _next._next;
        _next._next = (T) this;
        _prev = _next;
        _next._prev = pairPrev;
        if (pairPrev != null) pairPrev._next = _next;
        _next = pairNext;
        if (pairNext != null) pairNext._prev = (T) this;
    }
}
public static class HColorField implements HColorist {
    protected ArrayList < HColorPoint > _colorPoints;
    protected float _maxDist;
    protected boolean _appliesFill, _appliesStroke, _appliesAlpha;
    public HColorField() {
        this(H.app().width, H.app().height);
    }
    public HColorField(float xBound, float yBound) {
        this(H.app().sqrt(xBound * xBound + yBound * yBound));
    }
    public HColorField(float maximumDistance) {
        _colorPoints = new ArrayList < HColorField.HColorPoint > ();
        _maxDist = maximumDistance;
        fillAndStroke();
    }
    public HColorField addPoint(PVector loc, int clr, float radius) {
        return addPoint(loc.x, loc.y, clr, radius);
    }
    public HColorField addPoint(float x, float y, int clr, float radius) {
        HColorPoint pt = new HColorPoint();
        pt.x = x;
        pt.y = y;
        pt.radius = radius;
        pt.clr = clr;
        _colorPoints.add(pt);
        return this;
    }
    public int getColor(float x, float y, int baseColor) {
        PApplet app = H.app();
        int[] baseClrs = HColorUtil.explode(baseColor);
        int[] maxClrs = new int[4];
        int initJ;
        if (_appliesAlpha) {
            initJ = 0;
        } else {
            initJ = 1;
            maxClrs[0] = baseClrs[0];
        }
        for (int i = 0; i < _colorPoints.size(); ++i) {
            HColorPoint pt = _colorPoints.get(i);
            int[] ptClrs = HColorUtil.explode(pt.clr);
            float distLimit = _maxDist * pt.radius;
            float dist = app.dist(x, y, pt.x, pt.y);
            if (dist > distLimit) dist = distLimit;
            for (int j = initJ; j < 4; ++j) {
                int newClrVal = app.round(app.map(dist, 0, distLimit, ptClrs[j], baseClrs[j]));
                if (newClrVal > maxClrs[j]) maxClrs[j] = newClrVal;
            }
        }
        return HColorUtil.merge(maxClrs[0], maxClrs[1], maxClrs[2], maxClrs[3]);
    }
    public HColorField appliesAlpha(boolean b) {
        _appliesAlpha = b;
        return this;
    }
    public boolean appliesAlpha() {
        return _appliesAlpha;
    }
    public HColorField fillOnly() {
        _appliesFill = true;
        _appliesStroke = false;
        return this;
    }
    public HColorField strokeOnly() {
        _appliesFill = false;
        _appliesStroke = true;
        return this;
    }
    public HColorField fillAndStroke() {
        _appliesFill = _appliesStroke = true;
        return this;
    }
    public boolean appliesFill() {
        return _appliesFill;
    }
    public boolean appliesStroke() {
        return _appliesStroke;
    }
    public HDrawable applyColor(HDrawable drawable) {
        float x = drawable.x();
        float y = drawable.y();
        if (_appliesFill) {
            int baseFill = drawable.fill();
            drawable.fill(getColor(x, y, baseFill));
        }
        if (_appliesStroke) {
            int baseStroke = drawable.stroke();
            drawable.stroke(getColor(x, y, baseStroke));
        }
        return drawable;
    }
    public static class HColorPoint {
        public float x, y, radius;
        public int clr;
    }
}
public static interface HColorist {
    public HColorist fillOnly();
    public HColorist strokeOnly();
    public HColorist fillAndStroke();
    public boolean appliesFill();
    public boolean appliesStroke();
    public HDrawable applyColor(HDrawable drawable);
}
public static class HColorPool implements HColorist {
    protected ArrayList < Integer > _colorList;
    protected boolean _fillFlag, _strokeFlag;
    public HColorPool(int...colors) {
        _colorList = new ArrayList < Integer > ();
        for (int i = 0; i < colors.length; ++i) add(colors[i]);
        fillAndStroke();
    }
    public HColorPool createCopy() {
        HColorPool copy = new HColorPool();
        copy._fillFlag = _fillFlag;
        copy._strokeFlag = _strokeFlag;
        for (int i = 0; i < _colorList.size(); ++i) {
            int clr = _colorList.get(i);
            copy._colorList.add(clr);
        }
        return copy;
    }
    public int size() {
        return _colorList.size();
    }
    public HColorPool add(int clr) {
        _colorList.add(clr);
        return this;
    }
    public HColorPool add(int clr, int freq) {
        while (freq-- > 0) _colorList.add(clr);
        return this;
    }
    public int getColor() {
        if (_colorList.size() <= 0) return 0;
        PApplet app = H.app();
        int index = app.round(app.random(_colorList.size() - 1));
        return _colorList.get(index);
    }
    public int getColor(int seed) {
        HMath.tempSeed(seed);
        int clr = getColor();
        HMath.removeTempSeed();
        return clr;
    }
    public HColorPool fillOnly() {
        _fillFlag = true;
        _strokeFlag = false;
        return this;
    }
    public HColorPool strokeOnly() {
        _fillFlag = false;
        _strokeFlag = true;
        return this;
    }
    public HColorPool fillAndStroke() {
        _fillFlag = _strokeFlag = true;
        return this;
    }
    public boolean appliesFill() {
        return _fillFlag;
    }
    public boolean appliesStroke() {
        return _strokeFlag;
    }
    public HDrawable applyColor(HDrawable drawable) {
        if (_fillFlag) drawable.fill(getColor());
        if (_strokeFlag) drawable.stroke(getColor());
        return drawable;
    }
}
public static class HColorTransform implements HColorist {
    public float _percA, _percR, _percG, _percB;
    public int _offsetA, _offsetR, _offsetG, _offsetB;
    protected boolean fillFlag, strokeFlag;
    public HColorTransform() {
        _percA = _percR = _percG = _percB = 1;
        fillAndStroke();
    }
    public HColorTransform offset(int off) {
        _offsetA = _offsetR = _offsetG = _offsetB = off;
        return this;
    }
    public HColorTransform offset(int r, int g, int b, int a) {
        _offsetA = a;
        _offsetR = r;
        _offsetG = g;
        _offsetB = b;
        return this;
    }
    public HColorTransform offsetA(int a) {
        _offsetA = a;
        return this;
    }
    public int offsetA() {
        return _offsetA;
    }
    public HColorTransform offsetR(int r) {
        _offsetR = r;
        return this;
    }
    public int offsetR() {
        return _offsetR;
    }
    public HColorTransform offsetG(int g) {
        _offsetG = g;
        return this;
    }
    public int offsetG() {
        return _offsetG;
    }
    public HColorTransform offsetB(int b) {
        _offsetB = b;
        return this;
    }
    public int offsetB() {
        return _offsetB;
    }
    public HColorTransform perc(float percentage) {
        _percA = _percR = _percG = _percB = percentage;
        return this;
    }
    public HColorTransform perc(int r, int g, int b, int a) {
        _percA = a;
        _percR = r;
        _percG = g;
        _percB = b;
        return this;
    }
    public HColorTransform percA(float a) {
        _percA = a;
        return this;
    }
    public float percA() {
        return _percA;
    }
    public HColorTransform percR(float r) {
        _percR = r;
        return this;
    }
    public float percR() {
        return _percR;
    }
    public HColorTransform percG(float g) {
        _percG = g;
        return this;
    }
    public float percG() {
        return _percG;
    }
    public HColorTransform percB(float b) {
        _percB = b;
        return this;
    }
    public float percB() {
        return _percB;
    }
    public HColorTransform mergeWith(HColorTransform other) {
        if (other != null) {
            _percA *= other._percA;
            _percR *= other._percR;
            _percG *= other._percG;
            _percB *= other._percB;
            _offsetA += other._offsetA;
            _offsetR += other._offsetR;
            _offsetG += other._offsetG;
            _offsetB += other._offsetB;
        }
        return this;
    }
    public HColorTransform createCopy() {
        HColorTransform copy = new HColorTransform();
        copy._percA = _percA;
        copy._percR = _percR;
        copy._percG = _percG;
        copy._percB = _percB;
        copy._offsetA = _offsetA;
        copy._offsetR = _offsetR;
        copy._offsetG = _offsetG;
        copy._offsetB = _offsetB;
        return copy;
    }
    public HColorTransform createNew(HColorTransform other) {
        return createCopy().mergeWith(other);
    }
    public int getColor(int origColor) {
        PApplet app = H.app();
        int[] clrs = HColorUtil.explode(origColor);
        clrs[0] = app.round(clrs[0] * _percA) + _offsetA;
        clrs[1] = app.round(clrs[1] * _percR) + _offsetR;
        clrs[2] = app.round(clrs[2] * _percG) + _offsetG;
        clrs[3] = app.round(clrs[3] * _percB) + _offsetB;
        return HColorUtil.merge(clrs[0], clrs[1], clrs[2], clrs[3]);
    }
    public HColorTransform fillOnly() {
        fillFlag = true;
        strokeFlag = false;
        return this;
    }
    public HColorTransform strokeOnly() {
        fillFlag = false;
        strokeFlag = true;
        return this;
    }
    public HColorTransform fillAndStroke() {
        fillFlag = strokeFlag = true;
        return this;
    }
    public boolean appliesFill() {
        return fillFlag;
    }
    public boolean appliesStroke() {
        return strokeFlag;
    }
    public HDrawable applyColor(HDrawable drawable) {
        if (fillFlag) {
            int fill = drawable.fill();
            drawable.fill(getColor(fill));
        }
        if (strokeFlag) {
            int stroke = drawable.stroke();
            drawable.stroke(getColor(stroke));
        }
        return drawable;
    }
}
public static class HPixelColorist implements HColorist {
    protected PImage img;
    protected boolean fillFlag, strokeFlag;
    public HPixelColorist() {
        fillAndStroke();
    }
    public HPixelColorist(Object imgArg) {
        this();
        setImage(imgArg);
    }
    public HPixelColorist setImage(Object imgArg) {
        if (imgArg instanceof PImage) {
            img = (PImage) imgArg;
        } else if (imgArg instanceof HImage) {
            img = ((HImage) imgArg).image();
        } else if (imgArg instanceof String) {
            img = H.app().loadImage((String) imgArg);
        } else if (imgArg == null) {
            img = null;
        }
        return this;
    }
    public PImage getImage() {
        return img;
    }
    public int getColor(float x, float y) {
        if (img == null) return 0;
        PApplet app = H.app();
        return img.get(app.round(x), app.round(y));
    }
    public HPixelColorist fillOnly() {
        fillFlag = true;
        strokeFlag = false;
        return this;
    }
    public HPixelColorist strokeOnly() {
        fillFlag = false;
        strokeFlag = true;
        return this;
    }
    public HPixelColorist fillAndStroke() {
        fillFlag = strokeFlag = true;
        return this;
    }
    public boolean appliesFill() {
        return fillFlag;
    }
    public boolean appliesStroke() {
        return strokeFlag;
    }
    public HDrawable applyColor(HDrawable drawable) {
        int clr = getColor(drawable.x(), drawable.y());
        if (fillFlag) drawable.fill(clr);
        if (strokeFlag) drawable.stroke(clr);
        return drawable;
    }
}
public static abstract class HDrawable extends HNode < HDrawable > implements HSwarmer, HFollowable, HHittable {
    protected HDrawable _parent, _firstChild, _lastChild;
    protected HBundle _extras;
    protected float _x, _y, _anchorPercX, _anchorPercY, _width, _height, _rotationRad, _strokeWeight, _alpha;
    protected int _numChildren, _fill, _stroke, _strokeCap, _strokeJoin;
    public HDrawable() {
        _alpha = 1;
        _fill = HConstants.DEFAULT_FILL;
        _stroke = HConstants.DEFAULT_STROKE;
        _strokeCap = PConstants.ROUND;
        _strokeJoin = PConstants.MITER;
        _strokeWeight = 1;
        _width = HConstants.DEFAULT_WIDTH;
        _height = HConstants.DEFAULT_HEIGHT;
    }
    public void copyPropertiesFrom(HDrawable other) {
        _x = other._x;
        _y = other._y;
        _anchorPercX = other._anchorPercX;
        _anchorPercY = other._anchorPercY;
        _width = other._width;
        _height = other._height;
        _rotationRad = other._rotationRad;
        _alpha = other._alpha;
        _strokeWeight = other._strokeWeight;
        _fill = other._fill;
        _stroke = other._stroke;
        _strokeCap = other._strokeCap;
        _strokeJoin = other._strokeJoin;
    }
    public abstract HDrawable createCopy();
    protected boolean invalidDest(HDrawable dest, String warnLoc) {
        String warnType;
        String warnMsg;
        if (dest == null) {
            warnType = "Null Destination";
            warnMsg = HWarnings.NULL_ARGUMENT;
        } else if (dest._parent == null) {
            warnType = "Invalid Destination";
            warnMsg = HWarnings.INVALID_DEST;
        } else if (dest._parent.equals(this)) {
            warnType = "Recursive Child";
            warnMsg = HWarnings.CHILDCEPTION;
        } else if (dest.equals(this)) {
            warnType = "Invalid Destination";
            warnMsg = HWarnings.DESTCEPTION;
        } else return false;
        HWarnings.warn(warnType, warnLoc, warnMsg);
        return true;
    }
    public boolean poppedOut() {
        return (_parent == null);
    }
    public void popOut() {
        if (_parent == null) return;
        if (_prev == null) _parent._firstChild = _next;
        if (_next == null) _parent._lastChild = _prev;
        --_parent._numChildren;
        _parent = null;
        super.popOut();
    }
    public void putBefore(HDrawable dest) {
        if (invalidDest(dest, "HDrawable.putBefore()")) return;
        popOut();
        super.putBefore(dest);
        _parent = dest._parent;
        if (_prev == null) _parent._firstChild = this;
        ++_parent._numChildren;
    }
    public void putAfter(HDrawable dest) {
        if (invalidDest(dest, "HDrawable.putAfter()")) return;
        popOut();
        super.putAfter(dest);
        _parent = dest._parent;
        if (_next == null) _parent._lastChild = this;
        ++_parent._numChildren;
    }
    public void swapLeft() {
        boolean isLast = (_next == null);
        super.swapLeft();
        if (_prev == null) _parent._firstChild = this;
        if (_next != null && isLast) _parent._lastChild = _next;
    }
    public void swapRight() {
        boolean isFirst = (_prev == null);
        super.swapRight();
        if (_next == null) _parent._lastChild = this;
        if (_prev != null && isFirst) _parent._firstChild = _prev;
    }
    public void replaceNode(HDrawable dest) {
        if (invalidDest(dest, "HDrawable.replaceNode()")) return;
        super.replaceNode(dest);
        _parent = dest._parent;
        dest._parent = null;
        if (_prev == null) _parent._firstChild = this;
        if (_next == null) _parent._lastChild = this;
    }
    public HDrawable parent() {
        return _parent;
    }
    public boolean parentOf(HDrawable d) {
        return (d != null) && (d._parent != null) && (d._parent.equals(this));
    }
    public int numChildren() {
        return _numChildren;
    }
    public HDrawable add(HDrawable child) {
        if (child == null) {
            HWarnings.warn("An Empty Child", "HDrawable.add()", HWarnings.NULL_ARGUMENT);
        } else if (!parentOf(child)) {
            if (_lastChild == null) {
                _firstChild = _lastChild = child;
                child.popOut();
                child._parent = this;
                ++_numChildren;
            } else child.putAfter(_lastChild);
        }
        return child;
    }
    public HDrawable remove(HDrawable child) {
        if (parentOf(child)) child.popOut();
        else HWarnings.warn("Not a Child", "HDrawable.remove()", null);
        return child;
    }
    public HDrawableIterator iterator() {
        return new HDrawableIterator(this);
    }
    public HDrawable loc(float newX, float newY) {
        _x = newX;
        _y = newY;
        return this;
    }
    public HDrawable loc(PVector pt) {
        _x = pt.x;
        _y = pt.y;
        return this;
    }
    public PVector loc() {
        return new PVector(_x, _y);
    }
    public HDrawable x(float newX) {
        _x = newX;
        return this;
    }
    public float x() {
        return _x;
    }
    public HDrawable y(float newY) {
        _y = newY;
        return this;
    }
    public float y() {
        return _y;
    }
    public HDrawable move(float dx, float dy) {
        _x += dx;
        _y += dy;
        return this;
    }
    public HDrawable locAt(int where) {
        if (_parent != null) {
            if (HMath.hasBits(where, HConstants.CENTER_X)) _x = _parent.width() / 2 - _parent.anchorX();
            else if (HMath.hasBits(where, HConstants.LEFT)) _x = -_parent.anchorX();
            else if (HMath.hasBits(where, HConstants.RIGHT)) _x = _parent.width() - _parent.anchorX();
            if (HMath.hasBits(where, HConstants.CENTER_Y)) _y = _parent.height() / 2 - _parent.anchorY();
            else if (HMath.hasBits(where, HConstants.TOP)) _y = -_parent.anchorY();
            else if (HMath.hasBits(where, HConstants.BOTTOM)) _y = _parent.height() - _parent.anchorY();
        }
        return this;
    }
    public HDrawable anchor(float pxX, float pxY) {
        if (_height == 0 || _width == 0) {
            HWarnings.warn("Division by 0", "HDrawable.anchor()", HWarnings.ANCHORPX_ERR);
        } else {
            _anchorPercX = pxX / _width;
            _anchorPercY = pxY / _height;
        }
        return this;
    }
    public HDrawable anchor(PVector pt) {
        return anchor(pt.x, pt.y);
    }
    public PVector anchor() {
        return new PVector(anchorX(), anchorY());
    }
    public HDrawable anchorX(float pxX) {
        if (_width == 0) {
            HWarnings.warn("Division by 0", "HDrawable.anchorX()", HWarnings.ANCHORPX_ERR);
        } else {
            _anchorPercX = pxX / _width;
        }
        return this;
    }
    public float anchorX() {
        return _width * _anchorPercX;
    }
    public HDrawable anchorY(float pxY) {
        if (_height == 0) {
            HWarnings.warn("Division by 0", "HDrawable.anchorY()", HWarnings.ANCHORPX_ERR);
        } else {
            _anchorPercY = pxY / _height;
        }
        return this;
    }
    public float anchorY() {
        return _height * _anchorPercY;
    }
    public HDrawable anchorPerc(float percX, float percY) {
        _anchorPercX = percX;
        _anchorPercY = percY;
        return this;
    }
    public PVector anchorPerc() {
        return new PVector(_anchorPercX, _anchorPercY);
    }
    public HDrawable anchorPercX(float percX) {
        _anchorPercX = percX;
        return this;
    }
    public float anchorPercX() {
        return _anchorPercX;
    }
    public HDrawable anchorPercY(float percY) {
        _anchorPercY = percY;
        return this;
    }
    public float anchorPercY() {
        return _anchorPercY;
    }
    public HDrawable anchorAt(int where) {
        if (HMath.hasBits(where, HConstants.CENTER_X)) _anchorPercX = 0.5f;
        else if (HMath.hasBits(where, HConstants.LEFT)) _anchorPercX = 0;
        else if (HMath.hasBits(where, HConstants.RIGHT)) _anchorPercX = 1;
        if (HMath.hasBits(where, HConstants.CENTER_Y)) _anchorPercY = 0.5f;
        else if (HMath.hasBits(where, HConstants.TOP)) _anchorPercY = 0;
        else if (HMath.hasBits(where, HConstants.BOTTOM)) _anchorPercY = 1;
        return this;
    }
    public HDrawable size(float w, float h) {
        width(w);
        height(h);
        return this;
    }
    public HDrawable size(float s) {
        size(s, s);
        return this;
    }
    public PVector size() {
        return new PVector(_width, _height);
    }
    public HDrawable width(float w) {
        _width = w;
        return this;
    }
    public float width() {
        return _width;
    }
    public HDrawable height(float h) {
        _height = h;
        return this;
    }
    public float height() {
        return _height;
    }
    public HDrawable scale(float s) {
        size(_width * s, _height * s);
        return this;
    }
    public HDrawable scale(float sw, float sh) {
        size(_width * sw, _height * sh);
        return this;
    }
    public PVector boundingSize() {
        PApplet app = H.app();
        float cosVal = app.cos(_rotationRad);
        float sinVal = app.sin(_rotationRad);
        float drawX = -anchorX();
        float drawY = -anchorY();
        float x1 = drawX;
        float x2 = _width + drawX;
        float y1 = drawY;
        float y2 = _height + drawY;
        float[] xCoords = new float[4];
        float[] yCoords = new float[4];
        xCoords[0] = x1 * cosVal + y1 * sinVal;
        yCoords[0] = x1 * sinVal + y1 * cosVal;
        xCoords[1] = x2 * cosVal + y1 * sinVal;
        yCoords[1] = x2 * sinVal + y1 * cosVal;
        xCoords[2] = x1 * cosVal + y2 * sinVal;
        yCoords[2] = x1 * sinVal + y2 * cosVal;
        xCoords[3] = x2 * cosVal + y2 * sinVal;
        yCoords[3] = x2 * sinVal + y2 * cosVal;
        float minX = xCoords[3];
        float maxX = minX;
        float minY = yCoords[3];
        float maxY = maxX;
        for (int i = 0; i < 3; ++i) {
            float x = xCoords[i];
            float y = yCoords[i];
            if (x < minX) minX = x;
            else if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            else if (y > maxY) maxY = y;
        }
        return new PVector(maxX - minX, maxY - minY);
    }
    public HDrawable fill(int clr) {
        if (0 <= clr && clr <= 255) clr |= clr << 8 | clr << 16 | 0xFF000000;
        _fill = clr;
        return this;
    }
    public HDrawable fill(int clr, int alpha) {
        if (0 <= clr && clr <= 255) clr |= clr << 8 | clr << 16;
        _fill = HColorUtil.setAlpha(clr, alpha);
        return this;
    }
    public HDrawable fill(int r, int g, int b) {
        _fill = HColorUtil.merge(255, r, g, b);
        return this;
    }
    public HDrawable fill(int r, int g, int b, int a) {
        _fill = HColorUtil.merge(a, r, g, b);
        return this;
    }
    public int fill() {
        return _fill;
    }
    public HDrawable noFill() {
        return fill(HConstants.CLEAR);
    }
    public HDrawable stroke(int clr) {
        if (0 <= clr && clr <= 255) clr |= clr << 8 | clr << 16 | 0xFF000000;
        _stroke = clr;
        return this;
    }
    public HDrawable stroke(int clr, int alpha) {
        if (0 <= clr && clr <= 255) clr |= clr << 8 | clr << 16;
        _stroke = HColorUtil.setAlpha(clr, alpha);
        return this;
    }
    public HDrawable stroke(int r, int g, int b) {
        _stroke = HColorUtil.merge(255, r, g, b);
        return this;
    }
    public HDrawable stroke(int r, int g, int b, int a) {
        _stroke = HColorUtil.merge(a, r, g, b);
        return this;
    }
    public int stroke() {
        return _stroke;
    }
    public HDrawable noStroke() {
        return stroke(HConstants.CLEAR);
    }
    public HDrawable strokeCap(int type) {
        _strokeCap = type;
        return this;
    }
    public int strokeCap() {
        return _strokeCap;
    }
    public HDrawable strokeJoin(int type) {
        _strokeJoin = type;
        return this;
    }
    public int strokeJoin() {
        return _strokeJoin;
    }
    public HDrawable strokeWeight(float f) {
        _strokeWeight = f;
        return this;
    }
    public float strokeWeight() {
        return _strokeWeight;
    }
    public HDrawable rotation(float deg) {
        _rotationRad = deg * HConstants.D2R;
        return this;
    }
    public float rotation() {
        return _rotationRad * HConstants.R2D;
    }
    public HDrawable rotationRad(float rad) {
        _rotationRad = rad;
        return this;
    }
    public float rotationRad() {
        return _rotationRad;
    }
    public HDrawable rotate(float deg) {
        _rotationRad += deg * HConstants.D2R;
        return this;
    }
    public HDrawable rotateRad(float rad) {
        _rotationRad += rad;
        return this;
    }
    public HDrawable alpha(int a) {
        return alphaPerc(a / 255f);
    }
    public int alpha() {
        return H.app().round(alphaPerc() * 255);
    }
    public HDrawable alphaPerc(float aPerc) {
        _alpha = (aPerc < 0) ? 0 : (aPerc > 1) ? 1 : aPerc;
        return this;
    }
    public float alphaPerc() {
        return (_alpha < 0) ? 0 : _alpha;
    }
    public HDrawable visibility(boolean v) {
        if (v && _alpha == 0) {
            _alpha = 1;
        } else if (v == _alpha < 0) {
            _alpha = -_alpha;
        }
        return this;
    }
    public boolean visibility() {
        return _alpha > 0;
    }
    public HDrawable show() {
        return visibility(true);
    }
    public HDrawable hide() {
        return visibility(false);
    }
    public HDrawable alphaShift(int da) {
        return alphaShiftPerc(da / 255f);
    }
    public HDrawable alphaShiftPerc(float daPerc) {
        return alphaPerc(_alpha + daPerc);
    }
    public float followableX() {
        return _x;
    }
    public float followableY() {
        return _y;
    }
    public HDrawable extras(HBundle b) {
        _extras = b;
        return this;
    }
    public HBundle extras() {
        return _extras;
    }
    public boolean contains(float absX, float absY) {
        float[] rel = HMath.relLocArr(this, absX, absY);
        rel[0] += anchorX();
        rel[1] += anchorY();
        return containsRel(rel[0], rel[1]);
    }
    public boolean containsRel(float relX, float relY) {
        return (0 <= relX) && (relX <= width()) && (0 <= relY) && (relY <= height());
    }
    protected void applyStyle(PApplet app, float currAlphaPerc) {
        float faPerc = currAlphaPerc * (_fill >>> 24);
        app.fill(_fill | 0xFF000000, app.round(faPerc));
        if (_strokeWeight > 0) {
            float saPerc = currAlphaPerc * (_stroke >>> 24);
            app.stroke(_stroke | 0xFF000000, app.round(saPerc));
            app.strokeWeight(_strokeWeight);
            app.strokeCap(_strokeCap);
            app.strokeJoin(_strokeJoin);
        } else app.noStroke();
    }
    public void paintAll(PApplet app, float currAlphaPerc) {
        if (_alpha <= 0 || _width < 0 || _height < 0) return;
        app.pushMatrix();
        app.translate(_x, _y);
        app.rotate(_rotationRad);
        currAlphaPerc *= _alpha;
        draw(app, -anchorX(), -anchorY(), currAlphaPerc);
        HDrawable child = _firstChild;
        while (child != null) {
            child.paintAll(app, currAlphaPerc);
            child = child._next;
        }
        app.popMatrix();
    }
    public abstract void draw(PApplet app, float drawX, float drawY, float currAlphaPerc);
    public static class HDrawableIterator implements HIterator < HDrawable > {
        private HDrawable parent, d1, d2;
        public HDrawableIterator(HDrawable parentDrawable) {
            parent = parentDrawable;
            d1 = parent._firstChild;
            if (d1 != null) d2 = d1._next;
        }
        public boolean hasNext() {
            return (d1 != null);
        }
        public HDrawable next() {
            HDrawable nxt = d1;
            d1 = d2;
            if (d2 != null) d2 = d2._next;
            return nxt;
        }
        public void remove() {
            if (d1 != null) d1.popOut();
        }
    }
}
public static class HEllipse extends HDrawable {
    protected int _mode;
    protected float _startRad, _endRad;
    public HEllipse() {
        _mode = PConstants.PIE;
    }
    public HEllipse(float ellipseRadius) {
        this();
        radius(ellipseRadius);
    }
    public HEllipse(float radiusX, float radiusY) {
        radius(radiusX, radiusY);
    }
    public HEllipse createCopy() {
        HEllipse copy = new HEllipse();
        copy.copyPropertiesFrom(this);
        return copy;
    }
    public HEllipse radius(float r) {
        size(r * 2);
        return this;
    }
    public HEllipse radius(float radiusX, float radiusY) {
        size(radiusX * 2, radiusY * 2);
        return this;
    }
    public HEllipse radiusX(float radiusX) {
        width(radiusX * 2);
        return this;
    }
    public float radiusX() {
        return _width / 2;
    }
    public HEllipse radiusY(float radiusY) {
        height(radiusY * 2);
        return this;
    }
    public float radiusY() {
        return _height / 2;
    }
    public boolean isCircle() {
        return _width == _height;
    }
    public HEllipse mode(int t) {
        _mode = t;
        return this;
    }
    public float mode() {
        return _mode;
    }
    public HEllipse start(float deg) {
        return startRad(deg * H.D2R);
    }
    public float start() {
        return _startRad * H.R2D;
    }
    public HEllipse startRad(float rad) {
        _startRad = rad;
        return this;
    }
    public float startRad() {
        return _startRad;
    }
    public HEllipse end(float deg) {
        return endRad(deg * H.D2R);
    }
    public float end() {
        return _endRad * H.R2D;
    }
    public HEllipse endRad(float rad) {
        _endRad = rad;
        return this;
    }
    public float endRad() {
        return _endRad;
    }
    public boolean containsRel(float relX, float relY) {
        float cx = _width / 2;
        float cy = _height / 2;
        float dcx = relX - cx;
        float dcy = relY - cy;
        boolean b = ((dcx * dcx) / (cx * cx) + (dcy * dcy) / (cy * cy) <= 1);
        if (_startRad == _endRad) {
            return b;
        }
        float f = H.app().atan2(dcy, dcx);
        switch (_mode) {
        case PConstants.CHORD:
        case PConstants.OPEN:
            return b;
        default:
            return b && _startRad <= f && f <= _endRad;
        }
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        applyStyle(app, currAlphaPerc);
        drawX += _width / 2;
        drawY += _height / 2;
        if (_startRad == _endRad) {
            app.ellipse(drawX, drawY, _width, _height);
        } else {
            app.arc(drawX, drawY, _width, _height, _startRad, _endRad, _mode);
        }
    }
}
public static class HGroup extends HDrawable {
    public HGroup createCopy() {
        HGroup copy = new HGroup();
        copy.copyPropertiesFrom(this);
        return copy;
    }
    public void paintAll(PApplet app, float currAlphaPerc) {
        if (_alpha <= 0 || _width <= 0 || _height <= 0) return;
        app.pushMatrix();
        app.translate(_x, _y);
        app.rotate(_rotationRad);
        currAlphaPerc *= _alpha;
        HDrawable child = _firstChild;
        while (child != null) {
            child.paintAll(app, currAlphaPerc);
            child = child.next();
        }
        app.popMatrix();
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {}
}
public static class HImage extends HDrawable {
    protected PImage _image;
    public HImage() {
        this(null);
    }
    public HImage(Object imgArg) {
        image(imgArg);
    }
    public HImage createCopy() {
        HImage copy = new HImage(_image);
        copy.copyPropertiesFrom(this);
        return copy;
    }
    public HImage resetSize() {
        if (_image == null) size(0f, 0f);
        else size(_image.width, _image.height);
        return this;
    }
    public HImage image(Object imgArg) {
        if (imgArg instanceof PImage) {
            _image = (PImage) imgArg;
        } else if (imgArg instanceof String) {
            _image = H.app().loadImage((String) imgArg);
        } else if (imgArg instanceof HImage) {
            _image = ((HImage) imgArg)._image;
        } else if (imgArg == null) {
            _image = null;
        }
        return resetSize();
    }
    public PImage image() {
        return _image;
    }
    public HImage tint(int clr) {
        fill(clr);
        return this;
    }
    public HImage tint(int clr, int alpha) {
        fill(clr, alpha);
        return this;
    }
    public HImage tint(int r, int g, int b) {
        fill(r, g, b);
        return this;
    }
    public HImage tint(int r, int g, int b, int a) {
        fill(r, g, b, a);
        return this;
    }
    public int tint() {
        return fill();
    }
    public boolean containsRel(float relX, float relY) {
        if (_image == null || _image.width <= 0 || _image.height <= 0 || _width <= 0 || _height <= 0) return false;
        int ix = H.app().round(relX * _image.width / _width);
        int iy = H.app().round(relY * _image.height / _height);
        return (0 < _image.get(ix, iy) >>> 24);
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        if (_image == null) return;
        currAlphaPerc *= (_fill >>> 24);
        app.tint(_fill | 0xFF000000, app.round(currAlphaPerc));
        app.image(_image, drawX, drawY, _width, _height);
    }
}
public static class HPath extends HDrawable {
    protected ArrayList < HVertex > _vertices;
    protected int _mode;
    public HPath() {
        this(PConstants.PATH);
    }
    public HPath(int pathMode) {
        _vertices = new ArrayList < HPath.HVertex > ();
        _mode = pathMode;
    }
    public HPath createCopy() {
        HPath copy = new HPath();
        copy.copyPropertiesFrom(this);
        for (int i = 0; i < _vertices.size(); ++i) {
            HVertex v = _vertices.get(i);
            copy.vertexPerc(v.x, v.y, v.hx1, v.hy1, v.hx2, v.hy2);
        }
        return copy;
    }
    public HPath mode(int m) {
        _mode = m;
        return this;
    }
    public int mode() {
        return _mode;
    }
    public HVertex vertex(int index) {
        return _vertices.get(index);
    }
    public HPath removeVertex(int index) {
        _vertices.remove(index);
        return this;
    }
    public int numVertices() {
        return _vertices.size();
    }
    public HPath adjustVertices() {
        int numVertices = _vertices.size();
        float minX, maxX, minY, maxY;
        minX = maxX = minY = maxY = 0;
        for (int i = 0; i < numVertices; ++i) {
            HVertex v = _vertices.get(i);
            if (v.x < minX) minX = v.x;
            else if (v.x > maxX) maxX = v.x;
            if (v.y < minY) minY = v.y;
            else if (v.y > maxY) maxY = v.y;
        }
        float ratioX = maxX - minX;
        float ratioY = maxY - minY;
        scale(ratioX, ratioY);
        anchorPercX((ratioX == 0) ? 0 : -minX / ratioX);
        anchorPercY((ratioY == 0) ? 0 : -minY / ratioY);
        for (int j = 0; j < numVertices; ++j) {
            HVertex w = _vertices.get(j);
            w.x -= minX;
            w.hx1 -= minX;
            w.hx2 -= minX;
            if (ratioX != 0) {
                w.x /= ratioX;
                w.hx1 /= ratioX;
                w.hx2 /= ratioX;
            }
            w.y -= minY;
            w.hy1 -= minY;
            w.hy2 -= minY;
            if (ratioY != 0) {
                w.y /= ratioY;
                w.hy1 /= ratioY;
                w.hy2 /= ratioY;
            }
        }
        return this;
    }
    public HPath vertex(float pxX, float pxY) {
        if (_height == 0 || _width == 0) {
            HWarnings.warn("Division by 0", "HPath.vertex()", HWarnings.VERTEXPX_ERR);
        } else {
            vertexPerc(pxX / _width, pxY / _height);
        }
        return this;
    }
    public HPath vertex(float handlePxX1, float handlePxY1, float handlePxX2, float handlePxY2, float pxX, float pxY) {
        if (_height == 0 || _width == 0) {
            HWarnings.warn("Division by 0", "HPath.vertex()", HWarnings.VERTEXPX_ERR);
        } else {
            vertexPerc(handlePxX1 / _width, handlePxY1 / _height, handlePxX2 / _width, handlePxY2 / _height, pxX / _width, pxY / _height);
        }
        return this;
    }
    public HPath vertexPerc(float percX, float percY) {
        HVertex v = new HVertex();
        v.x = v.hx1 = v.hx2 = percX;
        v.y = v.hy1 = v.hy2 = percY;
        _vertices.add(v);
        return this;
    }
    public HPath vertexPerc(float handlePercX1, float handlePercY1, float handlePercX2, float handlePercY2, float percX, float percY) {
        HVertex v = new HVertex();
        v.isBezier = true;
        v.x = percX;
        v.y = percY;
        v.hx1 = handlePercX1;
        v.hy1 = handlePercY1;
        v.hx2 = handlePercX2;
        v.hy2 = handlePercY2;
        _vertices.add(v);
        return this;
    }
    public HPath polygon(int numEdges) {
        _vertices.clear();
        PApplet app = H.app();
        float radInc = PConstants.TWO_PI / numEdges;
        for (int i = 0; i < numEdges; ++i) {
            float rad = radInc * i;
            vertexPerc(0.5f + 0.5f * app.cos(rad), 0.5f + 0.5f * app.sin(rad));
        }
        _mode = PConstants.POLYGON;
        return this;
    }
    public HPath star(int numEdges, float depth) {
        _vertices.clear();
        PApplet app = H.app();
        float radInc = PConstants.TWO_PI / numEdges;
        for (int i = 0; i < numEdges; ++i) {
            float rad = radInc * i;
            vertexPerc(0.5f + 0.5f * app.cos(rad), 0.5f + 0.5f * app.sin(rad));
            rad = radInc * (i + 0.5f);
            vertexPerc(0.5f + 0.5f * app.cos(rad) * (1 - depth), 0.5f + 0.5f * app.sin(rad) * (1 - depth));
        }
        _mode = PConstants.POLYGON;
        return this;
    }
    public HPath clear() {
        _vertices.clear();
        return this;
    }
    public boolean containsRel(float relX, float relY) {
        boolean isIn = false;
        float xPerc = relX / _width;
        float yPerc = relY / _height;
        for (int i = 0; i < numVertices(); ++i) {
            HVertex v1 = _vertices.get(i);
            HVertex v2 = _vertices.get((i >= numVertices() - 1) ? 0 : i + 1);
            if ((v1.y < yPerc && yPerc <= v2.y) || (v2.y < yPerc && yPerc <= v1.y)) {
                float t = (yPerc - v1.y) / (v2.y - v1.y);
                float currX = v1.x + (v2.x - v1.x) * t;
                if (currX == xPerc) return true;
                if (currX < xPerc) isIn = !isIn;
            }
        }
        return isIn;
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        int numVertices = _vertices.size();
        if (numVertices <= 0) return;
        applyStyle(app, currAlphaPerc);
        if (_mode == PConstants.POINTS) app.beginShape(PConstants.POINTS);
        else app.beginShape();
        boolean startFlag = true;
        for (int i = 0; i < numVertices; ++i) {
            HVertex v = _vertices.get(i);
            float x = drawX + _width * v.x;
            float y = drawY + _height * v.y;
            if (!v.isBezier || _mode == PConstants.POINTS || startFlag) {
                app.vertex(x, y);
            } else {
                float hx1 = drawX + _width * v.hx1;
                float hy1 = drawY + _height * v.hy1;
                float hx2 = drawX + _width * v.hx2;
                float hy2 = drawY + _height * v.hy2;
                app.bezierVertex(hx1, hy1, hx2, hy2, x, y);
            } if (startFlag) startFlag = false;
            else if (i == 0) break;
            if (_mode == PConstants.POLYGON && i >= numVertices - 1) i = -1;
        }
        if (_mode == PConstants.POLYGON) app.endShape(PConstants.CLOSE);
        else app.endShape();
    }
    public static class HVertex {
        public float x, y, hx1, hy1, hx2, hy2;
        public boolean isBezier;
    }
}
public static class HRect extends HDrawable {
    public float _tl, _tr, _bl, _br;
    public HRect() {}
    public HRect(float s) {
        size(s);
    }
    public HRect(float w, float h) {
        size(w, h);
    }
    public HRect(float w, float h, float roundingRadius) {
        size(w, h);
        rounding(roundingRadius);
    }
    public HRect createCopy() {
        HRect copy = new HRect();
        copy._tl = _tl;
        copy._tr = _tr;
        copy._bl = _bl;
        copy._br = _br;
        copy.copyPropertiesFrom(this);
        return copy;
    }
    public HRect rounding(float radius) {
        _tl = _tr = _bl = _br = radius;
        return this;
    }
    public HRect rounding(float topleft, float topright, float bottomright, float bottomleft) {
        _tl = topleft;
        _tr = topright;
        _br = bottomright;
        _bl = bottomleft;
        return this;
    }
    public HRect roundingTL(float radius) {
        _tl = radius;
        return this;
    }
    public float roundingTL() {
        return _tl;
    }
    public HRect roundingTR(float radius) {
        _tr = radius;
        return this;
    }
    public float roundingTR() {
        return _tr;
    }
    public HRect roundingBR(float radius) {
        _br = radius;
        return this;
    }
    public float roundingBR() {
        return _br;
    }
    public HRect roundingBL(float radius) {
        _bl = radius;
        return this;
    }
    public float roundingBL() {
        return _bl;
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        applyStyle(app, currAlphaPerc);
        app.rect(drawX, drawY, _width, _height, _tl, _tr, _br, _bl);
    }
}
public static class HShape extends HDrawable {
    protected PShape _shape;
    protected HColorPool _randomColors;
    public HShape() {
        shape(null);
    }
    public HShape(Object shapeArg) {
        shape(shapeArg);
    }
    public HShape createCopy() {
        HShape copy = new HShape(_shape);
        copy.copyPropertiesFrom(this);
        return copy;
    }
    public HShape resetSize() {
        if (_shape == null) {
            size(0, 0);
        } else {
            size(_shape.width, _shape.height);
        }
        return this;
    }
    public HShape shape(Object shapeArg) {
        if (shapeArg instanceof PShape) {
            _shape = (PShape) shapeArg;
        } else if (shapeArg instanceof String) {
            _shape = H.app().loadShape((String) shapeArg);
        } else if (shapeArg instanceof HShape) {
            _shape = ((HShape) shapeArg)._shape;
        } else if (shapeArg == null) {
            _shape = null;
        }
        return resetSize();
    }
    public PShape shape() {
        return _shape;
    }
    public HShape enableStyle(boolean b) {
        if (b) _shape.enableStyle();
        else _shape.disableStyle();
        return this;
    }
    public HColorPool randomColors(HColorPool colorPool) {
        return randomColors(colorPool, true);
    }
    public HColorPool randomColors(HColorPool colorPool, boolean isCopy) {
        if (isCopy) colorPool = colorPool.createCopy();
        _shape.disableStyle();
        _randomColors = colorPool;
        return _randomColors;
    }
    public HColorPool randomColors() {
        return _randomColors;
    }
    public HShape resetRandomColors() {
        _shape.enableStyle();
        _randomColors = null;
        return this;
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        if (_shape == null) return;
        applyStyle(app, currAlphaPerc);
        if (_randomColors == null) {
            app.shape(_shape, drawX, drawY, _width, _height);
        } else
            for (int i = 0; i < _shape.getChildCount(); ++i) {
                PShape childShape = _shape.getChild(i);
                childShape.width = _shape.width;
                childShape.height = _shape.height;
                if (_randomColors.appliesFill()) app.fill(_randomColors.getColor());
                if (_randomColors.appliesStroke()) app.stroke(_randomColors.getColor());
                app.shape(childShape, drawX, drawY, _width, _height);
            }
    }
}
public static class HStage extends HDrawable {
    protected PApplet _app;
    protected PImage _bgImg;
    protected boolean _autoClearFlag;
    public HStage(PApplet papplet) {
        _app = papplet;
        _autoClearFlag = true;
        background(HConstants.DEFAULT_BACKGROUND_COLOR);
    }
    public void background(int clr) {
        _fill = clr;
        clear();
    }
    public void backgroundImg(Object arg) {
        if (arg instanceof String) {
            _bgImg = _app.loadImage((String) arg);
        } else if (arg instanceof PImage) {
            _bgImg = (PImage) arg;
        }
        clear();
    }
    public HStage autoClear(boolean b) {
        _autoClearFlag = b;
        return this;
    }
    public boolean autoClear() {
        return _autoClearFlag;
    }
    public HStage clear() {
        if (_bgImg == null) _app.background(_fill);
        else _app.background(_bgImg);
        return this;
    }
    public HDrawable fill(int clr) {
        background(clr);
        return this;
    }
    public HDrawable fill(int clr, int alpha) {
        return fill(clr);
    }
    public HDrawable fill(int r, int g, int b) {
        return fill(HColorUtil.merge(255, r, g, b));
    }
    public HDrawable fill(int r, int g, int b, int a) {
        return fill(r, g, b);
    }
    public PVector size() {
        return new PVector(_app.width, _app.height);
    }
    public float width() {
        return _app.width;
    }
    public float height() {
        return _app.height;
    }
    public void paintAll(PApplet app, float currAlphaPerc) {
        app.pushStyle();
        if (_autoClearFlag) clear();
        HDrawable child = _firstChild;
        while (child != null) {
            child.paintAll(app, 1);
            child = child.next();
        }
        app.popStyle();
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {}
    public void copyPropertiesFrom(HDrawable other) {}
    public HDrawable createCopy() {
        return null;
    }
    public HDrawable loc(float newX, float newY) {
        return this;
    }
    public HDrawable x(float newX) {
        return this;
    }
    public HDrawable y(float newY) {
        return this;
    }
    public HDrawable move(float dx, float dy) {
        return this;
    }
    public HDrawable locAt(int where) {
        return this;
    }
    public HDrawable rotation(float deg) {
        return this;
    }
    public HDrawable rotationRad(float rad) {
        return this;
    }
    public HDrawable rotate(float deg) {
        return this;
    }
    public HDrawable rotateRad(float rad) {
        return this;
    }
}
public static class HText extends HDrawable {
    protected PFont _font;
    protected String _text;
    protected float _descent;
    public HText() {
        this(null, 16);
    }
    public HText(String textString) {
        this(textString, 16, null);
    }
    public HText(String textString, float size) {
        this(textString, size, null);
    }
    public HText(String textString, float size, Object fontArg) {
        _text = textString;
        _height = size;
        font(fontArg);
        height(size);
    }
    public HText createCopy() {
        HText copy = new HText(_text, _height, _font);
        copy.copyPropertiesFrom(this);
        copy.adjustMetrics();
        return copy;
    }
    public HText text(String txt) {
        _text = txt;
        adjustMetrics();
        return this;
    }
    public String text() {
        return _text;
    }
    public HText font(Object arg) {
        PApplet app = H.app();
        if (arg instanceof PFont) {
            _font = (PFont) arg;
        } else if (arg instanceof String) {
            String str = (String) arg;
            _font = (str.indexOf(".vlw", str.length() - 4) > 0) ? app.loadFont(str) : app.createFont(str, _height);
        } else if (arg instanceof HText) {
            _font = ((HText) arg)._font;
        } else if (arg == null) {
            _font = app.createFont("SansSerif", _height);
        }
        adjustMetrics();
        return this;
    }
    public PFont font() {
        return _font;
    }
    public HText fontSize(float f) {
        return height(f);
    }
    public float fontSize() {
        return _height;
    }
    protected void adjustMetrics() {
        PApplet app = H.app();
        app.pushStyle();
        app.textFont(_font, _height);
        _descent = app.textDescent();
        super.width((_text == null) ? 0 : app.textWidth(_text));
        app.popStyle();
    }
    public HText width(float w) {
        return this;
    }
    public HText height(float h) {
        super.height(h);
        adjustMetrics();
        return this;
    }
    public HText size(float w, float h) {
        return height(h);
    }
    public HText size(float s) {
        return height(s);
    }
    public HText scale(float s) {
        super.scale(s);
        adjustMetrics();
        return this;
    }
    public HText scale(float sw, float sh) {
        return scale(sh);
    }
    public boolean containsRel(float relX, float relY) {
        if (_text == null || _height == 0) return false;
        PApplet app = H.app();
        int numChars = _text.length();
        float ratio = _font.getSize() / _height;
        float xoff = 0;
        float yoff = (_height - _descent) * ratio;
        relX *= ratio;
        relY *= ratio;
        for (int i = 0; i < numChars; ++i) {
            char c = _text.charAt(i);
            PFont.Glyph g = _font.getGlyph(c);
            int pxx = app.round(relX - xoff);
            int pxy = app.round(relY - yoff) + g.topExtent;
            if (g.image.get(pxx, pxy) >>> 24 > 0) return true;
            xoff += g.setWidth;
        }
        return false;
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        if (_text == null) return;
        applyStyle(app, currAlphaPerc);
        app.textFont(_font, _height);
        app.text(_text, drawX, drawY + _height - _descent);
    }
}
public static class HTriangle extends HDrawable {
    protected int _type;
    public HTriangle() {}
    public HTriangle(int triangleType) {
        type(triangleType);
    }
    public HTriangle createCopy() {
        HTriangle copy = new HTriangle();
        copy.copyPropertiesFrom(this);
        copy._type = _type;
        return copy;
    }
    public HTriangle type(int triangleType) {
        _type = triangleType;
        return this;
    }
    public int type() {
        return _type;
    }
    public HTriangle width(float w) {
        if (_type == H.EQUILATERAL) {
            super.height(w * H.app().sin(PConstants.TWO_PI / 6));
        }
        return (HTriangle) super.width(w);
    }
    public HTriangle height(float h) {
        if (_type == H.EQUILATERAL) {
            super.width(h / H.app().sin(PConstants.TWO_PI / 6));
        }
        return (HTriangle) super.height(h);
    }
    public boolean containsRel(float relX, float relY) {
        if (_width <= 0 || _height <= 0) return false;
        float xRatio = relX / _width;
        if (xRatio < 0 || xRatio > 1) return false;
        float yRatio = relY / _height;
        if (yRatio < 0 || yRatio > 1) return false;
        if (_type == HConstants.RIGHT) {
            return (xRatio / yRatio > 1);
        } else {
            float cx = _width / 2;
            float x1 = (1 - yRatio) * cx;
            float x2 = yRatio * cx + cx;
            return (x1 <= relX) && (relX <= x2) && (0 <= relY) && (relY <= _height);
        }
    }
    public void draw(PApplet app, float drawX, float drawY, float currAlphaPerc) {
        applyStyle(app, currAlphaPerc);
        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        if (_type == H.RIGHT) {
            x1 = drawX;
            y1 = drawY;
            x2 = drawX + _width;
            y2 = drawY;
            x3 = drawX + _width;
            y3 = drawY + _height;
        } else {
            x1 = drawX;
            y1 = drawY + _height;
            x2 = drawX + _width / 2;
            y2 = drawY;
            x3 = drawX + _width;
            y3 = drawY + _height;
        }
        app.triangle(x1, y1, x2, y2, x3, y3);
    }
}
public static class HMouse implements HFollowable {
    private PApplet _app;
    private boolean _started;
    public HMouse(PApplet app) {
        _app = app;
    }
    public boolean started() {
        return _started;
    }
    public void handleEvents() {
        if (!_started && _app.pmouseX + _app.pmouseY > 0) _started = true;
    }
    public float followableX() {
        return _app.mouseX;
    }
    public float followableY() {
        return _app.mouseY;
    }
}
public static interface HCallback {
    public void run(Object obj);
}
public static interface HFollowable {
    public float followableX();
    public float followableY();
}
public static interface HHittable {
    public boolean contains(float absX, float absY);
    public boolean containsRel(float relX, float relY);
}
public static interface HMovable {
    public float x();
    public float y();
    public HMovable move(float dx, float dy);
}
public static class HPoolAdapter implements HPoolListener {
    public void onCreate(HDrawable d, int index, HDrawablePool pool) {}
    public void onRequest(HDrawable d, int index, HDrawablePool pool) {}
    public void onRelease(HDrawable d, int index, HDrawablePool pool) {}
}
public static interface HPoolListener {
    public void onCreate(HDrawable d, int index, HDrawablePool pool);
    public void onRequest(HDrawable d, int index, HDrawablePool pool);
    public void onRelease(HDrawable d, int index, HDrawablePool pool);
}
public static interface HRotatable {
    public float rotationRad();
    public HRotatable rotationRad(float rad);
}
public static interface HSwarmer extends HMovable, HRotatable {}
public static class HGridLayout implements HLayout {
    protected int _currentIndex, _numCols;
    protected float _startX, _startY, _xSpace, _ySpace;
    public HGridLayout() {
        _xSpace = _ySpace = _numCols = 16;
    }
    public HGridLayout(int numOfColumns) {
        this();
        _numCols = numOfColumns;
    }
    public HGridLayout currentIndex(int i) {
        _currentIndex = i;
        return this;
    }
    public int currentIndex() {
        return _currentIndex;
    }
    public HGridLayout resetIndex() {
        _currentIndex = 0;
        return this;
    }
    public HGridLayout cols(int numOfColumns) {
        _numCols = numOfColumns;
        return this;
    }
    public int cols() {
        return _numCols;
    }
    public PVector startLoc() {
        return new PVector(_startX, _startY);
    }
    public HGridLayout startLoc(float x, float y) {
        _startX = x;
        _startY = y;
        return this;
    }
    public float startX() {
        return _startX;
    }
    public HGridLayout startX(float x) {
        _startX = x;
        return this;
    }
    public float startY() {
        return _startY;
    }
    public HGridLayout startY(float y) {
        _startY = y;
        return this;
    }
    public PVector spacing() {
        return new PVector(_xSpace, _ySpace);
    }
    public HGridLayout spacing(float xSpacing, float ySpacing) {
        _xSpace = xSpacing;
        _ySpace = ySpacing;
        return this;
    }
    public float spacingX() {
        return _xSpace;
    }
    public HGridLayout spacingX(float xSpacing) {
        _xSpace = xSpacing;
        return this;
    }
    public float spacingY() {
        return _ySpace;
    }
    public HGridLayout spacingY(float ySpacing) {
        _ySpace = ySpacing;
        return this;
    }
    public PVector getNextPoint() {
        int currentRow = H.app().floor(_currentIndex / _numCols);
        int currentCol = _currentIndex % _numCols;
        ++_currentIndex;
        return new PVector(currentCol * _xSpace + _startX, currentRow * _ySpace + _startY);
    }
    public void applyTo(HDrawable target) {
        target.loc(getNextPoint());
    }
}
public static interface HLayout {
    public void applyTo(HDrawable target);
    public abstract PVector getNextPoint();
}
public static class HRandomTrigger extends HTrigger {
    public float _chance;
    public HRandomTrigger() {}
    public HRandomTrigger(float percChance) {
        _chance = percChance;
    }
    public HRandomTrigger chance(float perc) {
        _chance = perc;
        return this;
    }
    public float chance() {
        return _chance;
    }
    public void runBehavior(PApplet app) {
        if (app.random(1) <= _chance) {
            if (_callback != null) _callback.run(null);
        }
    }
    public HRandomTrigger callback(HCallback cb) {
        return (HRandomTrigger) super.callback(cb);
    }
}
public static class HTimer extends HTrigger {
    protected int _lastInterval, _interval, _cycleCounter, _numCycles;
    protected boolean _usesFrames;
    public HTimer() {
        _interval = 1000;
        _lastInterval = -1;
    }
    public HTimer(int timerInterval) {
        _interval = timerInterval;
    }
    public HTimer(int timerInterval, int numberOfCycles) {
        _interval = timerInterval;
        _numCycles = numberOfCycles;
    }
    public HTimer interval(int i) {
        _interval = i;
        return this;
    }
    public int interval() {
        return _interval;
    }
    public HTimer cycleCounter(int cycleIndex) {
        _cycleCounter = cycleIndex;
        return this;
    }
    public int cycleCounter() {
        return _cycleCounter;
    }
    public HTimer numCycles(int cycles) {
        _numCycles = cycles;
        return this;
    }
    public int numCycles() {
        return _numCycles;
    }
    public HTimer cycleIndefinitely() {
        _numCycles = 0;
        return this;
    }
    public HTimer useMillis() {
        _usesFrames = false;
        return this;
    }
    public boolean usesMillis() {
        return !_usesFrames;
    }
    public HTimer useFrames() {
        _usesFrames = true;
        return this;
    }
    public boolean usesFrames() {
        return _usesFrames;
    }
    public void runBehavior(PApplet app) {
        int curr = (_usesFrames) ? app.frameCount : app.millis();
        if (_lastInterval < 0) _lastInterval = curr;
        if (curr - _lastInterval >= _interval) {
            _lastInterval = curr;
            if (_callback != null) _callback.run(_cycleCounter);
            if (_numCycles > 0 && ++_cycleCounter >= _numCycles) unregister();
        }
    }
    public HTimer callback(HCallback cb) {
        return (HTimer) super.callback(cb);
    }
    public HTimer register() {
        return (HTimer) super.register();
    }
    public HTimer unregister() {
        _numCycles = 0;
        _lastInterval = -1;
        return (HTimer) super.unregister();
    }
}
public static abstract class HTrigger extends HBehavior {
    public HCallback _callback;
    public HTrigger callback(HCallback cb) {
        if (cb == null) unregister();
        else register();
        _callback = cb;
        return this;
    }
    public HCallback callback() {
        return _callback;
    }
}
public static class H implements HConstants {
    private static H _self;
    private static PApplet _app;
    private static HStage _stage;
    private static HBehaviorRegistry _behaviors;
    private static HMouse _mouse;
    public static H init(PApplet applet) {
        _app = applet;
        HMath.init(_app);
        if (_self == null) _self = new H();
        if (_stage == null) _stage = new HStage(_app);
        if (_behaviors == null) _behaviors = new HBehaviorRegistry();
        if (_mouse == null) _mouse = new HMouse(_app);
        return _self;
    }
    public static HStage stage() {
        return _stage;
    }
    public static PApplet app() {
        return _app;
    }
    public static HBehaviorRegistry behaviors() {
        return _behaviors;
    }
    public static HMouse mouse() {
        return _mouse;
    }
    public static H background(int clr) {
        _stage.background(clr);
        return _self;
    }
    public static H backgroundImg(Object arg) {
        _stage.backgroundImg(arg);
        return _self;
    }
    public static H autoClear(boolean b) {
        _stage.autoClear(b);
        return _self;
    }
    public static boolean autoClear() {
        return _stage.autoClear();
    }
    public static H clearStage() {
        _stage.clear();
        return _self;
    }
    public static HDrawable add(HDrawable stageChild) {
        return _stage.add(stageChild);
    }
    public static HDrawable remove(HDrawable stageChild) {
        return _stage.remove(stageChild);
    }
    public static H drawStage() {
        _behaviors.runAll(_app);
        _mouse.handleEvents();
        _stage.paintAll(_app, 0);
        return _self;
    }
    public static boolean mouseStarted() {
        return _mouse.started();
    }
    private H() {}
}
public static class HBundle {
    private HashMap < String, Object > objectContents;
    private HashMap < String, Float > numberContents;
    public HBundle() {
        objectContents = new HashMap < String, Object > ();
        numberContents = new HashMap < String, Float > ();
    }
    public HBundle obj(String key, Object value) {
        objectContents.put(key, value);
        return this;
    }
    public HBundle num(String key, float value) {
        numberContents.put(key, value);
        return this;
    }
    public Object obj(String key) {
        return objectContents.get(key);
    }
    public float num(String key) {
        return numberContents.get(key);
    }
    public int numI(String key) {
        return H.app().round(numberContents.get(key));
    }
    public boolean numB(String key) {
        return (numberContents.get(key) != 0);
    }
}
public static class HColorUtil {
    public static int[] explode(int clr) {
        int[] explodedColors = new int[4];
        for (int i = 0; i < 4; ++i) explodedColors[3 - i] = (clr >>> (i * 8)) & 0xFF;
        return explodedColors;
    }
    public static int merge(int a, int r, int g, int b) {
        PApplet app = H.app();
        a = app.constrain(a, 0, 0xFF);
        r = app.constrain(r, 0, 0xFF);
        g = app.constrain(g, 0, 0xFF);
        b = app.constrain(b, 0, 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    public static int setAlpha(int clr, int newAlpha) {
        newAlpha = H.app().constrain(newAlpha, 0, 0xFF);
        return clr & 0x00FFFFFF | (newAlpha << 24);
    }
    public static int setRed(int clr, int newRed) {
        newRed = H.app().constrain(newRed, 0, 0xFF);
        return clr & 0xFF00FFFF | (newRed << 16);
    }
    public static int setGreen(int clr, int newGreen) {
        newGreen = H.app().constrain(newGreen, 0, 0xFF);
        return clr & 0xFFFF00FF | (newGreen << 8);
    }
    public static int setBlue(int clr, int newBlue) {
        newBlue = H.app().constrain(newBlue, 0, 0xFF);
        return clr & 0xFFFFFF00 | newBlue;
    }
    public static int getAlpha(int clr) {
        return clr >>> 24;
    }
    public static int getRed(int clr) {
        return (clr >>> 16) & 255;
    }
    public static int getGreen(int clr) {
        return (clr >>> 8) & 255;
    }
    public static int getBlue(int clr) {
        return clr & 255;
    }
    public static int multiply(int c1, int c2) {
        return H.app().round(c1 * c2 / 255f);
    }
    public static int multiplyAlpha(int clr, int a) {
        return clr & 0x00FFFFFF | (multiply(getAlpha(clr), a) << 24);
    }
    public static int multiplyRed(int clr, int r) {
        return clr & 0xFF00FFFF | (multiply(getRed(clr), r) << 16);
    }
    public static int multiplyGreen(int clr, int g) {
        return clr & 0xFFFF00FF | (multiply(getGreen(clr), g) << 8);
    }
    public static int multiplyBlue(int clr, int b) {
        return clr & 0xFFFFFF00 | multiply(getBlue(clr), b);
    }
}
public static interface HConstants {
    public static final int NONE = 0, LEFT = 1, RIGHT = 2, CENTER_X = 3, TOP = 4, BOTTOM = 8, CENTER_Y = 12, CENTER = 15, TOP_LEFT = 5, TOP_RIGHT = 6, BOTTOM_LEFT = 9, BOTTOM_RIGHT = 10, CENTER_LEFT = 13, CENTER_RIGHT = 14, CENTER_TOP = 7, CENTER_BOTTOM = 11, DEFAULT_BACKGROUND_COLOR = 0xFFECF2F5, DEFAULT_FILL = 0xFFFFFFFF, DEFAULT_STROKE = 0xFF000000, DEFAULT_WIDTH = 64, DEFAULT_HEIGHT = 64, CLEAR = 0x00FFFFFF, WHITE = 0xFFFFFFFF, LGREY = 0xFFC0C0C0, GREY = 0xFF808080, DGREY = 0xFF404040, BLACK = 0xFF000000, RED = 0xFFFF0000, GREEN = 0xFF00FF00, BLUE = 0xFF0000FF, CYAN = 0xFF00FFFF, MAGENTA = 0xFFFF00FF, YELLOW = 0xFFFFFF00, SAW = 0, SINE = 1, TRIANGLE = 2, SQUARE = 3, WIDTH = 0, HEIGHT = 1, SIZE = 2, ALPHA = 3, X = 4, Y = 5, LOCATION = 6, ROTATION = 7, DROTATION = 8, DX = 9, DY = 10, DLOC = 11, SCALE = 12, ISOCELES = 0, EQUILATERAL = 1;
    public static final float D2R = PConstants.PI / 180f, R2D = 180f / PConstants.PI;
}
public static class HDrawablePool {
    protected HLinkedHashSet < HDrawable > _activeSet, _inactiveSet;
    protected ArrayList < HDrawable > _prototypes;
    public HCallback _onCreate, _onRequest, _onRelease;
    public HPoolListener _listener;
    protected HLayout _layout;
    protected HColorist _colorist;
    protected HDrawable _autoParent;
    protected int _max;
    public HDrawablePool() {
        this(64);
    }
    public HDrawablePool(int maximumDrawables) {
        _max = maximumDrawables;
        _activeSet = new HLinkedHashSet < HDrawable > ();
        _inactiveSet = new HLinkedHashSet < HDrawable > ();
        _prototypes = new ArrayList < HDrawable > ();
    }
    public int max() {
        return _max;
    }
    public HDrawablePool max(int m) {
        _max = m;
        return this;
    }
    public int numActive() {
        return _activeSet.size();
    }
    public int numInactive() {
        return _inactiveSet.size();
    }
    public int currentIndex() {
        return _activeSet.size() - 1;
    }
    public HLayout layout() {
        return _layout;
    }
    public HDrawablePool layout(HLayout newLayout) {
        _layout = newLayout;
        return this;
    }
    public HColorist colorist() {
        return _colorist;
    }
    public HDrawablePool colorist(HColorist newColorist) {
        _colorist = newColorist;
        return this;
    }
    public HDrawablePool listener(HPoolListener newListener) {
        _listener = newListener;
        return this;
    }
    public HDrawablePool onCreate(HCallback callback) {
        _onCreate = callback;
        return this;
    }
    public HCallback onCreate() {
        return _onCreate;
    }
    public HPoolListener listener() {
        return _listener;
    }
    public HDrawablePool onRequest(HCallback callback) {
        _onRequest = callback;
        return this;
    }
    public HCallback onRequest() {
        return _onRequest;
    }
    public HDrawablePool onRelease(HCallback callback) {
        _onRelease = callback;
        return this;
    }
    public HCallback onRelease() {
        return _onRelease;
    }
    public HDrawablePool autoParent(HDrawable parent) {
        _autoParent = parent;
        return this;
    }
    public HDrawablePool autoAddToStage() {
        _autoParent = H.stage();
        return this;
    }
    public HDrawable autoParent() {
        return _autoParent;
    }
    public boolean isFull() {
        return count() >= _max;
    }
    public int count() {
        return _activeSet.size() + _inactiveSet.size();
    }
    public HDrawablePool destroy() {
        _activeSet.removeAll();
        _inactiveSet.removeAll();
        _prototypes.clear();
        _onCreate = _onRequest = _onRelease = null;
        _layout = null;
        _autoParent = null;
        _max = 0;
        return this;
    }
    public HDrawablePool add(HDrawable prototype, int frequency) {
        if (prototype == null) {
            HWarnings.warn("Null Prototype", "HDrawablePool.add()", HWarnings.NULL_ARGUMENT);
        } else {
            _prototypes.add(prototype);
            while (frequency-- > 0) _prototypes.add(prototype);
        }
        return this;
    }
    public HDrawablePool add(HDrawable prototype) {
        return add(prototype, 1);
    }
    public HDrawable request() {
        if (_prototypes.size() <= 0) {
            HWarnings.warn("No Prototype", "HDrawablePool.request()", HWarnings.NO_PROTOTYPE);
            return null;
        }
        HDrawable drawable;
        boolean onCreateFlag = false;
        if (_inactiveSet.size() > 0) {
            drawable = _inactiveSet.pull();
        } else if (count() < _max) {
            drawable = createRandomDrawable();
            onCreateFlag = true;
        } else return null;
        _activeSet.add(drawable);
        if (_autoParent != null) _autoParent.add(drawable);
        if (_layout != null) _layout.applyTo(drawable);
        if (_colorist != null) _colorist.applyColor(drawable);
        if (_listener != null) {
            int index = currentIndex();
            if (onCreateFlag) _listener.onCreate(drawable, index, this);
            _listener.onRequest(drawable, index, this);
        }
        if (onCreateFlag && _onCreate != null) _onCreate.run(drawable);
        if (_onRequest != null) _onRequest.run(drawable);
        return drawable;
    }
    public HDrawablePool requestAll() {
        if (_prototypes.size() <= 0) {
            HWarnings.warn("No Prototype", "HDrawablePool.requestAll()", HWarnings.NO_PROTOTYPE);
        } else {
            while (count() < _max) request();
        }
        return this;
    }
    public boolean release(HDrawable d) {
        if (_activeSet.remove(d)) {
            _inactiveSet.add(d);
            if (_autoParent != null) _autoParent.remove(d);
            if (_listener != null) _listener.onRelease(d, currentIndex(), this);
            if (_onRelease != null) _onRelease.run(d);
            return true;
        }
        return false;
    }
    public HLinkedHashSet < HDrawable > activeSet() {
        return _activeSet;
    }
    public HLinkedHashSet < HDrawable > inactiveSet() {
        return _inactiveSet;
    }
    protected HDrawable createRandomDrawable() {
        PApplet app = H.app();
        int numPrototypes = _prototypes.size();
        int index = app.round(app.random(numPrototypes - 1));
        return _prototypes.get(index).createCopy();
    }
    public HIterator < HDrawable > iterator() {
        return _activeSet.iterator();
    }
}
public static class HMath implements HConstants {
    private static PApplet _app;
    private static boolean _usingTempSeed;
    private static int _resetSeedValue;
    public static void init(PApplet applet) {
        _app = applet;
    }
    public static float[] rotatePointArr(float x, float y, float rad) {
        float[] pt = new float[2];
        float c = _app.cos(rad);
        float s = _app.sin(rad);
        pt[0] = x * c - y * s;
        pt[1] = x * s + y * c;
        return pt;
    }
    public static PVector rotatePoint(float x, float y, float rad) {
        float[] f = rotatePointArr(x, y, rad);
        return new PVector(f[0], f[1]);
    }
    public static float yAxisAngle(float x1, float y1, float x2, float y2) {
        return _app.atan2(x2 - x1, y2 - y1);
    }
    public static float xAxisAngle(float x1, float y1, float x2, float y2) {
        return _app.atan2(y2 - y1, x2 - x1);
    }
    public static float[] absLocArr(HDrawable ref, float relX, float relY) {
        float[] f = {
            relX, relY, 0
        };
        while (ref != null) {
            float rot = ref.rotationRad();
            float[] g = rotatePointArr(f[0], f[1], rot);
            f[0] = g[0] + ref.x();
            f[1] = g[1] + ref.y();
            f[2] += rot;
            ref = ref.parent();
        }
        return f;
    }
    public static PVector absLoc(HDrawable ref, float relX, float relY) {
        float[] f = absLocArr(ref, relX, relY);
        return new PVector(f[0], f[1]);
    }
    public static PVector absLoc(HDrawable d) {
        return absLoc(d, 0, 0);
    }
    public static float[] relLocArr(HDrawable ref, float absX, float absY) {
        float[] f = absLocArr(ref, 0, 0);
        return rotatePointArr(absX - f[0], absY - f[1], -f[2]);
    }
    public static PVector relLoc(HDrawable ref, float absX, float absY) {
        float[] f = relLocArr(ref, absX, absY);
        return new PVector(f[0], f[1]);
    }
    public static int randomInt32() {
        float f = _app.random(1);
        f = _app.map(f, 0, 1, -2147483648, 2147483647);
        return _app.round(f);
    }
    public static void tempSeed(long seed) {
        if (!_usingTempSeed) {
            _resetSeedValue = randomInt32();
            _usingTempSeed = true;
        }
        _app.randomSeed(seed);
    }
    public static void removeTempSeed() {
        _app.randomSeed(_resetSeedValue);
    }
    public static float sineWave(float stepDegrees) {
        return H.app().sin(stepDegrees * H.D2R);
    }
    public static float triangleWave(float stepDegrees) {
        float outVal = (stepDegrees % 180) / 90;
        if (outVal > 1) outVal = 2 - outVal;
        if (stepDegrees % 360 > 180) outVal = -outVal;
        return outVal;
    }
    public static float sawWave(float stepDegrees) {
        float outVal = (stepDegrees % 180) / 180;
        if (stepDegrees % 360 >= 180) outVal -= 1;
        return outVal;
    }
    public static float squareWave(float stepDegrees) {
        return (stepDegrees % 360 > 180) ? -1 : 1;
    }
    public static boolean hasBits(int target, int val) {
        return ((target & val) == val);
    }
}
public static class HWarnings {
    public static final String NULL_TARGET = "A target should be assigned before using this method.", NO_PROTOTYPE = "This pool needs at least one prototype before requesting.", NULL_ARGUMENT = "This method does not take null arguments.", INVALID_DEST = "The destination doesn't not belong to any parent.", DESTCEPTION = "The destination cannot be itself", CHILDCEPTION = "Can't add this parent as its own child.", ANCHORPX_ERR = "Set a non-zero size first for this drawable before setting the\n\t" + "anchor by pixels, or use the anchorPerc() & anchorAt() methods\n\t" + "instead.", VERTEXPX_ERR = "Set a non-zero size first for this path before setting the\n\t" + "vertex by pixels, or use the vertexPerc() methods instead.";
    public static void warn(String type, String loc, String msg) {
        PApplet app = H.app();
        app.println("[Warning: " + type + " @ " + loc + "]");
        if (msg != null && msg.length() > 0) app.println("\t" + msg);
    }
    private HWarnings() {}
}
public void keyPressed() {
  if (key == ' ') {
    if (!paused) {
      // stop
      noLoop();
      paused = true;
    } else {
      // restart
      loop();
      paused = false;
    }
  }

  if (key == '+') {
    redraw();
  }

  if (key == 'r') {
    record = true;
    draw();
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "hColoristPixelator" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
