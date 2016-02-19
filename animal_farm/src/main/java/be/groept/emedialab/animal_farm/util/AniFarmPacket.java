package be.groept.emedialab.animal_farm.util;

import java.io.Serializable;

/**
 * Created by Yoika on 24/11/2015.
 */
public class AniFarmPacket implements Serializable{

    private int soundId;
    private int imageId;

    public AniFarmPacket(int soundId, int imageId){
        this.soundId = soundId;
        this.imageId = imageId;
    }

    public int getSoundId(){
        return soundId;
    }

    public int getImageId(){
        return imageId;
    }
}
