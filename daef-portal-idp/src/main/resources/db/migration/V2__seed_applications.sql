-- ══════════════════════════════════════════════════════════════════
-- V2 — Données initiales : applications de l'écosystème DAEF
-- ══════════════════════════════════════════════════════════════════

INSERT INTO applications (id, code, name, base_url, icon_path, active) VALUES
    (gen_random_uuid(), 'TAXAWU',  'Taxawu — FNCF',  'http://taxawu.daef.sn',  '/icons/taxawu.svg',  TRUE),
    (gen_random_uuid(), 'SUQUALI', 'Suquali — FNEF', 'http://suquali.daef.sn', '/icons/suquali.svg', TRUE);

-- ── Compte admin DAEF initial ─────────────────────────────────────
-- Password : Admin@DAEF2025 — BCrypt généré hors migration
-- À CHANGER IMPÉRATIVEMENT en première connexion
INSERT INTO user_accounts (id, email, password_hash, nom_complet, telephone, actif) VALUES
    (
        gen_random_uuid(),
        'admin@daef.sn',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8n5g8e3bQmZKHJWRY8i',
        'Administrateur DAEF',
        '+221338000000',
        TRUE
    );

-- ── Accès admin sur toutes les applications ───────────────────────
INSERT INTO user_app_permissions (user_id, app_id, role)
SELECT
    ua.id,
    a.id,
    'ADMIN'
FROM user_accounts ua
CROSS JOIN applications a
WHERE ua.email = 'admin@daef.sn';
