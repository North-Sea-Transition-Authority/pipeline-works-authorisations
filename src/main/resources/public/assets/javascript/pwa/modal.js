// Modal - GOVUK Themed

// Setup a NAMESPACE
const IRS = IRS || {};

// Create the Modal object
IRS.Modal = {
  overlayID: 'modal-overlay',
  contentID: 'modal-content',
  htmlClass: 'has-modal',
  closeClass: 'close-modal',
  templateClass: 'modal-template',
  focusableElementsString: 'a[href], area[href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), button:not([disabled]), iframe, object, embed, *[tabindex], *[contenteditable]',
  $originalFocusedElement: null,

  /**
   * Displays a modal onscreen containing the content in $content
   *
   * @param $content The jQuery-wrapped element to display in the modal
   * @param ariaLabel The label for the modal, read out by screen readers but not shown onscreen
   * @param size The size of the modal. Is set to 'medium' by default
   */
  displayModal($content, ariaLabel, size) {
    "use strict";

    // Store reference to currently focused element
    IRS.Modal.$originalFocusedElement = $(document.activeElement);

    // Remove any existing modal
    IRS.Modal.closeModal();

    //Default to a medium-sized popup if size isn't specified
    if(typeof size === 'undefined' || size === '') { size = 'medium'; }

    // Build the new modal
    const $modal = $(`<div id="${IRS.Modal.overlayID}"> 
      <div id="modal" class="${size}-modal" role="dialog" aria-label="${ariaLabel}" aria-describedby="modal-aria-description"> 
      <div id="${IRS.Modal.contentID}" role="document"> 
      </div></div></div>`);
    const closePopoverButtonHidden = `<div id="modal-aria-description" class="govuk-visually-hidden">Press escape to close this popover</div>`;
    const closePopoverButton = `<button id="close-modal-fixed-button" class="${IRS.Modal.closeClass} govuk-button govuk-button--link" data-module="govuk-button">Close</a>`;

    $modal.find(`#${IRS.Modal.contentID}`).append(closePopoverButtonHidden);
    $modal.find(`#${IRS.Modal.contentID}`).append(closePopoverButton);
    $modal.find(`#${IRS.Modal.contentID}`).append($content);

    // Add the class that stops scrolling of the main page
    $('html').addClass(IRS.Modal.htmlClass);

    // Add the modal to the page
    $('body').append($modal);
    IRS.Modal.bindEvents();
    IRS.Modal.focusOnFirstElement();
  },

  /**
   * Displays a modal onscreen by cloning $template and replacing any content in {{double braces}} with the
   * corresponding value in templateParams
   *
   * @param $template The jQuery-wrapped element to use as the template for the modal
   * @param templateParams An object containing key-value pairs. Any instance of the key in double braces in the
   * content of the template will be replaced by the value specified in this object
   * @param ariaLabel The label for the modal, read out by screen readers but not shown onscreen
   */
  displayModalFromTemplate($template, templateParams, ariaLabel, size) {
    "use strict";

    // Clone the template
    const $content = $template.clone().removeClass(IRS.Modal.templateClass);

    // For each template param name, replace any instances of it in the template with the param value
    $.each(templateParams, function(key, value){
      const pattern = new RegExp('{{' + key + '}}', 'g');
      $content.html($content.html().replace(pattern, IRS.Modal.escapeHtml(value)));
    });

    // Show the modal
    IRS.Modal.displayModal($content, ariaLabel, size);
  },

  /**
   * Close the modal
   * @param event click event that triggered the close
   */
  closeModal(event) {
    "use strict";

    $(`#${IRS.Modal.overlayID}`).remove();
    $('html').removeClass(IRS.Modal.htmlClass);
    IRS.Modal.unbindEvents();

    // Refocus on element that was focused when the modal opened (probably the link/button that triggered it)
    IRS.Modal.$originalFocusedElement.get(0).focus();
  },

  /**
   * Sets focus to the first focusable element inside the modal
   */
  focusOnFirstElement() {
    "use strict";

    const $elms = IRS.Modal.getFocusableElements();

    if ($elms.length > 0) {
      $elms[0].focus();
    }
  },

  /**
   * Binds events associated with the modal
   * @private
   */
  bindEvents() {
    "use strict";

    // Keydown events
    $('body').on('keydown.IRS.Modal', (e) => {
      // Escape key closes modal
      if (e.which === 27) {
        IRS.Modal.closeModal();
      }

      // Trap tab key
      if (e.which === 9) {
        IRS.Modal.trapTabKey(e);
      }
    });

    // If anything other than the modal gets focus (eg tabbing from address bar), force focus into the modal
    $(`body>*[id!="${IRS.Modal.overlayID}"]`).on('focusin.IRS.Modal', (e) => {
      IRS.Modal.focusOnFirstElement();
      e.stopPropagation();
    });

    // Lets consumer define close links/buttons by giving them the class defined by closeClass
    $('body').on('click.IRS.Modal', `.${IRS.Modal.closeClass}`, () => {
      IRS.Modal.closeModal();
      return false;
    });
  },

  /**
   * Unbinds all events associated with this modal set by bindEvents()
   * @private
   */
  unbindEvents() {
    "use strict";

    $('*').off('.IRS.Modal');
  },

  /**
   * Ensures focus stays within the modal by forcing tab key to loop through elements in the modal
   * @param event keydown event
   * @private
   */
  trapTabKey(event) {
    "use strict";

    const $focusableElms = IRS.Modal.getFocusableElements();
    const $focusedElm = $(document.activeElement);

    const focusedElmIndex = $focusableElms.index($focusedElm);

    if (event.shiftKey && focusedElmIndex === 0) {
      // If we're going backwards (shift-tab) and we're at the first focusable element,
      // loop back to last focusable element
      $focusableElms.get($focusableElms.length-1).focus();
      event.preventDefault();
    } else if(!event.shiftKey && focusedElmIndex === $focusableElms.length-1) {
      // If we're going forwards and we're at the last focusable element,
      // loop forwards to the first focusable element
      $focusableElms.get(0).focus();
      event.preventDefault();
    }
    // Otherwise, allow the tab to proceed as normal because we're still in the group of
    // focusable elements in the modal
  },

  /**
   * Finds all of the focusable elements inside the modal
   * @returns {jQuery} collection of the focusable elements inside the modal
   * @private
   */
  getFocusableElements() {
    "use strict";

    return $(`#${IRS.Modal.contentID}`).find('*').filter(IRS.Modal.focusableElementsString).filter(':visible');
  },

  /**
   * Escapes a string by converting characters that could be part of HTML tags to entities
   * @param string The string to escape
   * @returns {string} the escaped string
   * @private
   */
  escapeHtml(string) {
    "use strict";

    const entityMap = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;',
      '/': '&#x2F;',
      '`': '&#x60;',
      '=': '&#x3D;'
    };

    return String(string).replace(/[&<>"'`=\/]/g, (s) => {
      return entityMap[s];
    });
  }
};