<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 


<#macro minMaxInput minFormPath maxFormPath nestedPath labelText>
    <@fdsNumberInput.twoNumberInputs pathOne=minFormPath pathTwo=maxFormPath formId="min-max-values" nestingPath=nestedPath labelText=labelText >
        <@fdsNumberInput.numberInputItem path=minFormPath labelText="min" inputClass="govuk-input--width-5"/>
        <@fdsNumberInput.numberInputItem path=maxFormPath labelText="max" inputClass="govuk-input--width-5"/>
    </@fdsNumberInput.twoNumberInputs>
</#macro>

