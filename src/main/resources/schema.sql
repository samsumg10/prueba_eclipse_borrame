-- ms-clientes · db_clientes

CREATE TABLE IF NOT EXISTS cliente (
    id                  BIGSERIAL     PRIMARY KEY,
    tipo_documento      VARCHAR(3)    NOT NULL CHECK (tipo_documento IN ('DNI','RUC')),
    documento           VARCHAR(11)   NOT NULL UNIQUE,                 -- RN-01
    nombres             VARCHAR(150)  NOT NULL,                        -- nombres o razón social
    apellidos           VARCHAR(100),
    telefono            VARCHAR(9)    NOT NULL,
    correo              VARCHAR(120)  NOT NULL,
    direccion           VARCHAR(200)  NOT NULL,
    tipo_cliente        VARCHAR(12)   NOT NULL
                        CHECK (tipo_cliente IN ('VIP','NORMAL','COMERCIAL','EMPRESARIAL')),
    ingreso_mensual     NUMERIC(12,2) NOT NULL CHECK (ingreso_mensual > 0),
    estado              VARCHAR(10)   NOT NULL DEFAULT 'ACTIVO'
                        CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO')),
    fecha_registro      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    viajes_completados  INTEGER       NOT NULL DEFAULT 0                -- suma el evento viaje.finalizado
);

-- Para bases creadas antes de que existiera viajes_completados
ALTER TABLE cliente ADD COLUMN IF NOT EXISTS viajes_completados INTEGER NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS historial_clasificacion (
    id            BIGSERIAL    PRIMARY KEY,
    cliente_id    BIGINT       NOT NULL REFERENCES cliente(id),
    tipo_anterior VARCHAR(12),
    tipo_nuevo    VARCHAR(12)  NOT NULL,
    motivo        VARCHAR(200) NOT NULL,                               -- RN-07
    fecha         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Idempotencia de eventos consumidos (viaje.finalizado): un evento repetido no se cuenta dos veces
CREATE TABLE IF NOT EXISTS evento_procesado (
    event_id        UUID      PRIMARY KEY,
    fecha_procesado TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
