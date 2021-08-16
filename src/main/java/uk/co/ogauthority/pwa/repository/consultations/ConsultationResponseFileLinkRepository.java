package uk.co.ogauthority.pwa.repository.consultations;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

public interface ConsultationResponseFileLinkRepository extends CrudRepository<ConsultationResponseFileLink, Integer> {
  Optional<ConsultationResponseFileLink> findByAppFile_PwaApplicationAndAppFile(PwaApplication application, AppFile appFile);

  @EntityGraph(attributePaths = {"consultationResponse", "appFile"})
  List<ConsultationResponseFileLink> findALlByConsultationResponseIn(List<ConsultationResponse> consultationResponses);
}
