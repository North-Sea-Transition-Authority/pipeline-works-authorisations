<#include '../../../layout.ftl'>
<#include 'propertyQuestion.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="properties" type="java.util.List<OtherPipelineProperty>" -->
<#-- @ftlvariable name="propertyAvailabilityOptions" type="java.util.List<PropertyAvailabilityOption>" -->



<@defaultPage htmlTitle="Other properties" pageHeading="What other properties affecting pipeline design are available/present?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>


    <@fdsForm.htmlForm>

        <#list properties as property>
            <@propertyQuestion property=property propertyAvailabilityOptions=propertyAvailabilityOptions />
        </#list>        
        
        
        <h3 class="govuk-heading-m">Phases present</h3>
        <@fdsCheckbox.checkbox path="form.oilPresent" labelText="Oil"/>
        <@fdsCheckbox.checkbox path="form.condensatePresent" labelText="Condensate"/>
        <@fdsCheckbox.checkbox path="form.gasPresent" labelText="Gas"/>
        <@fdsCheckbox.checkbox path="form.waterPresent" labelText="Water"/>
        <@fdsCheckbox.checkbox path="form.otherPresent" labelText="Other"/>
        <@fdsTextInput.textInput path="form.otherPhaseDescription" labelText="Provide other phase present" nestingPath="form.otherPresent"/>



        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>
