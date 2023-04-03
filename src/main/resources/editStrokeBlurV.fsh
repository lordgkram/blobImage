#version 330 core
in vec2 TexCoords;
out vec4 color;

uniform sampler2D stroke;
uniform sampler2D blur;
uniform sampler2D original;

uniform float clampAlphaMin;
uniform float clampAlphaMax;

uniform vec4 bcolor;

float map(float value, float min1, float max1, float min2, float max2) {
    return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

void main() {
    vec2 blur_size = textureSize(blur, 0);
    vec2 blur_offset = 1.0 / blur_size;

    vec2 stroke_offset = 1.0 / textureSize(stroke, 0);

    vec4 outcolor = texture(stroke, TexCoords) * texture(blur, vec2(0.0, 0.0)).r;
    float totC = texture(blur, vec2(0.0, 0.0)).r;

    for(int i = 1; i < blur_size.x; i++) {
        outcolor += texture(stroke, TexCoords + vec2(0.0, stroke_offset.y * i))
                * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        totC += texture(stroke, TexCoords + vec2(0.0, stroke_offset.y * i)).a * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        outcolor += texture(stroke, TexCoords - vec2(0.0, stroke_offset.y * i))
                * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
        totC += texture(stroke, TexCoords - vec2(0.0, stroke_offset.y * i)).a * texture(blur, vec2(blur_offset.x * i, 0.0)).r;
    }

    outcolor /= totC;

    float alpha = outcolor.a;

    if(alpha != 0.0) {
        if(clampAlphaMin != clampAlphaMax) {
            if(alpha < clampAlphaMin) alpha = clampAlphaMin;
            if(alpha > clampAlphaMax) alpha = clampAlphaMax;
            alpha = map(alpha, clampAlphaMin, clampAlphaMax, 0.0, 1.0);
        } else {
            if(alpha < clampAlphaMin) alpha = 0.0;
            else alpha = 1.0;
        }
    }
    
    vec4 drawC = vec4(bcolor.rgb, bcolor.a * alpha);
    vec4 origC = texture(original, vec2(TexCoords.x, 1- TexCoords.y));
    color = vec4(mix(drawC, mix(origC, drawC, drawC.a), origC.a).rgb, clamp(origC.a + drawC.a, 0, 1));
}
