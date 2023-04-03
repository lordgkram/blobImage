package io.github.lordgkram.blobimg;

import java.awt.Color;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import io.github.lordgkram.blobimg.shader.ShaderProgramm;

public class Edit {

    private GLTexture original;
    private GLTexture strokeA;
    private GLTexture strokeB;
    private GLTexture strokeBlur;
    private GLTexture target;

    private int fbStrokeA;
    private int fbStrokeB;
    private int fbBlur;
    private int fbOut;

    private ShaderProgramm shaderStrokePrev;    // editStrokePrev
    private ShaderProgramm shaderStrokeDot;     // editStrokeDot
    private ShaderProgramm shaderStrokeBlurH;   // editStrokeBlurH
    private ShaderProgramm shaderStrokeBlurV;   // editStrokeBlurV

    private boolean currStrokeA = false;

    private Color color;
    private float alphaClampMin;
    private float alphaClampMax;
    private float[] gausX;
    private float[] gausY;

    private GLTexture gausXT;
    private GLTexture gausYT;
    private float colorR;
    private float colorG;
    private float colorB;
    private float colorA;

    private int editVerteciesBuffer;
    private int editVerteciesArray;
    private float[] editVertecies = new float[]{
        0, 1,
        1, 0,
        1, 1,

        0, 1,
        0, 0,
        1, 0,
    };
    private int editDisplayVerteciesBuffer;
    private int editDisplayVerteciesArray;
    private float[] editDisplayVertecies = new float[]{
        // xy   texture
        0, 1,   0, 1,
        1, 0,   1, 0,
        1, 1,   1, 1,

        0, 1,   0, 1,
        0, 0,   0, 0,
        1, 0,   1, 0,
    };
    private int displayVerteciesBuffer;
    private int displayVerteciesArray;
    private float[] displayVertecies = new float[]{
        // xy   texture
        0, 1,   0, 0,
        1, 0,   1, 1,
        1, 1,   1, 0,

        0, 1,   0, 0,
        0, 0,   0, 1,
        1, 0,   1, 1,
    };

    public Edit(GLTexture original, ShaderProgramm shaderStrokePrev, ShaderProgramm shaderStrokeDot, ShaderProgramm shaderStrokeBlurH, ShaderProgramm shaderStrokeBlurV, Color color, float alphaClampMin, float alphaClampMax, float[] gausX, float[] gausY) {
        this.original = original;

        // display vertecies
        displayVerteciesArray = GL30.glGenVertexArrays();
        displayVerteciesBuffer = GL30.glGenBuffers();
        GL30.glBindVertexArray(displayVerteciesArray);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, displayVerteciesBuffer);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, 6 * 4 * 4, GL30.GL_DYNAMIC_DRAW);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 4, GL30.GL_FLOAT, false, 4 * 4, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        displayVertecies[ 1] = original.getHeight();
        displayVertecies[ 9] = original.getHeight();
        displayVertecies[13] = original.getHeight();
        displayVertecies[ 4] = original.getWidth();
        displayVertecies[ 8] = original.getWidth();
        displayVertecies[20] = original.getWidth();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, displayVerteciesBuffer);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, displayVertecies);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        // edit vertecies
        editVerteciesArray = GL30.glGenVertexArrays();
        editVerteciesBuffer = GL30.glGenBuffers();
        GL30.glBindVertexArray(editVerteciesArray);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, editVerteciesBuffer);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, editVertecies, GL30.GL_DYNAMIC_DRAW);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 2 * 4, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        // edit displayVertecies
        editDisplayVerteciesArray = GL30.glGenVertexArrays();
        editDisplayVerteciesBuffer = GL30.glGenBuffers();
        GL30.glBindVertexArray(editDisplayVerteciesArray);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, editDisplayVerteciesBuffer);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, 6 * 4 * 4, GL30.GL_DYNAMIC_DRAW);
        GL30.glEnableVertexAttribArray(0);
        GL30.glVertexAttribPointer(0, 4, GL30.GL_FLOAT, false, 4 * 4, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        editDisplayVertecies[ 1] = original.getHeight();
        editDisplayVertecies[ 9] = original.getHeight();
        editDisplayVertecies[13] = original.getHeight();
        editDisplayVertecies[ 4] = original.getWidth();
        editDisplayVertecies[ 8] = original.getWidth();
        editDisplayVertecies[20] = original.getWidth();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, editDisplayVerteciesBuffer);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, editDisplayVertecies);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);


        this.shaderStrokePrev = shaderStrokePrev;
        this.shaderStrokeDot = shaderStrokeDot;
        this.shaderStrokeBlurH = shaderStrokeBlurH;
        this.shaderStrokeBlurV = shaderStrokeBlurV;

        this.color = color;
        this.alphaClampMin = alphaClampMin;
        this.alphaClampMax = alphaClampMax;
        this.gausX = gausX;
        this.gausY = gausY;

        strokeA = ImgTextureUtil.emptyImmage(original.getWidth(), original.getHeight());
        strokeB = ImgTextureUtil.emptyImmage(original.getWidth(), original.getHeight());
        strokeBlur = ImgTextureUtil.emptyImmage(original.getWidth(), original.getHeight());
        target = ImgTextureUtil.emptyImmage(original.getWidth(), original.getHeight());

        fbStrokeA = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbStrokeA);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, strokeA.getGlImmage(), 0);
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            System.out.println("FRAMEBUFFER INCOMPLETE 0A");

        fbStrokeB = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbStrokeB);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, strokeB.getGlImmage(), 0);
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            System.out.println("FRAMEBUFFER INCOMPLETE 0B");

        fbBlur = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbBlur);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, strokeBlur.getGlImmage(), 0);
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            System.out.println("FRAMEBUFFER INCOMPLETE 1");

        fbOut = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbOut);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, target.getGlImmage(), 0);
        if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
            System.out.println("FRAMEBUFFER INCOMPLETE 2");

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        updateInternal();
    }

    public void stop() {
        GL30.glDeleteFramebuffers(fbStrokeA);
        GL30.glDeleteFramebuffers(fbStrokeB);
        GL30.glDeleteFramebuffers(fbBlur);
        GL30.glDeleteFramebuffers(fbOut);

        original.delete();
        strokeA.delete();
        strokeB.delete();
        strokeBlur.delete();
        gausXT.delete();
        gausYT.delete();

        GL30.glDeleteBuffers(editDisplayVerteciesBuffer);
        GL30.glDeleteBuffers(displayVerteciesBuffer);
        GL30.glDeleteBuffers(editVerteciesBuffer);

        GL30.glDeleteVertexArrays(editDisplayVerteciesArray);
        GL30.glDeleteVertexArrays(editVerteciesArray);
        GL30.glDeleteVertexArrays(displayVerteciesArray);
    }

    public void update(float x, float y) {
        // setup Matrix
        Matrix4f projection = new Matrix4f().ortho2D(0, target.getWidth(), 0, target.getHeight());
        Matrix4f object = new Matrix4f();
        
        // draw Cube on Stroke
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, currStrokeA ? fbStrokeA : fbStrokeB);
        GL30.glViewport(0, 0, target.getWidth(), target.getHeight());
        GL30.glClearColor(0f, 0f, 0f, 0f);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
        // - draw prev Stroke
        // -- setup shader
        shaderStrokePrev.use();
        shaderStrokePrev.setMat4("object", object);
        shaderStrokePrev.setMat4("projection", projection);
        // -- bind
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindVertexArray(editDisplayVerteciesArray);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, currStrokeA ? strokeB.getGlImmage() : strokeA.getGlImmage());
        // -- draw
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
        // -- unbind
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        // - cube
        // -- setup shader
        Matrix4f objectBrush = new Matrix4f().translate(x, y, 0);
        shaderStrokeDot.use();
        shaderStrokeDot.setMat4("object", objectBrush);
        shaderStrokeDot.setMat4("projection", projection);
        // -- bind
        GL30.glBindVertexArray(editVerteciesArray);
        // -- draw
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
        // - unbind
        GL30.glBindVertexArray(0);

        // draw Blur horizontal
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbBlur);
        GL30.glViewport(0, 0, target.getWidth(), target.getHeight());
        GL30.glClearColor(0f, 0f, 0f, 0f);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
        // - draw Stroke
        // -- setup shader
        shaderStrokeBlurH.use();
        shaderStrokeBlurH.setMat4("object", object);
        shaderStrokeBlurH.setMat4("projection", projection);
        shaderStrokeBlurH.setInt("stroke", 0);
        shaderStrokeBlurH.setInt("blur", 1);
        // -- bind
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindVertexArray(editDisplayVerteciesArray);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, currStrokeA ? strokeA.getGlImmage() : strokeB.getGlImmage());
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        gausXT.bind();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        // -- draw
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
        // -- unbind
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        GL30.glBindVertexArray(0);

        // draw Blur vertical & prev drawing
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbOut);
        GL30.glViewport(0, 0, target.getWidth(), target.getHeight());
        GL30.glClearColor(0f, 0f, 0f, 0f);
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
        // - draw Stroke
        // -- setup shader
        shaderStrokeBlurV.use();
        shaderStrokeBlurV.setMat4("object", object);
        shaderStrokeBlurV.setMat4("projection", projection);
        shaderStrokeBlurV.setInt("stroke", 0);
        shaderStrokeBlurV.setInt("blur", 1);
        shaderStrokeBlurV.setInt("original", 2);
        shaderStrokeBlurV.setFloat("clampAlphaMin", alphaClampMin);
        shaderStrokeBlurV.setFloat("clampAlphaMax", alphaClampMax);
        shaderStrokeBlurV.setVec4("bcolor", colorR, colorG, colorB, colorA);
        // -- bind
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        GL30.glBindVertexArray(displayVerteciesArray);
        strokeBlur.bind();
        GL30.glActiveTexture(GL30.GL_TEXTURE1);
        gausYT.bind();
        GL30.glActiveTexture(GL30.GL_TEXTURE2);
        original.bind();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        // -- draw
        GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);

        // unbind / finish
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
        GL30.glBindVertexArray(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        currStrokeA = !currStrokeA;
    }

    public GLTexture getTarget() {
        return target;
    }

    public void setAlphaClampMax(float alphaClampMax) {
        this.alphaClampMax = alphaClampMax;
    }

    public void setAlphaClampMin(float alphaClampMin) {
        this.alphaClampMin = alphaClampMin;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setGausX(float[] gausX) {
        this.gausX = gausX;
    }

    public void setGausY(float[] gausY) {
        this.gausY = gausY;
    }

    public void updateInternal() {
        if(gausXT != null) gausXT.delete();
        gausXT = ImgTextureUtil.foatArrayToImmage(gausX);
        if(gausYT != null) gausYT.delete();
        gausYT = ImgTextureUtil.foatArrayToImmage(gausY);
        colorR = (float) color.getRed() / 255f;
        colorG = (float) color.getGreen() / 255f;
        colorB = (float) color.getBlue() / 255f;
        colorA = (float) color.getAlpha() / 255f;
    }
    
}
