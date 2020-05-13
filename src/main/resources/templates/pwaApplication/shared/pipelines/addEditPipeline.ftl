<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelineNumber" type="String" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ${pipelineNumber!} pipeline" pageHeading="${screenActionType.actionText} ${pipelineNumber!} pipeline" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
                                          formId="fromLatitude"
                                          labelText="Start point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.fromCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="fromLongitude"
                                          labelText="Start point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
                                          formId="toLatitude"
                                          labelText="Finish point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
                                          direction="EW"
                                          directionPath="form.toCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="toLongitude"
                                          labelText="Finish point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Pipeline information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsSelect.select path="form.pipelineType" labelText="Pipeline type" options=pipelineTypes />

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-5"/>

            <@fdsTextarea.textarea path="form.componentPartsDescription" labelText="Description of component parts of the pipeline" hintText="Some guidance text here."/>

            <@fdsTextarea.textarea path="form.productsToBeConveyed" labelText="Products to be conveyed"/>

            <@fdsRadio.radioGroup path="form.trenchedBuriedBackfilled" labelText="Will the proposed pipeline be trenched and/or buried and/or backfilled?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.trenchedBuriedBackfilled">
                    <@fdsTextarea.textarea path="form.trenchingMethods" labelText="Describe the methods to be deployed to execute the trenching and the target depth of trench" nestingPath="form.trenchedBuriedBackfilled"/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.trenchedBuriedBackfilled"/>
            </@fdsRadio.radioGroup>

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>