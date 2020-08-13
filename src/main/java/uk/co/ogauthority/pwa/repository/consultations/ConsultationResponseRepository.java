package uk.co.ogauthority.pwa.repository.consultations;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;


public interface ConsultationResponseRepository extends CrudRepository<ConsultationResponse, Integer> {

  List<ConsultationResponse> getAllByConsultationRequestIn(List<ConsultationRequest> consultationRequests);

}
