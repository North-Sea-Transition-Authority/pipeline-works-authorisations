<#include '../../../layout.ftl'>
<#import 'blockCrossingsManagement.ftl' as blockCrossingManagement>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="blockCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="blockCrossingDocumentsUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Crossing agreements" pageHeading="Crossing agreements" breadcrumbs=true>

  <@blockCrossingManagement.blockCrossingManagement blockCrossings=blockCrossings blockCrossingFileViews=blockCrossingFiles urlFactory=blockCrossingUrlFactory />

  <h2 class="govuk-heading-l">Median line agreement</h2>
    <#if medianLineAgreementView?has_content>
        <@fdsAction.link linkText="Update median line agreement" linkUrl=springUrl(medianLineUrl) role=true linkClass="govuk-button govuk-button--blue"/>
      <table class="govuk-table">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Status</th>
          <td class="govuk-table__cell">${medianLineAgreementView.agreementStatus.displayText}</td>
        </tr>
        <#if medianLineAgreementView.agreementStatus != "NOT_CROSSED">
          <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="col">Name of negotiator</th>
            <td class="govuk-table__cell">${medianLineAgreementView.negotiatorName!}</td>
          </tr>
          <tr class="govuk-table__row">
            <th class="govuk-table__header" scope="col">Contact email for negotiator</th>
            <td class="govuk-table__cell">${medianLineAgreementView.negotiatorEmail!}</td>
          </tr>
        </#if>
        </tbody>
      </table>
    <#else>
        <@fdsInsetText.insetText>
          You must provide information regarding median line crossing agreements
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Provide median line agreement information" linkUrl=springUrl(medianLineUrl) role=true linkClass="govuk-button govuk-button--blue"/>
    </#if>
</@defaultPage>