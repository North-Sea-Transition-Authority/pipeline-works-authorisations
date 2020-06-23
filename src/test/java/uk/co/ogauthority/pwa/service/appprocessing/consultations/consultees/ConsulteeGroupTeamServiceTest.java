package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupTeamMemberRepository;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsulteeGroupTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private ConsulteeGroupDetailRepository groupDetailRepository;

  @Mock
  private ConsulteeGroupTeamMemberRepository groupTeamMemberRepository;

  private ConsulteeGroupTeamService groupTeamService;

  private WebUserAccount user;

  private ConsulteeGroupDetail emtGroupDetail;
  private ConsulteeGroupDetail oduGroupDetail;

  @Before
  public void setUp() {

    emtGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");
    oduGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Offshore Decommissioning Unit", "ODU");

    when(groupDetailRepository.findAllByEndTimestampIsNull()).thenReturn(List.of(emtGroupDetail, oduGroupDetail));

    groupTeamService = new ConsulteeGroupTeamService(teamService, groupDetailRepository, groupTeamMemberRepository);

    user = new WebUserAccount(1, new Person(1, "forename", "surname", null, null));

  }

  @Test
  public void getManageableGroupTeamsForUser_isRegulatorAdmin() {

    var adminTeamMember = TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(), user.getLinkedPerson(),
        Set.of(PwaRegulatorRole.TEAM_ADMINISTRATOR));

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson()))
        .thenReturn(Optional.of(adminTeamMember));

    assertThat(groupTeamService.getManageableGroupTeamsForUser(user))
        .extracting(ConsulteeGroupTeamView::getId, ConsulteeGroupTeamView::getName, ConsulteeGroupTeamView::getManageUrl)
        .containsExactlyInAnyOrder(
            tuple(emtGroupDetail.getId(), emtGroupDetail.getName(), "add later"),
            tuple(oduGroupDetail.getId(), oduGroupDetail.getName(), "add later")
        );

  }

  @Test
  public void getManageableGroupTeamsForUser_isAccessManager() {

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember(
        emtGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findAllByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));
    when(groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(any())).thenReturn(List.of(emtGroupDetail));

    assertThat(groupTeamService.getManageableGroupTeamsForUser(user))
        .extracting(ConsulteeGroupTeamView::getId, ConsulteeGroupTeamView::getName, ConsulteeGroupTeamView::getManageUrl)
        .containsExactlyInAnyOrder(
            tuple(emtGroupDetail.getId(), emtGroupDetail.getName(), "add later")
        );

  }

  @Test
  public void getGroupsUserHasRoleFor() {

    var emtAccessManager = new ConsulteeGroupTeamMember(
        emtGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    var oduRecipient = new ConsulteeGroupTeamMember(
        oduGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.RECIPIENT));

    when(groupTeamMemberRepository.findAllByPerson(user.getLinkedPerson())).thenReturn(List.of(emtAccessManager, oduRecipient));

    assertThat(groupTeamService.getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER)).containsOnly(emtGroupDetail.getConsulteeGroup());

    assertThat(groupTeamService.getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.RECIPIENT)).containsOnly(oduRecipient.getConsulteeGroup());

  }

}
