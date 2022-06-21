package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.exception.LastUserInRoleRemovedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.email.teammangement.AddedToTeamEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
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
import uk.co.ogauthority.pwa.service.teams.events.NonFoxTeamMemberEventPublisher;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Service
public class ConsulteeGroupTeamService {

  private final ConsulteeGroupDetailRepository groupDetailRepository;
  private final ConsulteeGroupTeamMemberRepository groupTeamMemberRepository;
  private final NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher;

  private final NotifyService notifyService;

  @Autowired
  public ConsulteeGroupTeamService(ConsulteeGroupDetailRepository groupDetailRepository,
                                   ConsulteeGroupTeamMemberRepository groupTeamMemberRepository,
                                   NonFoxTeamMemberEventPublisher nonFoxTeamMemberEventPublisher,
                                   NotifyService notifyService) {
    this.groupDetailRepository = groupDetailRepository;
    this.groupTeamMemberRepository = groupTeamMemberRepository;
    this.nonFoxTeamMemberEventPublisher = nonFoxTeamMemberEventPublisher;
    this.notifyService = notifyService;
  }

  public List<ConsulteeGroupDetail> getManageableGroupDetailsForUser(AuthenticatedUserAccount user) {

    boolean isOgaTeamAdmin = user.getUserPrivileges().stream()
        .anyMatch(priv -> priv.equals(PwaUserPrivilege.PWA_REGULATOR_ADMIN));

    // if user is an OGA team admin, they can administer any consultee group
    if (isOgaTeamAdmin) {
      return groupDetailRepository.findAllByEndTimestampIsNull();
    }

    // otherwise, get groups that user is an access manager for
    var groupSet = getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER);

    return groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(groupSet);

  }

  public List<ConsulteeGroupTeamView> getManageableGroupTeamViewsForUser(AuthenticatedUserAccount user) {
    return getManageableGroupDetailsForUser(user).stream()
        .map(this::convertDetailToTeamView)
        .sorted(Comparator.comparing(ConsulteeGroupTeamView::getName))
        .collect(Collectors.toList());
  }

  public Set<ConsulteeGroup> getGroupsUserHasRoleFor(WebUserAccount user, ConsulteeGroupMemberRole role) {
    return getTeamMemberByPerson(user.getLinkedPerson()).stream()
        .filter(member -> member.getRoles().contains(role))
        .map(ConsulteeGroupTeamMember::getConsulteeGroup)
        .collect(Collectors.toSet());
  }

  private ConsulteeGroupTeamView convertDetailToTeamView(ConsulteeGroupDetail detail) {
    return new ConsulteeGroupTeamView(detail.getConsulteeGroup().getId(), detail.getName());
  }

  public List<ConsulteeGroupTeamMember> getTeamMembersForGroup(ConsulteeGroup consulteeGroup) {
    return groupTeamMemberRepository.findAllByConsulteeGroup(consulteeGroup);
  }

  public List<TeamMemberView> getTeamMemberViewsForGroup(ConsulteeGroup consulteeGroup) {
    return getTeamMembersForGroup(consulteeGroup).stream()
        .map(this::mapGroupMemberToTeamMemberView)
        .collect(Collectors.toList());
  }

  public TeamMemberView mapGroupMemberToTeamMemberView(ConsulteeGroupTeamMember groupTeamMember) {

    var roleSet = groupTeamMember.getRoles().stream()
        .map(role -> new TeamRoleView(role.name(), role.getDisplayName(), role.getDescription(), role.getDisplayOrder()))
        .collect(Collectors.toSet());

    return new TeamMemberView(
        groupTeamMember.getPerson(),
        getEditUrl(groupTeamMember),
        getRemoveUrl(groupTeamMember),
        roleSet
    );

  }

  private String getEditUrl(ConsulteeGroupTeamMember groupTeamMember) {
    return ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderMemberRoles(
        groupTeamMember.getConsulteeGroup().getId(),
        groupTeamMember.getPerson().getId().asInt(),
        null,
        null));
  }

  private String getRemoveUrl(ConsulteeGroupTeamMember groupTeamMember) {
    return ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
        .renderRemoveMemberScreen(groupTeamMember.getConsulteeGroup().getId(),
            groupTeamMember.getPerson().getId().asInt(),
            null));
  }

  private void addMember(ConsulteeGroup consulteeGroup, Person person, Set<ConsulteeGroupMemberRole> roles) {

    if (roles.isEmpty()) {
      throw new IllegalStateException("Can't add a new ConsulteeGroupTeamMember when given an empty role set");
    }

    var newTeamMember = new ConsulteeGroupTeamMember(consulteeGroup, person, roles);
    groupTeamMemberRepository.save(newTeamMember);

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberAddedEvent(person);
    notifyNewTeamUser(consulteeGroup, person, roles);

  }

  public void notifyNewTeamUser(ConsulteeGroup consulteeGroup, Person person, Set<ConsulteeGroupMemberRole> selectedRoles) {
    var consulteeGroupDetail = groupDetailRepository.findByConsulteeGroupAndTipFlagIsTrue(consulteeGroup);
    if (consulteeGroupDetail.isPresent()) {
      var mdFormattedRoles = selectedRoles.stream()
          .map(ConsulteeGroupMemberRole::getDisplayName)
          .map(str -> "* " + str)
          .collect(Collectors.joining("\n"));

      var groupName = consulteeGroupDetail.get().getName();
      var emailProps = new AddedToTeamEmailProps(person.getFullName(), groupName, mdFormattedRoles);
      notifyService.sendEmail(emailProps, person.getEmailAddress());
    }
  }

  private void updateMemberRoles(ConsulteeGroupTeamMember teamMember, Set<ConsulteeGroupMemberRole> newRoles) {

    if (newRoles.isEmpty()) {
      throw new IllegalStateException("Can't update ConsulteeGroupTeamMember when given an empty role set");
    }

    assertNoEmptyRolesAfterUpdate(teamMember, newRoles);

    teamMember.setRoles(newRoles);
    groupTeamMemberRepository.save(teamMember);

  }

  private void assertNoEmptyRolesAfterUpdate(ConsulteeGroupTeamMember teamMember, Set<ConsulteeGroupMemberRole> newRoles) {

    // get number of users in each consultee role for group
    Map<ConsulteeGroupMemberRole, Long> roleToNumberOfUsersMap = getTeamMembersForGroup(teamMember.getConsulteeGroup()).stream()
        .flatMap(member -> member.getRoles().stream())
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    // get a list of roles that would be empty if this user's roles were updated to the newRoles
    var emptyRoles = teamMember.getRoles().stream()
        .filter(role -> roleToNumberOfUsersMap.get(role) == 1 && !newRoles.contains(role))
        .collect(Collectors.toList());

    // if there's one or more roles that would now have no users in it, throw exception
    if (!emptyRoles.isEmpty()) {

      String emptyRolesCsv = emptyRoles.stream()
          .map(role -> role.getDisplayName() + "s")
          .sorted(Comparator.comparing(roleString -> roleString))
          .collect(Collectors.joining(", "));

      throw new LastUserInRoleRemovedException(emptyRolesCsv);

    }

  }

  @Transactional
  public void updateUserRoles(ConsulteeGroup consulteeGroup,
                              Person person,
                              UserRolesForm form) {

    var roleSet = form.getUserRoles().stream()
        .map(roleString -> EnumUtils.getEnumValue(ConsulteeGroupMemberRole.class, roleString))
        .collect(Collectors.toSet());

    getTeamMemberByGroupAndPerson(consulteeGroup, person).ifPresentOrElse(
        member -> updateMemberRoles(member, roleSet),
        () -> addMember(consulteeGroup, person, roleSet)
    );

  }

  public Optional<ConsulteeGroupTeamMember> getTeamMemberByGroupAndPerson(ConsulteeGroup consulteeGroup, Person person) {
    return groupTeamMemberRepository.findByConsulteeGroupAndPerson(consulteeGroup, person);
  }

  public ConsulteeGroupTeamMember getTeamMemberOrError(ConsulteeGroup consulteeGroup, Person person) {
    return getTeamMemberByGroupAndPerson(consulteeGroup, person)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find team member for consultee group id [%s] and person id [%s]",
                consulteeGroup.getId(),
                person.getId())));
  }

  @Transactional
  public void removeTeamMember(ConsulteeGroup consulteeGroup, Person person) {

    var member = getTeamMemberOrError(consulteeGroup, person);

    assertNoEmptyRolesAfterUpdate(member, Set.of());

    groupTeamMemberRepository.delete(member);

    nonFoxTeamMemberEventPublisher.publishNonFoxTeamMemberRemovedEvent(person);

  }

  public Optional<ConsulteeGroupTeamMember> getTeamMemberByPerson(Person person) {
    return groupTeamMemberRepository.findByPerson(person);
  }

}
