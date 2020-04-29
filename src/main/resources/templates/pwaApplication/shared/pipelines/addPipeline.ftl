<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="viewMode" type="uk.co.ogauthority.pwa.model.form.enums.PipelineViewMode" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${viewMode.displayText} pipeline" pageHeading="${viewMode.displayText} pipeline" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            <@locationInput.locationInput degreesLocationPath="form.fromLatDeg" minutesLocationPath="form.fromLatMin" secondsLocationPath="form.fromLatSec" />
            <@locationInput.locationInput degreesLocationPath="form.fromLongDeg" minutesLocationPath="form.fromLongMin" secondsLocationPath="form.fromLongSec" direction="EW_MANUAL" directionPath="form.fromLongDirection" directionList=longDirections />

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            <@locationInput.locationInput degreesLocationPath="form.toLatDeg" minutesLocationPath="form.toLatMin" secondsLocationPath="form.toLatSec" />
            <@locationInput.locationInput degreesLocationPath="form.toLongDeg" minutesLocationPath="form.toLongMin" secondsLocationPath="form.toLongSec" direction="EW_MANUAL" directionPath="form.toLongDirection" directionList=longDirections />

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

        <#if viewMode == "UPDATE">
            <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Update pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
        <#elseif viewMode == "NEW">
            <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add new pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
        </#if>

    </@fdsForm.htmlForm>
</@defaultPage>