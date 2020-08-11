package uk.co.ogauthority.pwa.repository.consultations;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;


public interface ConsultationResponseRepository extends CrudRepository<ConsultationResponse, Integer> {

  Optional<ConsultationResponse> findByConsultationRequest(ConsultationRequest consultationRequest);

}
