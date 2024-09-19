package uk.co.ogauthority.pwa.service.pwaconsents;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsentReferenceSeqNumberGenerator implements PwaConsentReferenceNumberGenerator {

  private final EntityManager entityManager;

  @Autowired
  public ConsentReferenceSeqNumberGenerator(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public int getConsentNumber(LocalDate consentDate) {

    // find out how many consents have already been issued in the year of consent
    int numberOfConsentsInYear = ((BigDecimal) entityManager.createNativeQuery("" +
        "SELECT COUNT(*) " +
        "FROM pwa_consents pc " +
        "WHERE TO_CHAR(pc.consent_timestamp, 'YYYY') = :consentYear")
        .setParameter("consentYear", consentDate.getYear())
        .getSingleResult()).intValue();

    // if we are the first consent in the year (no others exist), restart the consent numbering sequence
    if (numberOfConsentsInYear == 0) {
      entityManager.createNativeQuery("ALTER SEQUENCE pwa_consent_ref_seq RESTART START WITH 1").executeUpdate();
    }

    // return the next value in the sequence
    return ((BigDecimal) entityManager.createNativeQuery("" +
        "SELECT pwa_consent_ref_seq.nextval value " +
        "FROM dual").getSingleResult()).intValue();

  }

}
