<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove ident" pageHeading="Are you sure you want to remove this ident?" breadcrumbs=true>

    <#assign from>
        <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
    </#assign>
    <#assign to>
        <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
    </#assign>

  <table class="govuk-table">
    <tbody class="govuk-table__body">
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Ident number</th>
      <td class="govuk-table__cell">${identView.identNumber}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Length</th>
      <td class="govuk-table__cell">${identView.length}m</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">From (coordinates)</th>
      <td class="govuk-table__cell">${from}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">To (coordinates)</th>
      <td class="govuk-table__cell">${to}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">External diameter</th>
      <td class="govuk-table__cell">${identView.externalDiameter}mm</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Internal diameter</th>
      <td class="govuk-table__cell">${identView.internalDiameter}mm</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Wall thickness</th>
      <td class="govuk-table__cell">${identView.wallThickness}mm</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">MAOP</th>
      <td class="govuk-table__cell">${identView.maop}barg</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Insulation / coating type</th>
      <td class="govuk-table__cell">${identView.insulationCoatingType}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Products to be conveyed</th>
      <td class="govuk-table__cell">${identView.productsToBeConveyed}</td>
    </tr>
    <tr class="govuk-table__row">
      <th class="govuk-table__header" scope="row">Description of component parts</th>
      <td class="govuk-table__cell">${identView.componentPartsDescription}</td>
    </tr>
    </tbody>
  </table>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove ident" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>