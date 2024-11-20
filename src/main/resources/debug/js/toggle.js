// noinspection JSUnusedGlobalSymbols

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

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
        // Restore all panels except the popup panel.
        togglePanels(panels, false, panelId);
        // Restore the gap.
        document.querySelector('.panel-container').style.gap = '20px';
    } else {
        // Hide all other panels except the popup panel, and expand the target panel.
        togglePanels(panels, true, panelId);
        // Remove the gap when only one panel is visible.
        document.querySelector('.panel-container').style.gap = '0';
    }
}

/**
 * Toggles the visibility of panels based on the provided condition.
 *
 * @param {NodeListOf<Element>} panels - List of all panels to be toggled.
 * @param {boolean} hide - Whether to hide all panels except the target one.
 * @param {string} targetId - The ID of the panel that should be expanded.
 */
function togglePanels(panels, hide, targetId) {
    panels.forEach(panel => {
        const icon = panel.querySelector('.toggle-icon');

        if (!panel.closest('.popup-content')) {
            if (hide) {
                if (panel.id === targetId) {
                    panel.classList.add('expanded');
                    if (icon) icon.textContent = "-";
                } else {
                    panel.classList.add('hidden');
                }
            } else {
                panel.classList.remove('hidden', 'expanded');
                if (icon) icon.textContent = "+";
            }
        }
    });
}
