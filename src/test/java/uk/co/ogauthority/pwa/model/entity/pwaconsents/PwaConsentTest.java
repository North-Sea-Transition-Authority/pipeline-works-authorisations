package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import javax.persistence.Entity;
import javax.persistence.Id;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class PwaConsentTest {

  @Test
  public void testEqualsAndHashCode() {
    EqualsVerifier.forClass(PwaConsent.class)
        .withIgnoredAnnotations(Entity.class, Id.class)
        .suppress(Warning.NONFINAL_FIELDS, Warning.STRICT_INHERITANCE)
        .verify();
  }
}