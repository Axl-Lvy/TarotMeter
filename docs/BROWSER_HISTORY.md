# Browser History Integration

## Overview

This document explains how browser history integration works in the TarotMeter web application.

## Implementation

### Navigation Controller

The application uses the Jetpack Navigation Compose library (version 2.9.0) for Kotlin Multiplatform, which provides automatic browser history integration on web platforms (JS/WASM).

The navigation controller is created using `rememberPlatformNavController()`, which is a platform-specific abstraction:

- **Web platforms (JS/WASM)**: Uses the browser's History API to synchronize navigation state with the URL
- **Other platforms (Android, iOS, JVM)**: Uses platform-specific navigation implementations

### How It Works

When the application is running on a web browser:

1. **URL Updates**: When you navigate from one screen to another (e.g., from Home to Players), the browser URL automatically updates to reflect the current route (e.g., from `/` to `/players`)

2. **Browser Back Button**: Clicking the browser's back button navigates to the previous screen in the app

3. **Browser Forward Button**: Clicking the browser's forward button navigates to the next screen (if you've gone back)

4. **Direct Navigation**: You can bookmark or share URLs to specific screens (e.g., `https://axl-lvy.github.io/TarotMeter/players` will open directly to the Players screen)

### GitHub Pages Configuration

For direct navigation to work on GitHub Pages, a `404.html` file is included that redirects any unknown routes back to `index.html`. This allows the application to handle routing client-side.

## Expected Behavior

After navigating through the app, you should see:

- URL changes in the address bar as you navigate (e.g., `/` → `/players` → `/new`)
- Browser back/forward buttons work to navigate through your history
- Sharing or bookmarking a URL allows direct navigation to that screen
- Page refreshes maintain your current location in the app

## Troubleshooting

If browser history integration isn't working as expected:

1. **Check the library version**: Ensure navigation-compose is version 2.9.0 or later
2. **Clear browser cache**: Sometimes cached JavaScript can cause issues
3. **Check browser console**: Look for any JavaScript errors that might prevent navigation from working
4. **Verify deployment**: Ensure the `404.html` file is deployed to GitHub Pages alongside `index.html`

## Technical Details

- **Library**: `org.jetbrains.androidx.navigation:navigation-compose:2.9.0`
- **Platform**: Web (Kotlin/JS and Kotlin/Wasm)
- **API**: Browser History API (pushState, replaceState, popstate events)
- **Routing Type**: Path-based (e.g., `/players` not `/#/players`)

## References

- [Jetbrains Navigation Compose](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html)
- [Browser History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API)
