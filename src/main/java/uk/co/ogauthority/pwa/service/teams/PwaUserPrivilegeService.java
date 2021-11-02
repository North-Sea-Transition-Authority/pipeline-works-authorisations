package uk.co.ogauthority.pwa.service.teams;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;

@Service
public class PwaUserPrivilegeService {

  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final PwaContactService pwaContactService;

  @Autowired
  public PwaUserPrivilegeService(ConsulteeGroupTeamService consulteeGroupTeamService,
                                 PwaContactService pwaContactService) {
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.pwaContactService = pwaContactService;
  }

  /**
   * Get user privileges for a user based on PWA domain objects instead of portal objects.
   */
  public Set<PwaUserPrivilege> getPwaUserPrivilegesForPerson(Person person) {

    var consulteePrivs = getConsulteeGrantedPrivs(person);

    var contactPrivs = getPwaContactGrantedPrivs(person);

    return Sets.union(consulteePrivs, contactPrivs);

  }

  private Set<PwaUserPrivilege> getPwaContactGrantedPrivs(Person person) {
    var contactRoleDtoList = pwaContactService.getPwaContactRolesForPerson(
        person,
        EnumSet.allOf(PwaContactRole.class)
    );

    var privSet = new HashSet<PwaUserPrivilege>();

    if (!contactRoleDtoList.isEmpty()) {
      privSet.add(PwaUserPrivilege.PWA_WORKAREA);
      privSet.add(PwaUserPrivilege.PWA_INDUSTRY);
    }

    return privSet;
  }

  private Set<PwaUserPrivilege> getConsulteeGrantedPrivs(Person person) {
    var teamMemberList = consulteeGroupTeamService.getTeamMemberByPerson(person);

    var privSet = new HashSet<PwaUserPrivilege>();

    if (!teamMemberList.isEmpty()) {

      boolean isAccessManager = teamMemberList.stream()
          .flatMap(groupTeamMember -> groupTeamMember.getRoles().stream())
          .anyMatch(role -> role.equals(ConsulteeGroupMemberRole.ACCESS_MANAGER));

      if (isAccessManager) {
        privSet.add(PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN);
      }

      privSet.add(PwaUserPrivilege.PWA_WORKAREA);
      privSet.add(PwaUserPrivilege.PWA_CONSULTEE);
      privSet.add(PwaUserPrivilege.PWA_APPLICATION_SEARCH);

    }

    return privSet;
  }


}
