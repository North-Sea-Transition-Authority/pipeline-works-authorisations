package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * A data class to store contextual information related to processing a PWA application to allow
 * for easier access to commonly required data.
 */
public class PwaAppProcessingContext {

  private final PwaApplicationDetail applicationDetail;

  private final WebUserAccount user;

  private final Set<PwaAppProcessingPermission> appProcessingPermissions;
  private final CaseSummaryView caseSummaryView;
  private final ApplicationInvolvementDto applicationInvolvement;

  private AppFile appFile;

  public PwaAppProcessingContext(PwaApplicationDetail applicationDetail,
                                 WebUserAccount user,
                                 Set<PwaAppProcessingPermission> appProcessingPermissions,
                                 CaseSummaryView caseSummaryView,
                                 ApplicationInvolvementDto applicationInvolvement) {
    this.applicationDetail = applicationDetail;
    this.user = user;
    this.appProcessingPermissions = appProcessingPermissions;
    this.caseSummaryView = caseSummaryView;
    this.applicationInvolvement = applicationInvolvement;
  }

  public PwaApplicationDetail getApplicationDetail() {
    return applicationDetail;
  }

  public WebUserAccount getUser() {
    return user;
  }

  public Set<PwaAppProcessingPermission> getAppProcessingPermissions() {
    return appProcessingPermissions;
  }

  public PwaApplication getPwaApplication() {
    return applicationDetail.getPwaApplication();
  }

  public PwaApplicationType getApplicationType() {
    return applicationDetail.getPwaApplicationType();
  }

  public CaseSummaryView getCaseSummaryView() {
    return caseSummaryView;
  }

  public ApplicationInvolvementDto getApplicationInvolvement() {
    return applicationInvolvement;
  }

  public Optional<ConsultationRequestDto> getActiveConsultationRequest() {
    return applicationInvolvement.getConsultationInvolvement()
        .flatMap(ConsultationInvolvementDto::getActiveRequestDto);
  }

  public ConsultationRequestDto getActiveConsultationRequestOrThrow() {
    return getActiveConsultationRequest()
        .orElseThrow(() -> new RuntimeException(String.format(
            "Expected processing context to have active consultation request, it didn't. App ID: %s",
            applicationDetail.getMasterPwaApplicationId())));
  }

  public Integer getActiveConsultationRequestId() {
    return getActiveConsultationRequest()
        .map(dto -> dto.getConsultationRequest().getId())
        .orElse(null);
  }

  public AppFile getAppFile() {
    return appFile;
  }

  public void setAppFile(AppFile appFile) {
    this.appFile = appFile;
  }

  public Integer getMasterPwaApplicationId() {
    return applicationDetail.getMasterPwaApplicationId();
  }

}
