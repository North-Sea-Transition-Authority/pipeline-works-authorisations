package uk.co.ogauthority.pwa.integrations.govuknotify;

import java.util.HashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

/**
 * Simple class to hold the default email properties for a GOV.UK notify template.
 */
public class EmailProperties {

  public static final String DEFAULT_RECIPIENT_NAME = "Sir/Madam";

  private final NotifyTemplate template;
  private final String recipientFullName;

  /**
   * Construct email properties using a template that the email will be based from.
   */
  public EmailProperties(NotifyTemplate template) {
    this.template = template;
    this.recipientFullName = DEFAULT_RECIPIENT_NAME;
  }

  public EmailProperties(NotifyTemplate template, String recipientFullName) {
    this.template = template;
    this.recipientFullName = recipientFullName;
  }

  /**
   * Retrieve the name of the email template.
   */
  public String getTemplateName() {
    return template.getTemplateName();
  }

  public NotifyTemplate getTemplate() {
    return template;
  }

  public String getRecipientFullName() {
    return recipientFullName;
  }

  /**
   * Get the default email personalisation for all templates.
   */
  public Map<String, String> getEmailPersonalisation() {

    Map<String, String> emailPersonalisation = new HashMap<>();

    // TEST_EMAIL set to "no" by default and only set to "yes" in the TestNotifyServiceImpl
    emailPersonalisation.put("TEST_EMAIL", "no");
    emailPersonalisation.put("RECIPIENT_FULL_NAME", recipientFullName);

    return emailPersonalisation;

  }
}
