<#include '../layout.ftl'>

<@defaultPage htmlTitle="govuk pay journey start" pageHeading="govuk pay journey start" fullWidthColumn=true wrapperWidth=true>


    Actve: ${journeyUuid!""}
    </br>
    return url: ${returnUrl!""}

    <@fdsCard.card >
        <ol>
      <#list uuids as uuid>
        <li>${uuid}</li>

      </#list>
        </ol>
    </@fdsCard.card>


    <@fdsForm.htmlForm>

        <@fdsAction.button buttonText="Start govukPayment journey" buttonValue="" buttonName="" />
    </@fdsForm.htmlForm>


</@defaultPage>