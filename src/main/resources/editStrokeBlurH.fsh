#version 330 core
in vec2 TexCoords;
out vec4 color;

uniform sampler2D stroke;
uniform sampler2D blur;

void main() {
    vec2 blur_size = textureSize(blur, 0);
    vec2 blur_offset = 1.0 / blur_size;

    vec2 stroke_offset = 1.0 / textureSize(stroke, 0);

    vec4 outcolor = texture(stroke, TexCoords) * texture(blur, vec2(0.0, 0.0)).r;
    float totC = texture(blur, vec2(0.0, 0.0)).r;

    for(int i = 1; i < blur_size.x; i++) {
        outcolor += texture(stroke, TexCoords + vec2(stroke_offset.x * i, 0.0))
                * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        totC += texture(stroke, TexCoords + vec2(stroke_offset.x * i, 0.0)).a * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        outcolor += texture(stroke, TexCoords - vec2(stroke_offset.x * i, 0.0))
                * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        totC += texture(stroke, TexCoords - vec2(stroke_offset.x * i, 0.0)).a * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
    }

    outcolor /= totC;
    
    color = outcolor;
}
