package uk.co.ogauthority.pwa.service.notify;

import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public interface NotifyService {

  String EMAIL_LOG_PREFIX = "PWA_EMAIL:";

  /**
   * Method to send an email to a single recipient.
   * @param emailProperties The properties for the mail merge fields in the email template
   * @param toEmailAddress The email address to send the email to
   */
  void sendEmail(EmailProperties emailProperties, String toEmailAddress);

  /**
   * Method to send an email to a single recipient.
   * @param emailProperties The properties for the mail merge fields in the email template
   * @param toEmailAddress The email address to send the email too
   * @param reference Identifies a single unique application or a batch of applications
   * @param emailReplyToId Specified email ID to receive replies from the users
   */
  void sendEmail(EmailProperties emailProperties,
                    String toEmailAddress,
                    String reference,
                    String emailReplyToId);

}
