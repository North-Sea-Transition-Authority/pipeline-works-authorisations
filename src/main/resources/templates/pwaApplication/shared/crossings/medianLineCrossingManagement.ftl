<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->

<#-- NOTE: medianLineAgreementView argument must have a default modifer applied to allow for null-safety. -->
<#macro medianLineCrossingManagement urlFactory medianLineAgreementView medianLineFileViews=[] isCompleted=false>
  <h2 class="govuk-heading-l">Median line
    agreement <@completedTag.completedTag isCompleted=crossingAgreementValidationResult.isSectionValid("MEDIAN_LINE")/></h2>
    <#if medianLineAgreementView?has_content>
        <@fdsAction.link linkText="Update median line agreement" linkUrl=springUrl(urlFactory.getAddMedianLineCrossingUrl()) role=true linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Status</th>
          <td class="govuk-table__cell">${medianLineAgreementView.agreementStatus.displayText}</td>
        </tr>
        <#if medianLineAgreementView.agreementStatus != "NOT_CROSSED">
          <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="col">Name of negotiator</th>
            <td class="govuk-table__cell">${medianLineAgreementView.negotiatorName!}</td>
          </tr>
          <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="col">Contact email for negotiator</th>
            <td class="govuk-table__cell">${medianLineAgreementView.negotiatorEmail!}</td>
          </tr>
        </#if>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          You must provide information regarding median line crossing agreements
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Provide median line agreement information" linkUrl=springUrl(urlFactory.getAddMedianLineCrossingUrl()) role=true linkClass="govuk-button govuk-button--blue"/>
    </#if>
  <h3 class="govuk-heading-m">Block crossing agreement documents</h3>
    <@fdsAction.link linkText="Add, edit or remove median line agreement documents" linkUrl=springUrl(urlFactory.getMedianLineCrossingDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    <#if medianLineFileViews?has_content>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=medianLineFileViews/>
    <#else>
      <p class="govuk-body">No median line crossing agreement documents have been added to this application</p>
    </#if>

</#macro>