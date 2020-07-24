package uk.co.ogauthority.pwa.repository.consultations;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;


public interface ConsultationRequestRepository extends CrudRepository<ConsultationRequest, Integer> {

  Optional<ConsultationRequest> findByConsulteeGroupDetail(ConsulteeGroupDetail consulteeGroupDetail);
}
