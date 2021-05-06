package uk.co.ogauthority.pwa.repository.appprocessing.prepareconsent;


import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.prepareconsent.ParallelConsentCheckLog;

public interface ParallelConsentCheckLogRepository extends CrudRepository<ParallelConsentCheckLog, Integer> {

}