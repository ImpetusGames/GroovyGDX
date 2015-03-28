import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration


LwjglApplicationConfiguration config = new LwjglApplicationConfiguration()
config.title = "Drop"
config.width = 800
config.height = 480
new LwjglApplication(new Drop(), config)