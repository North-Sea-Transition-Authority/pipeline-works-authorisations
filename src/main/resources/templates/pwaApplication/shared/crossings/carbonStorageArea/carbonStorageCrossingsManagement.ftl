<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="carbonStorageCrossings" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingView>" -->
<#-- @ftlvariable name="carbonStorageCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingUrlFactory" -->

<#macro carbonStorageCrossingManagement urlFactory isDocumentsRequired crossings=[] crossingFileViews=[]>
  <h2 class="govuk-heading-l">Carbon storage areas</h2>
    <@fdsInsetText.insetText>
      <span>Add each storage area the pipelines are located in or will cross.</span>
    </@fdsInsetText.insetText>

    <@fdsAction.link linkText="Add storage area" linkUrl=springUrl(urlFactory.getAddCarbonStorageCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>
    <#if crossings?has_content>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Reference</th>
          <th class="govuk-table__header" scope="col">Area owner</th>
          <th class="govuk-table__header" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list carbonStorageCrossings as crossing>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${crossing.storageAreaReference}</td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                  <#if crossing.ownedCompletelyByHolder>
                    <li>Holder owned</li>
                  </#if>
                  <#list crossing.operatorList as operator>
                    <li>${operator}</li>
                  </#list>
              </ul>
            </td>
            <td class="govuk-table__cell">
                <@fdsAction.link linkText="Edit" linkUrl=springUrl(urlFactory.getEditCarbonStorageCrossingUrl(crossing.id)) linkClass="govuk-link" linkScreenReaderText="crossing for carbon storage area ${crossing.storageAreaReference}"/>
              <br/>
                <@fdsAction.link linkText="Remove" linkUrl=springUrl(urlFactory.getRemoveCarbonStorageCrossingUrl(crossing.id)) linkClass="govuk-link" linkScreenReaderText="crossing for carbon storage area ${crossing.storageAreaReference}"/>
            </td>
          </tr>
        </#list>
        </tbody>
      </table>
    </#if>
    <#if isDocumentsRequired>
      <h3 class="govuk-heading-m">Carbon storage area crossing agreement documents</h3>
        <@fdsInsetText.insetText>
          <span>Any crossed area not 100% owned by the PWA holder(s) requires a crossing agreement document to be uploaded.</span>
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add, edit or remove crossing documents" linkUrl=springUrl(urlFactory.getCarbonStorageCrossingDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <#if crossingFileViews?has_content>
            <@pwaFiles.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=crossingFileViews/>
        </#if>
    </#if>
</#macro>
