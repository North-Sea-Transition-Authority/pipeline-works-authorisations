package uk.co.ogauthority.pwa.repository.consultations;


import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;


public interface ConsultationResponseRepository extends CrudRepository<ConsultationResponse, Integer> {

  List<ConsultationResponse> getAllByConsultationRequestIn(List<ConsultationRequest> consultationRequests);

  ConsultationResponse getFirstByConsultationRequestInOrderByResponseTimestampDesc(List<ConsultationRequest> consultationRequests);

  Optional<ConsultationResponse> findByConsultationRequest(ConsultationRequest consultationRequests);

  List<ConsultationResponse> findAllByConsultationRequest_consulteeGroup(ConsulteeGroup consulteeGroup);

}
