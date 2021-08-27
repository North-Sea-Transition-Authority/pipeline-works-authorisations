package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

public interface PwaConsentDtoRepository {

  List<PwaConsentApplicationDto> getConsentAndApplicationDtos(MasterPwa masterPwa);

  Optional<PwaConsentApplicationDto> getConsentAndApplicationDto(Integer consentId);

}
