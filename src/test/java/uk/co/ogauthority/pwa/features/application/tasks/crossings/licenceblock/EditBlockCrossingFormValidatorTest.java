package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class EditBlockCrossingFormValidatorTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;
  private EditBlockCrossingFormValidator validator;

  private PearsLicence pearsLicence;

  @BeforeEach
  void setUp() {
    pearsLicence = new PearsLicence();
    validator = new EditBlockCrossingFormValidator(portalOrganisationsAccessor);
  }


  @Test
  void validate_blockNotOwnedByHolder_selectedOwnerOrgIsActive_valid() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");
    form.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setBlockOwnersOuIdList(List.of(orgUnitId));

    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(orgUnitId, "org");
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getBlockOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pearsLicence);
    assertThat(errorsMap).doesNotContain(
        entry("blockOwnersOuIdList", Set.of("blockOwnersOuIdList" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }


  @Test
  void validate_blockNotOwnedByHolder_selectedOwnerOrgIsInActive_invalid() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");
    form.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setBlockOwnersOuIdList(List.of(orgUnitId));


    var orgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getBlockOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pearsLicence);
    assertThat(errorsMap).contains(
        entry("blockOwnersOuIdList", Set.of("blockOwnersOuIdList" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }

  @Test
  void validate_blockOwnerLicensedBlock_MarkedAsUnlicensed() {
    var form = new AddBlockCrossingForm();
    form.setPickedBlock("licensedBlock");
    form.setCrossingOwner(CrossingOwner.UNLICENSED);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pearsLicence);
    assertThat(errorsMap).contains(
        entry("crossingOwner", Set.of("crossingOwner" + FieldValidationErrorCodes.INVALID.getCode()))
    );
  }
}
