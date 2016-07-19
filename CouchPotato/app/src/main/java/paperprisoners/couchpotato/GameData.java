package paperprisoners.couchpotato;

import android.graphics.Bitmap;

/**
 * Created by Ian on 7/17/2016.
 */
public class GameData {

    protected String name;
    protected Bitmap logo;
    protected int minPlayers, maxPlayers;

    public GameData(String name, Bitmap logo, int minPlayers, int maxPlayers) {
        this.name = name;
        this.logo = logo;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public GameData(Bitmap logo, int minPlayers, int maxPlayers) {
        this(null, logo, minPlayers, maxPlayers);
    }

    public GameData(int minPlayers, int maxPlayers) {
        this(null, null, minPlayers, maxPlayers);
    }
}
