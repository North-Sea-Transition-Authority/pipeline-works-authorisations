<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${screenAction.actionText} deposit drawing" pageHeading="${screenAction.actionText} deposit drawing" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.reference" labelText="Drawing reference" inputClass="govuk-!-width-two-thirds"/>

        <@fileUpload.fileUpload path="form.uploadedFileWithDescriptionForms" id="deposit-doc-upload-file-id" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize 
         allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here" multiFile=fasle/>

        <@fdsSearchSelector.searchSelectorEnhanced path="form.selectedDeposits" options=depositOptions labelText="Select deposits" multiSelect=true optionalInputDefault="Select one or more"/>
  
        


        <@fdsAction.submitButtons primaryButtonText="Complete" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>