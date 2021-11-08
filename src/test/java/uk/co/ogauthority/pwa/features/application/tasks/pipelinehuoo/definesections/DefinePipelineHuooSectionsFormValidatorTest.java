package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.DefinePipelineHuooSectionsFormValidator.PIPELINE_SECTION_POINTS_ATTR;
import static uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.DefinePipelineHuooSectionsFormValidator.SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR;
import static uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.definesections.DefinePipelineHuooSectionsFormValidator.SECTION_POINT_IDENT_STRING_ATTR;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.INVALID;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.NOT_UNIQUE;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.REQUIRED;
import static uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes.TOO_MANY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class DefinePipelineHuooSectionsFormValidatorTest {

  private static final PipelineId PIPELINE_ID = new PipelineId(1);
  private static final HuooRole HUOO_ROLE = HuooRole.HOLDER;
  private static final int NUMBER_OF_SECTIONS = 3;

  @Mock
  private PickableHuooPipelineIdentService pickableHuooPipelineIdentService;

  private DefinePipelineHuooSectionsForm form;

  private DefinePipelineHuooSectionValidationHint defaultValidationHint;

  private DefinePipelineHuooSectionsFormValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private List<PickableIdentLocationOption> pipelineIdentLocationOptions;
  private PickableIdentLocationOption ident1LocationPoint1;
  private PickableIdentLocationOption ident1LocationPoint2;

  private PickableIdentLocationOption ident2LocationPoint1;
  private PickableIdentLocationOption ident2LocationPoint2;

  private PickableIdentLocationOption ident3LocationPoint1;
  private PickableIdentLocationOption ident3LocationPoint2;

  private PickableIdentLocationOption finalPickableIdentLocation;

  @Before
  public void setUp() throws Exception {
    validator = new DefinePipelineHuooSectionsFormValidator(pickableHuooPipelineIdentService);

    form = new DefinePipelineHuooSectionsForm();

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);

    defaultValidationHint = new DefinePipelineHuooSectionValidationHint(
        pwaApplicationDetail,
        HUOO_ROLE,
        PIPELINE_ID,
        NUMBER_OF_SECTIONS);

    ident1LocationPoint1 = new PickableIdentLocationOption(
        1, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "POINT1");
    ident1LocationPoint2 = new PickableIdentLocationOption(
        1, PickableIdentLocationOption.IdentPoint.TO_LOCATION, "POINT2");

    ident2LocationPoint1 = new PickableIdentLocationOption(
        2, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "POINT2");
    ident2LocationPoint2 = new PickableIdentLocationOption(
        2, PickableIdentLocationOption.IdentPoint.TO_LOCATION, "POINT3");

    ident3LocationPoint1 = new PickableIdentLocationOption(
        3, PickableIdentLocationOption.IdentPoint.FROM_LOCATION, "POINT3");
    ident3LocationPoint2 = new PickableIdentLocationOption(
        3, PickableIdentLocationOption.IdentPoint.TO_LOCATION, "POINT4");

    pipelineIdentLocationOptions = List.of(
        ident1LocationPoint1,
        ident1LocationPoint2,
        ident2LocationPoint1,
        ident2LocationPoint2,
        ident3LocationPoint1,
        ident3LocationPoint2
    );

    finalPickableIdentLocation = pipelineIdentLocationOptions.get(pipelineIdentLocationOptions.size() - 1);

    when(pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(pwaApplicationDetail, PIPELINE_ID))
        .thenReturn(pipelineIdentLocationOptions);
  }

  @Test
  public void validate_whenFormHasNullSectionPointList() {

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactly(
        entry(PIPELINE_SECTION_POINTS_ATTR, Set.of(REQUIRED.errorCode(PIPELINE_SECTION_POINTS_ATTR)))
    );
  }

  @Test
  public void validate_whenFormHasEmptySectionPointList() {

    form.setPipelineSectionPoints(new ArrayList<>());

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactly(
        entry(PIPELINE_SECTION_POINTS_ATTR, Set.of(REQUIRED.errorCode(PIPELINE_SECTION_POINTS_ATTR)))
    );
  }

  @Test
  public void validate_whenFormHasMoreSectionsThanDefinedInHint() {

    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(), new PipelineSectionPointFormInput(),
        new PipelineSectionPointFormInput(), new PipelineSectionPointFormInput()

    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactly(
        entry(PIPELINE_SECTION_POINTS_ATTR, Set.of(TOO_MANY.errorCode(PIPELINE_SECTION_POINTS_ATTR)))
    );
  }

  @Test
  public void validate_whenFormHasMatchingNumberOfSections_andNoDetailsProvided() {

    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(), new PipelineSectionPointFormInput(), new PipelineSectionPointFormInput()
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                0,
                SECTION_POINT_IDENT_STRING_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        ),
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                0,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        ),

        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                1,
                SECTION_POINT_IDENT_STRING_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        ),
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                1,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        ),

        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_STRING_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        ),
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
            Set.of(REQUIRED.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        )
    ));
  }

  @Test
  public void validate_whenSelectedSectionStartPointNotFound() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput("some nonsense string", true),
        new PipelineSectionPointFormInput(), new PipelineSectionPointFormInput()
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).contains(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                0,
                SECTION_POINT_IDENT_STRING_ATTR),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        )
    );
  }

  @Test
  public void validate_whenSectionsStartLocationValid_andSequentialStartLocationsAreTheSame_andAllIncludePoint() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true),
        new PipelineSectionPointFormInput(ident1LocationPoint2.getPickableString(), true),
        new PipelineSectionPointFormInput(ident1LocationPoint2.getPickableString(), true)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR
            ),
            Set.of(NOT_UNIQUE.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        )
    ));
  }

  @Test
  public void validate_whenSectionStartPointsAreOutOfIdentPointOrder() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true),
        new PipelineSectionPointFormInput(ident3LocationPoint2.getPickableString(), true),
        new PipelineSectionPointFormInput(ident2LocationPoint1.getPickableString(), true)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_STRING_ATTR
            ),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        )
    ));
  }

  @Test
  public void validate_whenSectionsStartLocationValid_andAllStartLocationsAreTheSame_andAllIncludePoint() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true),
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true)
    ));

    var validationHint = new DefinePipelineHuooSectionValidationHint(
        pwaApplicationDetail,
        HUOO_ROLE,
        PIPELINE_ID,
        2);

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, validationHint);

    assertThat(validationResult).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                1,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR
            ),
            Set.of(NOT_UNIQUE.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        )
    ));
  }

  @Test
  public void validate_whenSectionsStartLocationValid_andAllStartLocationsAreTheSame_andSecondDoesNotIncludePoint_andThirdStartPointIsIncludedInSection() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true),
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), false),
        new PipelineSectionPointFormInput(ident1LocationPoint1.getPickableString(), true)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR
            ),
            Set.of(NOT_UNIQUE.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        )
    ));
  }

  @Test
  public void validate_whenFirstSectionStartPointIsNotExpectedFirstIdentPoint() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint2.getPickableString(), true),
        new PipelineSectionPointFormInput(),
        new PipelineSectionPointFormInput()
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).contains(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                0,
                SECTION_POINT_IDENT_STRING_ATTR
            ),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        )
    );
  }

  @Test
  public void validate_whenSectionsFirstSectionStartPointIsExpectedFirstIdentPoint_andDoesNotIncludePoint() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(ident1LocationPoint2.getPickableString(), false),
        new PipelineSectionPointFormInput(),
        new PipelineSectionPointFormInput()
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).contains(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                0,
                SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR
            ),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR))
        )
    );
  }

  @Test
  public void validate_whenFinalSectionStartsAtAndIncludesLastPointOfPipeline() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(),
        new PipelineSectionPointFormInput(),
        new PipelineSectionPointFormInput(finalPickableIdentLocation.getPickableString(), true)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).doesNotContain(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_STRING_ATTR
            ),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        )
    );
  }

  @Test
  public void validate_whenLocationAllPopulatedInValidOrder() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(0).getPickableString(), true),
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(2).getPickableString(), false),
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(3).getPickableString(), false)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).isEmpty();
  }


  @Test
  public void validate_whenSectionStartPointsOverlapByNotIncludingTheStartPoint() {
    form.setPipelineSectionPoints(List.of(
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(0).getPickableString(), true),
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(1).getPickableString(), false),
        new PipelineSectionPointFormInput(pipelineIdentLocationOptions.get(1).getPickableString(), false)
    ));

    var validationResult = ValidatorTestUtils.getFormValidationErrors(validator, form, defaultValidationHint);

    assertThat(validationResult).contains(
        entry(
            DefinePipelineHuooSectionsFormValidator.getSectionPointInputAttributePath(
                2,
                SECTION_POINT_IDENT_STRING_ATTR
            ),
            Set.of(INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR))
        )
    );
  }

  @Test
  public void supports_whenIsSupported() {
    assertThat(validator.supports(DefinePipelineHuooSectionsForm.class)).isTrue();
  }

  @Test
  public void supports_whenIsNotSupported() {
    assertThat(validator.supports(Object.class)).isFalse();
  }
}