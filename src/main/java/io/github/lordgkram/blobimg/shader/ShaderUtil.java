package io.github.lordgkram.blobimg.shader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.opengl.GL30;

public class ShaderUtil {

    public static int compileShader(String code, ShaderType type) {
        int shader = GL30.glCreateShader(type.getGLType());
        GL30.glShaderSource(shader, code);
        GL30.glCompileShader(shader);
        // output error
        int success = GL30.glGetShaderi(shader, GL30.GL_COMPILE_STATUS);
        if(success == 0) {
            String error = GL30.glGetShaderInfoLog(shader);
            String typeS = type.getName();
            throw new IllegalStateException(String.format("ERROR::SHADER_COMPILATION_ERROR of type: %s%n%s%n", typeS, error));
        }
        // done
        return shader;
    }

    private static String readInputStream(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, "utf-8");
        char[] buff = new char[512];
        int read = 0;
        String data = "";
        do {
            read = isr.read(buff, 0, buff.length);
            if(read <= 0) continue;
            data += new String(buff, 0, read);
        } while(read > 0);
        isr.close();
        return data;
    }

    public static void createProgramms(Object obj) throws IOException {
        Field[] fields = obj.getClass().getFields();
        for(Field field : fields) {
            if(field.getType().equals(ShaderProgramm.class)) {
                try {
                    if(field.get(obj) == null) {
                        ShaderProgramm programm = new ShaderProgramm();
                        for(Annotation annotation : field.getAnnotationsByType(LinkShader.class)) {
                            LinkShader ls = (LinkShader) annotation;
                            String shaderSource = "";
                            switch(ls.source()) {
                            case EXTERNAL:
                                shaderSource = readInputStream(Files.newInputStream(Paths.get(ls.data())));
                                break;
                            case INTERNAL:
                                shaderSource = readInputStream(ShaderUtil.class.getResourceAsStream(ls.data()));
                                break;
                            case SOURCE:
                                shaderSource = ls.data();
                                break;
                            }
                            int shader = compileShader(shaderSource, ls.type());
                            programm.attatchShader(shader);
                            GL30.glDeleteShader(shader);
                        }
                        programm.checkProgrammLinkStatus();
                        field.set(obj, programm);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
