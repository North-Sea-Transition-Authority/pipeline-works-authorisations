package uk.co.ogauthority.pwa.repository.consultations;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;


public interface ConsultationRequestRepository extends CrudRepository<ConsultationRequest, Integer> {

  Optional<ConsultationRequest> findByConsulteeGroupAndPwaApplication(ConsulteeGroup consulteeGroup, PwaApplication pwaApplication);
}
