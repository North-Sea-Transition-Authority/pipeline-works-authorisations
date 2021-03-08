package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ReviewAndSubmitApplicationEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String requesterFullName;
  private final String reviewAndSubmitPageUrl;

  public ReviewAndSubmitApplicationEmailProps(String recipientFullName,
                                              String applicationReference,
                                              String requesterFullName,
                                              String reviewAndSubmitPageUrl) {
    super(NotifyTemplate.REVIEW_AND_SUBMIT_APPLICATION, recipientFullName);
    this.applicationReference = applicationReference;
    this.requesterFullName = requesterFullName;
    this.reviewAndSubmitPageUrl = reviewAndSubmitPageUrl;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("REQUESTER_FULL_NAME", requesterFullName);
    emailPersonalisation.put("APPLICATION_SUBMIT_SUMMARY", reviewAndSubmitPageUrl);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getRequesterFullName() {
    return requesterFullName;
  }

  public String getReviewAndSubmitPageUrl() {
    return reviewAndSubmitPageUrl;
  }

}
