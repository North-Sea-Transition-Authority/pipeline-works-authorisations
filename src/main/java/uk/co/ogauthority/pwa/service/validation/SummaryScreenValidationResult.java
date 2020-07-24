package uk.co.ogauthority.pwa.service.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;

/**
 * Class to encapsulate the validation information for a section summary screen.
 */
public class SummaryScreenValidationResult {

  private final Set<String> invalidObjectIds;
  private final String idPrefix;
  private final boolean sectionComplete;
  private final String sectionIncompleteError;
  private final List<ErrorItem> errorItems;

  /**
   * Construct a validation result object to pass through into the summary view template.
   * @param invalidObjectIdToDescriptorMap map of id of invalid object (or entity) to a string that describes it
   *                                       (to be shown in error summary) e.g. <1, "PL123">
   * @param idPrefixString string to prefix the object id with to make a sensible identifier e.g. "pipeline" to be used in
   *                       cards or check answer tags
   * @param itemErrorSuffix string to add to the object descriptor to form the error message that appears per object
 *                        in the error summary e.g. "is invalid"
   * @param sectionComplete whether or not the section associated with the validation result is considered complete
   * @param sectionIncompleteError string to be shown in the single error banner if the section is incomplete
   */
  public SummaryScreenValidationResult(Map<String, String> invalidObjectIdToDescriptorMap,
                                       String idPrefixString,
                                       String itemErrorSuffix,
                                       boolean sectionComplete,
                                       String sectionIncompleteError) {

    this.invalidObjectIds = invalidObjectIdToDescriptorMap.keySet();

    this.idPrefix = idPrefixString + "-";

    // construct error items for the invalid objects so that they can be shown in the error summary
    // use the id prefix to create the ids that will be added to the cards/template items
    // use the error suffix to create the error message for each object
    this.errorItems = new ArrayList<>();
    for (Map.Entry<String, String> entry : invalidObjectIdToDescriptorMap.entrySet()) {
      errorItems.add(new ErrorItem(errorItems.size() + 1, this.idPrefix + entry.getKey(), entry.getValue() + " " + itemErrorSuffix));
    }

    this.sectionComplete = sectionComplete;
    this.sectionIncompleteError = sectionIncompleteError;

  }

  public Set<String> getInvalidObjectIds() {
    return invalidObjectIds;
  }

  public boolean isSectionComplete() {
    return sectionComplete;
  }

  public String getSectionIncompleteError() {
    return sectionIncompleteError;
  }

  public List<ErrorItem> getErrorItems() {
    return errorItems;
  }

  public ErrorItem getErrorItem(String objectId) {
    return errorItems.stream()
        .filter(errorItem -> Objects.equals(errorItem.getFieldName(), objectId))
        .findFirst()
        .orElse(null);
  }

  public String getIdPrefix() {
    return idPrefix;
  }
}
