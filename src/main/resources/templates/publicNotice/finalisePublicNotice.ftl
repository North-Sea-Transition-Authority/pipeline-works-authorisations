<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="appRef" type="java.lang.String" -->
<#-- @ftlvariable name="cancelUrl" type="java.lang.String" -->
<#-- @ftlvariable name="publicNoticeAction" type="uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeAction" -->


<#include '../layout.ftl'>

<#assign headerText = "Finalise public notice" submitText = "Finalise public notice"/>
<#if publicNoticeAction == "UPDATE_DATES">
    <#assign headerText = "Update publication dates" submitText = "Update publication dates"/>
</#if>

<@defaultPage htmlTitle="${appRef} ${headerText?lower_case}" topNavigation=true twoThirdsColumn=false breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList/>
    </#if>

    <@pwaCaseSummary.summary caseSummaryView=caseSummaryView />

    <h2 class="govuk-heading-l">${headerText}</h2>


    <@fdsForm.htmlForm>

        <#if publicNoticeAction == "FINALISE">
            <@grid.gridRow>
                <@grid.twoThirdsColumn>
                    <@fdsInsetText.insetText>
                        You must ensure that you provide the finalised public notice document to the NSTA content team for publishing on the NSTA website.
                    </@fdsInsetText.insetText>
                </@grid.twoThirdsColumn>
            </@grid.gridRow>
        </#if>

        <@fdsDateInput.dateInput dayPath="form.startDay" monthPath="form.startMonth" yearPath="form.startYear" labelText="Public notice start date" formId="form.start"/>
        
        <@grid.gridRow>
            <@grid.twoThirdsColumn>
                <@fdsTextInput.textInput path="form.daysToBePublishedFor" labelText="How many calendar days will the public notice be published for?" inputClass="govuk-input--width-4"
                    hintText="The calendar days entered must account for public holidays that occur during the period for which the public notice will be published." />
            </@grid.twoThirdsColumn>
        </@grid.gridRow>

        <@fdsAction.submitButtons primaryButtonText=submitText linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>


</@defaultPage>
