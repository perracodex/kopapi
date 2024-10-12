// noinspection JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

/**
 * Updates the displayed JSON content within a specific panel based on the selected dropdown value.
 *
 * @param {string} panelId - The ID of the panel to update. This corresponds to the panel's container element.
 */
function filterJsonContent(panelId) {
    // Retrieve the dropdown (select) element associated with the panel.
    const selectElement = document.getElementById(`${panelId}-filter`);
    if (!selectElement) {
        console.error(`Select element with ID '${panelId}-filter' not found.`);
        return;
    }

    // Get the currently selected option within the dropdown,
    // and extract the value of the selected option.
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    const selectedValue = selectedOption.value;

    // Locate the <code> element within the panel where JSON content is displayed.
    const panelElement = document.getElementById(panelId);
    if (!panelElement) {
        console.error(`Panel element with ID '${panelId}' not found.`);
        return;
    }
    const codeElement = panelElement.querySelector('code');
    if (!codeElement) {
        console.error(`Code element within panel '${panelId}' not found.`);
        return;
    }

    if (selectedValue === "ALL") {
        // Display all JSON data from the data-full-json attribute.
        codeElement.textContent = selectedOption.getAttribute('data-full-json');
    } else {
        // Display the selected JSON object.
        codeElement.textContent = selectedValue;
    }

    // Re-apply syntax highlighting using Prism.js to ensure the JSON is properly formatted.
    Prism.highlightElement(codeElement);
}

/**
 * Initializes event listeners for all filter dropdowns once the DOM content is fully loaded.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Array of dropdown element IDs that correspond to different JSON panels.
    // Each ID follows the pattern '<panelId>-filter'.
    const filterIds = [
        'routes-api-metadata-filter',
        'objects-schemas-filter',
        'schema-conflicts-filter'
    ];

    // Iterate over each filter ID to set up its corresponding event listener.
    filterIds.forEach(filterId => {
        // Get the dropdown (select) element by its ID,
        // and derive the panel ID by removing the '-filter' suffix from the dropdown ID.
        const selectElement = document.getElementById(filterId);
        const panelId = filterId.replace('-filter', '');

        // Add a 'change' event listener to handle user selection changes.
        selectElement.addEventListener('change', function () {
            // Invoke the filterJsonContent function to update the panel's displayed JSON.
            filterJsonContent(panelId);
        });
    });
});
