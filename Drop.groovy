import groovy.transform.CompileStatic

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch


@CompileStatic
class Drop implements ApplicationListener {
	OrthographicCamera camera
	SpriteBatch batch

	Texture dropImage
	Texture bucketImage
	Sound dropSound
	Music rainMusic

	// could use libGDX Array class here, but on the desktop Groovy's built-in
	// lists (and maps) are often fine as well. also, providing a list type will
	// result in much faster code, but is not necessary.
	List<Rectangle> raindrops = []
	Rectangle bucket
	long lastDropTime

	Vector3 touchPos = new Vector3()


	@Override
	void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("assets/droplet.png"))
		bucketImage = new Texture(Gdx.files.internal("assets/bucket.png"))

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("assets/drop.wav"))
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/rain.mp3"))

		// start the playback of the background music immediately
		rainMusic.looping = true
		rainMusic.play()

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera()
		camera.setToOrtho(false, 800, 480)
		batch = new SpriteBatch()

		// create a Rectangle to logically represent the bucket
		// note the division by floats, which is much faster than by ints in Groovy
		bucket = new Rectangle(800 / 2f - 64 / 2f as float, 20, 64, 64)

		// spawn the first raindrop
		spawnRaindrop()
	}

	private void spawnRaindrop() {
		// lists are nicely integrated into Groovy, so you can use += for example
		raindrops += new Rectangle(MathUtils.random(0, 800 - 64), 480, 64, 64)
		lastDropTime = TimeUtils.nanoTime()
	}

	@Override
	void render() {
		// clear the screen with a dark blue color
		Gdx.gl.glClearColor(0, 0, 0.2f, 1)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		// tell the camera to update its matrices, then
		// tell the SpriteBatch to render in the coordinate system specified by the camera
		camera.update()
		batch.projectionMatrix = camera.combined

		// begin a new batch and draw the bucket and all drops
		batch.begin()
		batch.draw(bucketImage, bucket.x, bucket.y)
		raindrops.each { drop -> batch.draw(dropImage, drop.x, drop.y) }
		batch.end()

		// process user input
		if(Gdx.input.touched) {
			touchPos.set(Gdx.input.x, Gdx.input.y, 0)
			camera.unproject(touchPos)
			bucket.x = touchPos.x - 64 / 2f as float
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
			bucket.x -= 200 * Gdx.graphics.deltaTime
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
			bucket.x += 200 * Gdx.graphics.deltaTime

		// make sure the bucket stays within the screen bounds
		if(bucket.x < 0) bucket.x = 0
		if(bucket.x > 800 - 64) bucket.x = 800 - 64

		// check if we need to create a new raindrop
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000)
			spawnRaindrop()

		// move the raindrops, play sound effects
		// for performance-critical code sections you might want to use an Iterator instead
		raindrops.each { drop ->
			drop.y -= 200 * Gdx.graphics.deltaTime
			if(drop.y + 64 < 0) {
				raindrops -= drop  // beware, this will create a new list
			}
			if(drop.overlaps(bucket)) {
				dropSound.play()
				raindrops -= drop  // as above, for less garbage collection use an Iterator
			}
		}
	}

	@Override
	void dispose() {
		dropImage.dispose()
		bucketImage.dispose()
		dropSound.dispose()
		rainMusic.dispose()
		batch.dispose()
	}

	@Override
	void resize(int i, int i1) {
	}

	@Override
	void pause() {
	}

	@Override
	void resume() {
	}
}

