<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="isPermDepQuestionRequired" type="java.lang.Boolean" -->
<#-- @ftlvariable name="isAnyDepQuestionRequired" type="java.lang.Boolean" -->

<@defaultPage htmlTitle="Project information" pageHeading="Project information" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>

        <@fdsTextInput.textInput path="form.projectName" labelText="Name of project"/>
        <@fdsDateInput.dateInput dayPath="form.proposedStartDay" monthPath="form.proposedStartMonth" yearPath="form.proposedStartYear" labelText="Proposed start of works date" formId="form.proposedStart"/>
        <@fdsTextarea.textarea path="form.projectOverview" labelText="Overview of project" characterCount=true maxCharacterLength="4000"/>
        <@fdsTextarea.textarea path="form.methodOfPipelineDeployment" labelText="Pipeline installation method" hintText="Brief overview of method that will be deployed for the pipeline installation(s)" characterCount=true maxCharacterLength="4000"/>
        <@fdsDateInput.dateInput dayPath="form.mobilisationDay" monthPath="form.mobilisationMonth" yearPath="form.mobilisationYear" labelText="Date of mobilisation" formId="form.mobilisation"/>
        <@fdsDateInput.dateInput dayPath="form.earliestCompletionDay" monthPath="form.earliestCompletionMonth" yearPath="form.earliestCompletionYear" labelText="Earliest completion date" formId="form.earliestCompletion"/>
        <@fdsDateInput.dateInput dayPath="form.latestCompletionDay" monthPath="form.latestCompletionMonth" yearPath="form.latestCompletionYear" labelText="Latest completion date" formId="form.latestCompletion"/>

        <@fdsRadio.radioGroup path="form.licenceTransferPlanned" labelText="Is a licence transfer planned?" hiddenContent=true hintText="A licence transfer is in relation to a transfer of equity share from one company to another for a defined asset, field or infrastructure.">
            <@fdsRadio.radioYes path="form.licenceTransferPlanned">
                <@fdsDateInput.dateInput dayPath="form.licenceTransferDay" monthPath="form.licenceTransferMonth" yearPath="form.licenceTransferYear" labelText="Licence transfer date" formId="form.licenceTransfer" nestingPath="form.licenceTransferPlanned"/>
                <@fdsDateInput.dateInput dayPath="form.commercialAgreementDay" monthPath="form.commercialAgreementMonth" yearPath="form.commercialAgreementYear" labelText="Commercial agreement date" formId="form.commercialAgreement" nestingPath="form.licenceTransferPlanned"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.licenceTransferPlanned"/>
        </@fdsRadio.radioGroup>

        <@fdsRadio.radioGroup path="form.usingCampaignApproach" labelText="Will the work be completed using a campaign approach?" hintText="Campaign works are a series of planned activities that a Holder will carry out over a defined period of time in order to achieve a particular goal, which is confined to a specified asset or field or PWA.">
          <@fdsRadio.radioYes path="form.usingCampaignApproach"/>
          <@fdsRadio.radioNo path="form.usingCampaignApproach"/>
        </@fdsRadio.radioGroup>

        <#if isAnyDepQuestionRequired>
            <#if isPermDepQuestionRequired>
                <@fdsRadio.radioGroup path="form.permanentDepositsMadeType" labelText="Are permanent deposits being made?" hiddenContent=true>
                    <#assign firstItem=true/>
                    <#list permanentDepositsMadeOptions as depositOption>
                        <@fdsRadio.radioItem path="form.permanentDepositsMadeType" itemMap={depositOption : depositOption.getDisplayText()} isFirstItem=firstItem>
                        <#if depositOption == "LATER_APP">
                            <@fdsNumberInput.twoNumberInputs pathOne="form.futureSubmissionDate.month" pathTwo="form.futureSubmissionDate.year" labelText="Month and year that later application will be submitted" formId="date-of-future-app" nestingPath="form.permanentDepositsMadeType">
                                <@fdsNumberInput.numberInputItem path="form.futureSubmissionDate.month" labelText="Month" inputClass="govuk-input--width-2"/>
                                <@fdsNumberInput.numberInputItem path="form.futureSubmissionDate.year" labelText="Year" inputClass="govuk-input--width-4"/>
                            </@fdsNumberInput.twoNumberInputs>
                        </#if>
                        </@fdsRadio.radioItem>
                    <#assign firstItem=false/>
                    </#list>
                </@fdsRadio.radioGroup>
            </#if>

            <@fdsDetails.details detailsTitle="What is a temporary deposit?"
                detailsText="Temporary deposits are materials which are to be deposited on a temporary basis to provide support to a defined workscope and/or laying of pipelines and will be removed from the seabed back to shore following completion of a proposed workscope."/>
            <@fdsRadio.radioGroup path="form.temporaryDepositsMade" labelText="Are temporary deposits being made as part of this application?" hiddenContent=true hintText="A deposit left in place for less than 20 days, unless you have pre-approval from the OGA.">
                <@fdsRadio.radioYes path="form.temporaryDepositsMade">
                    <@fdsDetails.details detailsTitle="What information do I need to provide?"
                        detailsText="Details of the type of deposit; how long it will be on the seabed and whether the deposit will be within or outside a HSE 500m safety zone should be provided. This is for OGA information only and applications for temporary deposits should still be directed to the BEIS Environmental Management Team for consideration."/>
                    <@fdsTextarea.textarea path="form.temporaryDepDescription" labelText="Description of temporary deposits" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.temporaryDepositsMade"/>
            </@fdsRadio.radioGroup>
        </#if>

        <#if isFdpQuestionRequired>
            <@fdsRadio.radioGroup path="form.fdpOptionSelected" labelText="Do you have an approved field development plan (FDP) for the fields?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.fdpOptionSelected">
                    <@fdsCheckbox.checkboxItem path="form.fdpConfirmationFlag" labelText="The proposed works outlined in this application are consistent with the development as described in the FDP" />
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.fdpOptionSelected">
                    <@fdsTextarea.textarea path="form.fdpNotSelectedReason" labelText="Explain why" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>
        </#if>

        <@fdsFieldset.fieldset legendHeadingClass="govuk-fieldset__legend--l" legendHeading="Project layout diagram" legendHeadingSize="h2" hintText="Provide an overall project layout diagram showing pipeline(s) to be covered by the Authorisation and route of the pipeline(s)." nestingPath="" caption="" captionClass="govuk-caption-l">
            <@fdsFileUpload.fileUpload id="project-doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here" multiFile=false/>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>
    </@fdsForm.htmlForm>

</@defaultPage>