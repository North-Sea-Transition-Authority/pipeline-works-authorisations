package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;

public class HuooSummaryValidationResultTest {

  @Test
  public void isValid_whenHuooRoleMissing() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(HuooRole.OWNER), List.of(), Set.of()).isValid()
    ).isFalse();
  }

  @Test
  public void isValid_whenOrgInactive() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of("Some organisation name"), Set.of()).isValid()
    ).isFalse();
  }

  @Test
  public void isValid_whenSomeBreachedBusinessRule() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of(), Set.of(HuooSummaryValidationResult.HuooRules.CANNOT_HAVE_TREATY_AND_PORTAL_ORG_USERS)).isValid()
    ).isFalse();
  }

  @Test
  public void isValid_whenProvidedCheckCollectionsEmpty() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of(), Set.of()).isValid()
    ).isTrue();
  }

  @Test
  public void testEqualsAndHashcode() {
    EqualsVerifier.forClass(HuooSummaryValidationResult.class)
        .verify();
  }
}