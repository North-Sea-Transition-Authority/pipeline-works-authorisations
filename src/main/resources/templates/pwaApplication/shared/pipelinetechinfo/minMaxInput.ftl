<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="unitMeasurement" type="java.lang.String" -->


<#macro minMaxInput minFormPath maxFormPath nestedPath labelText unitMeasurement>
    <@fdsNumberInput.twoNumberInputs pathOne=minFormPath pathTwo=maxFormPath formId="min-max-values" nestingPath=nestedPath labelText=labelText hintText="If a single value, provide as both the min and max">    
        <@fdsNumberInput.numberInputItem path=minFormPath labelText="min" inputClass="govuk-input--width-5"/>
        <@fdsNumberInput.numberInputItem path=maxFormPath labelText="max" inputClass="govuk-input--width-5"/>
        <div class="govuk-date-input__item"><span class="govuk-label">${unitMeasurement}</span> </div>    
    </@fdsNumberInput.twoNumberInputs>



</#macro>

