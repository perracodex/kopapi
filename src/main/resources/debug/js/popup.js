// noinspection JSUnusedGlobalSymbols,JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

/**
 * Displays a popup for the specified panel by setting the overlay's display to 'flex'
 * and applying syntax highlighting to the code block within the popup.
 *
 * @param {string} panelId - The ID of the panel for which to show the popup.
 */
function showPopup(panelId) {
    const overlayId = `${panelId}-overlay`;
    const overlay = document.getElementById(overlayId);
    if (overlay) {
        overlay.style.display = 'flex';

        const codeBlock = overlay.querySelector('code');
        if (codeBlock) {
            // Determine the code format based on the panel ID
            const format = panelId === 'openapi-yaml-panel' ? 'yaml' : 'json';

            // Apply the format class
            codeBlock.className = ''; // Clear any existing classes
            codeBlock.classList.add(`language-${format}`);

            // Re-highlight the code block with the correct language
            Prism.highlightElement(codeBlock);
        } else {
            console.error("Code block not found for Prism.");
        }
    } else {
        console.error("Popup overlay not found for ID:", overlayId);
    }
}

/**
 * Hides the popup for the specified panel by setting the overlay's display to 'none'.
 *
 * @param {string} panelId - The ID of the panel for which to hide the popup.
 */
function hidePopup(panelId) {
    const overlayId = `${panelId}-overlay`;
    const overlay = document.getElementById(overlayId);
    if (overlay) {
        overlay.style.display = 'none';
    } else {
        console.error("Popup overlay not found for ID:", overlayId);
    }
}

/**
 * Event listener that hides popups when clicking out of the popup content.
 * It iterates over a list of panel IDs, checks if the click was outside the content area,
 * and hides the respective overlay if necessary.
 */
document.addEventListener('click', function (event) {
    const panels = ["configuration-panel", "openapi-yaml-panel", "openapi-json-panel"];
    panels.forEach(panelId => {
        const overlayId = `${panelId}-overlay`;
        const overlay = document.getElementById(overlayId);
        const content = overlay ? overlay.querySelector('.popup-content') : null;
        const button = document.getElementById(`${panelId}-button`);

        // Hide the popup if the click is outside the content and not on the associated button.
        if (overlay && content && button &&
            !content.contains(event.target) &&
            event.target !== button) {
            overlay.style.display = 'none';
        }
    });
});
