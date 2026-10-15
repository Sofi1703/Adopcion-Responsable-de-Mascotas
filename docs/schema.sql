-- esquema de la base adopcion_responsable (PostgreSQL)
-- ejecutar sobre esa base antes de arrancar la app porque ddl-auto es validate

CREATE TABLE mascota (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    especie     VARCHAR(50)  NOT NULL,
    raza        VARCHAR(100),
    descripcion TEXT,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'DISPONIBLE',
    imagen_url  VARCHAR(500),
    CONSTRAINT ck_mascota_estado
        CHECK (estado IN ('DISPONIBLE', 'ADOPTADA')),
    CONSTRAINT ck_mascota_imagen_url
        CHECK (imagen_url IS NULL OR imagen_url ~ '^https?://')
);

CREATE TABLE solicitud (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mascota_id         BIGINT       NOT NULL,
    solicitante_correo VARCHAR(150) NOT NULL,
    solicitante_nombre VARCHAR(100) NOT NULL,
    telefono           VARCHAR(30)  NOT NULL,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'RECIBIDA',
    creada_en          TIMESTAMP    NOT NULL DEFAULT now(),
    retirada_en        TIMESTAMP,
    CONSTRAINT fk_solicitud_mascota
        FOREIGN KEY (mascota_id) REFERENCES mascota (id),
    CONSTRAINT ck_solicitud_estado
        CHECK (estado IN ('RECIBIDA', 'EN_REVISION', 'APROBADA', 'RECHAZADA'))
);

CREATE INDEX ix_solicitud_mascota ON solicitud (mascota_id);

-- RN-01 una sola solicitud activa por solicitante y mascota
CREATE UNIQUE INDEX uq_solicitud_activa
    ON solicitud (mascota_id, lower(solicitante_correo))
    WHERE estado IN ('RECIBIDA', 'EN_REVISION') AND retirada_en IS NULL;

CREATE TABLE historial_solicitud (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    solicitud_id    BIGINT       NOT NULL,
    tipo            VARCHAR(30)  NOT NULL,
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20),
    actor           VARCHAR(150) NOT NULL,
    fecha           TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT fk_historial_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES solicitud (id),
    CONSTRAINT ck_historial_tipo
        CHECK (tipo IN ('CREACION', 'CAMBIO_ESTADO', 'CIERRE_AUTOMATICO', 'RETIRO'))
);

CREATE INDEX ix_historial_solicitud ON historial_solicitud (solicitud_id);
