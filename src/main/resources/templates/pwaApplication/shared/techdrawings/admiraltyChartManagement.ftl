<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro admiraltyChartManagement urlFactory optionalSection=false admiraltyChartFileViews=[]>
  <h2 class="govuk-heading-l">
    Admiralty chart
    <#if optionalSection == true> (Optional) </#if>
  </h2>
    <#if admiraltyChartFileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove admiralty chart" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getDocumentsDownloadUrl()) existingFiles=admiraltyChartFileViews/>
    <#else>
        <@fdsInsetText.insetText>
          No admiralty chart has been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add admiralty chart" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>