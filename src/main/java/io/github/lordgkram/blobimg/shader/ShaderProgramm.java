package io.github.lordgkram.blobimg.shader;

import java.nio.FloatBuffer;

import org.joml.Matrix2f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

public class ShaderProgramm {

    private int glProgramm = GL30.glCreateProgram();

    public void attatchShader(int glShader) {
        GL30.glAttachShader(glProgramm, glShader);
    }

    public void use() {
        GL30.glUseProgram(glProgramm);
    }

    public void checkProgrammLinkStatus() {
        GL30.glLinkProgram(glProgramm);
        // output error
        int success = GL30.glGetProgrami(glProgramm, GL30.GL_LINK_STATUS);
        if(success == 0) {
            String error = GL30.glGetProgramInfoLog(glProgramm);
            throw new IllegalStateException(String.format("ERROR::PROGRAM_LINKING_ERROR of type: PROGRAMM%n%s%n", error));
        }
    }

    public void setBoolean(String name, boolean value) {
        GL30.glUniform1i(GL30.glGetUniformLocation(glProgramm, name), value ? 1 : 0);
    }

    public void setInt(String name, int value) {
        GL30.glUniform1i(GL30.glGetUniformLocation(glProgramm, name), value);
    }

    public void setFloat(String name, float value) {
        GL30.glUniform1f(GL30.glGetUniformLocation(glProgramm, name), value);
    }

    public void setVec2(String name, float x, float y) {
        GL30.glUniform2f(GL30.glGetUniformLocation(glProgramm, name), x, y);
    }

    public void setVec2(String name, Vector2f value) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(2);
        fb = value.get(fb);
        GL30.glUniform2fv(GL30.glGetUniformLocation(glProgramm, name), fb);
    }

    public void setVec3(String name, float x, float y, float z) {
        GL30.glUniform3f(GL30.glGetUniformLocation(glProgramm, name), x, y, z);
    }

    public void setVec3(String name, Vector3f value) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(3);
        fb = value.get(fb);
        GL30.glUniform3fv(GL30.glGetUniformLocation(glProgramm, name), fb);
    }

    public void setVec4(String name, float x, float y, float z, float w) {
        GL30.glUniform4f(GL30.glGetUniformLocation(glProgramm, name), x, y, z, w);
    }

    public void setVec4(String name, Vector4f value) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb = value.get(fb);
        GL30.glUniform4fv(GL30.glGetUniformLocation(glProgramm, name), fb);
    }

    public void setMat2(String name, Matrix2f mat) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb = mat.get(fb);
        GL30.glUniformMatrix2fv(GL30.glGetUniformLocation(glProgramm, name), false, fb);
    }

    public void setMat3(String name, Matrix3f mat) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(9);
        fb = mat.get(fb);
        GL30.glUniformMatrix3fv(GL30.glGetUniformLocation(glProgramm, name), false, fb);
    }

    public void setMat4(String name, Matrix4f mat) {
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        fb = mat.get(fb);
        GL30.glUniformMatrix4fv(GL30.glGetUniformLocation(glProgramm, name), false, fb);
    }
    
}
