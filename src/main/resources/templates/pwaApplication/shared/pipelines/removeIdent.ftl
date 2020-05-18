<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove ident" pageHeading="Are you sure you want to remove this ident?" breadcrumbs=true>

    <#assign from>
        <@pwaCoordinate.display coordinatePair=identView.fromCoordinates />
    </#assign>
    <#assign to>
        <@pwaCoordinate.display coordinatePair=identView.toCoordinates />
    </#assign>

  <dl class="govuk-summary-list govuk-!-margin-bottom-9">
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Ident number</dt>
      <dd class="govuk-summary-list__value">${identView.identNumber}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Length</dt>
      <dd class="govuk-summary-list__value">${identView.length}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From</dt>
      <dd class="govuk-summary-list__value">${identView.fromLocation}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">From (coordinates)</dt>
      <dd class="govuk-summary-list__value">${from}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To</dt>
      <dd class="govuk-summary-list__value">${identView.toLocation}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">To (coordinates)</dt>
      <dd class="govuk-summary-list__value">${to}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">External diameter</dt>
      <dd class="govuk-summary-list__value">${identView.externalDiameter}mm</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Internal diameter</dt>
      <dd class="govuk-summary-list__value">${identView.internalDiameter}mm</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Wall thickness</dt>
      <dd class="govuk-summary-list__value">${identView.wallThickness}mm</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">MAOP</dt>
      <dd class="govuk-summary-list__value">${identView.maop}barg</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Insulation / coating type</dt>
      <dd class="govuk-summary-list__value">${identView.insulationCoatingType}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Products to be conveyed</dt>
      <dd class="govuk-summary-list__value">${identView.productsToBeConveyed}</dd>
    </div>
    <div class="govuk-summary-list__row">
      <dt class="govuk-summary-list__key">Description of component parts</dt>
      <dd class="govuk-summary-list__value">${identView.componentPartsDescription}</dd>
    </div>
  </dl>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove ident" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>