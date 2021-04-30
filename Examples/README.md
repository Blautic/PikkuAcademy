# Ejemplos
En este bloque de la documentación te presentamos código real que te va a ayudar en tus aplicaciones. Te ofrecemos código para implementar una app con la configuración mínima (Base), código con toda la funcionalidad completa (Full) y dos aplicaciones que usan la funcionalidad de los dispositivos para proporcionar datos y funciones extra a los usuarios (Activity y Snake).

Lee detenidamente sus características ya que durante su lectura te proponemos mejoras y acceso a información adicional.


## Aplicación Base

Esta aplicación consta del código necesario mínimo para el uso del sistema: batería, conexión y ajustes. Usa este código para no tener que empezar desde cero.  [Ver código](https://github.com/blautic/pikkuAcademy/tree/master/Examples/pikkuAcademyBase/)

<div style="text-align:center"><img src="/images/base.png " width="240"></div>


- Detectamos dispositivos que mantengan presionado el botón 1 y lo guardamos en la configuración del la Liberia 

```java
pikkuAcademy.scan(true, scanInfo -> {
	binding.buttonSave.setOnClickListener(v ->{
    pikkuAcademy.saveDevice(scanInfo);
    });
}); 
```

- Despues de establecer la conexión solicitamos el rssi y el estado actual de la batería, led, motor, etc

```java 
pikkuAcademy.connect(state -> {
	switch(state) {
      case CONNECTED: {
          readValues();
      break;
      }          
      case DISCONNECTED:
      case FAILED: {
      break;
      }
    }
});
```

```java
private void readValues() {
	pikkuAcademy.readRssiConnectedDevice(rssi -> {
      Log.d("rssi:", rssi);
    });

    pikkuAcademy.readStatusDevice(statusDevice -> {
      Log.d("readStatusDevice", statusDevice.toString());
      updateBatteryLevel(statusDevice.battery);
    });
}
private void updateBatteryIU(int value) {
    binding.battery.setImageResource(
            value > 75 ? R.drawable.ic_battery_3 :
                    value > 50 ? R.drawable.ic_battery_2 :
                            value > 25 ? R.drawable.ic_battery_1 :
                                    R.drawable.ic_battery_0);
}
```



# Full
<a href="https://play.google.com/store/apps/details?id=com.blautic.pikkuacademyfull"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="75"></a>


Toda la funcionalidad de los Pikkus disponible en la aplicación de Ejemplo de a que podrás extraer bloques de código [Ver codigo](https://github.com/blautic/pikkuAcademy/tree/master/Examples/pikkuAcademyFull/)


<div style="text-align:center"><img src="/images/full.gif " width="240" center></div>

- Obtenemos datos del accelerometro de los ejes x , y, z y los angulos xy, zy, xz

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

- Obtenemos datos del gyroscopio de los ejes x , y, z

```java
pikkuAcademy.readGyroscope(new GyroscopeCallback() {
    @Override
    public void onReadSuccess(float x, float y, float z) {

    }
});
```


- Encender o apagar motor


```java
binding.switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
    if (isChecked) {
        pikkuAcademy.startEngine();
    } else {
        pikkuAcademy.stopEngine();
    }
});
```

- Encender o apagar led

```java
binding.radioGroupLed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.led_off:
                pikkuAcademy.turnOffLed();
                break;
            case R.id.led_on:
                pikkuAcademy.turnOnLed();
                break;
            case R.id.led_flashing:
                pikkuAcademy.flashingLed();
                break;
        }
    }
});
```

- Estado de los botones

```java
pikkuAcademy.readButtons((nButton, pressedButton, durationMilliseconds) -> {
    String durationSeconds = String.format("%.1f''", durationMilliseconds / 1000.0);
    //Button 1 or 2
    if (nButton == 1) {
        binding.button1Time.setText(durationSeconds);
    } else {
        binding.button2Time.setText(durationSeconds);
    }

});
```


# Activity 

<a href="https://play.google.com/store/apps/details?id=com.blautic.pikkuacademyActivity"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="75" center></a>


Aplicación con guía paso a paso para control de pasos, distancia recorrida, pasos, tiempo de pie y en reposo y botón de emergencia [Ver código](https://github.com/blautic/pikkuAcademy/tree/master/Examples/pikkuAcademyActivity/)

<div style="text-align:center"><img src="/images/activity.png " width="240"></div>



Para detectar las actividad como caminar, el resultado no puede depender de si el usuario sostiene el dispositivo en posición vertical u horizontal, por lo que los valores individuales de X, Y y Z no servirán. En su lugar, tendrá que mirar la longitud del vector, es decir, sqrt (x² + y² + z²) que es independiente de la orientación del dispositivo.

Utilizando la [media móvil ponderada](https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average)   de la energía puede contar los pasos con solo buscar el máximo y el mínimo de sqrt (x² + y² + z²)   supere la media ponderada.



```java
public void stepDetect(float x, float y, float z) {
    float accelCurrent = (float) Math.sqrt(Math.pow(x, 2)
            + Math.pow(y, 2) + Math.pow(z, 2));
    float beta = 0.2f;
    EWMA = (1 - beta) * EWMA + beta * accelCurrent;
    
    float delta = accelCurrent - EWMA;
    if (delta > thresholdStep && !isOverThreshold && (System.currentTimeMillis()-lastTimeMovementDetected) > 300) {
        isOverThreshold = true;
        lastTimeMovementDetected = System.currentTimeMillis();
        isMoving = true;
        steps++;
        movementListener.onStep(steps, steps * averageStepDistance);
    } else if(accelCurrent < EWMA){
        isOverThreshold = false;
        long timeDelta = (System.currentTimeMillis() - lastTimeMovementDetected);
        if (timeDelta > timeBeforeDeclaringStationary && isMoving) {
            isMoving = false;
            movementListener.onStand();
        }
    }
}
```

# Snake Game
<a href="https://play.google.com/store/apps/details?id=com.blautic.pikkuacademySnake"><img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" height="75" ></a>

Aplicación con guía paso a paso para convertir el pikku en un joystick inalábrico de respuesta a gestos y videojuego Snake [Ver Código](https://github.com/blautic/pikkuAcademy/tree/master/Examples/pikkuAcademySnake/)



<div style="text-align:center"><img src="/images/snake.gif " width="480"></div>

Utilizando los ángulos del aceleramiento podemos ver la inclinación del dispositivo

```java
pikkuAcademy.readAccelerometer(new AccelerometerCallback() {
    @Override
    public void onReadSuccess(float x, float y, float z) {
    }
    @Override
    public void onReadAngles(float xy, float zy, float xz) {

        long stepStartTime = System.currentTimeMillis();
        if ((stepStartTime - stepPrevTime) > 300f) {
            if (Math.abs(xy) > 107) {
                animImg(binding.imgPikku, -20);
                stepPrevTime = stepStartTime;
                binding.snakeView.setSnakeDirection(GameType.TOP);
            } else if (Math.abs(xy) < 50) {
                animImg(binding.imgPikku, 20);
                stepPrevTime = stepStartTime;
                binding.snakeView.setSnakeDirection(GameType.BOTTOM);
            } else if (Math.abs(xz) > 107) {
                stepPrevTime = stepStartTime;
                binding.snakeView.setSnakeDirection(GameType.LEFT);
            } else if (Math.abs(xz) < 50) {
                stepPrevTime = stepStartTime;
                binding.snakeView.setSnakeDirection(GameType.RIGHT);
            }
        }
    }
});
```
