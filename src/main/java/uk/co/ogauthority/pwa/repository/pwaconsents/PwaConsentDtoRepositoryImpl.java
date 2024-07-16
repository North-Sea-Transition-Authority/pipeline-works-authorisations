package uk.co.ogauthority.pwa.repository.pwaconsents;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Repository
public class PwaConsentDtoRepositoryImpl implements PwaConsentDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PwaConsentDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Optional<PwaConsentApplicationDto> getConsentAndApplicationDto(Integer consentId) {
    var pwaConsentApplicationDto = entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto(" +
            "pc.id, " +
            "pc.consentInstant, " +
            "pc.reference, " +
            "pa.id, " +
            "pa.applicationType, " +
            "pa.appReference, " +
            "dr.id, " +
            "dr.status" +
            ") " +
            "FROM PwaConsent pc " +
            "LEFT JOIN PwaApplication pa ON pc.sourcePwaApplication = pa " +
            "LEFT JOIN DocgenRun dr ON dr.id = pc.docgenRunId " +
            "WHERE pc.id = :consent_id ",
        PwaConsentApplicationDto.class)
        .setParameter("consent_id", consentId)
        .setMaxResults(1)
        .getResultList();
    return pwaConsentApplicationDto.stream().findFirst();
  }

  @Override
  public List<PwaConsentApplicationDto> getConsentAndApplicationDtos(MasterPwa masterPwa) {
    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto(" +
        "pc.id, " +
        "pc.consentInstant, " +
        "pc.reference, " +
        "pa.id, " +
        "pa.applicationType, " +
        "pa.appReference, " +
        "dr.id, " +
        "dr.status" +
        ") " +
        "FROM PwaConsent pc " +
        "LEFT JOIN PwaApplication pa ON pc.sourcePwaApplication = pa " +
        "LEFT JOIN DocgenRun dr ON dr.id = pc.docgenRunId " +
        "WHERE pc.masterPwa = :master_pwa ",
        PwaConsentApplicationDto.class)
        .setParameter("master_pwa", masterPwa)
        .getResultList();
  }


}
