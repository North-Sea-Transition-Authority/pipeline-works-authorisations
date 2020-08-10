<#include '../layout.ftl'>
<#import '_applicationWorkAreaItem.ftl' as applicationWorkAreaItem>

<#-- @ftlvariable name="workAreaPageView" type="uk.co.ogauthority.pwa.mvc.PageView<uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem>" -->

<#macro tab workAreaPageView>

    <#if workAreaPageView?has_content>

      <#if !workAreaPageView.getPageContent()?has_content>
          <@fdsInsetText.insetText>You have no open applications.</@fdsInsetText.insetText>
      <#else>
        <table class="govuk-table">
          <thead class="govuk-table__head">
          <tr class="govuk-table__row">

            <@applicationWorkAreaItem.referenceColumnHeader />
            <th class="govuk-table__header" scope="col">Application type</th>
            <th class="govuk-table__header" scope="col">Master PWA reference</th>
            <@applicationWorkAreaItem.summaryColumnHeader />
            <th class="govuk-table__header" scope="col">Application status</th>

          </tr>
          </thead>
          <tbody class="govuk-table__body">
          <#list workAreaPageView.getPageContent() as item>

            <tr class="govuk-table__row">
              <td class="govuk-table__cell">
                  <@applicationWorkAreaItem.referenceColumn item=item noRefText="Resume draft PWA" />
              </td>
              <td class="govuk-table__cell"> ${item.getApplicationTypeDisplay()}</td>
              <td class="govuk-table__cell"> ${item.getMasterPwaReference()}</td>
              <td class="govuk-table__cell">
                <@applicationWorkAreaItem.summaryColumn item=item />
              </td>
              <td class="govuk-table__cell">
                <ul class="govuk-list">
                  <@applicationWorkAreaItem.statusLabelListItem>
                    ${item.getApplicationStatusDisplay()}
                  </@applicationWorkAreaItem.statusLabelListItem>
                    <#if item.caseOfficerName?has_content>
                      <li>Case officer: ${item.caseOfficerName}</li>
                    </#if>
                  <li>Status set: ${item.getFormattedStatusSetDatetime()}</li>
                  <@applicationWorkAreaItem.fastTrackLabelListItem item=item />
                </ul>
              </td>
            </tr>
          </#list>

          </tbody>
        </table>

        <@fdsPagination.pagination pageView=workAreaPageView />

      </#if>

    </#if>

</#macro>