<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="unitMeasurement" type="java.lang.String" -->


<#macro minMaxInput minFormPath maxFormPath nestedPath unitMeasurement labelText="" altMinLabel="min" altMaxLabel="max" formId="">
    <@fdsNumberInput.twoNumberInputs pathOne=minFormPath pathTwo=maxFormPath formId="${formId}-min-max-values" nestingPath=nestedPath labelText=labelText hintText="If a single value, provide as both the ${altMinLabel} and ${altMaxLabel}">
        <@fdsNumberInput.numberInputItem path=minFormPath labelText=altMinLabel inputClass="govuk-input--width-5" suffixScreenReaderPrompt=unitMeasurement.suffixScreenReaderDisplay/>
        <@fdsNumberInput.numberInputItem path=maxFormPath labelText=altMaxLabel inputClass="govuk-input--width-5" suffix=stringUtils.superscriptConverter(unitMeasurement.suffixDisplay) suffixScreenReaderPrompt=unitMeasurement.suffixScreenReaderDisplay/>
    </@fdsNumberInput.twoNumberInputs>
</#macro>

