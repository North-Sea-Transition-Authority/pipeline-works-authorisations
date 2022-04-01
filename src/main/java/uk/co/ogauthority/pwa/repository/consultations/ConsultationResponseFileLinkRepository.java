package uk.co.ogauthority.pwa.repository.consultations;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;

public interface ConsultationResponseFileLinkRepository extends CrudRepository<ConsultationResponseFileLink, Integer> {

  Optional<ConsultationResponseFileLink> findByAppFile_PwaApplicationAndAppFile(PwaApplication application, AppFile appFile);

  @EntityGraph(attributePaths = {
      "consultationResponse.consultationRequest.pwaApplication.masterPwa",
      "consultationResponse.consultationRequest.consulteeGroup",
      "appFile"
  })
  Set<ConsultationResponseFileLink> findAllByConsultationResponseIn(Set<ConsultationResponse> consultationResponses);
}
