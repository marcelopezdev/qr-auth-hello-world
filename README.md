# Proyecto de Autenticación con QR en Spring Boot

Este proyecto implementa un sistema de autenticación con códigos QR generados cada 60 segundos. El QR es utilizado para autenticar a un usuario en una aplicación bancaria. El QR se genera dinámicamente en el servidor y se presenta en la interfaz de usuario (`index.html`). Cada vez que se genera un nuevo QR, el anterior expira después de 60 segundos.

## Requisitos

Antes de ejecutar el proyecto, asegúrate de tener lo siguiente instalado:

- Java 21
- Maven
- Spring Boot

## Estructura del Proyecto

1. **API REST**: El backend está implementado con Spring Boot y expone un servicio para generar códigos QR y autenticar a los usuarios.
2. **Frontend**: Un archivo `index.html` sirve como cliente que visualiza el código QR generado.
3. **WebSocket**: El proyecto usa WebSockets para actualizar el QR dinámicamente cada 60 segundos.

## Configuración del Proyecto

### Clonación y Compilación

1. Clona este repositorio:

```bash
git clone https://github.com/tu_usuario/qr-authentication.git
cd qr-authentication
```

2. Compila y ejecuta el proyecto

```bash
mvn spring-boot:run
```

3. Abra su navegador y navegue `http://localhost:8080/index.html`para ver el código QR generado.
### Flujo de funcionamiento

1. **Generación del QR:**
    - El QR se genera cada 60 segundos en el backend y se envía al frontend.
    - El QR contiene un identificador único y se utiliza para autenticar al usuario que está escaneando el código.
2. **Escaneo del QR:**
    - El frontend envía el código QR escaneado al backend para validarlo y autenticar al usuario.
3. **Caída del QR:**
    - El QR generado tiene una validez de 60 segundos. Después de este tiempo, se genera uno nuevo automáticamente.
4. Autenticacion:
	
```bash
curl --location --request POST 'http://localhost:8080/api/auth/scan/{sessionId}'
```
