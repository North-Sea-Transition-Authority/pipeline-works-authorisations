package uk.co.ogauthority.pwa.model.dto.appprocessing;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;

public class ApplicationInvolvementDtoTest {

  @Test
  public void isUserInAppContactTeam_whenConstructedWithContactRoles() {
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
  public void isUserInAppContactTeam_whenConstructedWithoutContactRoles() {
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
  public void isUserInHolderTeam_whenConstructedWithHolderTeamRoles() {
    var involvement = new ApplicationInvolvementDto(
        null,
        Set.of(),
        null,
        false,
        false,
        false,
        Set.of(PwaOrganisationRole.APPLICATION_CREATOR),
        false,
        null);

    assertThat(involvement.isUserInHolderTeam()).isTrue();

  }

  @Test
  public void isUserInHolderTeam_whenConstructedWithoutHolderTeamRoles() {
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