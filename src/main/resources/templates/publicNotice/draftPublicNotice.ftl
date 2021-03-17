<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="publicNoticeRequestReasons" type="java.util.List<uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} draft a public notice" topNavigation=true twoThirdsColumn=false breadcrumbs=true>


  <#if errorList?has_content>
    <@fdsError.errorSummary errorItems=errorList/>
  </#if>

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Draft a public notice</h2>

  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea path="form.coverLetterText" labelText="Cover letter text" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>

    <@grid.gridRow>
      <@grid.twoThirdsColumn>
          <@fdsFieldset.fieldset legendHeading="Public notice document" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" hintText="Provide a draft public notice document (parts A-D)">
              <@fdsFileUpload.fileUpload id="doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl
              maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here" />
          </@fdsFieldset.fieldset>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>

    <@fdsRadio.radioGroup path="form.reason" labelText="Why is this public notice being requested?" hiddenContent=true>
      <#list publicNoticeRequestReasons as  requestReasonOption>
          <@fdsRadio.radioItem path="form.reason" itemMap={requestReasonOption : requestReasonOption.getReasonText()} isFirstItem=firstItem>        
            <#if requestReasonOption == "CONSULTEES_NOT_ALL_CONTENT">
              <@fdsTextarea.textarea path="form.reasonDescription" nestingPath="form.reason" labelText="Why does the public notice need to progress?" characterCount=true maxCharacterLength="4000" inputClass="govuk-!-width-two-thirds"/>
            </#if>
          </@fdsRadio.radioItem>
      <#assign firstItem=false/>
      </#list>
    </@fdsRadio.radioGroup>

    <@fdsAction.submitButtons primaryButtonText="Send for manager approval" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

  </@fdsForm.htmlForm>

  
</@defaultPage>
