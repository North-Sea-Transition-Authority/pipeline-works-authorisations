<#include '../../../layout.ftl'>
<#import '../../../components/utils/string.ftl' as string>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 
<#-- @ftlvariable name="chemicals" type="java.util.List<Chemicals>" -->
<#-- @ftlvariable name="fluidCompositionOptions" type="java.util.List<FluidCompositionOptions>" -->


<#macro fluidCompositionQuestion chemical fluidCompositionOptions>
    <@fdsRadio.radioGroup path="form.chemicalDataFormMap[${chemical}].fluidCompositionOption" labelText=string.subscriptConverter(chemical.getDisplayText()) hiddenContent=true>
        <#assign firstItem=true/>
        <#list fluidCompositionOptions as  fluidCompositionOption>
            <@fdsRadio.radioItem path="form.chemicalDataFormMap[${chemical}].fluidCompositionOption" itemMap={fluidCompositionOption : fluidCompositionOption.getDisplayText()} isFirstItem=firstItem>
                <#if fluidCompositionOption == "HIGHER_AMOUNT">
                    <@fdsTextInput.textInput path="form.chemicalDataFormMap[${chemical}].moleValue.value" nestingPath="form.chemicalDataFormMap[${chemical}].fluidCompositionOption"
                   labelText="" suffixScreenReaderPrompt="Provide mole %" suffix="mole %"  inputClass="govuk-input--width-5"/>
                </#if>
            </@fdsRadio.radioItem>
        <#assign firstItem=false/>
        </#list>    
    </@fdsRadio.radioGroup>

</#macro>

