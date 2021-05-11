package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import java.util.List;
import java.util.Map;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

/**
 * Interface to ensure common functionality is implemented across all PWA application form section services.
 */
public interface ApplicationFormSectionService {

  /**
   * Whether or not the section associated with the service is complete (all required data entered and valid).
   * @param detail record for application being checked
   * @return true if section is complete, false otherwise
   */
  boolean isComplete(PwaApplicationDetail detail);

  /**
   * Validate the form object associated with the section and return the result.
   *
   * @param form                 object
   * @param bindingResult        obtained by binding request into form
   * @param validationType       specifying whether to do full or partial validation
   * @param pwaApplicationDetail to give to validator context
   * @return binding result containing errors if there were validation problems, clean otherwise
   */
  BindingResult validate(Object form,
                         BindingResult bindingResult,
                         ValidationType validationType,
                         PwaApplicationDetail pwaApplicationDetail);

  /**
   * Used to show/hide the task list entry.
   * @return True if entry should be shown.
   */
  default boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return true;
  }

  /**
   * Use to retrieve a list of extra labels to add to the task list entry.
   * @return List of TaskInfo objects.
   */
  default List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    return List.of();
  }

  /**
   * Remove nested or otherwise hidden data that may have been entered by the user but is now not relevant.
   * @param detail of application to cleanup
   */
  default void cleanupData(PwaApplicationDetail detail) {
    // default implementation implies no cleanup required
  }

  /**
   * Each implementing class is responsible for copying all data associated with the old detail to new detail.
   */
  void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail);

  default boolean allowCopyOfSectionInformation(PwaApplicationDetail pwaApplicationDetail) {
    return canShowInTaskList(pwaApplicationDetail);
  }

  default List<MailMergeFieldMnem> getAvailableMailMergeFields(PwaApplicationDetail pwaApplicationDetail) {
    return List.of();
  }

  default Map<MailMergeFieldMnem, String> resolveMailMergeFields(PwaApplicationDetail pwaApplicationDetail) {
    return Map.of();
  }

}
