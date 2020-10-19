<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="docFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="suppDocFileDownloadUrl" type="java.lang.String" -->

<div class="pwa-application-summary-section">

  <h2 class="govuk-heading-l" id="supplementaryDocuments">${sectionDisplayText}</h2>

    <#if docFileViews?has_content>

      <@pwaFiles.uploadedFileList downloadUrl=springUrl(suppDocFileDownloadUrl) existingFiles=docFileViews />

        <#else>

          <@fdsInsetText.insetText>No supplementary documents have been uploaded.</@fdsInsetText.insetText>

    </#if>

</div>