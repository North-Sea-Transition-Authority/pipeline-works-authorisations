package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;


import org.springframework.data.repository.CrudRepository;

public interface ParallelConsentCheckLogRepository extends CrudRepository<ParallelConsentCheckLog, Integer> {

}