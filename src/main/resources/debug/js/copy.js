// noinspection JSUnusedGlobalSymbols

/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an Apache 2.0 license.
 */

/**
 * Copy the content of the element with the given ID to the clipboard.
 * @param elementId
 */
function copyToClipboard(elementId) {
    const element = document.getElementById(elementId)?.querySelector('code');
    if (!element) {
        showNotification('Element not found', 'error');
        return;
    }

    const range = document.createRange();
    range.selectNodeContents(element);
    const selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);

    const textToCopy = element.textContent;

    if (navigator.clipboard) {
        navigator.clipboard.writeText(textToCopy)
            .then(() => {
                showNotification('Copied to clipboard', 'success');
            })
            .catch(() => {
                showNotification('Failed to copy', 'error');
            });
    } else {
        try {
            // noinspection JSDeprecatedSymbols
            document.execCommand('copy');
            showNotification('Copied to clipboard', 'success');
        } catch (e) {
            showNotification('Failed to copy', 'error');
        }
    }

    selection.removeAllRanges();
}

/**
 * Show a notification message.
 * @param message The message to display.
 * @param type The type of notification. Can be 'success' or 'error'.
 */
function showNotification(message, type) {
    // Create notification element
    const notification = document.createElement('div');
    notification.classList.add('notification', type);
    notification.textContent = message;
    document.body.appendChild(notification);

    // Automatically remove notification after 3 seconds
    setTimeout(() => {
        notification.remove();
    }, 3000);
}
