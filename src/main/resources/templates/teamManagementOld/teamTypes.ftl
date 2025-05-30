<#-- @ftlvariable name="teamTypes" type="java.util.Map<uk.co.ogauthority.pwa.model.enums.teams.ManageTeamType, String>" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Manage teams" pageHeading="Manage teams" topNavigation=true twoThirdsColumn=false>

  <div class="pwa-category-list">

    <#list teamTypes as teamType, url>

        <div class="pwa-category-list__item">

          <@fdsAction.link linkText="${teamType.linkText}" linkClass="govuk-link govuk-link--no-visited-state pwa-category-list__link" linkUrl=springUrl(url)/>
          <span class="govuk-hint">${teamType.linkHint}</span>

        </div>

    </#list>

  </div>

</@defaultPage>