<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro cableCrossingManagement urlFactory optionalSection=false cableCrossingFileViews=[] isCompleted=false>
  <h2 class="govuk-heading-l">
    Admirality chart
    <#if optionalSection == true> (Optional) </#if>
  </h2>
  <h3 class="govuk-heading-m">Cable crossing agreement documents</h3>
    <@fdsAction.link linkText="Add, edit or remove admirality chart" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    <#if cableCrossingFileViews?has_content>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=cableCrossingFileViews/>
    <#else>
      <p class="govuk-body">No cable crossing agreement documents have been added to this application</p>
    </#if>

</#macro>