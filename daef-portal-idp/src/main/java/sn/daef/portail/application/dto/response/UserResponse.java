package sn.daef.portail.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nomComplet,
        String email,
        String telephone,
        boolean actif,
        LocalDateTime createdAt
) {}
