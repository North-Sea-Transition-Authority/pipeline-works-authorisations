package uk.co.ogauthority.pwa.service.appprocessing.context;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

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
  private final Set<UserType> userTypes;

  private AppFile appFile;

  public PwaAppProcessingContext(PwaApplicationDetail applicationDetail,
                                 WebUserAccount user,
                                 Set<PwaAppProcessingPermission> appProcessingPermissions,
                                 CaseSummaryView caseSummaryView,
                                 ApplicationInvolvementDto applicationInvolvement,
                                 Set<UserType> userTypes) {
    this.applicationDetail = applicationDetail;
    this.user = user;
    this.appProcessingPermissions = appProcessingPermissions;
    this.caseSummaryView = caseSummaryView;
    this.applicationInvolvement = applicationInvolvement;
    this.userTypes = userTypes;
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

  public boolean hasProcessingPermission(PwaAppProcessingPermission pwaAppProcessingPermission) {
    return this.appProcessingPermissions.contains(pwaAppProcessingPermission);
  }

  public boolean hasAnyProcessingPermission(PwaAppProcessingPermission... pwaAppProcessingPermissionsArray) {
    var searchPermissionSet = EnumSet.noneOf(PwaAppProcessingPermission.class);
    searchPermissionSet.addAll(Arrays.asList(pwaAppProcessingPermissionsArray));

    return hasAnyProcessingPermission(searchPermissionSet);
  }

  public boolean hasAnyProcessingPermission(Set<PwaAppProcessingPermission> pwaAppProcessingPermissions) {
    return !SetUtils.intersection(pwaAppProcessingPermissions, this.appProcessingPermissions).isEmpty();
  }

  public PwaApplication getPwaApplication() {
    return applicationDetail.getPwaApplication();
  }

  public PwaApplicationType getApplicationType() {
    return applicationDetail.getPwaApplicationType();
  }

  public PwaApplicationStatus getApplicationDetailStatus() {
    return applicationDetail.getStatus();
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

  public Set<UserType> getUserTypes() {
    return userTypes;
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

  public boolean hasOpenConsentReview() {
    return applicationInvolvement.getOpenConsentReview() == OpenConsentReview.YES;
  }

}
