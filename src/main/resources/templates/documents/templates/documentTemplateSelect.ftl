<#-- @ftlvariable name="documentTemplates" type="java.util.Map<uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSpec, String>" -->
<#-- @ftlvariable name="urlProvider" type="uk.co.ogauthority.pwa.service.documents.templates.DocumentTemplateSelectUrlProvider" -->

<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Select a document template" pageHeading="Select a document template" topNavigation=true twoThirdsColumn=false backLink=false>

  <@fdsAction.link linkText="Terms and Conditions Management" linkUrl=springUrl(tcManagement)/>
  <div class="pwa-category-list">

      <#list documentTemplates as template>

        <div class="pwa-category-list__item">

          <@fdsAction.link linkText="${template.displayName}" linkClass="govuk-link govuk-link--no-visited-state pwa-category-list__link" linkUrl=springUrl(urlProvider.getEditUrl(template))/>

        </div>

      </#list>

  </div>

</@defaultPage>
