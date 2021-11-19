package uk.co.ogauthority.pwa.features.appprocessing.processingwarnings;


/**
 * A view object to construct the 'return to' message for non blocking task warnings on the front end.
 */
public class NonBlockingWarningReturnMessage {

  private final String messagePrefix;
  private final String urlLinkText;
  private final String messageSuffix;
  private final String returnUrl;


  NonBlockingWarningReturnMessage(String messagePrefix,
                                  String urlLinkText,
                                  String messageSuffix,
                                  String returnUrl) {
    this.messagePrefix = messagePrefix;
    this.urlLinkText = urlLinkText;
    this.messageSuffix = messageSuffix;
    this.returnUrl = returnUrl;
  }

  static NonBlockingWarningReturnMessage withoutSuffixMessage(String messagePrefix,
                                                              String urlLinkText,
                                                              String returnUrl) {

    return new NonBlockingWarningReturnMessage(messagePrefix, urlLinkText, null, returnUrl);
  }

  public String getMessagePrefix() {
    return messagePrefix;
  }

  public String getMessageSuffix() {
    return messageSuffix;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public String getUrlLinkText() {
    return urlLinkText;
  }
}
