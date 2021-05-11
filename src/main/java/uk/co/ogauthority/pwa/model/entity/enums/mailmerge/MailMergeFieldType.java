package uk.co.ogauthority.pwa.model.entity.enums.mailmerge;

public enum MailMergeFieldType {

  AUTOMATIC("((", "))"),
  MANUAL("[[", "]]");

  private final String openingDelimiter;
  private final String closingDelimiter;

  MailMergeFieldType(String openingDelimiter, String closingDelimiter) {
    this.openingDelimiter = openingDelimiter;
    this.closingDelimiter = closingDelimiter;
  }

  public String getOpeningDelimiter() {
    return openingDelimiter;
  }

  public String getClosingDelimiter() {
    return closingDelimiter;
  }
}
