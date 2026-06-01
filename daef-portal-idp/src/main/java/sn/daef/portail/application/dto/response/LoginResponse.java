package sn.daef.portail.application.dto.response;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String nomComplet,
        String email,
        List<AppAccessResponse> applications  // pour le Launchpad frontend
) {
    public LoginResponse(String accessToken, long expiresIn,
                         String nomComplet, String email,
                         List<AppAccessResponse> applications) {
        this(accessToken, "Bearer", expiresIn, nomComplet, email, applications);
    }
}
