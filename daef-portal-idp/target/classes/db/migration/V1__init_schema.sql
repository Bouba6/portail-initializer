-- ══════════════════════════════════════════════════════════════════
-- V1 — Schéma initial DAEF Portal IDP
-- Auteur : DAEF Tech Team
-- ══════════════════════════════════════════════════════════════════

-- Extension UUID (PostgreSQL natif)
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Table : user_accounts ─────────────────────────────────────────
CREATE TABLE user_accounts (
    id            UUID          NOT NULL DEFAULT gen_random_uuid(),
    email         VARCHAR(255)  NOT NULL,
    password_hash VARCHAR(255)  NOT NULL,
    nom_complet   VARCHAR(255)  NOT NULL,
    telephone     VARCHAR(20),
    actif         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_user_accounts PRIMARY KEY (id),
    CONSTRAINT uq_user_accounts_email UNIQUE (email)
);

-- ── Table : applications ──────────────────────────────────────────
-- Référentiel des applications enregistrées dans l'écosystème DAEF
CREATE TABLE applications (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    code        VARCHAR(20)  NOT NULL,    -- ex: TAXAWU, SUQUALI
    name        VARCHAR(100) NOT NULL,
    base_url    VARCHAR(255) NOT NULL,
    icon_path   VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_applications PRIMARY KEY (id),
    CONSTRAINT uq_applications_code UNIQUE (code)
);

-- ── Table : user_app_permissions ──────────────────────────────────
-- Matrice user × application × role
-- C'est cette table qui pilote le Launchpad dynamique
CREATE TABLE user_app_permissions (
    id      UUID        NOT NULL DEFAULT gen_random_uuid(),
    user_id UUID        NOT NULL,
    app_id  UUID        NOT NULL,
    role    VARCHAR(50) NOT NULL,  -- ex: EVALUATEUR, PREFET, ADMIN, PROMOTRICE

    CONSTRAINT pk_user_app_permissions PRIMARY KEY (id),
    CONSTRAINT uq_user_app_permissions UNIQUE (user_id, app_id),
    CONSTRAINT fk_uap_user FOREIGN KEY (user_id)
        REFERENCES user_accounts (id) ON DELETE CASCADE,
    CONSTRAINT fk_uap_app  FOREIGN KEY (app_id)
        REFERENCES applications (id) ON DELETE CASCADE
);

-- ── Index pour les requêtes critiques ────────────────────────────
-- Validation Feign : findByEmail est le hot path
CREATE INDEX idx_user_accounts_email ON user_accounts (email);

-- Launchpad : findByUserId appelé à chaque login
CREATE INDEX idx_uap_user_id ON user_app_permissions (user_id);

-- ── Audit log (INSERT ONLY — jamais UPDATE ni DELETE) ─────────────
CREATE TABLE audit_log (
    id              UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID,                         -- nullable si action système
    action          VARCHAR(100) NOT NULL,         -- ex: LOGIN, LOGOUT, REGISTER
    resource_type   VARCHAR(50),                  -- ex: USER_ACCOUNT, APP_PERMISSION
    resource_id     VARCHAR(255),
    detail          TEXT,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_audit_log PRIMARY KEY (id)
);

CREATE INDEX idx_audit_log_user_id    ON audit_log (user_id);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);

-- Commentaire RLS future (à activer en prod selon politique DAEF)
-- ALTER TABLE audit_log ENABLE ROW LEVEL SECURITY;
