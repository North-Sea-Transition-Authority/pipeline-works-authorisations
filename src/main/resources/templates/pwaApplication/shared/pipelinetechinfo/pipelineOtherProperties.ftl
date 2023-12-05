<#include '../../../layout.ftl'>
<#include 'propertyQuestion.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>"-->
<#-- @ftlvariable name="properties" type="java.util.List<OtherPipelineProperty>" -->
<#-- @ftlvariable name="propertyAvailabilityOptions" type="java.util.List<PropertyAvailabilityOption>" -->
<#-- @ftlvariable name="propertyPhases" type="java.util.LinkedHashMap<PropertyPhase, String>" -->

<@defaultPage htmlTitle="Other properties" pageHeading="What other properties affecting pipeline design are available/present?" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <#list properties as property>
            <@propertyQuestion property=property propertyAvailabilityOptions=propertyAvailabilityOptions />
        </#list>
        <#if resourceType == "CCUS">
          <@fdsRadio.radioGroup path="form.phase" labelText="Phases present" hiddenContent=true>
              <#assign firstItem=true/>
              <#list propertyPhases as propertyPhase>
                  <@fdsRadio.radioItem path="form.phase" itemMap={propertyPhase : propertyPhase.getDisplayText()} isFirstItem=firstItem>
                      <#if propertyPhase == "OTHER">
                          <@fdsTextInput.textInput path="form.otherPhaseDescription" labelText="Provide other phase present" nestingPath="form.phase" />
                      </#if>
                  </@fdsRadio.radioItem>
                  <#assign firstItem=false/>
              </#list>
          </@fdsRadio.radioGroup>
        <#else>
          <@fdsFieldset.fieldset legendHeading="Phases present" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
            <@fdsCheckbox.checkboxGroup path="form.phasesSelection" hiddenContent=true>
              <#list propertyPhases as  propertyPhase>
                <@fdsCheckbox.checkboxItem path="form.phasesSelection[${propertyPhase}]" labelText=propertyPhase.getDisplayText() >
                  <#if propertyPhase == "OTHER">
                    <@fdsTextInput.textInput path="form.otherPhaseDescription" labelText="Provide other phase present" nestingPath="form.phasesSelection[${propertyPhase}]" />
                  </#if>
                </@fdsCheckbox.checkboxItem>
              </#list>
            </@fdsCheckbox.checkboxGroup>
          </@fdsFieldset.fieldset>
        </#if>



        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
