<#include '../../layout.ftl'>

<#-- @ftlvariable name="existingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="file" type="uk.co.ogauthority.pwa.model.form.files.UploadedFileView" -->

<#macro uploadedFileList downloadUrl existingFiles=[] blockClass="file-description">
 <#if existingFiles?has_content>
  <table class="govuk-table">
    <thead class="govuk-table__header">
      <tr class="govuk-table__row">
        <th class="govuk-table__header govuk-!-width-one-half" scope="col">Filename</th>
        <th class="govuk-table__header govuk-!-width-one-half" scope="col">Description</th>
      </tr>
    </thead>
    <tbody class="govuk-table__body">
    <#list existingFiles as file>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
          <@fdsAction.link linkText=file.getFileName() linkUrl=downloadUrl+file.getFileId() linkScreenReaderText="Download ${file.getFileName()}" role=false start=false openInNewTab=true/>
        </td>
        <td class="govuk-table__cell">
          <@multiLineText.multiLineText blockClass=blockClass>${file.fileDescription!}</@multiLineText.multiLineText>
        </td>
      </tr>
    </#list>
    </tbody>
  </table>
  </#if>
</#macro>


<#macro uploadedFile downloadUrl file blockClass="file-description">
  <#if file?has_content>
    <table class="govuk-table">
      <thead class="govuk-table__header">
      <tr class="govuk-table__row">
        <th class="govuk-table__header govuk-!-width-one-half" scope="col">Filename</th>
        <th class="govuk-table__header govuk-!-width-one-half" scope="col">Description</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <td class="govuk-table__cell">
            <@fdsAction.link linkText=file.getFileName() linkUrl=downloadUrl+file.getFileId() linkScreenReaderText="Download ${file.getFileName()}" role=false start=false openInNewTab=true/>
          </td>
          <td class="govuk-table__cell">
            <@multiLineText.multiLineText blockClass=blockClass>${file.fileDescription!}</@multiLineText.multiLineText>
          </td>
        </tr>
      </tbody>
    </table>
  </#if>
</#macro>