package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.LastUserInRoleRemovedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupTeamMemberRepository;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsulteeGroupTeamServiceTest {

  @Mock
  private ConsulteeGroupDetailRepository groupDetailRepository;

  @Mock
  private ConsulteeGroupTeamMemberRepository groupTeamMemberRepository;

  @Captor
  private ArgumentCaptor<ConsulteeGroupTeamMember> teamMemberArgumentCaptor;

  private ConsulteeGroupTeamService groupTeamService;

  private WebUserAccount user;
  private AuthenticatedUserAccount authenticatedUserAccount;

  private ConsulteeGroupDetail emtGroupDetail;
  private ConsulteeGroupDetail oduGroupDetail;

  @Before
  public void setUp() {

    emtGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");
    oduGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Offshore Decommissioning Unit", "ODU");

    when(groupDetailRepository.findAllByEndTimestampIsNull()).thenReturn(List.of(emtGroupDetail, oduGroupDetail));

    groupTeamService = new ConsulteeGroupTeamService(groupDetailRepository, groupTeamMemberRepository);

    user = new WebUserAccount(1, new Person(1, "forename", "surname", null, null));
    authenticatedUserAccount = new AuthenticatedUserAccount(user, List.of());

  }

  @Test
  public void getManageableGroupDetailsForUser_isRegulatorAdmin() {

    authenticatedUserAccount = new AuthenticatedUserAccount(user, List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN));

    assertThat(groupTeamService.getManageableGroupDetailsForUser(authenticatedUserAccount)).containsExactlyInAnyOrder(emtGroupDetail, oduGroupDetail);

  }

  @Test
  public void getManageableGroupDetailsForUser_isAccessManager() {

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember(
        emtGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findByPerson(user.getLinkedPerson())).thenReturn(Optional.of(consulteeGroupTeamMember));
    when(groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(any())).thenReturn(List.of(emtGroupDetail));

    assertThat(groupTeamService.getManageableGroupDetailsForUser(authenticatedUserAccount)).containsExactly(emtGroupDetail);

  }

  @Test
  public void getManageableGroupTeamViewsForUser_isRegulatorAdmin() {

    authenticatedUserAccount = new AuthenticatedUserAccount(user, List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN));

    assertThat(groupTeamService.getManageableGroupTeamViewsForUser(authenticatedUserAccount))
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

    when(groupTeamMemberRepository.findByPerson(user.getLinkedPerson())).thenReturn(Optional.of(consulteeGroupTeamMember));
    when(groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(any())).thenReturn(List.of(emtGroupDetail));

    assertThat(groupTeamService.getManageableGroupTeamViewsForUser(authenticatedUserAccount))
        .extracting(ConsulteeGroupTeamView::getConsulteeGroupId, ConsulteeGroupTeamView::getName, ConsulteeGroupTeamView::getManageUrl)
        .containsExactlyInAnyOrder(
            tuple(emtGroupDetail.getConsulteeGroupId(), emtGroupDetail.getName(), ReverseRouter
                .route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), null)))
        );

  }

  @Test
  public void getTeamMemberViewsForGroup_attributesMatchDetails() {

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
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), person1.getId().asInt(), null, null)),
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderRemoveMemberScreen(emtGroupDetail.getConsulteeGroupId(), person1.getId().asInt(), null))),

            tuple(person2.getForename(), person2.getSurname(), person2.getFullName(), person2.getEmailAddress(), person2.getTelephoneNo(),
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), person2.getId().asInt(), null, null)),
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderRemoveMemberScreen(emtGroupDetail.getConsulteeGroupId(), person2.getId().asInt(), null))),

            tuple(person3.getForename(), person3.getSurname(), person3.getFullName(), person3.getEmailAddress(), person3.getTelephoneNo(),
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), person3.getId().asInt(), null, null)),
                ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderRemoveMemberScreen(emtGroupDetail.getConsulteeGroupId(), person3.getId().asInt(), null)))

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
  public void getTeamMemberOrError_noErrorWhenTeamMemberFound() {

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(
        Optional.of(new ConsulteeGroupTeamMember())
    );

    assertThat(groupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).isNotNull();

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getTeamMemberOrError_error() {

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(any(), any())).thenReturn(Optional.empty());

    groupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

  }

  @Test
  public void removeTeamMember_successfulRemoval() {

    var member = new ConsulteeGroupTeamMember(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));
    var member2 = new ConsulteeGroupTeamMember(oduGroupDetail.getConsulteeGroup(), new Person(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.of(member));
    when(groupTeamMemberRepository.findAllByConsulteeGroup(oduGroupDetail.getConsulteeGroup())).thenReturn(List.of(member, member2));

    groupTeamService.removeTeamMember(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

    verify(groupTeamMemberRepository, times(1)).delete(member);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeTeamMember_doesntExist() {

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.empty());

    groupTeamService.removeTeamMember(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

  }

  @Test
  public void removeTeamMember_notLastInRoles() {

    var allRolesMember = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), new Person(), EnumSet.allOf(ConsulteeGroupMemberRole.class));
    var additionalAccessManager = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(
        Optional.of(additionalAccessManager));

    when(groupTeamMemberRepository.findAllByConsulteeGroup(emtGroupDetail.getConsulteeGroup())).thenReturn(
        List.of(allRolesMember, additionalAccessManager)
    );

    groupTeamService.removeTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

    verify(groupTeamMemberRepository, times(1)).delete(additionalAccessManager);

  }

  @Test
  public void removeTeamMember_lastInRoles() {

    var accessRecipient = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RECIPIENT));
    var responder = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), new Person(), Set.of(ConsulteeGroupMemberRole.RESPONDER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.of(accessRecipient));
    when(groupTeamMemberRepository.findAllByConsulteeGroup(emtGroupDetail.getConsulteeGroup())).thenReturn(
        List.of(accessRecipient, responder)
    );

    boolean thrown = false;
    try {
      groupTeamService.removeTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());
    } catch (LastUserInRoleRemovedException e) {
      thrown = true;
      assertThat(e.getMessage()).contains("Access managers, Consultation recipients");
    }

    assertThat(thrown).isTrue();

  }

  @Test
  public void updateUsersRoles() {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.of(member));
    when(groupTeamMemberRepository.findAllByConsulteeGroup(emtGroupDetail.getConsulteeGroup())).thenReturn(List.of(member));

    var newRoles = Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RECIPIENT);
    var form = getRolesFormWithRoles(newRoles);

    groupTeamService.updateUserRoles(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), form);

    verify(groupTeamMemberRepository, times(1)).save(teamMemberArgumentCaptor.capture());

    var updatedMember = teamMemberArgumentCaptor.getValue();

    assertThat(updatedMember.getConsulteeGroup()).isEqualTo(emtGroupDetail.getConsulteeGroup());
    assertThat(updatedMember.getPerson()).isEqualTo(user.getLinkedPerson());
    assertThat(updatedMember.getRoles()).containsExactlyInAnyOrder(
        ConsulteeGroupMemberRole.ACCESS_MANAGER,
        ConsulteeGroupMemberRole.RECIPIENT
    );

  }

  @Test
  public void updateUsersRoles_notMember() {

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.empty());

    var form = getRolesFormWithRoles(Set.of(ConsulteeGroupMemberRole.RESPONDER));

    groupTeamService.updateUserRoles(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), form);

    verify(groupTeamMemberRepository, times(1)).save(teamMemberArgumentCaptor.capture());

    var newMember = teamMemberArgumentCaptor.getValue();

    assertThat(newMember.getConsulteeGroup()).isEqualTo(oduGroupDetail.getConsulteeGroup());
    assertThat(newMember.getPerson()).isEqualTo(user.getLinkedPerson());
    assertThat(newMember.getRoles()).containsExactly(ConsulteeGroupMemberRole.RESPONDER);

  }

  @Test(expected = IllegalStateException.class)
  public void updateUsersRoles_emptyRoles() {

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(any(), any())).thenReturn(Optional.of(new ConsulteeGroupTeamMember()));

    var form = new UserRolesForm();
    form.setUserRoles(List.of());

    groupTeamService.updateUserRoles(oduGroupDetail.getConsulteeGroup(), new Person(), form);

  }

  @Test
  public void updateUsersRoles_changeRoles_notLastinRoles() {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RESPONDER));
    var additionalAccessManager = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), new Person(), Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RESPONDER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(Optional.of(member));
    when(groupTeamMemberRepository.findAllByConsulteeGroup(emtGroupDetail.getConsulteeGroup())).thenReturn(
        List.of(member, additionalAccessManager)
    );

    var form = getRolesFormWithRoles(Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    groupTeamService.updateUserRoles(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), form);

    verify(groupTeamMemberRepository, times(1)).save(teamMemberArgumentCaptor.capture());

    var updatedMember = teamMemberArgumentCaptor.getValue();

    assertThat(updatedMember.getConsulteeGroup()).isEqualTo(emtGroupDetail.getConsulteeGroup());
    assertThat(updatedMember.getPerson()).isEqualTo(user.getLinkedPerson());
    assertThat(updatedMember.getRoles()).containsExactlyInAnyOrder(ConsulteeGroupMemberRole.RECIPIENT);

  }

  @Test
  public void updateUsersRoles_changeRoles_lastInRoles() {

    var member = new ConsulteeGroupTeamMember(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson(),
        Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RESPONDER));

    when(groupTeamMemberRepository.findByConsulteeGroupAndPerson(oduGroupDetail.getConsulteeGroup(),
        user.getLinkedPerson())).thenReturn(Optional.of(member));
    when(groupTeamMemberRepository.findAllByConsulteeGroup(oduGroupDetail.getConsulteeGroup())).thenReturn(
        List.of(member));

    var form = getRolesFormWithRoles(Set.of(ConsulteeGroupMemberRole.RECIPIENT));

    boolean thrown = false;
    try {
      groupTeamService.updateUserRoles(oduGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), form);
    } catch (LastUserInRoleRemovedException e) {
      thrown = true;
      assertThat(e.getMessage()).contains("Access managers, Consultation responders");
    }

    assertThat(thrown).isTrue();

  }

  private UserRolesForm getRolesFormWithRoles(Set<ConsulteeGroupMemberRole> roles) {

    var form = new UserRolesForm();

    form.setUserRoles(roles.stream()
        .map(Enum::name)
        .collect(Collectors.toList()));

    return form;

  }

}
