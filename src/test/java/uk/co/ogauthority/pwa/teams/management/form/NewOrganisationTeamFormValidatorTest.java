package uk.co.ogauthority.pwa.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class NewOrganisationTeamFormValidatorTest {
  @Mock
  private TeamManagementService teamManagementService;

  @InjectMocks
  private NewOrganisationTeamFormValidator newOrganisationTeamFormValidator;

  private NewOrganisationTeamForm form;
  private BeanPropertyBindingResult errors;

  @BeforeEach
  void setUp() {
    form = new NewOrganisationTeamForm();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void isValid() {
    form.setOrgGroupId("50");

    when(teamManagementService.doesScopedTeamWithReferenceExist(eq(TeamType.ORGANISATION), refEq(TeamScopeReference.from("50", "ORGGRP"))))
        .thenReturn(false);

    assertThat(newOrganisationTeamFormValidator.isValid(form, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noId() {
    form.setOrgGroupId("");

    assertThat(newOrganisationTeamFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_orgTeamAlreadyExists() {
    form.setOrgGroupId("50");

    when(teamManagementService.doesScopedTeamWithReferenceExist(eq(TeamType.ORGANISATION), refEq(TeamScopeReference.from("50", "ORGGRP"))))
        .thenReturn(true);

    assertThat(newOrganisationTeamFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }
}
