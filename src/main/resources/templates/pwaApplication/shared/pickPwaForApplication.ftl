<#include '../../layout.ftl'>

<@defaultPage htmlTitle="Start PWA application">

    <@fdsInsetText.insetText>Select PWA to link to application</@fdsInsetText.insetText>

    <@fdsError.errorSummary errorItems=errorList />

    <@fdsForm.htmlForm>

        <@fdsSelect.select path="form.masterPwaId" labelText="" options=selectablePwaMap />

        <@fdsAction.submitButtons primaryButtonText="Continue" linkSecondaryAction=true secondaryLinkText="Back to workarea" linkSecondaryActionUrl=springUrl(workareaUrl) />


    </@fdsForm.htmlForm>

</@defaultPage>