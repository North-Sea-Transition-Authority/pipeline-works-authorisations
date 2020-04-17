<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="Location details" pageHeading="Location details" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.approximateProjectLocationFromShore" labelText="Approximate project location from shore"/>
        <@fdsRadio.radioGroup path="form.withinSafetyZone" labelText="Will work be carried out within a HSE recognised 500m safety zone?" hiddenContent=true>
            <#assign firstItem = true/>
            <#list safetyZoneOptions as name, value>
                <@fdsRadio.radioItem path="form.withinSafetyZone" itemMap={name:value} isFirstItem=firstItem>
                    <#if name == "YES">
                        <@fdsSelect.select path="form.facilitiesIfYes" options=facilityOptions labelText="Which structures are within 500m?" hintText="DEVUK facility or other structure" nestingPath="form.withinSafetyZone"/>
                    <#elseif name == "PARTIALLY">
                        <@fdsSelect.select path="form.facilitiesIfPartially" options=facilityOptions labelText="Which structures are within 500m?" hintText="DEVUK facility or other structure" nestingPath="form.withinSafetyZone"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem = false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsRadio.radioGroup path="form.facilitiesOffshore" labelText="Are all facilities wholly offshore and subsea?">
            <@fdsRadio.radioYes path="form.facilitiesOffshore"/>
            <@fdsRadio.radioNo path="form.facilitiesOffshore"/>
        </@fdsRadio.radioGroup>
        <@fdsRadio.radioGroup path="form.transportsMaterialsToShore" labelText="Will the pipeline be used to transport materials / facilitate the transportation of materials to shore?" hiddenContent=true>
            <@fdsRadio.radioYes path="form.transportsMaterialsToShore">
                <@fdsTextInput.textInput path="form.transportationMethod" labelText="State the method of transportation to shore" nestingPath="form.transportsMaterialsToShore"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.transportsMaterialsToShore"/>
        </@fdsRadio.radioGroup>

        <@fdsRadio.radioGroup path="form.routeSurveyUndertaken" labelText="Has a pipeline route survey been undertaken?" hiddenContent=true>
            <@fdsRadio.radioYes path="form.routeSurveyUndertaken">
                <@fdsDateInput.dateInput dayPath="form.surveyConcludedDay" monthPath="form.surveyConcludedMonth" yearPath="form.surveyConcludedYear" labelText="When was the pipeline route survey concluded?" formId="surveyConcludedDate"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.routeSurveyUndertaken"/>
        </@fdsRadio.radioGroup>
        <@fdsTextarea.textarea path="form.pipelineRouteDetails" labelText="Pipeline route details" hintText="Provide pipeline route details, including water depths along the pipeline route, seabed composition, bathymetric data, seabed features, and soil condition details"/>
        <@fdsCheckbox.checkbox path="form.withinLimitsOfDeviation" labelText="I confirm that the limit of deviation during construction will be ±100m"/>

        <#-- TODO: PWA-432 Update guidance text with correct supporting documents. -->
        <@fdsFieldset.fieldset legendHeading="Pipeline route documents" legendHeadingClass="govuk-fieldset__legend--m" legendHeadingSize="h2" optionalLabel=true hintText="You may attach supporting documents, such as bathymetric data">
            <#if uploadedFiles?has_content>
                <@fdsAction.link linkText="Add, edit or remove pipeline route documents" linkUrl=springUrl(urlFactory.getEditDocumentsUrl()) linkClass="govuk-button govuk-button--blue"/>
                <@fileUpload.uploadedFileList downloadUrl=springUrl(urlFactory.getFileDownloadUrl()) existingFiles=uploadedFiles/>
            <#else>
                <@fdsInsetText.insetText>
                  No pipeline route documents have been uploaded.
                </@fdsInsetText.insetText>
                <@fdsAction.button buttonText="Add, edit or remove pipeline route documents" buttonClass="govuk-button govuk-button--blue"/>
            </#if>
        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>