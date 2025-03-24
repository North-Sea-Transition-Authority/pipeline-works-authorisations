<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="partnerLettersTemplateLink" type="String" -->

<@defaultPage htmlTitle="Partner approval letters" pageHeading="Partner approval letters" breadcrumbs=true errorItems=errorList>

    <#assign templateLinkHtml>
        <@fdsAction.link linkUrl=partnerLettersTemplateLink linkText="template" linkClass="govuk-link govuk-link--no-visited-state" openInNewTab=true/>
    </#assign>

    <@fdsForm.htmlForm>

        <#assign detailsText>
        <p>
            Partner approval letters are required for Full PWA and Category 1 variations for all partners.
        </p>
        <p>
            For Category 2 and Decom applications partner approval letters are required if a new partner has an interest in the project and they have not supplied a letter as part of the initial PWA or any associated Category 1 Variations.
        </p>
        <p>
            You must use the partner approval letter ${templateLinkHtml}.
        </p>
        </#assign>

        <@fdsDetails.details detailsTitle="When do I need to provide partner approval letters?" detailsText=detailsText />

        <@fdsRadio.radioGroup path="form.partnerLettersRequired" labelText="Do you need to provide partner approval letters?" hiddenContent=true hintText="A partner is any holder, user, operator or owner that is not part of your corporate group">

            <@fdsRadio.radioYes path="form.partnerLettersRequired">

                <@fdsFileUpload.fileUpload
                    id="partnerLetterFiles"
                    path="form.uploadedFiles"
                    uploadUrl=fileUploadAttributes.uploadUrl()
                    downloadUrl=fileUploadAttributes.downloadUrl()
                    deleteUrl=fileUploadAttributes.deleteUrl()
                    maxAllowedSize=fileUploadAttributes.maxAllowedSize()
                    allowedExtensions=fileUploadAttributes.allowedExtensions()
                    existingFiles=fileUploadAttributes.existingFiles()
                    dropzoneText="Drag and drop your documents here"
                />

                <@fdsDetails.summaryDetails summaryTitle="What information do I need to provide on the partner approval letter?">
                    Partner approval letters should be drafted as per the ${templateLinkHtml}.
                    These can be the letters used in support of the FDP, if that is recent.
                </@fdsDetails.summaryDetails>

                <@fdsCheckbox.checkboxGroup path="form.partnerLettersConfirmed" nestingPath="form.partnerLettersRequired">
                    <@fdsCheckbox.checkboxItem path="form.partnerLettersConfirmed" labelText="I have provided all required partner approval letters" />
                </@fdsCheckbox.checkboxGroup>

            </@fdsRadio.radioYes>

            <@fdsRadio.radioNo path="form.partnerLettersRequired"/>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>