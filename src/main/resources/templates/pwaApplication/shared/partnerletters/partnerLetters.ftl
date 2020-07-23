<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Partner approval letters" pageHeading="Partner approval letters" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.partnerLettersRequired" labelText="Do you need to provide partner approval letters?" hiddenContent=true
        hintText="Partners letters are required for all new PWAs and for all Category 1 PWAs. For Category 2 and Decom applications partners letters may be required where existing pipelines have the product conveyed changed or other material changes.">

            <@fdsRadio.radioYes path="form.partnerLettersRequired">
                <@fdsFileUpload.fileUpload id="partner-letters-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
                <#--  TODO PWA-681 - OGA Link  -->
                <@fdsDetails.details detailsText="Partner approval letters should be drafted as per the template at OGA_LINK. These can be the letters used in support of the FDP, if that is recent." detailsTitle="What information do I need to provide on the partner approval letter?"/>

                <@fdsCheckbox.checkboxGroup path="form.partnerLettersConfirmed" nestingPath="form.partnerLettersRequired">
                    <@fdsCheckbox.checkboxItem path="form.partnerLettersConfirmed" labelText="I have provided all required partner approval letters" />
                </@fdsCheckbox.checkboxGroup>

            </@fdsRadio.radioYes>

            <@fdsRadio.radioNo path="form.partnerLettersRequired"/>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>