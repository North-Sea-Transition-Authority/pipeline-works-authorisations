<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../../layout.ftl'>
<#include '../../components/asBuiltSummary/notificationGroupSummary.ftl'>

<@defaultPage htmlTitle="Change ${asBuiltGroupReference} deadline" topNavigation=true fullWidthColumn=true breadcrumbs=true>

    <@fdsError.errorSummary errorItems=errorList />
    
    <@summary notificationGroupSummaryView />

    <br>
    
    <@fdsForm.htmlForm>
        <@fdsDatePicker.datePicker path="form.newDeadlineDateTimestampStr" labelText="New deadline date for as-built notification group" labelClass="govuk-label--m"/>

        <@fdsAction.submitButtons primaryButtonText="Submit" linkSecondaryAction=true secondaryLinkText="Back to as-built notifications" linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>