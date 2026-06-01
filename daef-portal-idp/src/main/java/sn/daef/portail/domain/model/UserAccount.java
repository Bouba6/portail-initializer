package sn.daef.portail.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root : représente un compte utilisateur dans l'écosystème DAEF.
 * AUCUNE annotation Spring ou JPA ici — domaine pur.
 */
public class UserAccount {

    private final UUID id;
    private String email;
    private String passwordHash;
    private String nomComplet;
    private String telephone;
    private boolean actif;
    private final LocalDateTime createdAt;
    private List<AppAccess> appAccesses;

    

    public UserAccount(UUID id, String email, String passwordHash,
                       String nomComplet, String telephone,
                       LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nomComplet = nomComplet;
        this.telephone = telephone;
        this.actif = true;
        this.createdAt = createdAt;
        this.appAccesses = List.of();
    }

    /** Règle métier : un compte désactivé ne peut pas se connecter */
    public boolean peutSeConnecter() {
        return this.actif;
    }

    /** Règle métier : vérifie si l'utilisateur a accès à une application donnée */
    public boolean aAccesA(String appCode) {
        return appAccesses.stream()
                .anyMatch(a -> a.appCode().equals(appCode));
    }

    /** Règle métier : retourne le rôle de l'utilisateur pour une app donnée */
    public String rolePourtApp(String appCode) {
        return appAccesses.stream()
                .filter(a -> a.appCode().equals(appCode))
                .map(AppAccess::role)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Aucun accès pour l'application : " + appCode));
    }

    public void desactiver() {
        this.actif = false;
    }

    public void assignerAcces(List<AppAccess> accesses) {
        this.appAccesses = List.copyOf(accesses);
    }

    // Getters (pas de setters publics — immutabilité contrôlée)
    public UUID getId()                  { return id; }
    public String getEmail()             { return email; }
    public String getPasswordHash()      { return passwordHash; }
    public String getNomComplet()        { return nomComplet; }
    public String getTelephone()         { return telephone; }
    public boolean isActif()             { return actif; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public List<AppAccess> getAppAccesses() { return appAccesses; }
}
