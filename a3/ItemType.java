package a3;

import java.util.Random;

public enum ItemType {
    BANANA,
    // GREEN_SHELL,
    ;
	
	public static int getValue(ItemType type) {
		return type.ordinal();
	}
	
	public static ItemType getType(int type) {
		return values()[type];
	}
    
    public static ItemType getRandomItemType() {
        Random random = new Random();
        return values()[random.nextInt(values().length)];
    }
}
