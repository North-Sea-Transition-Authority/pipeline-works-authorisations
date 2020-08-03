<#include '../layout.ftl'>

<#macro tab workAreaPageView>

    <#if !workAreaPageView.getPageContent()?has_content>
        <@fdsInsetText.insetText>You have no open applications.</@fdsInsetText.insetText>
    <#else >
      <table class="govuk-table">
        <thead class="govuk-table__head">
        <tr class="govuk-table__row">

          <th class="govuk-table__header" scope="col">Application reference</th>
          <th class="govuk-table__header" scope="col">Application type</th>
          <th class="govuk-table__header" scope="col">Master PWA reference</th>
          <th class="govuk-table__header" scope="col">Summary</th>
          <th class="govuk-table__header" scope="col">Application status</th>

        </tr>
        </thead>
        <tbody class="govuk-table__body">
        <#list workAreaPageView.getPageContent() as item>

          <tr class="govuk-table__row">
            <td class="govuk-table__cell">
                <#assign viewLinkText=item.padReference?has_content?then(item.padReference, "Resume draft PWA") />
                <@fdsAction.link linkText=viewLinkText
                linkUrl=springUrl(item.viewApplicationUrl)
                linkClass="govuk-link govuk-link--no-visited-state"
                />
            </td>
            <td class="govuk-table__cell"> ${item.applicationType}</td>
            <td class="govuk-table__cell"> ${item.masterPwaReference}</td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                <li>Project Name: ${item.projectName!""}</li>
                <li>Proposed start date: ${item.getFormattedProposedStartDate()!""}</li>
                  <#if item.orderedFieldList?has_content>
                    <li>Field: ${item.orderedFieldList?join(", ")}</li>
                  </#if>
              </ul>
            </td>
            <td class="govuk-table__cell">
              <ul class="govuk-list">
                <li><strong class="govuk-tag govuk-tag--blue">${item.padDisplayStatus}</strong></li>
                  <#if item.caseOfficerName?has_content>
                    <li>Case officer: ${item.caseOfficerName}</li>
                  </#if>
                <li>Status set: ${item.getFormattedStatusSetDatetime()}</li>
                  <#if item.wasSubmittedAsFastTrack()>
                    <li><strong class="govuk-tag govuk-tag--${item.isFastTrackAccepted()?then("green", "red")}">${item.getFastTrackLabelText()}</strong></li>
                  </#if>
              </ul>
            </td>
          </tr>
        </#list>

        </tbody>
      </table>

      <@fdsPagination.pagination pageView=workAreaPageView />

    </#if>

</#macro>