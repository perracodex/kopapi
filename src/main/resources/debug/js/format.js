// noinspection JSUnusedGlobalSymbols

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

/**
 * Switches the content of a panel based on the selected format.
 * @param panelId The ID of the panel to update.
 * @param format The selected format to display.
 */
function switchContent(panelId, format) {
    const panel = document.getElementById(panelId);
    const buttons = panel.querySelectorAll('.panel-action');

    // Remove active class from all buttons.
    buttons.forEach(button => button.classList.remove('active'));

    // Set the active class to the clicked button.
    const activeButton = panel.querySelector(`.${format}-button`);
    if (activeButton) {
        activeButton.classList.add('active');
    }

    // Store the selected format in localStorage.
    localStorage.setItem(`${panelId}-activeFormat`, format);

    // Update the content.
    updatePanelContent(panelId);
}

/**
 * Restores the active button states for each panel on page load.
 */
function restoreButtonStates() {
    const panels = document.querySelectorAll('.panel');
    panels.forEach(panel => {
        const panelId = panel.id;

        // Check if the panel has format buttons.
        const formatButtons = panel.querySelectorAll('.panel-action');
        if (formatButtons.length === 0) {
            return;
        }

        let activeFormat = localStorage.getItem(`${panelId}-activeFormat`);

        // If no format is stored, set default to 'yaml'.
        if (!activeFormat) {
            activeFormat = 'yaml';
            localStorage.setItem(`${panelId}-activeFormat`, activeFormat);
        }

        const activeButton = panel.querySelector(`.${activeFormat}-button`);
        if (activeButton) {
            activeButton.classList.add('active');
        }

        // Update the content.
        updatePanelContent(panelId);
    });
}

document.addEventListener('DOMContentLoaded', restoreButtonStates);
