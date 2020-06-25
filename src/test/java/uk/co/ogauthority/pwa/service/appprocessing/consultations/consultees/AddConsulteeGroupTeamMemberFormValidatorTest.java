package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberForm;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddConsulteeGroupTeamMemberFormValidatorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private ConsulteeGroupDetail groupDetail;

  private AddConsulteeGroupTeamMemberForm memberForm;
  private AddConsulteeGroupTeamMemberFormValidator memberFormValidator;

  @Before
  public void before() {

    groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");

    memberForm = new AddConsulteeGroupTeamMemberForm();
    memberFormValidator = new AddConsulteeGroupTeamMemberFormValidator(teamManagementService, consulteeGroupTeamService);

  }

  @Test
  public void validate_userIdentifier_noErrors() {

    var person = new Person();

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));

    memberForm.setUserIdentifier("userId");

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_userIdentifier_blank() {

    var errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.required")));

    verifyNoInteractions(teamManagementService, consulteeGroupTeamService);

  }

  @Test
  public void validate_userIdentifier_userNotFound() {

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.empty());

    memberForm.setUserIdentifier("userId");

    var errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userNotFound")));

    verifyNoInteractions(consulteeGroupTeamService);

  }

  @Test
  public void validate_userIdentifier_userAlreadyExists() {

    var person = new Person();
    var existingMember = new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), person, Set.of(
        ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(consulteeGroupTeamService.getTeamMembersForGroup(groupDetail.getConsulteeGroup()))
        .thenReturn(List.of(existingMember));

    memberForm.setUserIdentifier("userId");

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userAlreadyExists")));

  }

}
