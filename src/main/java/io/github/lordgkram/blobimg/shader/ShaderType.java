package io.github.lordgkram.blobimg.shader;

import org.lwjgl.opengl.GL30;

public enum ShaderType {
    VERTEX(GL30.GL_VERTEX_SHADER, "vertex"), FRAGMENT(GL30.GL_FRAGMENT_SHADER, "fragment");
    private int type;
    private String name;

    private ShaderType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getGLType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
