<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Median line agreements" breadcrumbs=true>
    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.agreementStatus" labelText="Will the proposed works cross the median line?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl" hiddenContent=true>
            <#assign firstItem=true/>
            <#list crossingOptions as name, displayText>
                <@fdsRadio.radioItem path="form.agreementStatus" itemMap={name:displayText} isFirstItem=firstItem>
                    <#if name == "NEGOTIATIONS_ONGOING">
                        <@fdsTextInput.textInput path="form.negotiatorNameIfOngoing" labelText="Name of negotiator" nestingPath="form.agreementStatus"/>
                        <@fdsTextInput.textInput path="form.negotiatorEmailIfOngoing" labelText="Email address of negotiator" nestingPath="form.agreementStatus"/>
                    <#elseif name == "NEGOTIATIONS_COMPLETED">
                        <@fdsTextInput.textInput path="form.negotiatorNameIfCompleted" labelText="Name of negotiator" nestingPath="form.agreementStatus"/>
                        <@fdsTextInput.textInput path="form.negotiatorEmailIfCompleted" labelText="Email address of negotiator" nestingPath="form.agreementStatus"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
    </@fdsForm.htmlForm>
</@defaultPage>