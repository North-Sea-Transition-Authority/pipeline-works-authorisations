package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

public enum ValidationType {

  FULL, PARTIAL;

  public static ValidationType getFromRequestParams(String saveAndCompleteLater, String complete) {

    if (saveAndCompleteLater == null && complete == null) {
      throw new IllegalStateException("Cannot save and complete later or complete as both params are null.");
    }

    return complete != null ? FULL : PARTIAL;

  }

}
