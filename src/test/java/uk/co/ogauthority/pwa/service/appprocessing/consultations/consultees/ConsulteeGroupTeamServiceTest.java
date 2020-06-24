package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
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

  @Captor
  private ArgumentCaptor<ConsulteeGroupTeamMember> teamMemberArgumentCaptor;

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
  public void getManageableGroupDetailsForUser_isRegulatorAdmin() {

    var adminTeamMember = TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(), user.getLinkedPerson(),
        Set.of(PwaRegulatorRole.TEAM_ADMINISTRATOR));

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson()))
        .thenReturn(Optional.of(adminTeamMember));

    assertThat(groupTeamService.getManageableGroupDetailsForUser(user)).containsExactlyInAnyOrder(emtGroupDetail, oduGroupDetail);

  }

  @Test
  public void getManageableGroupDetailsForUser_isAccessManager() {

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember(
        emtGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findAllByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));
    when(groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(any())).thenReturn(List.of(emtGroupDetail));

    assertThat(groupTeamService.getManageableGroupDetailsForUser(user)).containsExactly(emtGroupDetail);

  }

  @Test
  public void getManageableGroupTeamViewsForUser_isRegulatorAdmin() {

    var adminTeamMember = TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(), user.getLinkedPerson(),
        Set.of(PwaRegulatorRole.TEAM_ADMINISTRATOR));

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson()))
        .thenReturn(Optional.of(adminTeamMember));

    assertThat(groupTeamService.getManageableGroupTeamViewsForUser(user))
        .extracting(ConsulteeGroupTeamView::getConsulteeGroupId, ConsulteeGroupTeamView::getName, ConsulteeGroupTeamView::getManageUrl)
        .containsExactlyInAnyOrder(
            tuple(emtGroupDetail.getConsulteeGroupId(), emtGroupDetail.getName(), ReverseRouter
                .route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), null))),
            tuple(oduGroupDetail.getConsulteeGroupId(), oduGroupDetail.getName(), ReverseRouter
                .route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(oduGroupDetail.getConsulteeGroupId(), null)))
        );

  }

  @Test
  public void getManageableGroupTeamViewsForUser_isAccessManager() {

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember(
        emtGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findAllByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));
    when(groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(any())).thenReturn(List.of(emtGroupDetail));

    assertThat(groupTeamService.getManageableGroupTeamViewsForUser(user))
        .extracting(ConsulteeGroupTeamView::getConsulteeGroupId, ConsulteeGroupTeamView::getName, ConsulteeGroupTeamView::getManageUrl)
        .containsExactlyInAnyOrder(
            tuple(emtGroupDetail.getConsulteeGroupId(), emtGroupDetail.getName(), ReverseRouter
                .route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), null)))
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

  @Test
  public void getTeamMemberViewsForGroup() {

    var person1 = new Person(12, "1", "11", "a@b.com", "01234567889");
    var person2 = new Person(13, "2", "22", "b@c.com", "0987654321");
    var person3 = new Person(14, "3", "33", "c@d.com", "645378389201");

    var member1 = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), person1, Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));
    var member2 = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), person2, Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    var member3 = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), person3, Set.of(ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER));

    var memberList = List.of(member1, member2, member3);

    when(groupTeamMemberRepository.findAllByConsulteeGroup(emtGroupDetail.getConsulteeGroup())).thenReturn(memberList);

    var teamMemberViews = groupTeamService.getTeamMemberViewsForGroup(emtGroupDetail.getConsulteeGroup());

    assertThat(teamMemberViews).extracting(
        TeamMemberView::getForename,
        TeamMemberView::getSurname,
        TeamMemberView::getFullName,
        TeamMemberView::getEmailAddress,
        TeamMemberView::getTelephoneNo,
        TeamMemberView::getEditRoute,
        TeamMemberView::getRemoveRoute).contains(

            tuple(person1.getForename(), person1.getSurname(), person1.getFullName(), person1.getEmailAddress(), person1.getTelephoneNo(),
                "#", "#"),

            tuple(person1.getForename(), person1.getSurname(), person1.getFullName(), person1.getEmailAddress(), person1.getTelephoneNo(),
                "#", "#"),

            tuple(person1.getForename(), person1.getSurname(), person1.getFullName(), person1.getEmailAddress(), person1.getTelephoneNo(),
                "#", "#")
    );

    teamMemberViews.forEach(teamMemberView -> {

      var foundMember = memberList.stream()
          .filter(member -> member.getPerson().getFullName().equals(teamMemberView.getFullName()))
          .findFirst()
          .orElseThrow();

      var roleTuples = foundMember.getRoles().stream()
          .map(role -> tuple(role.name(), role.getDisplayName(), role.getDescription(), role.getDisplayOrder()))
          .collect(Collectors.toList());

      assertThat(teamMemberView.getRoleViews()).extracting(
          TeamRoleView::getRoleName,
          TeamRoleView::getTitle,
          TeamRoleView::getDescription,
          TeamRoleView::getDisplaySequence
      ).containsExactlyInAnyOrderElementsOf(roleTuples);

    });

  }

  @Test
  public void updateUserRoles() {

    var form = new UserRolesForm();
    form.setUserRoles(List.of("ACCESS_MANAGER", "RECIPIENT"));

    groupTeamService.updateUserRoles(emtGroupDetail, user.getLinkedPerson(), form, null);

    verify(groupTeamMemberRepository, times(1)).save(teamMemberArgumentCaptor.capture());

    var newMember = teamMemberArgumentCaptor.getValue();

    assertThat(newMember.getConsulteeGroup()).isEqualTo(emtGroupDetail.getConsulteeGroup());
    assertThat(newMember.getPerson()).isEqualTo(user.getLinkedPerson());
    assertThat(newMember.getRoles()).containsExactlyInAnyOrder(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RECIPIENT);

  }

}
