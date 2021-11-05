# Empezando con PikkuAcademy
En los siguientes apartados vas a aprender a cómo interactuar con el dispositivo Pikku y extraer información de todos sus recursos hardware. Para ello, te ofrecemos una librería ya preparada que facilita el acceso al dispositivo y te proporciona todos los datos.

## Aplicaciones de ejemplo

Este repositorio incluye 4 aplicaciones de demostración que ilustra el uso de esta biblioteca.
[Ver código](https://github.com/blautic/pikkuAcademy/tree/master/Examples/)

## Incorporación a tu proyecto

Lee detenidamente los siguientes puntos para que su integración en tus aplicaciones sea todo un éxito.

### Dependencias 

Para poder usar la librería simplemente agrega la dependencia a tu `build.gradle` archivo de proyecto :

<pre><code>dependencies {
    ...
    implementation "com.blautic:pikkuAcademy:$version"
    ...
</code></pre>

La última versión es: ![pikkuAcademy](https://maven-badges.herokuapp.com/maven-central/com.blautic/pikkuAcademy/badge.svg)

### Permisos

Es importante que tu aplicación gestione correctamente los permisos de usuario necesarios.

Android desde API 23 (6.0 / Marshmallow) requiere permisos de ubicación declarados en el manifiesto para que una aplicación para trabajar con bluetooth. Pikku Academy ya proporciona todos los permisos de bluetooth necesarios para usted en AndroidManifest.


| desde API | a API | Permisos de tiempo de ejecución                              |
|:---:|:---:| --- |
| 18 | 22 | (No se necesitan permisos de tiempo de ejecución)            |
| 23 | 28 | Uno de los siguientes::<br>- `android.permission.ACCESS_COARSE_LOCATION`<br>- `android.permission.ACCESS_FINE_LOCATION` |
| 29 | actual | - `android.permission.ACCESS_FINE_LOCATION` |


### Instanciar 

Para poder interaccionar con la librería necesitamos crear un objeto PikkuAcademy de la siguiente forma:




```java
PikkuAcademy pikkuAcademy = PikkuAcademy.getInstance(this);
```
Habilitar la depuración

```java
pikkuAcademy.enableLog();
```


## Comunicaciones

En los siguientes puntos te contamos como usar la variada funcionalidad de la librería

### Escanear

Una de las primeras necesidades que vas a tener en cualquier aplicación es descubrir los dispositivos Pikku que tienes a tu alrededor e identificar el que quieras usar para conectar.

Para ello tienes dos posibilidades de escan:
1. Identificar dispositivo pikku que mantenga presionado el botón 1, pasamos el primer parametro true para indicar que solo detecte dispositivos pikku con el botón 1 presionado 

   ```java
   pikkuAcademy.scan(true, new ScanCallback() {
       @Override
       public void onScan(ScanInfo scanInfo) {
          pikkuAcademy.saveDevice(scanInfo) // guardar dispositivo para futuras conexiones 
       }
   });
   ```

2. Filtrar y mostrar todos los dispositivos pikku disponibles con su información asociada, pasamos el primer parametro false para indicar que detecte todos los dispositivos pikku disponibles

   ```java
   pikkuAcademy.scan(false, new ScanCallback() {
       @Override
       public void onScan(ScanInfo scanInfo) {
   	 
       }
   });
   ```



ScanInfo tiene información relevante del dispositivo como: dirección mac, nombre, versión de firmware, rssi, nivel de batería, estado del botón 1 y 2, grupo, code y number. 

Puedes leer todos estos parámetros del dispositivo sin necesidad de conectarte por lo que podrías diseñar aplicaciones de qué personas o qué objetos están a tu alrededor en cada momento simplemente gestionando la información proporcionada.



### Conectar

Si queremos usar los recursos de los sensores de forma específica de un Pikku tenemos que conectarnos del siguiente modo. La librería te devolverá la información en el código del Callback que le proporcionemos.

Para conectarse a un dispositivo Pikku pasando su dirección mac como parámetro.

```java
pikkuAcademy.connect("00:00:00:00:00", new ConnectionCallback() {
	@Override
	public void onConnect(ConnectionState state) {
		if(state == ConnectionState.CONNECTED){
           // habilitar sensores
		}
	}
});
```
Para conectarse al dispositivo guardado en la configuración de la librería

```java 
pikkuAcademy.connect(new ConnectionCallback() {
	@Override
	public void onConnect(ConnectionState state) {
		if(state == ConnectionState.CONNECTED){
           // habilitar sensores
		}

	}
});
```

ConnectionState tiene tres estados **CONNECTED**, **DISCONNECTED**, **FAILED**. Habilitar los sensores a partir del estado CONNECTED.



# Funcionalidad

En el siguiente apartado se detallan cómo hacer uso de la funcionalidad del dispositivo una vez conectados tal y como se describe en el punto anterior.

### Estado del dispositivo

Puedes recibir información del estado del dispositivo de forma periódica.

Su petición devuelve un StatusDevice que muestra información del dispositivo conectado:

* Porcentaje de batería:

* Estado del Led ( 0: apagado, 1: encendido, 2: parpadeo) ,

* Estado del motor (true: encendido , false: apagado )

* Período de transmisión ( valor en milisegundos )

* Estado de los sensores ( 0: inactivo, 1: Sensores habilitados enviando datos) 

  

```java
pikkuAcademy.readStatusDevice(new StatusDeviceCallback() {
    @Override
    public void onReadSuccess(StatusDevice statusDevice) {
        Log.d("status:" , statusDevice.toString());
    }
});
```




### Acelerómetro

Para habilitar o deshabilitarla transmicion de los sensores 

```java
pikkuAcademy.enableReportSensors(true);
```

Obetener datos del acelerómetro en sus 3 ejes

```java 
pikkuAcademy.readAccelerometer(new AccelerometerCallback() {
	@Override
	public void onReadSuccess(float x, float y, float z) {
	
	}
	@Override
	public void onReadAngles(float xy, float zy, float xz) {
	
	}
});
        
```

onReadSuccess:  Valor de la aceleración en g’s (1g equivale a 9,8m/sg2) del eje **x, y, z** 

onReadAngles: Grados de inclinacion del plano **xy, zy, xz**



Ver el manual de los dispositivos Pikku para conocer los ejes del acelerómetro [Manual](/manual.md)



La escala por defecto es 4g, puedes cambiar la configuración por defecto al iniciar el acelerómetro pasando AccScale que tiene definido 4 escalas 2g, 4g, 8g, 16g.

Para cambiar la escala por defecto:

```java 
pikkuAcademy.changeDefaultAccelerometerScale(AccScale.ACC_SCALE_2G);
```

Ideas: puedes usar esta información para ofrecer al usuario de tu aplicación funcionalidad interesante:

* Intensidad de movimiento
* Vibración/Balanceo del cuerpo/objetos
* Impactos
* Inclinación de objetos
* Ángulos de posturas del cuerpo
* Detección de caídas
* Detección de cuerpo sin movimiento
* Posición del cuerpo: tumbado, sentado, de pie

Puedes ver parte de esta funcionalidad en la APP PikkuAcademy Activity en [Ver codigo](https://github.com/blautic/pikkuAcademy/tree/master/Examples/pikkuAcademyActitivy/)


### Giroscopio

Para habilitar o deshabilitar la transmisión de los sensores

```java
pikkuAcademy.enableReportSensors(true);
```

Activar el accelerometro y obtener datos del gyroscopio en su 3 ejes:

```java 
pikkuAcademy.readGyroscope(new GyroscopeCallback() {
	@Override
	public void onReadSuccess(float x, float y, float z) {
	}
});
```

onReadSuccess:  Velocidad angular en o/sg (grados por segundo) de los ejes **x, y, z**

Ver el manual de los dispositivos Pikku para conocer los ejes del Gyroscopio [Manual](/manual.md)



La escala por defecto es 1000dps, para cambiar la escala al iniciar el Gyroscopio pase GyrScale que tiene definido 4 escalas 250dps, 500dps, 1000dps, 2000dps.

Cambiar la escala por defecto:

```java 
pikkuAcademy.changeDefaultGyroscopeScale(GyrScale.GYR_SCALE_500);
```

Ideas: puedes usar esta información para ofrecer al usuario de tu aplicación funcionalidad interesante.

* Velocidad angular de los movimientos de extremidades
* Medir frecuencias de giro
* Medir intensidad de rotación
* Generar acciones en el teléfono mediante el movimiento de rotación de la muñeca


### Rssi

Obteniendo el rssi de la conexión puedes apreciar la calidad de la conexión avisando a los usuarios.

Obtener el Rssi del dispositivo.

```java
pikkuAcademy.readRssiConnectedDevice(new RssiCallback() {
    @Override
    public void onReadSuccess(int rssi) {
        Log.d("Rssi:", String.valueOf(rssi));
    }
});
```



### Botones

Obtener el estado de los botones:



```
pikkuAcademy.readButtons(new ButtonsCallback() {
    @Override
    public void onReadSuccess(int nButton, boolean pressed, int duration) {
        
    }
});
```

nButton: 1: botón 1, 2: botón 2

pressed: true: presionado, false: no presionado

duration: duración en mili segundos que el botón esta presionado

Ver el manual para conocer la distribución de los botones [Manual](/manual.md)

Ideas: puedes usar esta información para ofrecer al usuario de tu aplicación funcionalidad interesante.

* Botón de emergencia
* Ejecutar acciones remotas sobre el teléfono
* Dispositivo controlador de juegos
* Abrir puertas o accionar dispositivos
* Selección por pulsación rápida de turno de concursante en concursos de preguntas
* Control sistema de turnos en tablet
* Control de marcadores deportivos

### Motor

El uso del motor te puede permitir avisar al usuario en diferentes escenarios de la aplicación: empiece o acabe una tarea, notificación de recepción de llamada, qué dispositivo usar, ... hay que tener presente su alto consumo de batería y usarlo adecuadamente.

Encender Motor:
```java
pikkuAcademy.startEngine();
```
Apagar Motor:
```java
pikkuAcademy.stopEngine();
```

### Led

El led se puede controlar de las siguientes tres formas:

Encender Led:
```java
pikkuAcademy.turnOnLed()
```
Apagar Led:
```java
pikkuAcademy.turnOffLed();
```
Parpadeo: genera un parpadeo constante con un periodo de 2 segundos

```java
pikkuAcademy.flashingLed();

```

Puedes usar el led para notificar silenciosamente la llegada de información al teléfono, el estado de conexión, el inicio de acciones, el final de un temporizador, ...

### Período de transmisión

Modificar el valor en ms del período de envío de datos que recibe el dispositivo central, valor maximo 255 ms, valor minimo recomendado 5ms. Afecta a los datos de los sensores.

```java
pikkuAcademy.changeTransmittingPeriod(period);
```
