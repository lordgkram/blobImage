# Blob Image

## Kompilation
```sh
# generiere Native
./gradlew nativesJar
# generiere Programm
./gradlew shadowJar
```

## Starten
```sh
java -cp build/libs/blobImage-0.1-natives-${platform}.jar:build/libs/blobImage-0.1.jar io.github.lordgkram.blobimg.Main
```

## Bibliotheken
- LWJGL
- LWJGL/OpenGL
- JOML
- LWJGL3-awt
- flatlaf

## Icons
Die genutzten Icons sind von Fontawsome(Free) v6.2.0.

Lizensiert unter [Creativecommons 4.0](https://creativecommons.org/licenses/by/4.0/).

von Fonticons, Inc. ([https://fontawesome.com](https://fontawesome.com))
