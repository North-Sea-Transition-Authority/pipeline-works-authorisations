<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro umbilicalCrossSectionDiagramManagement urlFactory optionalSection=false fileViews=[]>
  <h2 class="govuk-heading-l">
    Umbilical cross-section diagram
    <#if optionalSection == true> (Optional) </#if>
  </h2>

    <@fdsInsetText.insetText>
      You must provide a cross-section diagram for the main umbilical if it is part of your application.
        <#if fileViews?size == 0>
            <br/><br/>
          No umbilical cross-section diagram has been added to this application.
        </#if>
    </@fdsInsetText.insetText>

    <#if fileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove diagram" linkUrl=springUrl(urlFactory.getAddDocumentUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@fileUpload.uploadedFileList
          downloadUrl=springUrl(urlFactory.getDocumentDownloadUrl())
          existingFiles=fileViews
        />
    <#else>
        <@fdsAction.link linkText="Add diagram" linkUrl=springUrl(urlFactory.getAddDocumentUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>