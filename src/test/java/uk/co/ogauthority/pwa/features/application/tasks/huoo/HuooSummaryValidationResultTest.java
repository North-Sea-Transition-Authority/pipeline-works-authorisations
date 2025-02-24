package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;

class HuooSummaryValidationResultTest {

  @Test
  void isValid_whenHuooRoleMissing() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(HuooRole.OWNER), List.of(), Set.of()).isValid()
    ).isFalse();
  }

  @Test
  void isValid_whenOrgInactive() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of("Some organisation name"), Set.of()).isValid()
    ).isFalse();
  }

  @Test
  void isValid_whenSomeBreachedBusinessRule() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of(), Set.of(HuooSummaryValidationResult.HuooRules.CANNOT_HAVE_TREATY_AND_PORTAL_ORG_USERS)).isValid()
    ).isFalse();
  }

  @Test
  void isValid_whenProvidedCheckCollectionsEmpty() {

    assertThat(
        new HuooSummaryValidationResult(Set.of(), List.of(), Set.of()).isValid()
    ).isTrue();
  }

  @Test
  void equalsAndHashcode() {
    EqualsVerifier.forClass(HuooSummaryValidationResult.class)
        .verify();
  }
}