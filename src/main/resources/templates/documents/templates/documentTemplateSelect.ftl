<#-- @ftlvariable name="documentTemplates" type="java.util.Map<uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec, String>" -->
<#-- @ftlvariable name="urlProvider" type="uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateSelectUrlProvider" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Public notice management" pageHeading="Manage template clauses" pageHeadingClass="govuk-heading-l" topNavigation=true twoThirdsColumn=false backLink=false>
  <div class="pwa-category-list">
    <div class="pwa-category-list__item">
      <@fdsAction.link linkText="Terms & conditions management" linkClass="govuk-link govuk-link--no-visited-state pwa-category-list__link" linkUrl=springUrl(tcUrl)/>
    </div>
  </div>
  <h2 class="govuk-heading-l">Document templates</h2>
  <div class="pwa-category-list">
    <#list documentTemplates as template>
      <div class="pwa-category-list__item">
        <@fdsAction.link linkText="${template.displayName}" linkClass="govuk-link govuk-link--no-visited-state pwa-category-list__link" linkUrl=springUrl(urlProvider.getEditUrl(template))/>
      </div>
    </#list>
  </div>
</@defaultPage>
