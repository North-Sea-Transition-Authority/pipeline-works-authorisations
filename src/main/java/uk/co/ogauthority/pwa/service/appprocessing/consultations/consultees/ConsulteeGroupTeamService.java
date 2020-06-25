package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupDetailRepository;
import uk.co.ogauthority.pwa.repository.appprocessing.consultations.consultees.ConsulteeGroupTeamMemberRepository;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Service
public class ConsulteeGroupTeamService {

  private final TeamService teamService;
  private final ConsulteeGroupDetailRepository groupDetailRepository;
  private final ConsulteeGroupTeamMemberRepository groupTeamMemberRepository;

  @Autowired
  public ConsulteeGroupTeamService(TeamService teamService,
                                   ConsulteeGroupDetailRepository groupDetailRepository,
                                   ConsulteeGroupTeamMemberRepository groupTeamMemberRepository) {
    this.teamService = teamService;
    this.groupDetailRepository = groupDetailRepository;
    this.groupTeamMemberRepository = groupTeamMemberRepository;
  }

  public List<ConsulteeGroupDetail> getManageableGroupDetailsForUser(WebUserAccount user) {

    Set<PwaRegulatorRole> userRegRoles = teamService
        .getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())
        .map(member -> member.getRoleSet().stream()
            .map(role -> PwaRegulatorRole.getValueByPortalTeamRoleName(role.getName()))
            .collect(Collectors.toSet()))
        .orElse(Set.of());

    // if user is an OGA team admin, they can administer any consultee group
    if (userRegRoles.contains(PwaRegulatorRole.TEAM_ADMINISTRATOR)) {
      return groupDetailRepository.findAllByEndTimestampIsNull();
    }

    // otherwise, get groups that user is an access manager for
    var groupSet = getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER);

    return groupDetailRepository.findAllByConsulteeGroupInAndEndTimestampIsNull(groupSet);

  }

  public List<ConsulteeGroupTeamView> getManageableGroupTeamViewsForUser(WebUserAccount user) {
    return getManageableGroupDetailsForUser(user).stream()
        .map(this::convertDetailToTeamView)
        .sorted(Comparator.comparing(ConsulteeGroupTeamView::getName))
        .collect(Collectors.toList());
  }

  public Set<ConsulteeGroup> getGroupsUserHasRoleFor(WebUserAccount user, ConsulteeGroupMemberRole role) {
    return groupTeamMemberRepository.findAllByPerson(user.getLinkedPerson()).stream()
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

  private TeamMemberView mapGroupMemberToTeamMemberView(ConsulteeGroupTeamMember groupTeamMember) {

    var roleSet = groupTeamMember.getRoles().stream()
        .map(role -> new TeamRoleView(role.name(), role.getDisplayName(), role.getDescription(), role.getDisplayOrder()))
        .collect(Collectors.toSet());

    return new TeamMemberView(
        groupTeamMember.getPerson(),
        "#", // TODO later update
        "#",
        roleSet
    );

  }

  @Transactional
  public void updateUserRoles(ConsulteeGroupDetail consulteeGroupDetail,
                              Person person,
                              UserRolesForm form,
                              AuthenticatedUserAccount currentUser) {

    var roleSet = form.getUserRoles().stream()
        .map(roleString -> EnumUtils.getEnumValue(ConsulteeGroupMemberRole.class, roleString))
        .collect(Collectors.toSet());

    var newTeamMember = new ConsulteeGroupTeamMember(consulteeGroupDetail.getConsulteeGroup(), person, roleSet);

    groupTeamMemberRepository.save(newTeamMember);

  }

}
