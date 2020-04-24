<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro admiralityChartManagement urlFactory optionalSection=false cableCrossingFileViews=[]>
  <h2 class="govuk-heading-l">
    Admirality chart
    <#if optionalSection == true> (Optional) </#if>
  </h2>
    <#if cableCrossingFileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove admirality chart" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@fileUpload.uploadedFileList downloadUrl=springUrl("#") existingFiles=cableCrossingFileViews/>
    <#else>
        <@fdsInsetText.insetText>
          No admirality chart has been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add admirality chart" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>