package uk.co.ogauthority.pwa.domain.pwa.application.model;

public class PwaApplicationDisplayUtils {

  public static String getApplicationTypeDisplay(PwaApplicationType applicationType, PwaResourceType resourceType) {
    return String.format("%s - %s",
        applicationType.getDisplayName(),
        resourceType.getDisplayName()
    );
  }
}
