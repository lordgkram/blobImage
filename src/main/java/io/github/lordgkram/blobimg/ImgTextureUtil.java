package io.github.lordgkram.blobimg;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

public class ImgTextureUtil {

    private static final ByteBuffer nullBuff = null;

    public static GLTexture foatArrayToImmage(float[] array) {
        int textT = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textT);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_R32F, array.length, 1, 0, GL30.GL_RED, GL30.GL_FLOAT, array);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        return new GLTexture(textT, array.length, 1);
    }

    public static GLTexture emptyImmage(int width, int height) {
        int textT = GL30.glGenTextures();
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textT);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, nullBuff);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        return new GLTexture(textT, width, height);
    }

    public static GLTexture textureFromBufferedImmage(BufferedImage img) {
        int textT = GL30.glGenTextures();
        textureDataFromBufferedImmage(textT, img);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textT);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        return new GLTexture(textT, img.getWidth(), img.getHeight());
    }

    public static void textureDataFromBufferedImmage(int texture, BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);
        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);

        for(int i = 0; i < w * h; i++) {
            int pixel = pixels[i];
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }

        buffer.flip();
        
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA8, w, h, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }
    
}
