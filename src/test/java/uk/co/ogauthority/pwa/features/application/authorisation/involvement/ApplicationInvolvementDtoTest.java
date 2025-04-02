package uk.co.ogauthority.pwa.features.application.authorisation.involvement;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.teams.Role;

class ApplicationInvolvementDtoTest {

  @Test
  void isUserInAppContactTeam_whenConstructedWithContactRoles() {
    var involvement = new ApplicationInvolvementDto(
        null,
        Set.of(PwaContactRole.VIEWER),
        null,
        false,
        false,
        false,
        Set.of(),
        false,
        null);

    assertThat(involvement.isUserInAppContactTeam()).isTrue();

  }

  @Test
  void isUserInAppContactTeam_whenConstructedWithoutContactRoles() {
    var involvement = new ApplicationInvolvementDto(
        null,
        Set.of(),
        null,
        false,
        false,
        false,
        Set.of(),
        false,
        null);

    assertThat(involvement.isUserInAppContactTeam()).isFalse();
  }


  @Test
  void isUserInHolderTeam_whenConstructedWithHolderTeamRoles() {
    var involvement = new ApplicationInvolvementDto(
        null,
        Set.of(),
        null,
        false,
        false,
        false,
        Set.of(Role.APPLICATION_CREATOR),
        false,
        null);

    assertThat(involvement.isUserInHolderTeam()).isTrue();

  }

  @Test
  void isUserInHolderTeam_whenConstructedWithoutHolderTeamRoles() {
    var involvement = new ApplicationInvolvementDto(
        null,
        Set.of(),
        null,
        false,
        false,
        false,
        Set.of(),
        false,
        null);

    assertThat(involvement.isUserInHolderTeam()).isFalse();
  }
}