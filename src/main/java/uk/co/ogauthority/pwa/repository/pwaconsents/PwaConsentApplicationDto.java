package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.time.Instant;
import java.util.Optional;
import org.apache.commons.lang3.BooleanUtils;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.docgen.DocgenRunStatus;
import uk.co.ogauthority.pwa.util.DateUtils;

public class PwaConsentApplicationDto {

  private final Integer consentId;
  private final Instant consentInstant;
  private final String consentReference;
  private final Integer pwaApplicationId;
  private final PwaApplicationType applicationType;
  private final String appReference;
  private final Long docgenRunId;
  private final DocgenRunStatus docgenRunStatus;
  private final Boolean fileDownloadable;

  public PwaConsentApplicationDto(Integer consentId,
                                  Instant consentInstant,
                                  String consentReference,
                                  Integer pwaApplicationId,
                                  PwaApplicationType pwaApplicationType,
                                  String appReference,
                                  Long docgenRunId,
                                  DocgenRunStatus docgenRunStatus,
                                  Boolean fileDownloadable
  ) {
    this.consentId = consentId;
    this.consentInstant = consentInstant;
    this.consentReference = consentReference;
    this.pwaApplicationId = pwaApplicationId;
    this.applicationType = pwaApplicationType;
    this.appReference = appReference;
    this.docgenRunId = docgenRunId;
    this.docgenRunStatus = docgenRunStatus;
    this.fileDownloadable = fileDownloadable;
  }

  public Integer getConsentId() {
    return consentId;
  }

  public Instant getConsentInstant() {
    return consentInstant;
  }

  public String getConsentDateDisplay() {
    return DateUtils.formatDate(consentInstant);
  }

  public String getConsentReference() {
    return consentReference;
  }

  public Integer getPwaApplicationId() {
    return pwaApplicationId;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public String getAppReference() {
    return appReference;
  }

  public Optional<Long> getDocgenRunId() {
    return Optional.ofNullable(docgenRunId);
  }

  public Optional<DocgenRunStatus> getDocgenRunStatus() {
    return Optional.ofNullable(docgenRunStatus);
  }

  public boolean consentDocumentDownloadable() {
    return BooleanUtils.isTrue(fileDownloadable)
        || (getDocgenRunId().isPresent()
        && getDocgenRunStatus()
          .map(status -> status == DocgenRunStatus.COMPLETE)
          .orElse(false));
  }

  public String getDocStatusDisplay() {
    return getDocgenRunStatus()
        .map(status -> {
          if (status == DocgenRunStatus.PENDING) {
            return "Document is generating";
          } else if (status == DocgenRunStatus.FAILED) {
            return "Document failed to generate";
          } else {
            return "";
          }
        })
        .orElse("");
  }
}
