<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->

<#macro pipelineCrossingManagement urlFactory pipelineCrossings=[] pipelineCrossingFileViews=[] isCompleted=false>
  <h2 class="govuk-heading-l">Pipeline crossings <@completedTag.completedTag isCompleted/></h2>

    <@fdsAction.link linkText="Add pipeline crossing" linkUrl=springUrl(urlFactory.getAddCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>

    <#if pipelineCrossings?has_content>
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
    </#if>

</#macro>