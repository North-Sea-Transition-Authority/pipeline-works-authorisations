<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->
<#-- @ftlvariable name="caseManagementUrl" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${caseSummaryView.pwaApplicationRef} - Add case note" breadcrumbs=true fullWidthColumn=true>

    <@grid.gridRow>
      <@grid.twoThirdsColumn>
          <#if errorList?has_content>
              <@fdsError.errorSummary errorItems=errorList />
          </#if>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>

    <@grid.gridRow>
      <@grid.fullColumn>
          <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />
      </@grid.fullColumn>
    </@grid.gridRow>

    <@grid.gridRow>
      <@grid.twoThirdsColumn>

          <h2 class="govuk-heading-l">Add case note</h2>

          <@fdsForm.htmlForm>

              <@fdsTextarea.textarea path="form.noteText" labelText="Note text" />

              <@fdsFieldset.fieldset legendHeading="Documents (optional)" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2">
                  <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here" />
              </@fdsFieldset.fieldset>

              <@fdsAction.submitButtons primaryButtonText="Add case note" linkSecondaryAction=true secondaryLinkText="Back to case management" linkSecondaryActionUrl=springUrl(caseManagementUrl) />

          </@fdsForm.htmlForm>

      </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>