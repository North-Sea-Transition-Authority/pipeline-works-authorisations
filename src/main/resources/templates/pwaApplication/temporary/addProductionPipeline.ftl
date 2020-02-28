<#include '../../layout.ftl'>
<#import '../../pwaApplication/temporary/widgets/locationInput.ftl' as tempLocationInput>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.temp.model.form.AddProductionPipelineForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="viewMode" type="uk.co.ogauthority.pwa.temp.model.ViewMode" -->

<@defaultPage htmlTitle="Add pipeline" pageHeading="${viewMode.displayText} pipeline" breadcrumbs=true>
    <@fdsForm.htmlForm>

        <@fdsSelect.select path="form.pipelineType" labelText="Pipeline type" options=pipelineTypes />

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.from" labelText="Structure" />

            <@tempLocationInput.locationInput degreesLocationPath="form.fromLatitudeDegrees" minutesLocationPath="form.fromLatitudeMinutes" secondsLocationPath="form.fromLatitudeSeconds" />
            <@tempLocationInput.locationInput degreesLocationPath="form.fromLongitudeDegrees" minutesLocationPath="form.fromLongitudeMinutes" secondsLocationPath="form.fromLongitudeSeconds" direction="EW" />

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.to" labelText="Structure" />

            <@tempLocationInput.locationInput degreesLocationPath="form.toLatitudeDegrees" minutesLocationPath="form.toLatitudeMinutes" secondsLocationPath="form.toLatitudeSeconds" />
            <@tempLocationInput.locationInput degreesLocationPath="form.toLongitudeDegrees" minutesLocationPath="form.toLongitudeMinutes" secondsLocationPath="form.toLongitudeSeconds" direction="EW" />

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Pipeline information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.componentParts" labelText="Description of component parts of the pipeline" />

            <@fdsNumberInput.numberInputItem path="form.length" labelText="Length (m)" />

            <@fdsTextInput.textInput path="form.productsToBeConveyed" labelText="Products to be conveyed" />

        </@fdsFieldset.fieldset>

        <#if viewMode == "UPDATE">
            <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Update pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
        <#elseif viewMode == "NEW">
            <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add new pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
        </#if>

    </@fdsForm.htmlForm>
</@defaultPage>