let DocgenStatusPoller = {
  contextPath: "",
  statusUrl: "",
  completeUrl: "",
  returnUrl: "",

  intervalId: null,

  startPolling: () => {
    DocgenStatusPoller.intervalId = setInterval(DocgenStatusPoller.getRunStatus, 2000)
  },

  getRunStatus: () => {
    $.get(DocgenStatusPoller.contextPath + DocgenStatusPoller.statusUrl)
      .done((data) => {
        DocgenStatusPoller.parseStatusResponse(data)
      })
      .fail(() => {
        DocgenStatusPoller.handleErrorResponse();
      });
  },

  parseStatusResponse: (data) => {
    if (data.status === 'COMPLETE') {
      clearInterval(DocgenStatusPoller.intervalId);
      let url = data.onCompleteUrl;
      // add fragment to auto trigger download request after page has loaded
      url += `#${data.docgenRunId}`;
      window.location.replace(DocgenStatusPoller.contextPath + url); // replace so the current page is NOT kept on the stack
    }
    else if (data.status === 'FAILED') {
      DocgenStatusPoller.handleErrorResponse();
    }
  },

  handleErrorResponse: () => {
    clearInterval(DocgenStatusPoller.intervalId);
    const errorMessage = `<h1 class="govuk-heading-xl">Your document failed to generate</h1>
                          <div class="govuk-form-group govuk-form-group--error">
                            <span class="govuk-error-message"><span class="govuk-visually-hidden">Error:</span>There was a problem generating your document.</span> <a class="govuk-link govuk-!-font-size-19" href="${DocgenStatusPoller.returnUrl}">Go back and try again.</a>
                          </div>`;

    // empty out the previous content and prepend error message
    $('#main-content').empty().prepend(errorMessage);
  }
};