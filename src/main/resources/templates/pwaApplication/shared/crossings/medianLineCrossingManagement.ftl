<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#-- NOTE: You must pass in a default value to medianLineAgreementView to stop freemarker throwing an error if null. -->
<#macro medianLineCrossingManagement urlFactory medianLineAgreementView medianLineFileViews=[]>
  <h2 class="govuk-heading-l">Median line agreement</h2>
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
          You must provide information regarding median line crossing agreements.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Provide median line agreement information" linkUrl=springUrl(urlFactory.getAddMedianLineCrossingUrl()) role=true linkClass="govuk-button govuk-button--blue"/>
    </#if>
  <h3 class="govuk-heading-m">Median line agreement documents</h3>
    <#if medianLineFileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove median line agreement documents" linkUrl=springUrl(urlFactory.getMedianLineCrossingDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=medianLineFileViews/>
    <#else>
        <@fdsInsetText.insetText>
          No median line crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add, edit or remove median line agreement documents" linkUrl=springUrl(urlFactory.getMedianLineCrossingDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>