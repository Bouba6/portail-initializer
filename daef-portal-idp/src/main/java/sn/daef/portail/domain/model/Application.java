package sn.daef.portail.domain.model;

/**
 * Value Object : représente une application enregistrée dans l'écosystème DAEF.
 * C'est le référentiel des apps disponibles sur le Launchpad.
 */
public record Application(
        String code,       // identifiant technique unique ex: "TAXAWU"
        String name,       // nom affiché ex: "Taxawu - FNCF"
        String baseUrl,    // URL de redirection ex: "http://taxawu.daef.sn"
        String iconPath,   // chemin icône pour le launchpad
        boolean active
) {}
