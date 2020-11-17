package uk.co.ogauthority.pwa.model.dto.appprocessing;

import java.util.Set;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;

public class ProcessingPermissionsDto {

  private final ApplicationInvolvementDto applicationInvolvement;
  private final Set<PwaAppProcessingPermission> processingPermissions;

  public ProcessingPermissionsDto(ApplicationInvolvementDto applicationInvolvement,
                                  Set<PwaAppProcessingPermission> processingPermissions) {
    this.applicationInvolvement = applicationInvolvement;
    this.processingPermissions = processingPermissions;
  }

  public ApplicationInvolvementDto getApplicationInvolvement() {
    return applicationInvolvement;
  }

  public Set<PwaAppProcessingPermission> getProcessingPermissions() {
    return processingPermissions;
  }

}
