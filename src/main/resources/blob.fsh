#version 330 core
in vec2 TexCoords;
out vec4 color;

uniform sampler2D text;
uniform sampler2D colorA;
uniform sampler2D colorB;
uniform sampler2D border;
uniform sampler2D star;
uniform bool colorBVis;
uniform bool borderVis;
uniform bool starVis;

uniform vec2 perlinGrid;
uniform float perlinSeed;
uniform float perlinLayerExp;
uniform int perlinLayers;

uniform float textMinAlpha;

uniform vec2 pixelGrid;

uniform vec2 clusterGrid;
uniform float clusterSeed;

uniform vec2 starGrid;
uniform float starSize;
uniform float starSeed;

// start https://stackoverflow.com/a/17479300
// by Spatial 05 July 2013

// A single iteration of Bob Jenkins' One-At-A-Time hashing algorithm.
uint hash( uint x ) {
    x += ( x << 10u );
    x ^= ( x >>  6u );
    x += ( x <<  3u );
    x ^= ( x >> 11u );
    x += ( x << 15u );
    return x;
}

uint hash( uvec2 v ) { return hash( v.x ^ hash(v.y)                         ); }
uint hash( uvec3 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z)             ); }
uint hash( uvec4 v ) { return hash( v.x ^ hash(v.y) ^ hash(v.z) ^ hash(v.w) ); }

// Construct a float with half-open range [0:1] using low 23 bits.
// All zeroes yields 0.0, all ones yields the next smallest representable value below 1.0.
float floatConstruct( uint m ) {
    const uint ieeeMantissa = 0x007FFFFFu; // binary32 mantissa bitmask
    const uint ieeeOne      = 0x3F800000u; // 1.0 in IEEE binary32

    m &= ieeeMantissa;                     // Keep only mantissa bits (fractional part)
    m |= ieeeOne;                          // Add fractional part to 1.0

    float  f = uintBitsToFloat( m );       // Range [1:2]
    return f - 1.0;                        // Range [0:1]
}

// Pseudo-random value in half-open range [0:1].
float random( float x ) { return floatConstruct(hash(floatBitsToUint(x))); }
float random( vec2  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec3  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
float random( vec4  v ) { return floatConstruct(hash(floatBitsToUint(v))); }
// end https://stackoverflow.com/a/17479300

float fade(float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
}

vec2 fade(vec2 v) {
    return vec2(fade(v.x), fade(v.y));
}

float perlin2s(vec2 p, float seed) {
    vec2 p0 = floor(p);
    vec2 d = p - p0;
    vec2 f = fade(d);

    float v0 = mix(random(vec3(p0 + vec2(0., 0.), seed)), random(vec3(p0 + vec2(1., 0.), seed)), f.x);
    float v1 = mix(random(vec3(p0 + vec2(0., 1.), seed)), random(vec3(p0 + vec2(1., 1.), seed)), f.x);
    return mix(v0, v1, f.y);
}

float perlin2ls(vec2 p, int detail, float seed) {
    float outV = 0;
    float maxV = 0;
    for(int i = 0; i < detail; i++) {
        float powV = pow(perlinLayerExp, i);
        outV += perlin2s(p / powV, seed) * powV;
        maxV += powV;
    }
    return outV / maxV;
}

vec4 getPerlinColor() {
    if(colorBVis)
        return mix(texture(colorA, TexCoords), texture(colorB, TexCoords), perlin2ls(TexCoords * perlinGrid, perlinLayers, perlinSeed));
    else
        return texture(colorA, TexCoords);
}

float squareDistance(vec2 dis) {
    return dis.x * dis.x + dis.y * dis.y;
}

vec4 getTXColor() {
    if(starVis) {
        vec2 starSpace = TexCoords * starGrid;
        vec2 inStarGrid = round(starSpace);

        float amtStar = 0;

        for(int x = -1; x < 2; x++)
            for(int y = -1; y < 2; y++) {
                vec2 agc = inStarGrid + vec2(x, y);
                vec2 startPos = agc + vec2(random(vec4(agc, starSeed, 10.)), random(vec4(agc, starSeed, 11.)));
                float d = starSize - sqrt(squareDistance(startPos - starSpace));
                if(d > 0) return texture(star, TexCoords);
                else if(-d < starSize) amtStar += (1 + d / starSize) / 2;
            }
        
        return mix(getPerlinColor(), texture(star, TexCoords), clamp(amtStar, 0, 1));
    } else
        return getPerlinColor();
}

void main() {

    vec2 inPixelGrid = round(TexCoords * pixelGrid);
    vec2 inClusterSpace = (inPixelGrid / pixelGrid) * clusterGrid;
    vec2 inClusterGrid = round(inClusterSpace);

    vec2 cluster = vec2(0., 0.);
    float clusterDistance = 10;
    
    for(int x = -1; x < 2; x++)
        for(int y = -1; y < 2; y++) {
            vec2 agc = inClusterGrid + vec2(x, y);
            vec2 clusterC = agc + vec2(random(vec4(agc, clusterSeed, 0.)), random(vec4(agc, clusterSeed, 1.)));
            float dis = squareDistance(clusterC - inClusterSpace);
            if(dis < clusterDistance) {
                clusterDistance = dis;
                cluster = clusterC;
            }
        }

    if(texture(text, cluster / clusterGrid).a < textMinAlpha) color = vec4(0., 0., 0., 0.);
    else {
        if(borderVis)
            color = mix(getTXColor(), texture(border, TexCoords), sqrt(clusterDistance) / 1.5);
        else
            color = getTXColor();
    }
    
}
