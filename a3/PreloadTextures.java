package a3;

import java.io.IOException;

import ray.rage.asset.texture.Texture;

public class PreloadTextures {
	MyGame g;
	private Texture
		blue1,
		blue2,
		red1,
		green1,
		yellow1,
		gray1,
		n1, n2, n3, n4, n5, n6, n7, n8, n9, n0, colon,
		background,
		car1, car2, car3, car4, car5, car6, car7, car8
	;
	
	enum TEXTURE {
		BLUE1,
		BLUE2,
		RED1,
		GREEN1,
		YELLOW1,
		GRAY1,
		N1, N2, N3, N4, N5, N6, N7, N8, N9, N0, COLON,
		BACKGROUND,
		CAR1, CAR2, CAR3, CAR4, CAR5, CAR6, CAR7, CAR8
	}

	public PreloadTextures(MyGame myGame) {
		this.g = myGame;
		loadTextures();
		System.out.println("Textures loaded");
	}

	private void loadTextures() {
		try {
			blue1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("blue1Tile.png");
			blue2 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("blue2Tile.png");
			red1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("red1Tile.png");
			green1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("green1Tile.png");
			yellow1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("yellow1Tile.png");
			gray1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("gray1Tile.png");
			n1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("1.png");
			n2 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("2.png");
			n3 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("3.png");
			n4 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("4.png");
			n5 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("5.png");
			n6 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("6.png");
			n7 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("7.png");
			n8 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("8.png");
			n9 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("9.png");
			n0 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("0.png");
			car1 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car1.png");
			car2 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car2.png");
			car3 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car3.png");
			car4 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car4.png");
			car5 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car5.png");
			car6 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car6.png");
			car7 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car7.png");
			car8 = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("car8.png");
			colon = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("colon.png");
			background = g.getEngine().getSceneManager().getTextureManager().getAssetByPath("timerBackground.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Texture getTexture(int num) {
		switch(num) {
		case 0:
			return n0;
		case 1:
			return n1;
		case 2:
			return n2;
		case 3:
			return n3;
		case 4:
			return n4;
		case 5:
			return n5;
		case 6:
			return n6;
		case 7:
			return n7;
		case 8:
			return n8;
		case 9:
			return n9;
		}
		return null;
	}
	
	public Texture getCarTexture(int num) {
		switch(num) {
		case 1:
			return car1;
		case 2:
			return car2;
		case 3:
			return car3;
		case 4:
			return car4;
		case 5:
			return car5;
		case 6:
			return car6;
		case 7:
			return car7;
		case 8:
			return car8;
		}
		return null;
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
		case N1:
			return n1;
		case N2:
			return n2;
		case N3:
			return n3;
		case N4:
			return n4;
		case N5:
			return n5;
		case N6:
			return n6;
		case N7:
			return n7;
		case N8:
			return n8;
		case N9:
			return n9;
		case N0:
			return n0;
		case COLON:
			return colon;
		case BACKGROUND:
			return background;
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
