<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="preselectedFacilitiesIfYes" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="preselectedFacilitiesIfPartially" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="safetyZoneOptions" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="facilityRestUrl" type="java.lang.String" -->
<#-- @ftlvariable name="requiredQuestions" type="java.util.Set<uk.co.ogauthority.pwa.model.entity.enums.LocationDetailsQuestion>" -->

<@defaultPage htmlTitle="Location details" pageHeading="Location details" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <#if requiredQuestions?seq_contains("APPROXIMATE_PROJECT_LOCATION_FROM_SHORE")>
            <@fdsTextInput.textInput path="form.approximateProjectLocationFromShore" labelText="Approximate project location from shore" hintText="e.g. 127km east of Norwick (Shetland Isles) and 390km northeast of Aberdeen"/>
        </#if>

        <#if requiredQuestions?seq_contains("WITHIN_SAFETY_ZONE")>
            <@fdsRadio.radioGroup path="form.withinSafetyZone" labelText="Will work be carried out within a HSE recognised 500m safety zone?" hiddenContent=true>
                <#assign firstItem = true/>
                <#list safetyZoneOptions as name, value>
                    <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={name:value} isFirstItem=firstItem>
                        <#if name == "YES">
                            <@safetyZoneQuestion formPath="form.completelyWithinSafetyZoneForm" preselectedItems=preselectedFacilitiesIfYes/>
                        <#elseif name == "PARTIALLY">
                            <@safetyZoneQuestion formPath="form.partiallyWithinSafetyZoneForm" preselectedItems=preselectedFacilitiesIfPartially/>
                        </#if>
                    </@fdsRadio.radioItem>
                    <#assign firstItem = false/>
                </#list>
            </@fdsRadio.radioGroup>
        </#if>

        <#if requiredQuestions?seq_contains("PSR_NOTIFICATION")>
            <@fdsRadio.radioGroup path="form.psrNotificationSubmittedOption" labelText="Have you submitted a Pipelines Safety Regulations notification to HSE?" hintText="Timely submission in advance of work is advised to avoid potential delays" hiddenContent=true nestingPath="form.withinSafetyZone">                
                <#assign firstItem = true/>
                <#list psrNotificationOptions as name, value>
                    <@fdsRadio.radioItem path="form.psrNotificationSubmittedOption" itemMap={name:value} isFirstItem=firstItem>
                        <#if name == "YES">
                        <@fdsNumberInput.twoNumberInputs pathOne="form.psrNotificationSubmittedDate.month" pathTwo="form.psrNotificationSubmittedDate.year" labelText="Date submitted" formId="submitted-month-year" nestingPath="form.psrNotificationSubmittedOption">
                            <@fdsNumberInput.numberInputItem path="form.psrNotificationSubmittedDate.month" labelText="Month" inputClass="govuk-input--width-2"/>
                            <@fdsNumberInput.numberInputItem path="form.psrNotificationSubmittedDate.year" labelText="Year" inputClass="govuk-input--width-4"/>
                        </@fdsNumberInput.twoNumberInputs>
                        <#elseif name == "NO">
                            <@fdsNumberInput.twoNumberInputs pathOne="form.psrNotificationExpectedSubmissionDate.month" pathTwo="form.psrNotificationExpectedSubmissionDate.year" labelText="Expected submission date" formId="expected-submission-month-year" nestingPath="form.psrNotificationSubmittedOption">
                                <@fdsNumberInput.numberInputItem path="form.psrNotificationExpectedSubmissionDate.month" labelText="Month" inputClass="govuk-input--width-2"/>
                                <@fdsNumberInput.numberInputItem path="form.psrNotificationExpectedSubmissionDate.year" labelText="Year" inputClass="govuk-input--width-4"/>
                            </@fdsNumberInput.twoNumberInputs>
                        <#else>
                            <@fdsTextarea.textarea path="form.psrNotificationNotRequiredReason" labelText="Why is a PSR notification not required?" characterCount=true maxCharacterLength="4000" nestingPath="form.psrNotificationSubmittedOption"/>
                        </#if>
                    </@fdsRadio.radioItem>
                    <#assign firstItem = false/>
                </#list>
            </@fdsRadio.radioGroup>
        </#if>

        <#if requiredQuestions?seq_contains("DIVERS_USED")>
            <@fdsRadio.radioGroup path="form.diversUsed" labelText="Will divers be used?">
                <@fdsRadio.radioYes path="form.diversUsed"/>
                <@fdsRadio.radioNo path="form.diversUsed"/>
            </@fdsRadio.radioGroup>
        </#if>

        <#if requiredQuestions?seq_contains("FACILITIES_OFFSHORE")>
            <@fdsRadio.radioGroup path="form.facilitiesOffshore" labelText="Are all facilities wholly offshore and subsea?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.facilitiesOffshore"/>
                <@fdsRadio.radioNo path="form.facilitiesOffshore">
                <@fdsTextarea.textarea path="form.pipelineAshoreLocation" labelText="Where do the pipelines come ashore?" nestingPath="form.facilitiesOffshore" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>
        </#if>

        <#if requiredQuestions?seq_contains("TRANSPORTS_MATERIALS_TO_SHORE")>
            <@fdsRadio.radioGroup path="form.transportsMaterialsToShore" labelText="Will the pipeline(s) be used to transport products / facilitate the transportation of products to shore?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.transportsMaterialsToShore">
                    <@fdsTextarea.textarea path="form.transportationMethod" labelText="State the method of transportation to shore" nestingPath="form.transportsMaterialsToShore" characterCount=true maxCharacterLength="4000" hintText="Processed oil is stored on the FPSO before being exported onshore by tanker. Gas is either exported via a 16\" flowline to Platform and onward to the SAGE system, or used as fuel or lift gas."/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.transportsMaterialsToShore"/>
            </@fdsRadio.radioGroup>
        </#if>

        <#if requiredQuestions?seq_contains("ROUTE_SURVEY_UNDERTAKEN")>
            <@fdsRadio.radioGroup path="form.routeSurveyUndertaken" labelText="Has a pipeline route survey been undertaken?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.routeSurveyUndertaken">
                    <@fdsDateInput.dateInput dayPath="form.surveyConcludedDay" monthPath="form.surveyConcludedMonth" yearPath="form.surveyConcludedYear" labelText="When was the pipeline route survey concluded?" formId="surveyConcludedDate" nestingPath="form.routeSurveyUndertaken"/>
                    <@fdsTextarea.textarea path="form.pipelineRouteDetails" labelText="Pipeline route details" hintText="Provide pipeline route details, including water depths along the pipeline route, seabed composition, bathymetric data, seabed features, and soil condition details" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.routeSurveyUndertaken">
                    <@fdsTextarea.textarea path="form.routeSurveyNotUndertakenReason" labelText="Why has a pipeline route survey not been undertaken?" hintText="If you have access to the results of a recent survey, answer Yes to route survey undertaken and provide the information" characterCount=true maxCharacterLength="4000"/>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>
        </#if>
        <#if requiredQuestions?seq_contains("WITHIN_LIMITS_OF_DEVIATION")>
            <@fdsCheckbox.checkbox path="form.withinLimitsOfDeviation" labelText="I confirm that the limit of deviation during construction will be ±100m"/>
        </#if>

        <#-- TODO: PWA-432 Update guidance text with correct supporting documents. -->
        <#if requiredQuestions?seq_contains("ROUTE_DOCUMENTS")>
            <@fdsFieldset.fieldset legendHeading="Pipeline route documents" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" optionalLabel=true hintText="You may attach supporting documents, such as bathymetric data">
                <@fdsFileUpload.fileUpload id="project-doc-upload-file-id" path="form.uploadedFileWithDescriptionForms" uploadUrl=uploadUrl deleteUrl=deleteUrl maxAllowedSize=fileuploadMaxUploadSize allowedExtensions=fileuploadAllowedExtensions downloadUrl=downloadUrl existingFiles=uploadedFileViewList dropzoneText="Drag and drop your documents here"/>
            </@fdsFieldset.fieldset>
        </#if>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>



<#macro safetyZoneQuestion formPath preselectedItems={}>

    <@fdsSearchSelector.searchSelectorRest path="${formPath}.facilities" labelText="Which structures are within 500m?" multiSelect=true restUrl=springUrl(facilityRestUrl)
    nestingPath="form.withinSafetyZone" preselectedItems=preselectedItems hintText="e.g the platform, FPSO, boat, or storage unit"/>
</#macro>