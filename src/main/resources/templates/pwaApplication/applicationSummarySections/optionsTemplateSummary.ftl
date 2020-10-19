<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="optionsTemplateFileView" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView" -->
<#-- @ftlvariable name="optionsFileDownloadUrl" type="java.lang.String" -->

<div class="pwa-application-summary-section">

  <h2 class="govuk-heading-l" id="optionsTemplate">${sectionDisplayText}</h2>

    <#if optionsTemplateFileView?has_content>

      <@pwaFiles.uploadedFile downloadUrl=springUrl(optionsFileDownloadUrl) file=optionsTemplateFileView />

      <#else>

        <@fdsInsetText.insetText>No options template has been uploaded.</@fdsInsetText.insetText>

    </#if>

</div>