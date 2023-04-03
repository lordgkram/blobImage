package io.github.lordgkram.blobimg;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FreeType;

public class FTUtil {

    private PointerBuffer freeTypePointer;
    private long freeType;
    private MemoryStack stack;

    private Map<String, Long> fontMap;
    private String activeFont;

    public FTUtil() {
        stack = MemoryStack.stackPush();
        freeTypePointer = stack.mallocPointer(1);
        int freetypeError = FreeType.FT_Init_FreeType(freeTypePointer);
        if(freetypeError != FreeType.FT_Err_Ok) {
            throw new IllegalStateException("Failed to initialize FreeType: " + FreeType.FT_Error_String(freetypeError));
        }
        freeType = freeTypePointer.get(0);
        fontMap = new HashMap<>();
        // TODO: delete/stop function
        if(activeFont.equals("activeFont")); // TODO: RMME
    }

    public boolean setFont(String name) {
        if(!fontMap.containsKey(name)) return false; // font not loaded
        activeFont = name;
        // TODO: clear bitmaps
        loadAsciiChars();
        return true;
    }

    public boolean loadFont(String name, String path) {
        if(fontMap.containsKey(name)) return false; // alredy existent
        PointerBuffer facePointer = stack.mallocPointer(1);
        FreeType.FT_New_Face(freeType, path, fontMap.size(), facePointer);
        long face = facePointer.get(0);
        fontMap.put(name, face);
        return false;
    }

    public void loadAsciiChars() {
        for(int i = 0x20; i < 0x7F; i++) loadChar((char) i);
    }

    public boolean loadChar(char c) {
        return false;
    }

    public void printVersion() {
        IntBuffer major = stack.mallocInt(1);
        IntBuffer minor = stack.mallocInt(1);
        IntBuffer patch = stack.mallocInt(1);

        FreeType.FT_Library_Version(freeType, major, minor, patch);
        System.out.printf("FreeType version: %d.%d.%d%n", major.get(0), minor.get(0), patch.get(0));
    }
    
}
