package uk.co.ogauthority.pwa.repository.consultations;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;


public interface ConsultationRequestRepository extends CrudRepository<ConsultationRequest, Integer> {

  Optional<ConsultationRequest> findByConsulteeGroupAndPwaApplicationAndStatusNotIn(
      ConsulteeGroup consulteeGroup, PwaApplication pwaApplication, List<ConsultationRequestStatus> statuses);

  List<ConsultationRequest> findByConsulteeGroupAndPwaApplicationAndStatus(ConsulteeGroup consulteeGroup,
                                                                           PwaApplication pwaApplication, ConsultationRequestStatus status);

  List<ConsultationRequest> findByPwaApplicationOrderByConsulteeGroupDescStartTimestampDesc(PwaApplication pwaApplication);

}
