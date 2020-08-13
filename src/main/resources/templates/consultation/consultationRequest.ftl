<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} request consultations" pageHeading="${appRef} request consultations" topNavigation=true twoThirdsColumn=false>
  <@fdsError.errorSummary errorItems=errorList />

  <@fdsForm.htmlForm>


    <@fdsFieldset.fieldset legendHeading="Who do you want to consult?" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
      <@fdsCheckbox.checkboxGroup path="form.consulteeGroupSelection" hiddenContent=true>
          <#list consulteeGroups as  consulteeGroup>                   
              <#assign abbreviation = "" /> 
              <#if (consulteeGroup.abbreviation)?has_content>
                <#assign abbreviation = "(${(consulteeGroup.abbreviation)!})" />
              </#if>              
              <@fdsCheckbox.checkboxItem path="form.consulteeGroupSelection[${consulteeGroup.id}]" labelText="${consulteeGroup.name} ${abbreviation}"/>
          </#list>                        
      </@fdsCheckbox.checkboxGroup>
    </@fdsFieldset.fieldset>

    <@fdsTextInput.textInput path="form.daysToRespond" labelText="How many calendar days do they have to respond?" inputClass="govuk-input--width-4"/>



    <@fdsAction.submitButtons primaryButtonText="Send consultations" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>