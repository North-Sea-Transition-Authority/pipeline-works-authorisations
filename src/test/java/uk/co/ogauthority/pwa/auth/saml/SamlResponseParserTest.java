package uk.co.ogauthority.pwa.auth.saml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.impl.XSAnyBuilder;
import org.opensaml.saml.saml1.core.AttributeValue;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeBuilder;
import org.opensaml.saml.saml2.core.impl.AttributeStatementBuilder;
import org.opensaml.saml.saml2.core.impl.ResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@ActiveProfiles("integration-test")
class SamlResponseParserTest {

  @Autowired
  private SamlResponseParser samlResponseParser;

  @Test
  void parseSamlResponse() {

    var attributes = samlAttributeBuilder()
        .withWebUserAccountId("1")
        .withPersonId("2")
        .withForename("Forename")
        .withSurname("Surname")
        .withEmailAddress("email@address.com")
        .withPortalPrivileges("PRIV_ONE,PRIV_TWO,PRIV_THREE")
        .build();

    var samlResponse = createResponse(attributes);

    var authentication = samlResponseParser.parseSamlResponse(samlResponse);
    var user = (AuthenticatedUserAccount) authentication.getPrincipal();

    assertThat(user.getWuaId()).isEqualTo(1);
    assertThat(user.getLinkedPerson().getId().asInt()).isEqualTo(2);
    assertThat(user.getForename()).isEqualTo("Forename");
    assertThat(user.getSurname()).isEqualTo("Surname");
    assertThat(user.getEmailAddress()).isEqualTo("email@address.com");
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly(
            "PRIV_ONE",
            "PRIV_TWO",
            "PRIV_THREE"
        );
  }

  @org.junit.jupiter.api.Test
  void parseSamlResponse_withProxy() {
    var attributes = samlAttributeBuilder()
        .withWebUserAccountId("1")
        .withPersonId("2")
        .withForename("Forename")
        .withSurname("Surname")
        .withEmailAddress("email@address.com")
        .withPortalPrivileges("PRIV_ONE,PRIV_TWO,PRIV_THREE")
        .withProxyWuaId("999")
        .build();

    var samlResponse = createResponse(attributes);

    var authentication = samlResponseParser.parseSamlResponse(samlResponse);
    var user = (AuthenticatedUserAccount) authentication.getPrincipal();

    assertThat(user.getWuaId()).isEqualTo(1);
    assertThat(user.getLinkedPerson().getId().asInt()).isEqualTo(2);
    assertThat(user.getForename()).isEqualTo("Forename");
    assertThat(user.getSurname()).isEqualTo("Surname");
    assertThat(user.getEmailAddress()).isEqualTo("email@address.com");
    assertThat(user.getProxyUserWuaId().orElse(null)).isEqualTo(999);
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly(
            "PRIV_ONE",
            "PRIV_TWO",
            "PRIV_THREE"
        );
  }

  @Test
  void parseSamlResponse_missingAttributes() {
    var samlResponse = createResponse(List.of());
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @Test
  void parseSamlResponse_whenWebUserAccountIdAttributeEmpty_thenException() {
    List<String> testValues = Arrays.asList(null, "");
    for (String webUserAccountId : testValues) {
      var attributes = samlAttributeBuilder()
          .withWebUserAccountId(webUserAccountId)
          .build();

      var samlResponse = createResponse(attributes);

      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
    }
  }

  @Test
  void parseSamlResponse_whenPersonIdAttributeEmpty_thenException() {
    List<String> testValues = Arrays.asList(null, "");
    for (String personId : testValues) {
      var attributes = samlAttributeBuilder()
          .withPersonId(personId)
          .build();

      var samlResponse = createResponse(attributes);

      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
    }
  }

  @Test
  void parseSamlResponse_whenForenameAttributeEmpty_thenException() {
    List<String> testValues = Arrays.asList(null, "");
    for (String forename : testValues) {
      var attributes = samlAttributeBuilder()
          .withForename(forename)
          .build();

      var samlResponse = createResponse(attributes);

      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
    }
  }

  @Test
  void parseSamlResponse_whenSurnameAttributeEmpty_thenException() {
    List<String> testValues = Arrays.asList(null, "");
    for (String surname : testValues) {
      var attributes = samlAttributeBuilder()
          .withSurname(surname)
          .build();

      var samlResponse = createResponse(attributes);

      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
    }
  }

  @Test
  void parseSamlResponse_whenEmailAttributeEmpty_thenException() {
    List<String> testValues = Arrays.asList(null, "");
    for (String emailAddress : testValues) {
      var attributes = samlAttributeBuilder()
          .withEmailAddress(emailAddress)
          .build();

      var samlResponse = createResponse(attributes);

      assertThatExceptionOfType(IllegalArgumentException.class)
          .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
    }
  }

  @Test
  void parseSamlResponse_whenPrivilegesNull_thenException() {

    var attributes = samlAttributeBuilder()
        .withEmailAddress(null)
        .build();

    var samlResponse = createResponse(attributes);
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> samlResponseParser.parseSamlResponse(samlResponse));
  }

  @Test
  void parseSamlResponse_whenProxyUser_assertValues() {
    var attributes = samlAttributeBuilder()
        .withProxyWuaId("2")
        .build();
    var samlResponse = createResponse(attributes);
    assertThat((AuthenticatedUserAccount) samlResponseParser.parseSamlResponse(samlResponse).getPrincipal())
        .extracting(authenticatedUserAccount -> authenticatedUserAccount.getProxyUserWuaId().orElse(null)).isEqualTo(2);
  }

  @Test
  void parseSamlResponse_whenNullProxyValuesProvided_assertNulls() {
    var attributes = samlAttributeBuilder()
        .withProxyWuaId(null)
        .build();
    var samlResponse = createResponse(attributes);
    assertThat((AuthenticatedUserAccount) samlResponseParser.parseSamlResponse(samlResponse).getPrincipal())
        .extracting(authenticatedUserAccount -> authenticatedUserAccount.getProxyUserWuaId().orElse(null)).isNull();
  }

  @Test
  void getAuthenticatedUserAccount_validInputs_returnsCorrectAccount() throws Exception {
    String personId = "123";
    String forename = "John";
    String surname = "Doe";
    String email = "john.doe@example.com";
    String wuaId = "456";
    Integer proxyWuaId = 999;
    List<String> portalPrivileges = Arrays.asList("PWA_WORKAREA", "PWA_APPLICATION_SEARCH");

    AuthenticatedUserAccount result = samlResponseParser.getAuthenticatedUserAccount(
        personId, forename, surname, email, wuaId, proxyWuaId, portalPrivileges
    );

    // Person
    var linkedPerson = result.getLinkedPerson();
    assertThat(linkedPerson.getId().asInt()).isEqualTo(Integer.valueOf(personId));
    assertThat(linkedPerson.getForename()).isEqualTo(forename);
    assertThat(linkedPerson.getSurname()).isEqualTo(surname);
    assertThat(linkedPerson.getEmailAddress()).isEqualTo(email);

    // WebUserAccount
    assertThat(result.getWuaId()).isEqualTo(Integer.valueOf(wuaId));
    assertThat(result.getForename()).isEqualTo(forename);
    assertThat(result.getSurname()).isEqualTo(surname);
    assertThat(result.getEmailAddress()).isEqualTo(email);

    // Privileges
    assertThat(result.getUserPrivileges())
        .hasSize(2)
        .extracting(PwaUserPrivilege::name)
        .containsExactlyInAnyOrder("PWA_WORKAREA", "PWA_APPLICATION_SEARCH");

    // Proxy
    assertThat(result.getProxyUserWuaId().orElse(null)).isEqualTo(proxyWuaId);
  }

  @Test
  void getAuthenticatedUserAccount_withInvalidPrivilege_ignoresInvalid() throws Exception {
    String personId = "123";
    String forename = "John";
    String surname = "Doe";
    String email = "john.doe@example.com";
    String wuaId = "456";
    List<String> portalPrivileges = Arrays.asList("PWA_WORKAREA", "INVALID_ONE");

    AuthenticatedUserAccount result = samlResponseParser.getAuthenticatedUserAccount(
        personId, forename, surname, email, wuaId, null, portalPrivileges
    );

    // Only PWA_WORKAREA should be included
    assertThat(result.getUserPrivileges())
        .hasSize(1)
        .extracting(PwaUserPrivilege::name)
        .containsExactly("PWA_WORKAREA");
  }

  private Response createResponse(List<Attribute> samlAttributes) {
    var samlResponse = new ResponseBuilder().buildObject();
    var samlAssertion = new AssertionBuilder().buildObject();
    var attributeStatement = new AttributeStatementBuilder().buildObject();
    attributeStatement.getAttributes().addAll(samlAttributes);
    samlAssertion.getAttributeStatements().add(attributeStatement);
    samlResponse.getAssertions().add(samlAssertion);
    return samlResponse;
  }

  static SamlAttributeTestBuilder samlAttributeBuilder() {
    return new SamlAttributeTestBuilder();
  }

  static class SamlAttributeTestBuilder {

    private String webUserAccountId = "1";
    private String personId = "2";
    private String forename = "Forename";
    private String surname = "Surname";
    private String emailAddress = "email@address.com";
    private String proxyWuaId = "1";
    private String portalPrivilegeCsv = "PRIVILEGE_1";

    private final List<Attribute> attributes = new ArrayList<>();

    private void addAttribute(EnergyPortalSamlAttribute samlAttribute, String value) {
      attributes.add(createSamlAttribute(samlAttribute, value));
    }

    SamlAttributeTestBuilder withWebUserAccountId(String webUserAccountId) {
      this.webUserAccountId = webUserAccountId;
      return this;
    }

    SamlAttributeTestBuilder withPersonId(String personId) {
      this.personId = personId;
      return this;
    }

    SamlAttributeTestBuilder withForename(String forename) {
      this.forename = forename;
      return this;
    }

    SamlAttributeTestBuilder withSurname(String surname) {
      this.surname = surname;
      return this;
    }

    SamlAttributeTestBuilder withEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
      return this;
    }

    SamlAttributeTestBuilder withProxyWuaId(String proxyWuaId) {
      this.proxyWuaId = proxyWuaId;
      return this;
    }

    SamlAttributeTestBuilder withPortalPrivileges(String portalPrivilegeCsv) {
      this.portalPrivilegeCsv = portalPrivilegeCsv;
      return this;
    }

    List<Attribute> build() {
      addAttribute(EnergyPortalSamlAttribute.WEB_USER_ACCOUNT_ID, webUserAccountId);
      addAttribute(EnergyPortalSamlAttribute.PERSON_ID, personId);
      addAttribute(EnergyPortalSamlAttribute.FORENAME, forename);
      addAttribute(EnergyPortalSamlAttribute.SURNAME, surname);
      addAttribute(EnergyPortalSamlAttribute.EMAIL_ADDRESS, emailAddress);
      addAttribute(EnergyPortalSamlAttribute.PROXY_USER_WUA_ID, proxyWuaId);
      addAttribute(EnergyPortalSamlAttribute.PORTAL_PRIVILEGES, portalPrivilegeCsv);
      return this.attributes;
    }

    private Attribute createSamlAttribute(EnergyPortalSamlAttribute samlAttribute, String value) {
      try {
        var attribute = new AttributeBuilder().buildObject();
        attribute.setName(samlAttribute.getAttributeName());

        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        var element = document.createElement(samlAttribute.getAttributeName());
        element.setTextContent(value);

        var attributeValue = new XSAnyBuilder().buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
        attributeValue.setDOM(element);

        attribute.getAttributeValues().add(attributeValue);

        return attribute;
      } catch (Exception e) {
        throw new RuntimeException("Failed to construct SAML attribute", e);
      }
    }
  }
}
