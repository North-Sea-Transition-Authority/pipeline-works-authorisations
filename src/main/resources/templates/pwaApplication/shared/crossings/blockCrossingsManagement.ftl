<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->

<#macro blockCrossingManagement urlFactory blockCrossings=[] blockCrossingFileViews=[]>
  <h2 class="govuk-heading-l">Blocks</h2>
    <@fdsInsetText.insetText>
      <span>Add each block the pipelines are located in or will cross.</span>
    </@fdsInsetText.insetText>

    <@fdsAction.link linkText="Add block" linkUrl=springUrl(urlFactory.getAddBlockCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>
    <#if blockCrossings?has_content>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">UK block reference</th>
          <th class="govuk-table__header" scope="col">Licence</th>
          <th class="govuk-table__header" scope="col">Block operator</th>
          <th class="govuk-table__header" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list blockCrossings as crossing>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${crossing.blockReference}</td>
            <td class="govuk-table__cell">${crossing.licenceReference}</td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                  <#if crossing.blockOwnedCompletelyByHolder>
                    <li>Holder owned</li>
                  </#if>
                  <#list crossing.blockOperatorList as operator>
                    <li>${operator}</li>
                  </#list>
              </ul>
            </td>
            <td class="govuk-table__cell">
                <@fdsAction.link linkText="Edit" linkUrl=springUrl(urlFactory.getEditBlockCrossingUrl(crossing.id)) linkClass="govuk-link"/>
              <br/>
                <@fdsAction.link linkText="Remove" linkUrl=springUrl(urlFactory.getRemoveBlockCrossingUrl(crossing.id)) linkClass="govuk-link"/>
            </td>
          </tr>
        </#list>
        </tbody>
      </table>
    </#if>

  <h3 class="govuk-heading-m">Block crossing agreement documents</h3>
    <@fdsInsetText.insetText>
      <span>Any crossed block not 100% owned by the PWA holder(s) requires a block crossing agreement document to be uploaded.</span>
    </@fdsInsetText.insetText>
    <@fdsAction.link linkText="Add, edit or remove block crossing documents" linkUrl=springUrl(urlFactory.getBlockCrossingDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    <#if blockCrossingFileViews?has_content>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=blockCrossingFileViews/>
    </#if>




</#macro>