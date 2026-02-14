package esprit.farouk.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Temporary HTTP server to handle OAuth callback
 */
public class OAuthCallbackServer {

    private HttpServer server;
    private String authorizationCode;
    private String error;
    private CountDownLatch latch;
    private static final int PORT = 3000;

    /**
     * Starts the server and waits for OAuth callback
     * @param timeoutSeconds Maximum time to wait for callback
     * @return Authorization code or null if timeout/error
     */
    public String waitForCallback(int timeoutSeconds) {
        try {
            latch = new CountDownLatch(1);

            // Create HTTP server on localhost only
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
            server.createContext("/oauth/callback", new CallbackHandler());
            server.setExecutor(null);

            System.out.println("✓ Starting OAuth callback server on http://127.0.0.1:" + PORT);
            server.start();
            System.out.println("✓ OAuth callback server is listening...");

            // Wait for callback or timeout
            boolean received = latch.await(timeoutSeconds, TimeUnit.SECONDS);

            if (!received) {
                System.err.println("✗ OAuth callback timeout after " + timeoutSeconds + " seconds");
                stopServer();
                return null;
            }

            // Stop server after receiving callback
            stopServer();

            if (error != null) {
                System.err.println("✗ OAuth error: " + error);
                return null;
            }

            System.out.println("✓ Authorization code received successfully");
            return authorizationCode;

        } catch (Exception e) {
            System.err.println("✗ Failed to start OAuth callback server: " + e.getMessage());
            e.printStackTrace();
            stopServer();
            return null;
        }
    }

    /**
     * Stops the server
     */
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("✓ OAuth callback server stopped");
        }
    }

    /**
     * HTTP handler for OAuth callback
     */
    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("✓ Received OAuth callback request");

            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();

            System.out.println("  Request URI: " + requestURI);
            System.out.println("  Query string: " + query);

            if (query != null) {
                // Parse query parameters
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if ("code".equals(keyValue[0])) {
                            authorizationCode = keyValue[1];
                            System.out.println("  ✓ Authorization code extracted");
                        } else if ("error".equals(keyValue[0])) {
                            error = keyValue[1];
                            System.err.println("  ✗ Error in callback: " + error);
                        }
                    }
                }
            }

            // Send response to browser
            String response;
            if (authorizationCode != null) {
                response = buildSuccessPage();
            } else {
                response = buildErrorPage();
            }

            // Add CORS and security headers
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.getResponseHeaders().set("Pragma", "no-cache");
            exchange.getResponseHeaders().set("Expires", "0");

            byte[] responseBytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.flush();
            os.close();

            System.out.println("✓ Response sent to browser");

            // Signal that we received the callback
            latch.countDown();
        }

        private String buildSuccessPage() {
            return "<!DOCTYPE html>" +
                    "<html><head><title>Login Successful</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }" +
                    ".container { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); text-align: center; }" +
                    "h1 { color: #4caf50; margin: 0 0 20px 0; }" +
                    "p { color: #666; margin: 10px 0; }" +
                    ".icon { font-size: 80px; margin-bottom: 20px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='icon'>✅</div>" +
                    "<h1>Login Successful!</h1>" +
                    "<p>You have successfully signed in with Google.</p>" +
                    "<p><strong>You can close this window now.</strong></p>" +
                    "</div></body></html>";
        }

        private String buildErrorPage() {
            return "<!DOCTYPE html>" +
                    "<html><head><title>Login Failed</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }" +
                    ".container { background: white; padding: 40px; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); text-align: center; }" +
                    "h1 { color: #f44336; margin: 0 0 20px 0; }" +
                    "p { color: #666; margin: 10px 0; }" +
                    ".icon { font-size: 80px; margin-bottom: 20px; }" +
                    "</style></head><body>" +
                    "<div class='container'>" +
                    "<div class='icon'>❌</div>" +
                    "<h1>Login Failed</h1>" +
                    "<p>There was an error signing in.</p>" +
                    "<p><strong>You can close this window and try again.</strong></p>" +
                    "</div></body></html>";
        }
    }
}
