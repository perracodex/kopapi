// noinspection JSUnusedGlobalSymbols,JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

const FILTER_SUFFIX = '-data-filter';

/**
 * Updates the displayed JSON content within a specific panel based on the selected dropdown value.
 *
 * @param {string} panelId - The ID of the panel to update. This corresponds to the panel's container element.
 */
function filterJsonContent(panelId) {
    // Retrieve the dropdown (select) element associated with the panel.
    const filterElementId = `${panelId}${FILTER_SUFFIX}`;
    const selectElement = document.getElementById(filterElementId);
    if (!selectElement) {
        console.error(`Select element with ID '${filterElementId}' not found.`);
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
 * Toggles the visibility of panels.
 * When a panel is expanded, other panels are hidden. Clicking again restores the original layout.
 *
 * @param {string} panelId - The ID of the panel to toggle.
 */
function togglePanel(panelId) {
    const panels = document.querySelectorAll('.panel');
    const targetPanel = document.getElementById(panelId);
    const toggleIcon = targetPanel.querySelector('.toggle-icon');

    if (!targetPanel || !toggleIcon) {
        console.error(`Panel or toggle icon with ID '${panelId}' not found.`);
        return;
    }

    const isExpanded = targetPanel.classList.contains('expanded');

    if (isExpanded) {
        // Restore all panels.
        panels.forEach(panel => {
            panel.classList.remove('hidden', 'expanded');
            const icon = panel.querySelector('.toggle-icon');
            if (icon) {
                icon.textContent = "+";
            }
        });
        // Restore the gap.
        document.querySelector('.panel-container').style.gap = '20px';
    } else {
        // Hide all other panels and expand the target.
        panels.forEach(panel => {
            if (panel.id === panelId) {
                panel.classList.add('expanded');
                const icon = panel.querySelector('.toggle-icon');
                if (icon) {
                    icon.textContent = "-";
                }
            } else {
                panel.classList.add('hidden');
            }
        });
        // Remove the gap when only one panel is visible.
        document.querySelector('.panel-container').style.gap = '0';
    }
}


/**
 * Initializes event listeners for all filter dropdowns once the DOM content is fully loaded.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Select all elements used for filtering JSON data.
    const filterElements = document.querySelectorAll(`[id$="${FILTER_SUFFIX}"]`);

    // Iterate over each filter ID to set up its corresponding event listener.
    filterElements.forEach(selectElement => {
        // Get the dropdown (select) element by its ID,
        // and derive the panel ID by removing the '-filter' suffix from the dropdown ID.
        // Derive the panel ID by removing the '-filter' suffix from the dropdown ID.
        const panelId = selectElement.id.replace(FILTER_SUFFIX, '');

        // Add a 'change' event listener to handle user selection changes.
        selectElement.addEventListener('change', function () {
            // Invoke the filterJsonContent function to update the panel's displayed JSON.
            filterJsonContent(panelId);
        });
    });
});
