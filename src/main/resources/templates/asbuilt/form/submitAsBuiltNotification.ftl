<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../../layout.ftl'>
<#include '../../components/asBuiltSummary/notificationGroupSummary.ftl'>

<@defaultPage htmlTitle="${pipelineNumber} as-built notification" topNavigation=true fullWidthColumn=true breadcrumbs=true>

    <@fdsError.errorSummary errorItems=errorList />

    <@summary notificationGroupSummaryView />

    <@grid.gridRow>
        <@grid.twoThirdsColumn>
            <h2 class="govuk-heading-l">Submit as-built notification for ${pipelineNumber}</h2>

            <div class="govuk-inset-text">
                The consented status of the pipeline is "${consentedPipelineStatus}".
            </div>

            <@fdsForm.htmlForm>

                <@fdsRadio.radioGroup path="form.asBuiltNotificationStatus" labelText="Describe the status of the pipeline" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--s" hiddenContent=true hintText="If previously you have submitted an 'as-built' in error, select 'Not provided'.">
                    <#assign firstItem=true/>
                    <#list asBuiltStatusOptions as asBuiltStatusOption>
                        <@fdsRadio.radioItem path="form.asBuiltNotificationStatus" itemMap={asBuiltStatusOption : asBuiltStatusOption.getDisplayName()} isFirstItem=firstItem>
                            <#if asBuiltStatusOption == "PER_CONSENT">
                                <@fdsDatePicker.datePicker path="form.perConsentDateWorkCompletedTimestampStr" nestingPath="form.asBuiltNotificationStatus" labelText="Date work completed" labelClass="govuk-label--s"/>
                            <#elseif asBuiltStatusOption == "NOT_PER_CONSENT">
                                <@fdsDatePicker.datePicker path="form.notPerConsentDateWorkCompletedTimestampStr" nestingPath="form.asBuiltNotificationStatus" labelText="Date work completed" labelClass="govuk-label--s"/>
                            <#elseif asBuiltStatusOption == "NOT_COMPLETED_IN_CONSENT_TIMEFRAME">
                                <@fdsDatePicker.datePicker path="form.notInConsentTimeframeDateWorkCompletedTimestampStr" nestingPath="form.asBuiltNotificationStatus" labelText="Estimated date work will be completed" labelClass="govuk-label--s"/>
                            </#if>
                            <#if pipelineChangeCategory == "NEW_PIPELINE">
                                <#if asBuiltStatusOption == "PER_CONSENT">
                                    <@fdsDatePicker.datePicker path="form.perConsentDateBroughtIntoUseTimestampStr" nestingPath="form.asBuiltNotificationStatus" labelText="Date pipeline was/will be brought into use" labelClass="govuk-label--s"/>
                                <#elseif asBuiltStatusOption == "NOT_PER_CONSENT">
                                    <@fdsDatePicker.datePicker path="form.notPerConsentDateBroughtIntoUseTimestampStr" nestingPath="form.asBuiltNotificationStatus" labelText="Date pipeline was/will be brought into use" labelClass="govuk-label--s"/>
                                </#if>
                            </#if>
                        </@fdsRadio.radioItem>
                        <#assign firstItem=false/>
                    </#list>
                </@fdsRadio.radioGroup>

                <#if isOgaUser = true>
                    <@fdsTextarea.textarea path="form.ogaSubmissionReason" labelText="Why is the NSTA submitting on behalf of the Holder?" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </#if>

                <@fdsAction.submitButtons primaryButtonText="Submit" linkSecondaryAction=true secondaryLinkText="Back to as-built notifications" linkSecondaryActionUrl=springUrl(cancelUrl)/>

            </@fdsForm.htmlForm>
        </@grid.twoThirdsColumn>
    </@grid.gridRow>

</@defaultPage>