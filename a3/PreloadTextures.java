package a3;

import java.io.IOException;
import java.util.Random;

import ray.rage.asset.texture.Texture;

public class PreloadTextures {
	MyGame g;
	private Texture
		blue1,
		blue2,
		red1,
		green1,
		yellow1,
		gray1;
	
	public enum TEXTURE {
		BLUE1,
		BLUE2,
		RED1,
		GREEN1,
		YELLOW1,
		GRAY1
	}

	public PreloadTextures(MyGame myGame) {
		this.g = myGame;
		loadTextures();
	}

	private void loadTextures() {
		try {
			blue1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("blue1Tile.png");
			blue2 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("blue2Tile.png");
			red1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("red1Tile.png");
			green1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("green1Tile.png");
			yellow1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("yellow1Tile.png");
			gray1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("gray1Tile.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Texture getTexture(TEXTURE tex) {
		switch(tex) {
		case BLUE1:
			return blue1;
		case BLUE2:
			return blue2;
		case RED1:
			return red1;
		case GREEN1:
			return green1;
		case YELLOW1:
			return yellow1;
		case GRAY1:
			return gray1;
		default:
			return null;
		}
	}
	
	public Texture getRandomTexture() {
        float random = (float) Math.random();
        if (random < 0.166f) {
        	return getTexture(TEXTURE.BLUE1);
        }
        else if (random < 0.333f) {
        	return getTexture(TEXTURE.BLUE2);
        }
        else if (random < 0.5f) {
        	return getTexture(TEXTURE.RED1);
        }
        else if (random < 0.666f) {
        	return getTexture(TEXTURE.GREEN1);
        }
        else if (random < 0.833f) {
        	return getTexture(TEXTURE.YELLOW1);
        }
        return getTexture(TEXTURE.GRAY1);
	}
}
