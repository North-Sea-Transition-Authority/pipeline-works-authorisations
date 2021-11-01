<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsValidationResult" -->

<#macro cableCrossingManagement urlFactory cableCrossingViews=[] cableCrossingFileViews=[]>
  <h2 class="govuk-heading-l">Cable crossings</h2>
    <#if cableCrossingViews?has_content>
        <@fdsAction.link linkText="Add cable crossing" linkUrl=springUrl(urlFactory.getAddCableCrossingUrl()) role=true linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Cable name</th>
          <th class="govuk-table__header" scope="col">Cable owner</th>
          <th class="govuk-table__header" scope="col">Location</th>
          <th class="govuk-table__header" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list cableCrossingViews as crossing>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${crossing.cableName}</td>
            <td class="govuk-table__cell">${crossing.owner}</td>
            <td class="govuk-table__cell">${crossing.location}</td>
            <td class="govuk-table__cell">
              <a href="${springUrl(urlFactory.getEditCableCrossingUrl(crossing.id))}" class="govuk-link">Edit</a>
              <br/>
              <a href="${springUrl(urlFactory.getRemoveCableCrossingUrl(crossing.id))}" class="govuk-link">Remove</a>
            </td>
          </tr>
        </#list>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          There are currently no cable crossings added.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add cable crossing" linkUrl=springUrl(urlFactory.getAddCableCrossingUrl()) role=true linkClass="govuk-button govuk-button--blue"/>
    </#if>
  <h3 class="govuk-heading-m">Cable crossing agreement documents</h3>
    <#if cableCrossingFileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove cable crossing agreement documents" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=cableCrossingFileViews/>
    <#else>
        <@fdsInsetText.insetText>
          No cable crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add, edit or remove cable crossing agreement documents" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>