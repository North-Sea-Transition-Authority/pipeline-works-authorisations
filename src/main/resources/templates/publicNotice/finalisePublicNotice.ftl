<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->


<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} finalise public notice" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l">Finalise public notice</h2>


    <@fdsForm.htmlForm>

        <@fdsInsetText.insetText>
            You must ensure that you provide the finalised public notice document to the OGA content team for publishing on the OGA website.
        </@fdsInsetText.insetText>

        <@fdsDateInput.dateInput dayPath="form.startDay" monthPath="form.startMonth" yearPath="form.startYear" labelText="Public notice start date" formId="form.start"/>
    
        <@fdsTextInput.textInput path="form.daysToBePublishedFor" labelText="How many calendar days will the public notice be published for" inputClass="govuk-input--width-4"
            hintText="The calendar days entered must account for public holidays that occur during the period for which the public notice will be published." />

        <@fdsAction.submitButtons primaryButtonText="Finalise public notice" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>


</@defaultPage>
