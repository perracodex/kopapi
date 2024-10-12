// noinspection JSUnusedGlobalSymbols
// noinspection JSUnusedGlobalSymbols
// noinspection JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

/**
 * Filters JSON content based on the selected value from the dropdown.
 *
 * @param {Array} jsonData - The array of JSON objects to be filtered.
 * @param {string} filterValue - The selected value from the dropdown to filter the JSON data.
 * @param {string} key - The key within the JSON object to filter by (e.g., 'path', 'name').
 * @returns {Array} - Returns the filtered JSON data or the full array if 'All' is selected.
 */
function filterJsonContent(jsonData, filterValue, key) {
    if (filterValue === "All") {
        return jsonData;
    }
    return jsonData.filter(item => item[key] === filterValue);
}

/**
 * Updates the displayed JSON content within a specific panel.
 *
 * @param {string} panelId - The ID of the panel where the filtered JSON will be displayed.
 * @param {Array} filteredData - The filtered JSON data to display in the panel.
 */
function updateDisplayedJson(panelId, filteredData) {
    const pre = document.getElementById(panelId).querySelector('code');
    pre.textContent = JSON.stringify(filteredData, null, 2); // Pretty-print the JSON data.
    Prism.highlightElement(pre); // Re-apply syntax highlighting.
}

/**
 * Sets up event listeners for dropdowns to filter JSON data based on user selection.
 * When the user selects an option from a dropdown, the corresponding panel will be updated with the filtered JSON data.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Add event listeners to dropdowns for filtering.
    ['routes-api-metadata-filter', 'objects-schemas-filter', 'schema-conflicts-filter'].forEach(filterId => {
        document.getElementById(filterId).addEventListener('change', function () {
            const selectedOption = this.options[this.selectedIndex];
            const jsonString = selectedOption.value; // Get the hidden JSON data (key value or "All").
            const parsedJson = JSON.parse(jsonString); // Parse the hidden JSON string back to an object.

            const panelId = filterId.replace('-filter', '');
            updateDisplayedJson(panelId, [parsedJson]); // Update the displayed JSON with the selected data.
        });
    });
});
