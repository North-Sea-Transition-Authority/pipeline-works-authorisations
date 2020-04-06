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
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>