<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseSummaryView" type="uk.co.ogauthority.pwa.service.appprocessing.context.CaseSummaryView" -->

<#include '../layout.ftl'>

<#assign heading = "${caseSummaryView.pwaApplicationRef} request consultations" />

<@defaultPage htmlTitle=heading pageHeading="" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

  <@fdsError.errorSummary errorItems=errorList />

  <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

  <h2 class="govuk-heading-l">Request consultations</h2>

  <@fdsForm.htmlForm>

    <@fdsFieldset.fieldset legendHeading="Who do you want to consult?" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
      <@fdsCheckbox.checkboxGroup path="form.consulteeGroupSelection" hiddenContent=true>
          <#list consulteeGroups as consulteeGroup>
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