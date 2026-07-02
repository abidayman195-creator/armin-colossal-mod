# Armin's Colossal Mod (Fabric 1.21.1 + GeckoLib)

Mod de Java Edition que añade el Armin's Colossal de Danny's AoT como una entidad real,
usando GeckoLib para el modelo y las animaciones.

## Qué incluye

- Entidad `ArminsColossalEntity` (hostil, 5000 HP, lenta, daño alto, hitbox de 12x60 bloques)
- Modelo convertido desde `colossal.geo.json` (Bedrock) a formato GeckoLib
- Texturas `armins_colossal.png` / `armins_colossal_e.png` (las mismas del resource pack)
- Animaciones `idle`, `walk`, `attack` — **IMPORTANTE: son un placeholder genérico que escribí
  a mano para que compile**, no son las animaciones reales del titán. Debes reemplazar
  `src/client/resources/assets/colossalmod/animations/entity/armins_colossal.animation.json`
  por las tuyas (exportadas de Blockbench) cuando las tengas.

## Antes de compilar: revisa el modelo en Blockbench

El `armins_colossal.geo.json` viene de una conversión automática del formato Bedrock al
formato GeckoLib. La estructura de bones y UVs se mantuvo igual, pero **recomiendo
abrirlo en Blockbench con el plugin de GeckoLib instalado** para comprobar visualmente que:

1. El modelo se ve bien (proporciones, sin cubos desplazados)
2. Los pivotes de los bones permiten animar correctamente brazos/piernas/cabeza
3. La textura se mapea bien sobre el modelo

Si algo se ve mal, ajusta y reexporta el `.geo.json` y el `.png` desde Blockbench
directamente — sustituye los archivos en `src/client/resources/assets/colossalmod/`.

## Requisitos para compilar

- **Java JDK 21** (no solo el JRE — necesitas el compilador `javac`)
- Conexión a internet (Gradle descargará Fabric Loader, Fabric API, GeckoLib y las
  Minecraft mappings automáticamente la primera vez)
- ~2GB de espacio libre para las dependencias de Gradle

## Cómo compilar

Desde la carpeta raíz del proyecto (donde está `build.gradle`):

```bash
# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

La primera vez tardará varios minutos porque Gradle descarga todo. El `.jar` final
aparecerá en:

```
build/libs/colossalmod-1.0.0.jar
```

Ese es el archivo que metes en la carpeta `mods/` de tu instalación de Fabric
(junto con `fabric-api` y `geckolib-fabric` como dependencias, que ya tienes).

## Cómo probarlo

1. Instala Fabric Loader 0.16.9+ para Minecraft 1.21.1
2. Copia a `mods/`:
   - `colossalmod-1.0.0.jar` (el que compilaste)
   - `fabric-api-0.116.12+1.21.1.jar` (el que ya tienes)
   - `geckolib-fabric-1.21.1-4.9.1.jar` (el que ya tienes)
3. Abre el juego, crea un mundo, y haz spawn del titán con:
   ```
   /summon colossalmod:armins_colossal
   ```

## Cosas que probablemente quieras ajustar después

- **Balance**: HP, daño, velocidad — están en `ArminsColossalEntity.createColossalAttributes()`
- **Hitbox**: tamaño en `ModEntities.java` (`EntityDimensions.fixed(ancho, alto)`)
- **Sonido del rugido**: el `colossal_roar.ogg` no está conectado todavía a un
  `SoundEvent` registrado — hay que registrar un `SoundEvent` propio y un
  `sounds.json` en `assets/colossalmod/sounds.json` apuntando al ogg, y usarlo
  en `getAmbientSound()`.
- **Animaciones reales**: sustituir el placeholder como se explica arriba
- **Loot table / spawn rules**: no están definidas aún, el titán no suelta nada
  ni spawnea naturalmente — solo por `/summon`

## Estructura del proyecto

```
colossal_mod/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/
│   ├── main/                          (código común server+client)
│   │   ├── java/com/dannysaot/colossalmod/
│   │   │   ├── ColossalMod.java       (entrypoint principal)
│   │   │   └── entity/
│   │   │       ├── ArminsColossalEntity.java
│   │   │       └── ModEntities.java   (registro)
│   │   └── resources/
│   │       ├── fabric.mod.json
│   │       └── assets/colossalmod/lang/en_us.json
│   └── client/                        (código solo-cliente: render)
│       ├── java/com/dannysaot/colossalmod/client/
│       │   ├── ColossalModClient.java (entrypoint cliente)
│       │   ├── ArminsColossalModel.java
│       │   └── ArminsColossalRenderer.java
│       └── resources/assets/colossalmod/
│           ├── geo/entity/armins_colossal.geo.json
│           ├── textures/entity/armins_colossal.png
│           ├── textures/entity/armins_colossal_e.png
│           └── animations/entity/armins_colossal.animation.json
```
