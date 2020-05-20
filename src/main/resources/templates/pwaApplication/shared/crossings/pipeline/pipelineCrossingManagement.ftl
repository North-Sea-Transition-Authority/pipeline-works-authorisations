<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingUrlFactory" -->

<#macro pipelineCrossingManagement urlFactory pipelineCrossings=[] pipelineCrossingFileViews=[] isCompleted=false>
  <h2 class="govuk-heading-l">Pipeline crossings <@completedTag.completedTag isCompleted/></h2>

    <#if pipelineCrossings?has_content>
        <@fdsAction.link linkText="Add pipeline crossing" linkUrl=springUrl(urlFactory.getAddCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Pipeline reference</th>
          <th class="govuk-table__header" scope="col">Owners</th>
          <th class="govuk-table__header" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list pipelineCrossings as crossing>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${crossing.reference}</td>
            <td class="govuk-table__cell">${crossing.owners}</td>
            <td class="govuk-table__cell">
                <@fdsAction.link linkText="Edit" linkUrl=springUrl(urlFactory.getEditCrossingUrl(crossing.id)) linkClass="govuk-link"/>
              <br/>
                <@fdsAction.link linkText="Remove" linkUrl=springUrl(urlFactory.getRemoveCrossingUrl(crossing.id)) linkClass="govuk-link"/>
            </td>
          </tr>
        </#list>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          There are currently no pipeline crossings added.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add pipeline crossing" linkUrl=springUrl(urlFactory.getAddCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

  <h3 class="govuk-heading-m">Pipeline crossing agreement documents</h3>
    <#if pipelineCrossingFileViews?has_content>
        <@fdsAction.link linkText="Add, edit or remove pipeline crossing agreement documents" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=pipelineCrossingFileViews/>
    <#else>
        <@fdsInsetText.insetText>
          No pipeline crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add, edit or remove pipeline crossing agreement documents" linkUrl=springUrl(urlFactory.getAddDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>