package uk.co.ogauthority.pwa.validators;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.EditBlockCrossingFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class EditBlockCrossingFormValidatorTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;
  private EditBlockCrossingFormValidator validator;

  private PearsLicence pearsLicence;

  @Before
  public void setUp() {
    pearsLicence = new PearsLicence();
    validator = new EditBlockCrossingFormValidator(portalOrganisationsAccessor);
  }



  @Test
  public void validate_blockNotOwnedByHolder_selectedOwnerOrgIsActive_valid() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");
    form.setCrossedBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setBlockOwnersOuIdList(List.of(orgUnitId));


    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(orgUnitId, "org");
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getBlockOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pearsLicence);
    assertThat(errorsMap).doesNotContain(
        entry("blockOwnersOuIdList[0]", Set.of("blockOwnersOuIdList[0]" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }


  @Test
  public void validate_blockNotOwnedByHolder_selectedOwnerOrgIsInActive_invalid() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");
    form.setCrossedBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);
    var orgUnitId = 1;
    form.setBlockOwnersOuIdList(List.of(orgUnitId));


    var orgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(form.getBlockOwnersOuIdList())).thenReturn(List.of(orgUnit));

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, pearsLicence);
    assertThat(errorsMap).contains(
        entry("blockOwnersOuIdList[0]", Set.of("blockOwnersOuIdList[0]" + FieldValidationErrorCodes.INVALID.getCode()))
    );

  }



}
