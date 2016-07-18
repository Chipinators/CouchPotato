package paperprisoners.couchpotato;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Static class used to globally store Typefaces.
 * Kind of a design faux paux but simplifies things a lot.
 *
 * Created by Ian on 7/14/2016.
 */
public class TypefaceManager {

    private static HashMap<String, Typeface> fonts = new HashMap<>();

    private static final int EXTENSION_OFFSET = 3;
    private static final String FONT_EXTENSION = "ttf";
    private static final String ASSETS_DIRECTORY = "main/assets/";

    //Private constructor to disable instantiation
    private TypefaceManager() {
    }


    //RETRIEVAL METHODS

    public static Typeface get(String name) {
        Typeface t = fonts.get(name);
        if (t != null)
            return t;
        else {
            return Typeface.DEFAULT;
        }
    }


    //SETUP METHODS

    protected static void manualLoadFonts(AssetManager manager) {
        try {
            fonts.put("Oswald-Light", Typeface.createFromAsset(manager, "font/oswald/Oswald-Light.ttf"));
            fonts.put("Oswald-Regular", Typeface.createFromAsset(manager, "font/oswald/Oswald-Regular.ttf"));
            fonts.put("Oswald-Bold", Typeface.createFromAsset(manager, "font/oswald/Oswald-Bold.ttf"));
            fonts.put("Kreon-Light", Typeface.createFromAsset(manager, "font/kreon/Kreon-Light.ttf"));
            fonts.put("Kreon-Regular", Typeface.createFromAsset(manager, "font/kreon/Kreon-Regular.ttf"));
            fonts.put("Kreon-Regular", Typeface.createFromAsset(manager, "font/kreon/Kreon-Bold.ttf"));
            fonts.put("PassionOne-Light", Typeface.createFromAsset(manager, "font/passion_one/PassionOne-Regular.ttf"));
            fonts.put("PassionOne-Regular", Typeface.createFromAsset(manager, "font/passion_one/PassionOne-Bold.ttf"));
            fonts.put("PassionOne-Bold", Typeface.createFromAsset(manager, "font/passion_one/PassionOne-Black.ttf"));
            fonts.put("CabinCondensed-Light", Typeface.createFromAsset(manager, "font/cabin_condensed/CabinCondensed-Regular.ttf"));
            fonts.put("CabinCondensed-Regular", Typeface.createFromAsset(manager, "font/cabin_condensed/CabinCondensed-Medium.ttf"));
            fonts.put("CabinCondensed-SemiBold", Typeface.createFromAsset(manager, "font/cabin_condensed/CabinCondensed-SemiBold.ttf"));
            fonts.put("CabinCondensed-Bold", Typeface.createFromAsset(manager, "font/cabin_condensed/CabinCondensed-Bold.ttf"));
        } catch (Exception e) {
        }
    }

    protected static void loadFonts(AssetManager manager, String directory) {
        String[] fontPaths = findFonts(manager, directory);
        for (String path : fontPaths) {
            Typeface typeface = Typeface.createFromAsset(manager, path);
            String name = generateName(path);
            fonts.put(name, typeface);
        }
    }

    private static String[] findFonts(AssetManager manager, String directory) {
        //Gets all of the font paths you'll need
        ArrayList<String> fontList = new ArrayList<>();
        crawlFontPaths(directory, fontList, manager);
        String[] fontArray = new String[fontList.size()];
        for (int i = 0; i < fontArray.length; i++)
            fontArray[i] = fontList.get(i);
        return fontArray;
    }

    private static void crawlFontPaths(String location, ArrayList<String> fontList, AssetManager manager) {
        try {
            String[] list = manager.list(location);
            if (list.length > 0) {
                //If path at location is a directory
                for (String file : list)
                    crawlFontPaths(location+'/'+file, fontList, manager);
            } else {
                //If path a location is a file
                String extension = location;
                extension = extension.substring(extension.length() - EXTENSION_OFFSET);
                Log.v("TFM", "Ext... "+extension);
                if (extension.equalsIgnoreCase(FONT_EXTENSION)) {
                    String path = location;
                    int index = path.indexOf(ASSETS_DIRECTORY);
                    if (index >= 0)
                        path = path.substring(index + ASSETS_DIRECTORY.length());
                    fontList.add(path);
                    Log.v("TFM", "Font added!");
                }
            }
        }
        catch (IOException e) { }
    }

    private static String generateName(String path) {
        //Tries to find directories
        int index = path.indexOf('/');
        if (index < 0)
            index = path.indexOf('\\');
        //Then figures out what to do
        if (index > 0)
            return generateName(path.substring(index + 1));
        else {
            index = path.indexOf('.');
            return path.substring(0, index);
        }
    }

}
