package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaUserPrivilegeServiceTest {

  @Mock
  private ConsulteeGroupTeamService groupTeamService;

  private PwaUserPrivilegeService userPrivilegeService;

  private Person person = new Person(1, null, null, null, null);

  private ConsulteeGroupDetail groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");

  @Before
  public void setUp() {
    userPrivilegeService = new PwaUserPrivilegeService(groupTeamService);
  }

  @Test
  public void getPwaUserPrivilegesForPerson_noConsulteeGroupMembership() {

    when(groupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.empty());

    var privSet = userPrivilegeService.getPwaUserPrivilegesForPerson(person);

    assertThat(privSet).isEmpty();

  }

  @Test
  public void getPwaUserPrivilegesForPerson_consulteeGroupMember_notAccessManager() {

    var member = new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), person, Set.of(ConsulteeGroupMemberRole.RECIPIENT));

    when(groupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(member));

    var privSet = userPrivilegeService.getPwaUserPrivilegesForPerson(person);

    assertThat(privSet).containsExactlyInAnyOrder(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_CONSULTEE);

  }

  @Test
  public void getPwaUserPrivilegesForPerson_consulteeGroupMember_isAccessManager() {

    var member = new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), person, Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(groupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(member));

    var privSet = userPrivilegeService.getPwaUserPrivilegesForPerson(person);

    assertThat(privSet).containsExactlyInAnyOrder(
        PwaUserPrivilege.PWA_WORKAREA,
        PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN,
        PwaUserPrivilege.PWA_CONSULTEE);

  }

}
