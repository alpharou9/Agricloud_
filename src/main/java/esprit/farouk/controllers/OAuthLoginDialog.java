package esprit.farouk.controllers;

import esprit.farouk.config.OAuthConfig;
import esprit.farouk.models.User;
import esprit.farouk.services.FacebookOAuthService;
import esprit.farouk.services.GoogleOAuthService;
import esprit.farouk.services.OAuthCallbackServer;
import esprit.farouk.services.UserService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.util.Map;

public class OAuthLoginDialog {

    private Stage dialog;
    private WebView webView;
    private WebEngine webEngine;
    private GoogleOAuthService googleOAuthService;
    private FacebookOAuthService facebookOAuthService;
    private UserService userService;
    private User authenticatedUser;
    private boolean success = false;
    private String provider; // "google" or "facebook"

    public OAuthLoginDialog() {
        this.googleOAuthService = new GoogleOAuthService();
        this.facebookOAuthService = new FacebookOAuthService();
        this.userService = new UserService();
    }

    /**
     * Shows Google OAuth login using system browser
     */
    public User showGoogleLogin(Stage owner) {
        this.provider = "google";
        return showOAuthLoginWithBrowser(owner, "Sign in with Google", googleOAuthService.getAuthorizationUrl());
    }

    /**
     * Shows Facebook OAuth login using system browser
     */
    public User showFacebookLogin(Stage owner) {
        this.provider = "facebook";
        return showOAuthLoginWithBrowser(owner, "Sign in with Facebook", facebookOAuthService.getAuthorizationUrl());
    }

    /**
     * Generic OAuth login using system browser
     */
    private User showOAuthLoginWithBrowser(Stage owner, String title, String authUrl) {
        if (authUrl == null) {
            showError("Failed to generate OAuth URL");
            return null;
        }

        // Create waiting dialog
        dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setWidth(400);
        dialog.setHeight(200);

        // Create waiting UI
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(30));

        ProgressIndicator progress = new ProgressIndicator();
        Label messageLabel = new Label("Opening browser...\n\nPlease sign in with your " +
                                      (provider.equals("google") ? "Google" : "Facebook") +
                                      " account in the browser window.");
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-alignment: center;");

        content.getChildren().addAll(progress, messageLabel);

        Scene scene = new Scene(content);
        dialog.setScene(scene);

        // Open browser and wait for callback in background thread
        new Thread(() -> {
            try {
                // Start callback server
                OAuthCallbackServer callbackServer = new OAuthCallbackServer();

                // Open browser
                Platform.runLater(() -> {
                    try {
                        Desktop.getDesktop().browse(new URI(authUrl));
                        System.out.println("✓ Opened browser for OAuth login");
                    } catch (Exception e) {
                        showError("Failed to open browser: " + e.getMessage());
                        e.printStackTrace();
                        dialog.close();
                    }
                });

                // Wait for callback (60 seconds timeout)
                String authCode = callbackServer.waitForCallback(60);

                if (authCode != null) {
                    // Process the authorization code
                    processAuthorizationCode(authCode);
                } else {
                    Platform.runLater(() -> {
                        showError("Login timeout or cancelled");
                        dialog.close();
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("OAuth error: " + e.getMessage());
                    e.printStackTrace();
                    dialog.close();
                });
            }
        }).start();

        // Show dialog and wait
        dialog.showAndWait();

        return success ? authenticatedUser : null;
    }

    /**
     * Processes the authorization code received from OAuth callback
     */
    private void processAuthorizationCode(String authCode) {
        try {
            String accessToken = null;
            Map<String, String> userInfo = null;

            // Get access token based on provider
            if ("google".equals(provider)) {
                accessToken = googleOAuthService.getAccessToken(authCode);
                if (accessToken != null) {
                    userInfo = googleOAuthService.getUserInfo(accessToken);
                }
            } else if ("facebook".equals(provider)) {
                accessToken = facebookOAuthService.getAccessToken(authCode);
                if (accessToken != null) {
                    userInfo = facebookOAuthService.getUserInfo(accessToken);
                }
            }

            if (accessToken == null) {
                Platform.runLater(() -> {
                    showError("Failed to get access token");
                    dialog.close();
                });
                return;
            }

            // Get user info
            if (userInfo == null) {
                Platform.runLater(() -> {
                    showError("Failed to get user info from " + provider);
                    dialog.close();
                });
                return;
            }

            // Create or update user in database
            String oauthId = userInfo.get("id");
            String email = userInfo.get("email");
            String name = userInfo.get("name");
            String picture = userInfo.get("picture");

            User user = userService.createOrUpdateOAuthUser(provider, oauthId, email, name, picture);

            if (user != null) {
                Platform.runLater(() -> {
                    authenticatedUser = user;
                    success = true;
                    System.out.println("✓ " + provider + " OAuth login successful: " + email);
                    dialog.close();
                });
            } else {
                Platform.runLater(() -> {
                    showError("Failed to create user account");
                    dialog.close();
                });
            }

        } catch (Exception e) {
            Platform.runLater(() -> {
                showError("OAuth error: " + e.getMessage());
                e.printStackTrace();
                dialog.close();
            });
        }
    }

    /**
     * Shows error alert
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("OAuth Error");
            alert.setHeaderText("Google Sign-In Failed");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
