package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

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
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class EditCarbonStorageCrossingFormValidatorTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;
  private EditCarbonStorageAreaCrossingFormValidator validator;

  @BeforeEach
  void setUp() {
    validator = new EditCarbonStorageAreaCrossingFormValidator(portalOrganisationsAccessor);
  }


  @Test
  void validate_NotOwnedByHolder_selectedOwnerOrgIsActive_valid() {

    var form = new AddCarbonStorageAreaCrossingForm();
    form.setStorageAreaRef("ref");
    form.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setOwnersOuIdList(List.of(orgUnitId));

    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(orgUnitId, "org");
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).doesNotContain(
        entry("ownersOuIdList", Set.of("ownersOuIdList" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }


  @Test
  void validate_NotOwnedByHolder_selectedOwnerOrgIsInActive_invalid() {

    var form = new EditCarbonStorageAreaCrossingForm();
    form.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setOwnersOuIdList(List.of(orgUnitId));


    var orgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("ownersOuIdList", Set.of("ownersOuIdList" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }



}
