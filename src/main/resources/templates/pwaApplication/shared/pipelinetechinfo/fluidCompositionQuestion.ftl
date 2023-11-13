<#include '../../../layout.ftl'>
<#import '../../../components/utils/string.ftl' as string>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="chemicals" type="java.util.List<Chemicals>" -->
<#-- @ftlvariable name="resourceType" type="uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType" -->


<#macro fluidCompositionQuestion chemical resourceType>
    <@fdsRadio.radioGroup path="form.chemicalDataFormMap[${chemical}].chemicalMeasurementType" labelText=string.subscriptConverter(chemical.getDisplayText()) hiddenContent=true>
        <#assign firstItem=true/>
        <#list chemical.getApplicableMeasurementTypes(resourceType) as chemicalMeasurementType>
            <@fdsRadio.radioItem path="form.chemicalDataFormMap[${chemical}].chemicalMeasurementType" itemMap={chemicalMeasurementType : chemicalMeasurementType.getDisplayText()} isFirstItem=firstItem>
                <#if chemicalMeasurementType == "MOLE_PERCENTAGE">
                  <@fdsTextInput.textInput path="form.chemicalDataFormMap[${chemical}].measurementValue.value" nestingPath="form.chemicalDataFormMap[${chemical}].chemicalMeasurementType"
                  labelText="" suffixScreenReaderPrompt="Provide mole %" suffix="mole %"  inputClass="govuk-input--width-5"/>
                <#elseif chemicalMeasurementType.name()?contains("PPMV")>
                    <@fdsTextInput.textInput path="form.chemicalDataFormMap[${chemical}].measurementValue.value" nestingPath="form.chemicalDataFormMap[${chemical}].chemicalMeasurementType"
                    labelText="" suffixScreenReaderPrompt="Provide ppmv" suffix="ppmv"  inputClass="govuk-input--width-5"/>
                </#if>
            </@fdsRadio.radioItem>
        <#assign firstItem=false/>
        </#list>
    </@fdsRadio.radioGroup>

</#macro>

