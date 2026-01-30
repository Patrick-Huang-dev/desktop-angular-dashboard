/*
 *  Copyright 2025, TeamDev. All rights reserved.
 *
 *  Redistribution and use in source and/or binary forms, with or without
 *  modification, must retain the above copyright notice and the following
 *  disclaimer.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.teamdev.jxbrowser.angular;

import com.formdev.flatlaf.FlatDarkLaf;
import com.teamdev.jxbrowser.angular.backend.DashboardBackend;
import com.teamdev.jxbrowser.angular.production.UrlRequestInterceptor;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.browser.callback.InjectJsCallback;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.net.Scheme;
import com.teamdev.jxbrowser.view.swing.BrowserView;

import java.util.Optional;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.teamdev.jxbrowser.engine.RenderingMode.HARDWARE_ACCELERATED;
import static javax.swing.SwingUtilities.invokeLater;

/**
 * Initializes and configures JxBrowser, sets up the JavaScript-Java Bridge,
 * and displays the application window.
 */
public final class AppInitializer {

    private static final String APP_TITLE = "Angular Dashboard - JxBrowser";
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 800;

    /**
     * Initializes the application.
     */
    public void initialize() {
        // Setup dark theme for Swing UI (must be done before creating any UI components)
        setupLookAndFeel();

        Engine engine = createEngine();
        Browser browser = engine.newBrowser();

        setupJavaScriptBridge(browser);
        setupUI(engine, browser);

        // Load the Angular application
        browser.navigation().loadUrl(AppDetails.appUrl());
    }

    /**
     * Sets up the FlatLaf dark theme for Swing components.
     * This affects the window title bar and any native UI elements.
     */
    private void setupLookAndFeel() {
        try {
            // Force macOS to use dark appearance (must be set before FlatLaf.setup())
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.err.println("Failed to setup FlatLaf dark theme: " + e.getMessage());
        }
    }

    /**
     * Creates and configures the JxBrowser engine.
     */
    private Engine createEngine() {
        EngineOptions.Builder optionsBuilder = EngineOptions.newBuilder(HARDWARE_ACCELERATED)
                .userDataDir(AppDetails.INSTANCE.chromiumUserDataDir());

        // Set license key from system property or environment variable
        getLicenseKey().ifPresent(optionsBuilder::licenseKey);

        // In production mode, register custom scheme to load resources from JAR
        if (!AppDetails.isDevMode()) {
            Scheme scheme = Scheme.of(AppDetails.appScheme());
            optionsBuilder.addScheme(scheme, new UrlRequestInterceptor());
        }

        return Engine.newInstance(optionsBuilder.build());
    }

    /**
     * Gets the JxBrowser license key from system property or environment variable.
     */
    private Optional<String> getLicenseKey() {
        // First try system property
        String key = System.getProperty("jxbrowser.license.key");
        if (key != null && !key.isEmpty()) {
            return Optional.of(key);
        }
        // Then try environment variable
        key = System.getenv("JXBROWSER_LICENSE_KEY");
        if (key != null && !key.isEmpty()) {
            return Optional.of(key);
        }
        return Optional.empty();
    }

    /**
     * Sets up the JavaScript-Java Bridge to inject the backend object
     * into the browser's window object.
     */
    private void setupJavaScriptBridge(Browser browser) {
        DashboardBackend backend = new DashboardBackend();

        browser.set(InjectJsCallback.class, params -> {
            JsObject window = params.frame().executeJavaScript("window");
            if (window != null) {
                // Inject the Java backend object into window.backend
                // This makes it accessible from JavaScript as window.backend
                window.putProperty("backend", backend);
            }
            return InjectJsCallback.Response.proceed();
        });

        // Show DevTools in development mode for debugging
        if (AppDetails.isDevMode()) {
            browser.devTools().show();
        }
    }

    /**
     * Sets up the Swing UI with JxBrowser embedded.
     */
    private void setupUI(Engine engine, Browser browser) {
        invokeLater(() -> {
            JFrame frame = new JFrame(APP_TITLE);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Clean up engine when window is closed
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    engine.close();
                }
            });

            // Add the browser view to the frame
            BrowserView browserView = BrowserView.newInstance(browser);
            frame.add(browserView, BorderLayout.CENTER);

            // Set window size and position
            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setMinimumSize(new Dimension(800, 600));
            frame.setLocationRelativeTo(null); // Center on screen

            frame.setVisible(true);
        });
    }
}

