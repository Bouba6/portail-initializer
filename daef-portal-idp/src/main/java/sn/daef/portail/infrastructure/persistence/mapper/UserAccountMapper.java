package sn.daef.portail.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import sn.daef.portail.application.dto.response.AppAccessResponse;
import sn.daef.portail.application.dto.response.TokenValidationResponse;
import sn.daef.portail.application.dto.response.UserResponse;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.model.TokenValidation;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.infrastructure.persistence.entity.UserAccountEntity;
import sn.daef.portail.infrastructure.persistence.entity.UserAppPermissionEntity;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    // ── Entité JPA → Modèle domaine ──────────────────────────────────────────

    @Mapping(target = "appAccesses", ignore = true)
    UserAccount toDomain(UserAccountEntity entity);

    @Mapping(target = "permissions", ignore = true)
    UserAccountEntity toEntity(UserAccount domain);

    // ── Modèle domaine → DTO réponse (utilisé dans les controllers) ──────────

    @Mapping(target = "id",         source = "id")
    @Mapping(target = "nomComplet", source = "nomComplet")
    @Mapping(target = "email",      source = "email")
    @Mapping(target = "telephone",  source = "telephone")
    @Mapping(target = "actif",      source = "actif")
    @Mapping(target = "createdAt",  source = "createdAt")
    UserResponse toUserResponse(UserAccount account);

    @Mapping(target = "appCode",   source = "appCode")
    @Mapping(target = "appName",   source = "appName")
    @Mapping(target = "appBaseUrl",source = "appBaseUrl")
    @Mapping(target = "appIconPath",source = "appIconPath")
    @Mapping(target = "role",      source = "role")
    AppAccessResponse toAppAccessResponse(AppAccess appAccess);

    List<AppAccessResponse> toAppAccessResponseList(List<AppAccess> accesses);

    // ── TokenValidation domaine → DTO réponse ────────────────────────────────

    @Mapping(target = "applications", source = "appAccesses")
    TokenValidationResponse toTokenValidationResponse(TokenValidation validation);

    // ── Entité permission → Value Object domaine ──────────────────────────────

    default AppAccess permissionToAppAccess(UserAppPermissionEntity perm) {
        return new AppAccess(
                perm.getApplication().getCode(),
                perm.getApplication().getName(),
                perm.getApplication().getBaseUrl(),
                perm.getApplication().getIconPath(),
                perm.getRole()
        );
    }
}
