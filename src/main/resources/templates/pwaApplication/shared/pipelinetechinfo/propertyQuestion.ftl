<#include '../../../layout.ftl'>
<#include 'minMaxInput.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="property" type="java.util.List<OtherPipelineProperty>" -->
<#-- @ftlvariable name="propertyAvailabilityOptions" type="java.util.List<PropertyAvailabilityOption>" -->


<#macro propertyQuestion property propertyAvailabilityOptions>
    <@fdsRadio.radioGroup path="form.propertyDataFormMap[${property}].propertyAvailabilityOption" labelText=stringUtils.subscriptConverter(property.getDisplayText()) hiddenContent=true>
        <#assign firstItem=true/>
        <#list propertyAvailabilityOptions as  propertyAvailabilityOption>
            <@fdsRadio.radioItem path="form.propertyDataFormMap[${property}].propertyAvailabilityOption" itemMap={propertyAvailabilityOption : propertyAvailabilityOption.getDisplayText()} isFirstItem=firstItem>
                <#if propertyAvailabilityOption == "AVAILABLE">
                    <@minMaxInput minFormPath="form.propertyDataFormMap[${property}].minMaxInput.minValue" maxFormPath="form.propertyDataFormMap[${property}].minMaxInput.maxValue"
                    nestedPath="form.propertyDataFormMap[${property}].propertyAvailabilityOption" labelText="Provide the minimum and maximum measurement for ${property.getDisplayText()?lower_case}" unitMeasurement=property.getUnitMeasurement()/>
                </#if>
            </@fdsRadio.radioItem>
        <#assign firstItem=false/>
        </#list>    
    </@fdsRadio.radioGroup>

</#macro>

