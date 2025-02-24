package uk.co.ogauthority.pwa.auth.saml;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@Service
public class SamlResponseParser {

  public ServiceSaml2Authentication parseSamlResponse(Response response) {
    var attributes = getSamlAttributes(response);
    var parsedAttributes = parseAttributes(attributes);

    var wuaId = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID);
    var personId = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.PERSON_ID);
    var forename = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.FORENAME);
    var surname = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.SURNAME);
    var email = getNonEmptyAttribute(parsedAttributes, EnergyPortalSamlAttribute.EMAIL_ADDRESS);
    var proxyWuaId = Optional.ofNullable(parsedAttributes.get(EnergyPortalSamlAttribute.PROXY_USER_WUA_ID.getAttributeName()))
        .filter(StringUtils::isNotBlank)
        .map(Integer::valueOf)
        .orElse(null);

    var portalPrivileges = getNonNullAttribute(parsedAttributes, EnergyPortalSamlAttribute.PORTAL_PRIVILEGES);
    var portalPrivilegesList = Arrays.stream(StringUtils.split(portalPrivileges, ",")).toList();

    var grantedAuthorities = portalPrivilegesList.stream()
        .map(SimpleGrantedAuthority::new)
        .toList();

    var user = getAuthenticatedUserAccount(personId, forename, surname, email, wuaId, proxyWuaId, portalPrivilegesList);

    return new ServiceSaml2Authentication(user, grantedAuthorities);
  }

  @VisibleForTesting
  AuthenticatedUserAccount getAuthenticatedUserAccount(String personId,
                                                       String forename,
                                                       String surname,
                                                       String email,
                                                       String wuaId,
                                                       Integer proxyWuaId,
                                                       List<String> portalPrivileges) {
    var person = new Person();
    person.setId(Integer.valueOf(personId));
    person.setForename(forename);
    person.setSurname(surname);
    person.setEmailAddress(email);

    var webUserAccount = new WebUserAccount();
    webUserAccount.setWuaId(Integer.parseInt(wuaId));
    webUserAccount.setPerson(person);
    webUserAccount.setForename(forename);
    webUserAccount.setSurname(surname);
    webUserAccount.setEmailAddress(email);

    List<PwaUserPrivilege> pwaUserPrivilegeList = portalPrivileges.stream()
        .map(name -> {
          try {
            return PwaUserPrivilege.valueOf(name);
          } catch (IllegalArgumentException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .toList();

    return new AuthenticatedUserAccount(
        webUserAccount,
        pwaUserPrivilegeList,
        proxyWuaId
    );
  }

  private String getNonEmptyAttribute(Map<String, String> parsedAttributes, EnergyPortalSamlAttribute samlAttribute) {
    return ObjectUtils.requireNonEmpty(parsedAttributes.get(samlAttribute.getAttributeName()));
  }

  private String getNonNullAttribute(Map<String, String> parsedAttributes, EnergyPortalSamlAttribute samlAttribute) {
    return Objects.requireNonNull(parsedAttributes.get(samlAttribute.getAttributeName()));
  }

  private List<Attribute> getSamlAttributes(Response response) {
    var assertions = response.getAssertions();
    if (assertions.size() != 1) {
      throw new SamlResponseException(String.format("SAML response contained %s assertions, expected 1", assertions.size()));
    }
    var attributeStatements = assertions.get(0).getAttributeStatements();
    if (attributeStatements.size() != 1) {
      throw new SamlResponseException(
          String.format("SAML response contained %s attribute statements, expected 1", attributeStatements.size())
      );
    }
    return attributeStatements.get(0).getAttributes();
  }

  private Map<String, String> parseAttributes(List<Attribute> attributes) {
    return attributes.stream()
        .collect(Collectors.toMap(Attribute::getName, this::getAttributeValue));
  }

  private String getAttributeValue(Attribute attribute) {
    return Objects.requireNonNull(
        attribute.getAttributeValues().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException(String.format("No values present for attribute '%s'", attribute.getName())))
            .getDOM()
    ).getTextContent();
  }

}
