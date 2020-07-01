<#include '../../../layout.ftl'>
<#include 'propertyQuestion.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="properties" type="java.util.List<OtherPipelineProperty>" -->
<#-- @ftlvariable name="propertyAvailabilityOptions" type="java.util.List<PropertyAvailabilityOption>" -->  
<#-- @ftlvariable name="propertyPhases" type="java.util.LinkedHashMap<PropertyPhase, String>" --> 



<@defaultPage htmlTitle="Other properties" pageHeading="What other properties affecting pipeline design are available/present?" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>


    <@fdsForm.htmlForm>

        <#list properties as property>
            <@propertyQuestion property=property propertyAvailabilityOptions=propertyAvailabilityOptions />
        </#list>        
        

        <@fdsCheckbox.checkboxGroup path="form.phasesSelection" hiddenContent=true>
            <#list propertyPhases as  propertyPhase>
                <@fdsCheckbox.checkboxItem path="form.phasesSelection[${propertyPhase}]" labelText=propertyPhase.getDisplayText() >   

                    <#if propertyPhase == "OTHER">
                        <@fdsTextInput.textInput path="form.otherPhaseDescription" labelText="Provide other phase present" nestingPath="form.phasesSelection[${propertyPhase}]" />
                    </#if>
                    
                </@fdsCheckbox.checkboxItem>
            </#list>    
        </@fdsCheckbox.checkboxGroup>



        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>

</@defaultPage>
