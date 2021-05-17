package uk.co.ogauthority.pwa.validators.pipelinehuoo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.ValidationUtils;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.DefinePipelineHuooSectionsForm;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.form.PipelineSectionPointFormInput;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableHuooPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.PickableIdentLocationOption;

@Service
public class DefinePipelineHuooSectionsFormValidator implements SmartValidator {

  static final String PIPELINE_SECTION_POINTS_ATTR = "pipelineSectionPoints";

  static final String SECTION_POINT_IDENT_STRING_ATTR = "pickedPipelineIdentString";

  static final String SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR = "pointIncludedInSection";

  private final PickableHuooPipelineIdentService pickableHuooPipelineIdentService;

  @Autowired
  public DefinePipelineHuooSectionsFormValidator(PickableHuooPipelineIdentService pickableHuooPipelineIdentService) {
    this.pickableHuooPipelineIdentService = pickableHuooPipelineIdentService;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(DefinePipelineHuooSectionsForm.class);
  }

  @Override
  public void validate(Object target, Errors errors) {
    validate(target, errors, new Object[0]);
  }

  @Override
  public void validate(Object target, Errors errors, Object... validationHints) {
    var form = (DefinePipelineHuooSectionsForm) target;
    var defineSectionsValidationHint = Arrays.stream(validationHints)
        .filter(o -> o.getClass().equals(DefinePipelineHuooSectionValidationHint.class))
        .map(o -> (DefinePipelineHuooSectionValidationHint) o)
        .findFirst()
        .orElseThrow(() ->
            new ActionNotAllowedException("Expected instance of DefinePipelineHuooSectionValidationHint but not found"
            ));

    // Sanity check that we dont have too many sections and that we have at least 1 section with provided details
    validatePipelineSectionPointsLength(errors, form, defineSectionsValidationHint.getNumberOfSections());
    if (errors.hasErrors()) {
      // dont bother validating anything  else as the form does not include the expected number of sections.
      // this is not possible through the screens.
      return;
    }

    List<PickableIdentLocationOption> sortedValidOptions = pickableHuooPipelineIdentService.getSortedPickableIdentLocationOptions(
        defineSectionsValidationHint.getPwaApplicationDetail(),
        defineSectionsValidationHint.getPipelineId()
    );

    // this is the core validation loop over all sections
    validateSectionStartPositionsInOrder(errors, form, defineSectionsValidationHint.getNumberOfSections(),
        sortedValidOptions);

    // Sanity Check first section is defined as expected
    validateFirstSectionIsCorrect(errors, form, sortedValidOptions.get(0));

    // sanity check final section does not start at and not include the very last ident point (the section would have no content)
    validateFinalSectionMakesSense(
        errors,
        form,
        sortedValidOptions.get(sortedValidOptions.size() - 1),
        defineSectionsValidationHint.getNumberOfSections() - 1);
  }

  /**
   * The last section cannot start at, and also not include, the final point on the pipeline because
   * it must end at, and include that same point.
   */
  private void validateFinalSectionMakesSense(Errors errors,
                                              DefinePipelineHuooSectionsForm form,
                                              PickableIdentLocationOption finalPickableIdentLocation,
                                              int lastSectionIndex) {
    var lastSectionHasErrors = doesSectionPointInputHaveErrors(errors, lastSectionIndex);
    var lastSectionInError = form.getSectionPointFormAtIndex(lastSectionIndex)
        .filter(sectionInput -> finalPickableIdentLocation.getPickableString().equals(
            sectionInput.getPickedPipelineIdentString()))
        .filter(sectionInput -> BooleanUtils.isFalse(sectionInput.getPointIncludedInSection()))
        .isPresent();
    if (!lastSectionHasErrors && lastSectionInError) {
      errors.rejectValue(
          getSectionPointInputAttributePath(lastSectionIndex, SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
          FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
          String.format("Section %s cannot start at the final ident's \"to\" location and not include it", lastSectionIndex + 1)
      );
    }

  }

  /**
   * The first section must start at and include the very first pickable ident point.
  */
  private void validateFirstSectionIsCorrect(Errors errors,
                                             DefinePipelineHuooSectionsForm form,
                                             PickableIdentLocationOption mandatoryFirstIdentLocation) {

    var firstSectionHasErrors = doesSectionPointInputHaveErrors(errors, 0);

    var firstSectionStartsAtExpectedPoint = form.getSectionPointFormAtIndex(0)
        .filter(sectionInput ->  mandatoryFirstIdentLocation.getPickableString().equals(sectionInput.getPickedPipelineIdentString()))
        .isPresent();

    if (!firstSectionHasErrors && !firstSectionStartsAtExpectedPoint) {
      errors.rejectValue(
          getSectionPointInputAttributePath(0, SECTION_POINT_IDENT_STRING_ATTR),
          FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR),
          String.format("Section 1 must start at point %s", mandatoryFirstIdentLocation.getDisplayString())
      );
    }

    var firstSectionIncludesStartPoint = form.getSectionPointFormAtIndex(0)
        .filter(sectionInput -> BooleanUtils.isTrue(sectionInput.getPointIncludedInSection()))
        .isPresent();

    if (!firstSectionHasErrors && !firstSectionIncludesStartPoint) {
      errors.rejectValue(
          getSectionPointInputAttributePath(0, SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
          FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
          String.format("Section 1 must include point %s", mandatoryFirstIdentLocation.getDisplayString())
      );
    }

  }

  /**
   * Loop over each section, and determine if the section start point and inclusion value make sense given previous
   * section responses. This is the core validation loop that also does the basic mandatory checks per required section.
   */
  private void validateSectionStartPositionsInOrder(Errors errors,
                                                    DefinePipelineHuooSectionsForm form,
                                                    int numberOfSections,
                                                    List<PickableIdentLocationOption> sortedValidOptions) {

    Map<String, PickableIdentLocationOption> validOptionsLookup = sortedValidOptions.stream()
        .collect(Collectors.toMap(PickableIdentLocationOption::getPickableString, Function.identity()));

    // always start with first as minimal including
    PickableIdentLocationOption minimumIncludingIdentLocation = sortedValidOptions.get(0);
    // start with second as minimal not including
    PickableIdentLocationOption minimumNotIncludingIdentLocation = sortedValidOptions.get(1);

    // maintain a Lookup of PickableIdentLocationOptions to sections numbers that start at that location and include it.
    // if a section already starts at a location and includes, no other section can.
    Map<PickableIdentLocationOption, Integer> sectionsStartingAtAndIncludingIdentLocationLookup = new HashMap<>();

    for (int sectionIndex = 0; sectionIndex < numberOfSections; sectionIndex++) {

      var currentSectionInput = form.getSectionPointFormAtIndex(sectionIndex).orElse(new PipelineSectionPointFormInput());
      // do mandatory and basic validity check per section
      validateSectionBasic(errors,
          currentSectionInput,
          sectionIndex,
          validOptionsLookup
      );

      var currentSectionHasErrors = doesSectionPointInputHaveErrors(errors, sectionIndex);

      if (!currentSectionHasErrors) {
        var selectedIdentLocation = validOptionsLookup.get(currentSectionInput.getPickedPipelineIdentString());
        if (currentSectionInput.getPointIncludedInSection()
            && selectedIdentLocation.compareTo(minimumIncludingIdentLocation) < 0) {
          errors.rejectValue(
              getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_STRING_ATTR),
              FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR),
              String.format("Section %s cannot include a point before %s",
                  sectionIndex + 1,
                  minimumIncludingIdentLocation.getDisplayString())
          );
        } else if (!currentSectionInput.getPointIncludedInSection()
            && selectedIdentLocation.compareTo(minimumNotIncludingIdentLocation) < 0) {
          errors.rejectValue(
              getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_STRING_ATTR),
              FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR),
              String.format("Section %s cannot start before the point %s",
                  sectionIndex + 1,
                  minimumNotIncludingIdentLocation.getDisplayString())
          );
        }

        if (currentSectionInput.getPointIncludedInSection()
            && sectionsStartingAtAndIncludingIdentLocationLookup.containsKey(selectedIdentLocation)) {
          errors.rejectValue(
              getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
              FieldValidationErrorCodes.NOT_UNIQUE.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
              String.format("Section %s cannot include this point as it is included in section %s",
                  sectionIndex + 1,
                  sectionsStartingAtAndIncludingIdentLocationLookup.get(selectedIdentLocation))
          );
        } else if (currentSectionInput.getPointIncludedInSection()) {
          sectionsStartingAtAndIncludingIdentLocationLookup.putIfAbsent(selectedIdentLocation, sectionIndex + 1);
        }

        var indexOfSelectedOption = Collections.binarySearch(sortedValidOptions, selectedIdentLocation);

        var nextIdentOptionOrSelected = indexOfSelectedOption + 1 < sortedValidOptions.size()
            ? sortedValidOptions.get(indexOfSelectedOption + 1)
            : sortedValidOptions.get(indexOfSelectedOption);

        if (currentSectionInput.getPointIncludedInSection()
            && sortedValidOptions.get(indexOfSelectedOption).compareTo(minimumIncludingIdentLocation) >= 0) {
          // if the start point of section contains point and that point is same or after the previous minimum, set that to new minimum
          minimumIncludingIdentLocation = nextIdentOptionOrSelected;
          minimumNotIncludingIdentLocation = sortedValidOptions.get(indexOfSelectedOption);
        } else if (!currentSectionInput.getPointIncludedInSection()) {
          // else if the start point of section does not include ident location, next section can include current selected point
          minimumIncludingIdentLocation = sortedValidOptions.get(indexOfSelectedOption);
          minimumNotIncludingIdentLocation = nextIdentOptionOrSelected;
        }

      }
    }
  }


  private void validateSectionBasic(Errors errors,
                                    PipelineSectionPointFormInput pipelineSectionPointFormInput,
                                    int sectionIndex,
                                    Map<String, PickableIdentLocationOption> validIdentOptions) {

    var sectionStartPointPath = getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_STRING_ATTR);

    if (pipelineSectionPointFormInput.getPickedPipelineIdentString() == null) {
      errors.rejectValue(
          sectionStartPointPath,
          FieldValidationErrorCodes.REQUIRED.errorCode(SECTION_POINT_IDENT_STRING_ATTR),
          String.format("Enter the section %s start point", sectionIndex + 1)
      );
    } else if (!validIdentOptions.containsKey(pipelineSectionPointFormInput.getPickedPipelineIdentString())) {
      errors.rejectValue(
          sectionStartPointPath,
          FieldValidationErrorCodes.INVALID.errorCode(SECTION_POINT_IDENT_STRING_ATTR),
          String.format("Enter a valid section %s start point", sectionIndex + 1)
      );
    }

    ValidationUtils.rejectIfEmpty(
        errors,
        getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
        FieldValidationErrorCodes.REQUIRED.errorCode(SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR),
        String.format("Enter No if section %s begins at but does not include the selected point.", sectionIndex + 1)
    );

  }

  private void validatePipelineSectionPointsLength(Errors errors,
                                                   DefinePipelineHuooSectionsForm form,
                                                   int numberOfSections) {
    if (form.getPipelineSectionPoints() == null || form.getPipelineSectionPoints().isEmpty()) {
      errors.rejectValue(
          PIPELINE_SECTION_POINTS_ATTR,
          FieldValidationErrorCodes.REQUIRED.errorCode(PIPELINE_SECTION_POINTS_ATTR),
          String.format("Enter details for each of the %s sections", numberOfSections)
      );
    }

    if (form.getPipelineSectionPoints() != null && form.getPipelineSectionPoints().size() > numberOfSections) {
      errors.rejectValue(
          PIPELINE_SECTION_POINTS_ATTR,
          FieldValidationErrorCodes.TOO_MANY.errorCode(PIPELINE_SECTION_POINTS_ATTR),
          String.format("Enter details for only %s sections", numberOfSections)
      );
    }

  }

  private boolean doesSectionPointInputHaveErrors(Errors errors, int sectionIndex) {
    return errors.hasFieldErrors(getSectionPointInputPath(sectionIndex))
        || errors.hasFieldErrors(getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_STRING_ATTR))
        || errors.hasFieldErrors(
        getSectionPointInputAttributePath(sectionIndex, SECTION_POINT_IDENT_INCLUDED_IN_SECTION_ATTR));
  }

  static String getSectionPointInputPath(int sectionIndex) {
    return String.format("%s[%s]", PIPELINE_SECTION_POINTS_ATTR, sectionIndex);
  }

  static String getSectionPointInputAttributePath(int sectionIndex, String attribute) {
    return getSectionPointInputPath(sectionIndex) + "." + attribute;
  }
}
