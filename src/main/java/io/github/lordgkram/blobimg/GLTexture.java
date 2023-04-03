package io.github.lordgkram.blobimg;

import org.lwjgl.opengl.GL30;

public class GLTexture {

    private int glImmage;
    private int width;
    private int height;

    public GLTexture(int glImmage, int width, int height) {
        this.glImmage = glImmage;
        this.width = width;
        this.height = height;
    }

    public int getGlImmage() {
        return glImmage;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void delete() {
        GL30.glDeleteTextures(glImmage);
    }

    public void bind() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, glImmage);
    }

    public static void bindNone() {
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
    }
    
}
