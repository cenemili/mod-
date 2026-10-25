# The Watcher (Fabric 1.20.1)

Entidad que te acecha: solo se mueve cuando NO la miras. Si te alcanza -> jumpscare.
Si la miras fijo ~7 s, desaparece (y volverá más tarde).

## Compilar
1. Necesitas JDK 17.
2. Falta el Gradle wrapper: copia `gradlew`, `gradlew.bat` y la carpeta `gradle/` de
   https://github.com/FabricMC/fabric-example-mod (rama 1.20) o ejecuta `gradle wrapper`.
3. `./gradlew build` -> el .jar queda en `build/libs/`.
4. Ponlo en `mods/` junto con Fabric Loader 0.15+ y Fabric API 0.92.2+1.20.1.

## Probar
- `/watcher` (op) invoca uno a ~28 bloques.
- De noche o bajo tierra aparece solo (25 % cada 30 s si no hay uno cerca).

## Personalizar
- `textures/entity/watcher.png`  -> skin 64x64 del monstruo.
- `textures/gui/jumpscare.png`   -> imagen del susto (reemplázala por la tuya).
- Sonidos: en `WatcherEntity.jumpscare()` (usa sonidos vanilla; puedes añadir los tuyos con sounds.json).
