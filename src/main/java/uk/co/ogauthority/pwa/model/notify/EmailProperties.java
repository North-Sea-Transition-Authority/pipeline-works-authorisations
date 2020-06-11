package uk.co.ogauthority.pwa.model.notify;

import java.util.HashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

/**
 * Simple class to hold the default email properties for a GOV.UK notify template.
 */
public class EmailProperties {

  private final NotifyTemplate template;

  /**
   * Construct email properties using a template that the email will be based from.
   */
  public EmailProperties(NotifyTemplate template) {
    this.template = template;
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

  /**
   * Get the default email personalisation for all templates.
   */
  public Map<String, String> getEmailPersonalisation() {

    Map<String, String> emailPersonalisation = new HashMap<>();
    // TEST_EMAIL set to "no" by default and only set to "yes" in the TestNotifyServiceImpl
    emailPersonalisation.put("TEST_EMAIL", "no");
    return emailPersonalisation;
  }
}
