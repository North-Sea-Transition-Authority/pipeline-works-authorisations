<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="unitMeasurement" type="java.lang.String" -->


<#macro minMaxInput minFormPath maxFormPath nestedPath labelText unitMeasurement altMinLabel altMaxLabel>

    <#assign minLabel="min" maxLabel="max"/>
    <#if !altMinLabel?matches("")>
        <#assign minLabel = altMinLabel/>
    </#if>
    <#if !altMaxLabel?matches("")>
        <#assign maxLabel = altMaxLabel/>
    </#if>
    

    <@fdsNumberInput.twoNumberInputs pathOne=minFormPath pathTwo=maxFormPath formId="min-max-values" nestingPath=nestedPath labelText=labelText hintText="If a single value, provide as both the " + minLabel + " and " + maxLabel>    
        <@fdsNumberInput.numberInputItem path=minFormPath labelText=minLabel inputClass="govuk-input--width-5"/>
        <@fdsNumberInput.numberInputItem path=maxFormPath labelText=maxLabel inputClass="govuk-input--width-5"/>
        <div class="govuk-date-input__item"><span class="govuk-label">${unitMeasurement}</span> </div>    
    </@fdsNumberInput.twoNumberInputs>


</#macro>

