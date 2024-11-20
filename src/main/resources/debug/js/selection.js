// noinspection JSUnusedGlobalSymbols,JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */
const FILTER_SUFFIX = '-data-filter';

/**
 * Updates the displayed content within a specific panel based on the selected format and option.
 *
 * @param {string} panelId - The ID of the panel to update.
 */
function updatePanelContent(panelId) {
    let format;

    // Force 'json' format for the conflicts panel.
    if (panelId === 'type-schema-conflicts') {
        format = 'json';
    } else {
        // Get the selected format from localStorage.
        format = localStorage.getItem(`${panelId}-activeFormat`) || 'yaml';
        localStorage.setItem(`${panelId}-activeFormat`, format);
    }

    // Retrieve and set the selected format's data
    const filterElementId = `${panelId}${FILTER_SUFFIX}`;
    const selectElement = document.getElementById(filterElementId);
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const data = selectedOption.getAttribute(`data-${format}`);

    // Update content only if panel exists
    const panelElement = document.getElementById(panelId);
    const codeElement = panelElement?.querySelector('code');
    if (!codeElement) return;

    // Set the content and apply the language class
    codeElement.textContent = data;
    codeElement.className = ''; // Reset any previous classes
    codeElement.classList.add(`language-${format === 'yaml' ? 'yaml' : 'json'}`);

    // Re-highlight
    Prism.highlightElement(codeElement);
}

/**
 * Initializes event listeners for all filter dropdowns once the DOM content is fully loaded.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Select all elements used for filtering data.
    const filterElements = document.querySelectorAll(`[id$="${FILTER_SUFFIX}"]`);

    // Iterate over each filter ID to set up its corresponding event listener.
    filterElements.forEach(selectElement => {
        // Get the dropdown (select) element by its ID,
        // and derive the panel ID by removing the '-data-filter' suffix from the dropdown ID.
        const panelId = selectElement.id.replace(FILTER_SUFFIX, '');

        // Add a 'change' event listener to handle user selection changes.
        selectElement.addEventListener('change', function () {
            // Invoke the updatePanelContent function to update the panel's displayed content.
            updatePanelContent(panelId);
        });
    });
});
