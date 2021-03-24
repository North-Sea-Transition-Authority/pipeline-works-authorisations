package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.util.List;
import javax.persistence.EntityManager;
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
  public List<PwaConsentApplicationDto> getConsentAndApplicationDto(MasterPwa masterPwa) {
    return entityManager.createQuery("" +
        "SELECT new uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto(" +
        "pc.id, " +
        "pc.consentInstant, " +
        "pc.reference, " +
        "pa.id, " +
        "pa.applicationType, " +
        "pa.appReference" +
        ") " +
        "FROM PwaConsent pc " +
        "LEFT JOIN uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication pa ON pc.sourcePwaApplication = pa " +
        "WHERE pc.masterPwa = :master_pwa ",
        PwaConsentApplicationDto.class)
        .setParameter("master_pwa", masterPwa)
        .getResultList();
  }


}
