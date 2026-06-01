package sn.daef.portail.infrastructure.persistence.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sn.daef.portail.application.dto.response.AppAccessResponse;
import sn.daef.portail.application.dto.response.TokenValidationResponse;
import sn.daef.portail.application.dto.response.UserResponse;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.model.TokenValidation;
import sn.daef.portail.domain.model.UserAccount;
import sn.daef.portail.infrastructure.persistence.entity.UserAccountEntity;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-01T16:53:18+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UserAccountMapperImpl implements UserAccountMapper {

    @Override
    public UserAccount toDomain(UserAccountEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        String email = null;
        String passwordHash = null;
        String nomComplet = null;
        String telephone = null;
        LocalDateTime createdAt = null;

        id = entity.getId();
        email = entity.getEmail();
        passwordHash = entity.getPasswordHash();
        nomComplet = entity.getNomComplet();
        telephone = entity.getTelephone();
        createdAt = entity.getCreatedAt();

        UserAccount userAccount = new UserAccount( id, email, passwordHash, nomComplet, telephone, createdAt );

        return userAccount;
    }

    @Override
    public UserAccountEntity toEntity(UserAccount domain) {
        if ( domain == null ) {
            return null;
        }

        UserAccountEntity.UserAccountEntityBuilder userAccountEntity = UserAccountEntity.builder();

        userAccountEntity.id( domain.getId() );
        userAccountEntity.email( domain.getEmail() );
        userAccountEntity.passwordHash( domain.getPasswordHash() );
        userAccountEntity.nomComplet( domain.getNomComplet() );
        userAccountEntity.telephone( domain.getTelephone() );
        userAccountEntity.actif( domain.isActif() );
        userAccountEntity.createdAt( domain.getCreatedAt() );

        return userAccountEntity.build();
    }

    @Override
    public UserResponse toUserResponse(UserAccount account) {
        if ( account == null ) {
            return null;
        }

        UUID id = null;
        String nomComplet = null;
        String email = null;
        String telephone = null;
        boolean actif = false;
        LocalDateTime createdAt = null;

        id = account.getId();
        nomComplet = account.getNomComplet();
        email = account.getEmail();
        telephone = account.getTelephone();
        actif = account.isActif();
        createdAt = account.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, nomComplet, email, telephone, actif, createdAt );

        return userResponse;
    }

    @Override
    public AppAccessResponse toAppAccessResponse(AppAccess appAccess) {
        if ( appAccess == null ) {
            return null;
        }

        String appCode = null;
        String appName = null;
        String appBaseUrl = null;
        String appIconPath = null;
        String role = null;

        appCode = appAccess.appCode();
        appName = appAccess.appName();
        appBaseUrl = appAccess.appBaseUrl();
        appIconPath = appAccess.appIconPath();
        role = appAccess.role();

        AppAccessResponse appAccessResponse = new AppAccessResponse( appCode, appName, appBaseUrl, appIconPath, role );

        return appAccessResponse;
    }

    @Override
    public List<AppAccessResponse> toAppAccessResponseList(List<AppAccess> accesses) {
        if ( accesses == null ) {
            return null;
        }

        List<AppAccessResponse> list = new ArrayList<AppAccessResponse>( accesses.size() );
        for ( AppAccess appAccess : accesses ) {
            list.add( toAppAccessResponse( appAccess ) );
        }

        return list;
    }

    @Override
    public TokenValidationResponse toTokenValidationResponse(TokenValidation validation) {
        if ( validation == null ) {
            return null;
        }

        List<AppAccessResponse> applications = null;
        boolean valid = false;
        UUID userId = null;
        String email = null;
        String nomComplet = null;
        List<String> roles = null;

        applications = toAppAccessResponseList( validation.appAccesses() );
        valid = validation.valid();
        userId = validation.userId();
        email = validation.email();
        nomComplet = validation.nomComplet();
        List<String> list1 = validation.roles();
        if ( list1 != null ) {
            roles = new ArrayList<String>( list1 );
        }

        TokenValidationResponse tokenValidationResponse = new TokenValidationResponse( valid, userId, email, nomComplet, roles, applications );

        return tokenValidationResponse;
    }
}
