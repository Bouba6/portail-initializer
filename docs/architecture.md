# Architecture DAEF — Documentation technique

## C'est quoi ce projet en une phrase ?

Un **système de connexion centralisé** pour l'écosystème DAEF.
Tu te connectes une seule fois → tu accèdes à toutes les applications (`TAXAWU`, `SUQUALI`, etc.) sans retaper tes identifiants.

---

## Les deux modules dans le zip

```
initializer/
├── daef-portal-idp/     → Le "serveur de connexion" central (IDP)
└── uno/security/        → La librairie de sécurité à brancher dans chaque app
```

---

## 1. `daef-portal-idp` — Le portail d'identité

C'est **le seul endroit** où un utilisateur peut se connecter, s'inscrire, ou se déconnecter.
Il connaît les mots de passe. Il émet les tokens. Il gère les droits d'accès par application.

### Ce qu'il fait concrètement

| Endpoint | Ce que ça fait |
|---|---|
| `POST /api/auth/register` | Crée un compte utilisateur |
| `POST /api/auth/login` | Vérifie le mot de passe, retourne un **JWT** |
| `GET /api/auth/validate` | Vérifie qu'un token est valide (appelé par les autres apps) |
| `POST /api/auth/logout` | Révoque le token (le met en blacklist) |
| `GET /api/users/{id}/apps` | Retourne les applications accessibles à un user (Launchpad) |
| `POST /api/admin/users/{id}/access` | Donne accès à une application à un user |
| `DELETE /api/admin/users/{id}/access/{appCode}` | Retire un accès |

### Architecture interne — Hexagonale

Le code est structuré en 3 couches qui ne se mélangent jamais :

```
daef-portal-idp/src/main/java/sn/daef/portail/
│
├── domain/                        ← Règles métier pures, ZÉRO dépendance Spring/JPA
│   ├── model/
│   │   ├── UserAccount.java       ← Un compte utilisateur (email, password, actif/inactif)
│   │   ├── AppAccess.java         ← L'accès d'un user à une app (appCode + role)
│   │   ├── Application.java       ← Une application enregistrée (TAXAWU, SUQUALI...)
│   │   └── TokenValidation.java   ← Résultat d'une validation de token
│   ├── service/
│   │   └── AuthDomainService.java ← Vérifie: compte actif ? password correct ?
│   ├── port/
│   │   ├── input/                 ← Ce que le domaine EXPOSE (interfaces use cases)
│   │   │   ├── LoginUseCase.java
│   │   │   ├── RegisterUserUseCase.java
│   │   │   ├── LogoutUseCase.java
│   │   │   ├── ValidateTokenUseCase.java
│   │   │   ├── GetUserAppsUseCase.java
│   │   │   └── AdminUseCase.java
│   │   └── output/                ← Ce dont le domaine a BESOIN (interfaces repos)
│   │       ├── UserAccountRepository.java
│   │       ├── AppAccessRepository.java
│   │       └── TokenBlacklistRepository.java
│   └── exception/
│       ├── UserNotFoundException.java
│       ├── UserAlreadyExistsException.java
│       ├── InvalidCredentialsException.java
│       ├── AccountDisabledException.java
│       └── InvalidTokenException.java
│
├── application/                   ← Orchestration des use cases (pas de logique métier ici)
│   ├── usecase/
│   │   ├── LoginUseCaseImpl.java         ← Charge le user, vérifie via domaine, charge les accès
│   │   ├── RegisterUserUseCaseImpl.java  ← Vérifie doublon, encode password, sauvegarde
│   │   ├── LogoutUseCaseImpl.java        ← Met le token en blacklist Redis
│   │   ├── ValidateTokenUseCaseImpl.java ← Blacklist → JWT → compte actif → accès apps
│   │   ├── GetUserAppsUseCaseImpl.java   ← Retourne les apps d'un user (Launchpad)
│   │   └── AdminUseCaseImpl.java         ← Assigner/révoquer des accès
│   └── dto/
│       ├── request/
│       │   ├── LoginRequest.java          ← { email, password }
│       │   ├── RegisterRequest.java       ← { email, password, nomComplet, telephone, appCode, role }
│       │   └── AdminUserAccessRequest.java
│       └── response/
│           ├── LoginResponse.java         ← { token, expiresIn, nomComplet, email, applications[] }
│           ├── TokenValidationResponse.java
│           ├── UserResponse.java
│           └── AppAccessResponse.java     ← { appCode, appName, appBaseUrl, appIconPath, role }
│
└── infrastructure/                ← Tout ce qui touche au monde extérieur (Spring, DB, Redis)
    ├── security/
    │   ├── jwt/
    │   │   └── JwtService.java    ← Génère et valide les JWT (JJWT, clé HMAC)
    │   ├── filter/
    │   │   └── JwtAuthFilter.java ← Filtre Spring Security du portail lui-même
    │   └── config/
    │       └── SecurityConfig.java
    ├── persistence/
    │   ├── entity/
    │   │   ├── UserAccountEntity.java      ← Entité JPA (table user_accounts)
    │   │   ├── ApplicationEntity.java      ← Entité JPA (table applications)
    │   │   └── UserAppPermissionEntity.java← Entité JPA (table user_app_permissions)
    │   ├── repository/
    │   │   ├── UserAccountJpaRepository.java
    │   │   ├── ApplicationJpaRepository.java
    │   │   └── UserAppPermissionJpaRepository.java
    │   ├── mapper/
    │   │   └── UserAccountMapper.java     ← MapStruct : Entity ↔ Domain model
    │   └── adapter/
    │       ├── UserAccountRepositoryAdapter.java      ← Implémente UserAccountRepository
    │       ├── AppAccessRepositoryAdapter.java         ← Implémente AppAccessRepository
    │       └── TokenBlacklistRepositoryAdapter.java   ← Implémente TokenBlacklistRepository (Redis)
    ├── web/
    │   ├── controller/
    │   │   ├── AuthController.java   ← /api/auth/*
    │   │   ├── UserController.java   ← /api/users/*
    │   │   └── AdminController.java  ← /api/admin/*
    │   └── advice/
    │       └── GlobalExceptionHandler.java ← Transforme les exceptions domaine en HTTP 4xx/5xx
    └── config/
        ├── RedisConfig.java     ← Connexion Redis (blacklist des tokens révoqués)
        └── OpenApiConfig.java   ← Swagger UI
```

### La base de données (PostgreSQL + Flyway)

3 tables principales :

```sql
user_accounts           ← Les comptes utilisateurs
    id UUID, email, password_hash, nom_complet, telephone, actif, created_at

applications            ← Les apps enregistrées dans l'écosystème
    id UUID, code (TAXAWU/SUQUALI), name, base_url, icon_path, active

user_app_permissions    ← Qui a accès à quoi et avec quel rôle
    id UUID, user_id → user_accounts, app_id → applications, role (ADMIN/EVALUATEUR/...)
```

Données pré-insérées au démarrage (migration V2) :
- Application `TAXAWU` → `http://taxawu.daef.sn`
- Application `SUQUALI` → `http://suquali.daef.sn`
- Compte admin : `admin@daef.sn` / `Admin@DAEF2025`

### Le token JWT — ce qu'il contient

Quand tu te connectes, le portail génère un token qui ressemble à ça une fois décodé :

```json
{
  "sub": "uuid-du-user",
  "email": "user@daef.sn",
  "nomComplet": "Prénom Nom",
  "roles": ["EVALUATEUR"],
  "iat": 1234567890,
  "exp": 1234567890
}
```

Ce token est signé avec une clé secrète. **Seul le portail connaît cette clé.**
Les autres apps ne la connaissent pas — elles demandent au portail de valider le token à leur place.

### Le logout et Redis

Quand un user se déconnecte, le token est mis en **blacklist Redis** :

```
SET blacklist:{le_token} "revoked" EX {temps_restant_avant_expiration}
```

À chaque validation, le portail vérifie Redis en premier. Si le token est blacklisté → refus immédiat, même si la signature est valide.

---

## 2. `uno/security` — La librairie de sécurité

C'est un **module Spring Boot réutilisable** à brancher dans chaque application métier (`TAXAWU`, `SUQUALI`, etc.).
Il remplace le besoin de gérer la sécurité dans chaque app individuellement.

### Ce qu'il fait

À chaque requête entrante sur l'application :
1. Extrait le token du header `Authorization: Bearer <token>`
2. Appelle le portail via **Feign** → `GET /api/auth/validate`
3. Si valide ET si le user a bien accès à **cette application précisément** → laisse passer
4. Si invalide → répond `401 Unauthorized`
5. Si le portail est down → répond `503 Service Unavailable`

### Structure du module

```
uno/security/src/main/java/sn/daef/taxawu/security/
│
├── SecurityApplication.java            ← Point d'entrée Spring Boot
│
├── filter/
│   └── JwtAuthFilter.java              ← Filtre principal (OncePerRequestFilter)
│                                          Extrait le Bearer, appelle Feign, peuple SecurityContext
│
├── feign/
│   ├── PortailFeignClient.java          ← Interface Feign → GET /api/auth/validate
│   ├── PortailFeignConfig.java          ← Config Feign (ErrorDecoder personnalisé)
│   └── PortailTokenValidationDto.java   ← DTO de la réponse du portail
│                                          Contient rolePourTaxawu() pour filtrer par appCode
│
├── context/
│   ├── AuthenticatedUser.java           ← Record immuable injecté dans le SecurityContext
│   │                                      { userId, email, nomComplet, roleSurTaxawu, allRoles }
│   └── AuthenticatedUserHolder.java     ← Helper pour récupérer le user courant dans le code
│
├── config/
│   └── SecurityConfig.java             ← @EnableWebSecurity + enregistrement du filtre
│
├── exception/
│   ├── TokenRejectedException.java      ← Token refusé par le portail (401)
│   └── PortailUnavailableException.java ← Portail injoignable (503)
│
└── web/
    ├── controller/
    │   └── TestSecurityController.java  ← Endpoint de test /me (à retirer en prod)
    └── dto/
        └── MeResponse.java
```

### Comment ça filtre par application

Le portail retourne dans la réponse `/validate` la liste de **toutes** les applications du user :

```json
{
  "valid": true,
  "userId": "uuid...",
  "email": "user@daef.sn",
  "nomComplet": "Prénom Nom",
  "roles": ["EVALUATEUR"],
  "applications": [
    { "appCode": "TAXAWU",  "role": "EVALUATEUR", "appBaseUrl": "..." },
    { "appCode": "SUQUALI", "role": "ADMIN",       "appBaseUrl": "..." }
  ]
}
```

Le filtre appelle `rolePourTaxawu()` dans `PortailTokenValidationDto` qui cherche l'entrée avec `appCode = "TAXAWU"`. Si elle n'existe pas → le user n'a pas accès à cette app, même si son token est valide.

**C'est ici que se fait le contrôle d'accès applicatif** : un token valide ne suffit pas, il faut aussi avoir un accès explicitement assigné dans la table `user_app_permissions`.

---

## Le flux complet de bout en bout

### Connexion

```
[Navigateur / Frontend]
    │
    │  POST /api/auth/login  { email, password }
    ▼
[daef-portal-idp]
    │  1. Charge UserAccount depuis PostgreSQL
    │  2. AuthDomainService.authentifier() → compte actif ? password correct ?
    │  3. Charge les AppAccess depuis user_app_permissions
    │  4. Génère le JWT signé
    │  5. Retourne { token, expiresIn, applications: [...] }
    ▼
[Frontend] stocke le token (localStorage ou mémoire)
    │
    │  Affiche le Launchpad dynamique avec les apps auxquelles le user a accès
```

### Accès à une application métier (TAXAWU par exemple)

```
[Frontend TAXAWU]
    │
    │  GET /api/dossiers/123
    │  Authorization: Bearer <token>
    ▼
[uno/security — JwtAuthFilter]
    │  1. Extrait le Bearer token
    │  2. Appelle portail via Feign → GET /api/auth/validate
    │
    │         ┌─────────────────────────────┐
    │         │     daef-portal-idp         │
    │         │  1. Redis : blacklisté ?    │
    │         │  2. JWT valide ?            │
    │         │  3. Compte actif en DB ?    │
    │         │  4. Charge les app accesses │
    │         │  → Retourne TokenValidation │
    │         └─────────────────────────────┘
    │
    │  3. rolePourTaxawu() → user a bien accès à TAXAWU ?
    │  4. Construit AuthenticatedUser → SecurityContext
    ▼
[Controller TAXAWU]
    │  Traite la requête normalement
    │  AuthenticatedUser user = AuthenticatedUserHolder.current()
    │  → user.userId(), user.roleSurTaxawu(), etc.
```

### Déconnexion

```
[Frontend]
    │  POST /api/auth/logout
    │  Authorization: Bearer <token>
    ▼
[daef-portal-idp]
    │  LogoutUseCaseImpl :
    │  1. Calcule le TTL restant du token
    │  2. SET blacklist:{token} "revoked" EX {ttl} dans Redis
    ▼
[Résultat]
    Toutes les apps refusent ce token lors de la prochaine requête
    sans qu'aucune app métier n'ait besoin de gérer quoi que ce soit
```

---

## Ce que les apps métier n'ont PAS à faire

Grâce à cette architecture, `TAXAWU`, `SUQUALI` et les futures apps n'ont jamais à :

- Stocker des mots de passe
- Connaître la clé secrète JWT
- Gérer leur propre table d'utilisateurs
- Implémenter un formulaire de login
- Gérer la révocation de tokens
- Se soucier de savoir si un user est actif ou non

Elles branchent juste `uno/security`, configurent l'URL du portail, et récupèrent le `AuthenticatedUser` depuis le `SecurityContextHolder`.

---

## Technologies utilisées

| Technologie | Rôle |
|---|---|
| **Spring Boot 3** | Framework principal |
| **Spring Security** | Chaîne de filtres HTTP |
| **JJWT** | Génération et validation des tokens JWT |
| **PostgreSQL** | Stockage des users, apps, et permissions |
| **Flyway** | Migrations de base de données versionnées |
| **Redis** | Blacklist des tokens révoqués (logout) |
| **MapStruct** | Mapping automatique Entity ↔ Domain model |
| **OpenFeign** | Appel HTTP du portail depuis les apps métier |
| **Swagger / OpenAPI** | Documentation des endpoints |
| **Lombok** | Réduction du boilerplate Java |

---

## Variables d'environnement à configurer

### Pour `daef-portal-idp`

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://host:5432/portail_db
SPRING_DATASOURCE_USERNAME: postgres
SPRING_DATASOURCE_PASSWORD: ***
REDIS_HOST: localhost
REDIS_PORT: 6379
JWT_SECRET: une-cle-de-minimum-32-caracteres
JWT_EXPIRATION: 86400   # 24h en secondes
```

### Pour chaque app métier (`uno/security`)

```yaml
app.portail.base-url: http://daef-portal-idp:8080
```

C'est tout. L'app métier n'a besoin de rien d'autre pour la sécurité.

---

## Ce qui reste à faire / points d'attention

- **`@Cacheable` sur `/validate`** : cet endpoint est appelé à chaque requête de chaque app. En prod avec du trafic, mettre en place un cache Redis côté consommateur (TTL court, ex: 30s) pour éviter de marteler le portail.
- **HTTPS obligatoire** : les tokens Bearer dans les headers doivent impérativement transiter en HTTPS en production.
- **Changer le compte admin initial** : le compte `admin@daef.sn` avec le password `Admin@DAEF2025` est créé par la migration V2. À changer en première connexion.
- **`appCode` dans `uno/security`** : le filtre cherche en dur `"TAXAWU"`. Chaque app métier doit configurer son propre `appCode` via une property (`${app.code}`).
- **Audit log** : la table `audit_log` est créée en V1 mais pas encore alimentée dans le code — à brancher dans les use cases login/logout/register.