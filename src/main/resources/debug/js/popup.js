// noinspection JSUnusedGlobalSymbols,JSUnresolvedReference

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

/**
 * Shows the popup by setting the overlay's display to flex and applying Prism syntax highlighting.
 */
function showPopup() {
    const overlay = document.getElementById('popup-overlay');
    if (overlay) {
        overlay.style.display = 'flex';

        const codeBlock = overlay.querySelector('code.language-json');
        if (codeBlock) {
            Prism.highlightElement(codeBlock);  // Apply syntax highlighting to the popup's code block
        } else {
            console.error("Code block not found for Prism.");
        }
    } else {
        console.error("Popup overlay not found.");
    }
}

/**
 * Hides the popup by setting the overlay's display to none.
 */
function hidePopup() {
    const overlay = document.getElementById('popup-overlay');
    if (overlay) {
        overlay.style.display = 'none';
    } else {
        console.error("Popup overlay not found.");
    }
}

/**
 * Closes the popup when clicking outside the popup content.
 */
document.addEventListener('click', function (event) {
    const overlay = document.getElementById('popup-overlay');
    const popupContent = document.getElementById('popup-content');
    const popupButton = document.getElementById('configuration-popup-button');

    // If the click is outside the popup content and not on the popup button, hide the popup
    if (overlay && popupContent && popupButton &&
        !popupContent.contains(event.target) &&
        event.target !== popupButton) {
        overlay.style.display = 'none';
    }
});
