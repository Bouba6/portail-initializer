package sn.daef.portail.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.daef.portail.domain.model.AppAccess;
import sn.daef.portail.domain.port.input.GetUserAppsUseCase;
import sn.daef.portail.domain.port.output.AppAccessRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserAppsUseCaseImpl implements GetUserAppsUseCase {

    private final AppAccessRepository appAccessRepository;

    @Override
    public List<AppAccess> execute(UUID userId) {
        return appAccessRepository.findByUserId(userId);
    }
}
