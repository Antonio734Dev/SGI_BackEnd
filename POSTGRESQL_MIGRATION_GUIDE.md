# Guía de Migración MySQL a PostgreSQL

## Cambios Realizados

### 1. **Tabla "user" renombrada a "users"**
   - **Problema**: "user" es una palabra reservada en PostgreSQL
   - **Solución**: Cambiada a "users" en la entidad `User.java`
   - **Impacto**: Todas las referencias a la tabla ahora usan "users"

### 2. **Tipos de Datos Actualizados**

#### UUID (BINARY(16) → UUID)
   - **Antes**: `@Column(columnDefinition = "BINARY(16)")`
   - **Ahora**: `@Column(columnDefinition = "UUID")`
   - **Archivos afectados**:
     - `User.java`
     - `Notice.java`
     - `Customer.java`
     - `NoticeRecipient.java`
     - Todas las foreign keys que referencian UUIDs

#### Boolean (TINYINT(1) → BOOLEAN)
   - **Antes**: `@Column(columnDefinition = "TINYINT(1)")`
   - **Ahora**: `@Column(columnDefinition = "BOOLEAN")`
   - **Archivos afectados**:
     - `User.java`
     - `Document.java`
     - `Notice.java`
     - `Customer.java`
     - `StockCatalogue.java`

#### ENUM → VARCHAR
   - **Antes**: `@Column(columnDefinition = "ENUM('entrada', 'salida', 'ajuste')")`
   - **Ahora**: `@Column(columnDefinition = "VARCHAR(20)")`
   - **Archivos afectados**:
     - `ProductStockMovement.java` (campo `tipo`)

### 3. **TIMESTAMP sin ON UPDATE**
   - **Problema**: PostgreSQL no soporta `ON UPDATE CURRENT_TIMESTAMP`
   - **Solución**: Removido `ON UPDATE CURRENT_TIMESTAMP` de todas las columnas `updated_at`
   - **Nota**: Los valores de `updated_at` ahora deben actualizarse manualmente en el código Java
   - **Archivos afectados**: Todas las entidades con campo `updated_at`

### 4. **Estrategia DDL**
   - **Primera vez**: `ddl-auto=create` para crear todas las tablas
   - **Después**: Cambiar a `ddl-auto=update` para preservar datos
   - **Variable de entorno**: `DDL_STRATEGY` puede controlar esto
     - `DDL_STRATEGY=create` - Crea/recrea tablas (primera vez)
     - `DDL_STRATEGY=update` - Solo actualiza esquema (producción)

## Configuración de Variables de Entorno

### Primera vez (crear tablas):
```
DDL_STRATEGY=create
```

### Después del primer despliegue (preservar datos):
```
DDL_STRATEGY=update
```

## Diferencias Clave MySQL vs PostgreSQL

### 1. **Palabras Reservadas**
   - PostgreSQL tiene más palabras reservadas que MySQL
   - "user" es reservada en PostgreSQL → usar "users"

### 2. **Tipos de Datos**
   - MySQL: `BINARY(16)` para UUIDs
   - PostgreSQL: `UUID` nativo (mejor rendimiento)

### 3. **Booleanos**
   - MySQL: `TINYINT(1)` o `BOOLEAN`
   - PostgreSQL: `BOOLEAN` nativo

### 4. **ENUMs**
   - MySQL: Soporta ENUMs nativos
   - PostgreSQL: Usar VARCHAR con validación en aplicación o CHECK constraints

### 5. **TIMESTAMP AUTO-UPDATE**
   - MySQL: Soporta `ON UPDATE CURRENT_TIMESTAMP`
   - PostgreSQL: Requiere triggers o actualización manual en código

## Pasos para Despliegue

1. **Primera vez**:
   - Configurar `DDL_STRATEGY=create` en Render
   - Desplegar aplicación
   - Verificar que las tablas se crearon correctamente

2. **Después del primer despliegue**:
   - Cambiar `DDL_STRATEGY=update` en Render
   - Reiniciar aplicación
   - Las tablas existentes se preservarán y solo se actualizarán cambios

## Verificación Post-Migración

1. Verificar que todas las tablas existen:
   ```sql
   SELECT table_name FROM information_schema.tables 
   WHERE table_schema = 'public';
   ```

2. Verificar estructura de tabla "users":
   ```sql
   \d users
   ```

3. Verificar tipos de datos:
   ```sql
   SELECT column_name, data_type 
   FROM information_schema.columns 
   WHERE table_name = 'users';
   ```

## Notas Importantes

- **DataInitializer**: Se ejecuta después de que Hibernate crea las tablas
- **Foreign Keys**: Todas las referencias UUID ahora usan tipo UUID nativo
- **Índices**: Se mantienen igual, PostgreSQL los maneja correctamente
- **Performance**: PostgreSQL con tipos nativos (UUID, BOOLEAN) es más eficiente

## Troubleshooting

### Error: "relation does not exist"
- **Causa**: Tablas no creadas aún
- **Solución**: Usar `DDL_STRATEGY=create` la primera vez

### Error: "syntax error at or near 'user'"
- **Causa**: Palabra reservada
- **Solución**: Ya corregido - tabla renombrada a "users"

### Error: "column does not exist"
- **Causa**: Esquema desactualizado
- **Solución**: Usar `DDL_STRATEGY=create` temporalmente o actualizar manualmente


