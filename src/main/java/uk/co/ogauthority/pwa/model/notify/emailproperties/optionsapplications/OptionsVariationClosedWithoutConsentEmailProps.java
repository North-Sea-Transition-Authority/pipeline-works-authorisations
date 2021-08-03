package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.ConfirmedOptionType;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class OptionsVariationClosedWithoutConsentEmailProps extends EmailProperties {

  private final String applicationReference;
  private final ConfirmedOptionType confirmedOptionType;
  private final String closingPersonName;
  private final String caseManagementLink;

  public OptionsVariationClosedWithoutConsentEmailProps(String recipientFullName,
                                                        String applicationReference,
                                                        ConfirmedOptionType confirmedOptionType,
                                                        String closingPersonName,
                                                        String caseManagementLink) {
    super(NotifyTemplate.OPTIONS_VARIATION_CLOSED_WITHOUT_CONSENT, recipientFullName);
    this.applicationReference = applicationReference;
    this.confirmedOptionType = confirmedOptionType;
    this.closingPersonName = closingPersonName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONFIRMED_OPTION_TYPE", confirmedOptionType.getDisplayName());
    emailPersonalisation.put("CLOSING_PERSON_NAME", closingPersonName);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OptionsVariationClosedWithoutConsentEmailProps that = (OptionsVariationClosedWithoutConsentEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(closingPersonName, that.closingPersonName)
        && Objects.equals(confirmedOptionType, that.confirmedOptionType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, closingPersonName, confirmedOptionType);
  }

}

