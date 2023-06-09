package io.github.lordgkram.blobimg.shader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(LinkShaders.class)
public @interface LinkShader {

    String data();
    ShaderType type();
    ShaderSource source() default ShaderSource.INTERNAL;
    
}
