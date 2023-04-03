package io.github.lordgkram.blobimg;

import java.awt.Color;

import org.lwjgl.opengl.GL30;

public class Fill {

    public static void fill(GLTexture toFill, Color color) {
        int fillFrameBuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fillFrameBuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, toFill.getGlImmage(), 0);
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            System.out.println("FRAMEBUFFER INCOMPLETE FILL");

        float colorR = (float) color.getRed() / 255f;
        float colorG = (float) color.getGreen() / 255f;
        float colorB = (float) color.getBlue() / 255f;
        float colorA = (float) color.getAlpha() / 255f;
        GL30.glClearColor(colorR, colorG, colorB, colorA);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glDeleteFramebuffers(fillFrameBuffer);
    }
    
}
