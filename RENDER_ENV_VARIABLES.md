# Variables de Entorno para Render

## Configuración en el Panel de Render

Copia y pega estas variables de entorno en el panel de Render (Settings > Environment Variables):

### Variables Requeridas

```
# Puerto (Render lo inyecta automáticamente, pero puedes definirlo explícitamente)
PORT=8080

# Base de Datos PostgreSQL (Render)
DATABASE_URL=jdbc:postgresql://dpg-d4pj7rmuk2gs73f68kq0-a:5432/sgi_database_0p0l
DATABASE_USERNAME=sgi_database_0p0l_user
DATABASE_PASSWORD=bRidToHh2HXMr5ei0Fzj4Yjna9KtatAp

# JWT Configuration
JWT_SECRET=fX+zq1uV1W5zF0x2UEs6YT/NVzM/NvMjFd+5HdRZkEOLM7OaeZ0ND7eZc3B7RoJhI7Op9NGsWy0=
JWT_EXPIRATION=3600000
JWT_EXPIRATION_RECOVERY=1800000

# Resend Email Configuration
RESEND_API_KEY=re_6e3NgUUV_DGEpGtUsY1idpJzuAAE3c1bE
RESEND_DEFAULT_SENDER=onboarding@resend.dev
SPRING_MAIL_USERNAME=antonio734contacto@gmail.com

# Frontend URL for CORS
FRONTEND_URL=https://sgi-front-l5kqu8w9w-antonios-projects-8bf8b09e.vercel.app/
```

## Instrucciones de Configuración

1. Ve a tu servicio en Render Dashboard
2. Navega a **Settings** > **Environment Variables**
3. Haz clic en **Add Environment Variable** para cada variable
4. Copia y pega cada variable de la lista anterior
5. Guarda los cambios

## Notas Importantes

1. **Puerto**: Render inyecta automáticamente la variable `PORT`, pero puedes definirlo explícitamente. El Dockerfile y application-prod.properties ya están configurados para usar esta variable.

2. **Base de Datos**: Todas las credenciales de PostgreSQL ahora se configuran mediante variables de entorno para mayor seguridad. El archivo `application-prod.properties` está configurado para usar estas variables.

3. **Perfil de Spring**: El Dockerfile ejecuta la aplicación con `--spring.profiles.active=prod`, por lo que se usará `application-prod.properties`.

4. **Valores por Defecto**: Si alguna variable no está definida, `application-prod.properties` usará los valores por defecto que están configurados. Sin embargo, es recomendable definir todas las variables explícitamente en Render.

## Estructura de la URL de Base de Datos

La variable `DATABASE_URL` debe tener el formato completo de JDBC:
```
jdbc:postgresql://HOST:PORT/DATABASE_NAME
```

En tu caso:
```
jdbc:postgresql://dpg-d4pj7rmuk2gs73f68kq0-a:5432/sgi_database_0p0l
```

## Verificación

Después de configurar las variables de entorno:
1. Reinicia el servicio en Render
2. Verifica los logs para confirmar que la conexión a PostgreSQL es exitosa
3. La aplicación debería iniciar con el perfil `prod` y conectarse a la base de datos
