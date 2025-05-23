package uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberForm;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AddConsulteeGroupTeamMemberFormValidatorTest {

  @Mock
  private OldTeamManagementService teamManagementService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private ConsulteeGroupDetail groupDetail;

  private AddConsulteeGroupTeamMemberForm memberForm;
  private AddConsulteeGroupTeamMemberFormValidator memberFormValidator;

  @BeforeEach
  void before() {

    groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");

    memberForm = new AddConsulteeGroupTeamMemberForm();
    memberFormValidator = new AddConsulteeGroupTeamMemberFormValidator(teamManagementService, consulteeGroupTeamService);

  }

  @Test
  void validate_userIdentifier_noErrors() {

    var person = new Person();

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));

    memberForm.setUserIdentifier("userId");

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).isEmpty();

  }

  @Test
  void validate_userIdentifier_blank() {

    var errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.required")));

    verifyNoInteractions(teamManagementService, consulteeGroupTeamService);

  }

  @Test
  void validate_userIdentifier_userNotFound() {

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.empty());

    memberForm.setUserIdentifier("userId");

    var errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userNotFound")));

    verifyNoInteractions(consulteeGroupTeamService);

  }

  @Test
  void validate_userIdentifier_userAlreadyExists() {

    var person = new Person();
    var existingMember = new ConsulteeGroupTeamMember(groupDetail.getConsulteeGroup(), person, Set.of(
        ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(consulteeGroupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(existingMember));

    memberForm.setUserIdentifier("userId");

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.userAlreadyExists")));

  }

  @Test
  void validate_userIdentifier_partOfAnotherConsulteeGroupTeam() {

    var person = new Person();
    var newGroup = ConsulteeGroupTestingUtils.createConsulteeGroup("group2", "g2").getConsulteeGroup();

    var existingMember = new ConsulteeGroupTeamMember(newGroup, person, Set.of(
        ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(teamManagementService.getPersonByEmailAddressOrLoginId("userId")).thenReturn(Optional.of(person));
    when(consulteeGroupTeamService.getTeamMemberByPerson(person)).thenReturn(Optional.of(existingMember));

    memberForm.setUserIdentifier("userId");

    Map<String, Set<String>> errors = ValidatorTestUtils.getFormValidationErrors(memberFormValidator, memberForm, groupDetail);

    assertThat(errors).containsOnly(entry("userIdentifier", Set.of("userIdentifier.invalid")));

  }

}
