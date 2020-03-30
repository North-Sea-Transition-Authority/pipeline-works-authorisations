<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true>
  <h2 class="govuk-heading-l">Median line agreement</h2>
    <#if medianLineCrossing?has_content>
        <@fdsAction.link linkText="Update median line agreement" linkUrl=springUrl(medianLineUrl) role=true linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Will the proposed works cross the median line?</th>
          <td class="govuk-table__cell">Not crossed</td>
        </tr>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          You must provide information regarding median line crossing agreements
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Provide median line agreement information" linkUrl=springUrl(medianLineUrl) role=true linkClass="govuk-button govuk-button--blue"/>
    </#if>
</@defaultPage>