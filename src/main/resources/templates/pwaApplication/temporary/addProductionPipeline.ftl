<#include '../../layout.ftl'>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.temp.model.form.AddProductionPipelineForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Add production pipeline" pageHeading="Add production pipeline">
    <@fdsForm.htmlForm>

        <@fdsRadio.radio path="form.pipelineType" labelText="Pipeline type" radioItems=pipelineTypes />

        <@fdsTextInput.textInput path="form.from" labelText="From" />

        <@fdsNumberInput.locationInput degreesLocationPath="form.fromLatitudeDegrees" minutesLocationPath="form.fromLatitudeMinutes" secondsLocationPath="form.fromLatitudeSeconds" />
        <@fdsNumberInput.locationInput degreesLocationPath="form.fromLongitudeDegrees" minutesLocationPath="form.fromLongitudeMinutes" secondsLocationPath="form.fromLongitudeSeconds" />

        <@fdsTextInput.textInput path="form.to" labelText="To" />

        <@fdsNumberInput.locationInput degreesLocationPath="form.toLatitudeDegrees" minutesLocationPath="form.toLatitudeMinutes" secondsLocationPath="form.toLatitudeSeconds" />
        <@fdsNumberInput.locationInput degreesLocationPath="form.toLongitudeDegrees" minutesLocationPath="form.toLongitudeMinutes" secondsLocationPath="form.toLongitudeSeconds" />

        <@fdsTextInput.textInput path="form.componentParts" labelText="Description of component parts of the pipeline" />

        <@fdsNumberInput.numberInputItem path="form.length" labelText="Length (m)" />

        <@fdsTextInput.textInput path="form.productsToBeConveyed" labelText="Products to be conveyed" />

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>