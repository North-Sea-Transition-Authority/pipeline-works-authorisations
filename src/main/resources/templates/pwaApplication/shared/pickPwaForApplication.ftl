<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start PWA application">
    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>
        <@fdsSelect.select path="form.pickablePwaString" labelText="Select PWA to link to application" options=selectablePwaMap pageHeading=true labelHeadingClass="govuk-label--l" />
        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to workarea" linkSecondaryActionUrl=springUrl(workareaUrl) />
    </@fdsForm.htmlForm>

</@defaultPage>