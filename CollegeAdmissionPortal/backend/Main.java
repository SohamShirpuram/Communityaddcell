import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * Class Name: Main
 * OOP & System Architecture Role:
 *   1. Driver / Entry Point: Contains main(String[] args) method to launch program.
 *   2. Web Server: Uses Core Java's built-in HttpServer (com.sun.net.httpserver).
 *      No external framework (no Spring Boot, Tomcat, or Node.js) required.
 *   3. Controller: Routes incoming web requests to AdmissionSystem and returns responses.
 * ============================================================================
 * What this class does:
 *   - Starts a local web server at port 8080 (http://localhost:8080/).
 *   - Serves HTML and CSS files from the frontend/ directory to the user's browser.
 *   - Handles Student Registration, Login, Application Submission, Document Uploads,
 *     Document View/Download, Admin Verification, Branch Allocation, and Student Acceptance.
 * ============================================================================
 */
public class Main {

    private static final int PORT = 8080;
    private static AdmissionSystem admissionSystem;
    private static String frontendPath;

    public static void main(String[] args) {
        try {
            System.out.println("=================================================");
            System.out.println("   COLLEGE ADMISSION PORTAL (OOPS PROJECT)       ");
            System.out.println("=================================================");

            admissionSystem = new AdmissionSystem();
            locateFrontendFolder();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Static frontend routes (HTML, CSS, JS)
            server.createContext("/", new StaticFileHandler());

            // Student API Endpoints
            server.createContext("/api/register", new RegisterHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/apply", new ApplyHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/upload-document", new UploadDocumentHandler());
            server.createContext("/api/document/view", new DocumentViewHandler(false));
            server.createContext("/api/document/download", new DocumentViewHandler(true));
            server.createContext("/api/student/allocation-response", new StudentAllocationResponseHandler());

            // Admin API Endpoints
            server.createContext("/api/admin-login", new AdminLoginHandler());
            server.createContext("/api/admin/applications", new AdminApplicationsHandler());
            server.createContext("/api/admin/action", new AdminActionHandler());
            server.createContext("/api/admin/verify-documents", new AdminVerifyDocumentsHandler());
            server.createContext("/api/admin/allocate-branch", new AdminAllocateBranchHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("\n[SERVER STARTED] Web server is running!");
            System.out.println(">>> Open your browser and go to: http://localhost:" + PORT + "/");
            System.out.println(">>> Press Ctrl+C in terminal to stop the server.\n");

            // Keep main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[SERVER ERROR] Failed to start HTTP server: " + e.getMessage());
        }
    }

    private static void locateFrontendFolder() {
        File dir = new File("frontend");
        if (dir.exists() && dir.isDirectory()) {
            frontendPath = "frontend";
        } else {
            frontendPath = "../frontend";
        }
        System.out.println("[Main] Serving frontend files from: " + new File(frontendPath).getAbsolutePath());
    }

    // =============================================================
    // HTTP HANDLER: Serves HTML, CSS, JS files
    // =============================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            File file = new File(frontendPath + requestPath);

            if (file.exists() && !file.isDirectory()) {
                String contentType = "text/plain";
                if (requestPath.endsWith(".html")) {
                    contentType = "text/html; charset=UTF-8";
                } else if (requestPath.endsWith(".css")) {
                    contentType = "text/css; charset=UTF-8";
                } else if (requestPath.endsWith(".js")) {
                    contentType = "application/javascript; charset=UTF-8";
                } else if (requestPath.endsWith(".png")) {
                    contentType = "image/png";
                } else if (requestPath.endsWith(".jpg") || requestPath.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (requestPath.endsWith(".pdf")) {
                    contentType = "application/pdf";
                }

                byte[] fileBytes = new byte[(int) file.length()];
                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileBytes);
                }

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(fileBytes);
                }
            } else {
                String notFound = "<h1>404 Not Found</h1><p>The requested page was not found.</p>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes());
                }
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Student Registration (/api/register)
    // =============================================================
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String name = params.getOrDefault("name", "");
                String email = params.getOrDefault("email", "");
                String password = params.getOrDefault("password", "");
                String phone = params.getOrDefault("phone", "");

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"All fields are required!\"}");
                    return;
                }

                boolean success = admissionSystem.registerStudent(name, email, password, phone);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Registration successful! You can now login.\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"This email is already registered! Please login.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Student Login (/api/login)
    // =============================================================
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String email = params.getOrDefault("email", "");
                String password = params.getOrDefault("password", "");

                Student student = admissionSystem.authenticateStudent(email, password);
                if (student != null) {
                    String json = String.format(
                        "{\"success\":true,\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"message\":\"Login successful!\"}",
                        escapeJson(student.getId()),
                        escapeJson(student.getName()),
                        escapeJson(student.getEmail()),
                        escapeJson(student.getPhoneNumber())
                    );
                    sendJsonResponse(exchange, 200, json);
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password! Please check credentials.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admission Form Submission (/api/apply)
    // Supports branch preferences in priority order!
    // =============================================================
    static class ApplyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String email = params.getOrDefault("email", "");
                String name = params.getOrDefault("name", "");
                String sscStr = params.getOrDefault("sscMarks", "0");
                String hscStr = params.getOrDefault("hscMarks", "0");
                String cetStr = params.getOrDefault("cetScore", "0");
                String course = params.getOrDefault("course", "");
                String category = params.getOrDefault("category", "General");
                String preferencesRaw = params.getOrDefault("preferences", "");

                double ssc = 0.0, hsc = 0.0, cet = 0.0;
                try {
                    ssc = Double.parseDouble(sscStr);
                    hsc = Double.parseDouble(hscStr);
                    cet = Double.parseDouble(cetStr);
                } catch (NumberFormatException e) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Please enter valid numerical marks!\"}");
                    return;
                }

                List<String> branchPreferences = new ArrayList<>();
                if (!preferencesRaw.isEmpty()) {
                    String[] items = preferencesRaw.split("[,;]");
                    for (String item : items) {
                        String clean = item.trim();
                        if (!clean.isEmpty() && !branchPreferences.contains(clean)) {
                            branchPreferences.add(clean);
                        }
                    }
                }
                if (branchPreferences.isEmpty() && !course.isEmpty()) {
                    branchPreferences.add(course.trim());
                }

                Application app = admissionSystem.submitApplication(
                    email, name, ssc, hsc, cet, course, category, branchPreferences
                );

                String json = String.format(
                    "{\"success\":true,\"appId\":\"%s\",\"message\":\"Application submitted successfully!\"}",
                    escapeJson(app.getApplicationId())
                );
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Document Upload (/api/upload-document)
    // =============================================================
    static class UploadDocumentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> queryParams = parseQueryString(exchange.getRequestURI().getQuery());

                String appId = queryParams.getOrDefault("appId", "");
                if (appId.isEmpty()) {
                    appId = queryParams.getOrDefault("email", "");
                }
                String docType = queryParams.getOrDefault("docType", "");
                String fileName = queryParams.getOrDefault("fileName", "");

                // Check headers if not in query params
                if (appId.isEmpty() && exchange.getRequestHeaders().containsKey("X-App-Id")) {
                    appId = exchange.getRequestHeaders().getFirst("X-App-Id");
                }
                if (docType.isEmpty() && exchange.getRequestHeaders().containsKey("X-Doc-Type")) {
                    docType = exchange.getRequestHeaders().getFirst("X-Doc-Type");
                }
                if (fileName.isEmpty() && exchange.getRequestHeaders().containsKey("X-File-Name")) {
                    fileName = exchange.getRequestHeaders().getFirst("X-File-Name");
                }

                if (appId.isEmpty() || docType.isEmpty() || fileName.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Missing required parameters: appId, docType, fileName\"}");
                    return;
                }

                byte[] fileBytes = readAllBytes(exchange.getRequestBody());

                if (fileBytes.length == 0) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Uploaded file is empty!\"}");
                    return;
                }

                boolean saved = admissionSystem.saveUploadedDocument(appId, docType, fileName, fileBytes);
                if (saved) {
                    sendJsonResponse(exchange, 200, String.format(
                        "{\"success\":true,\"message\":\"Document '%s' uploaded successfully!\",\"docType\":\"%s\",\"fileName\":\"%s\"}",
                        escapeJson(Document.getTitleForType(docType)),
                        escapeJson(docType),
                        escapeJson(fileName)
                    ));
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to upload document. Verify application ID and allowed formats (PDF, JPG, PNG under 5MB).\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Document Preview & Download
    // (/api/document/view & /api/document/download)
    // =============================================================
    static class DocumentViewHandler implements HttpHandler {
        private final boolean isDownload;

        public DocumentViewHandler(boolean isDownload) {
            this.isDownload = isDownload;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> queryParams = parseQueryString(exchange.getRequestURI().getQuery());
                String appId = queryParams.getOrDefault("appId", "");
                if (appId.isEmpty()) {
                    appId = queryParams.getOrDefault("email", "");
                }
                String docType = queryParams.getOrDefault("docType", "");

                File docFile = admissionSystem.getDocumentFile(appId, docType);
                if (docFile != null && docFile.exists()) {
                    String name = docFile.getName();
                    String contentType = "application/octet-stream";
                    if (name.toLowerCase().endsWith(".pdf")) {
                        contentType = "application/pdf";
                    } else if (name.toLowerCase().endsWith(".png")) {
                        contentType = "image/png";
                    } else if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    }

                    String disposition = isDownload ? "attachment" : "inline";
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.getResponseHeaders().set("Content-Disposition", disposition + "; filename=\"" + name + "\"");

                    byte[] fileBytes = new byte[(int) docFile.length()];
                    try (FileInputStream fis = new FileInputStream(docFile)) {
                        fis.read(fileBytes);
                    }

                    exchange.sendResponseHeaders(200, fileBytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(fileBytes);
                    }
                } else {
                    String notFound = "<h1>404 Not Found</h1><p>The requested document was not found or has not been uploaded yet.</p>";
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(404, notFound.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(notFound.getBytes());
                    }
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Student Application Status (/api/status)
    // =============================================================
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> queryParams = parseQueryString(exchange.getRequestURI().getQuery());
                String email = queryParams.getOrDefault("email", "");
                String appId = queryParams.getOrDefault("appId", "");

                Application app = null;
                if (!appId.isEmpty()) {
                    app = admissionSystem.getApplicationById(appId);
                }
                if (app == null && !email.isEmpty()) {
                    app = admissionSystem.getApplicationByEmail(email);
                }

                if (app != null) {
                    Student student = admissionSystem.getStudentByEmail(app.getStudentEmail());
                    String phone = student != null ? student.getPhoneNumber() : "";

                    String json = formatApplicationJson(app, phone);
                    sendJsonResponse(exchange, 200, json);
                } else {
                    sendJsonResponse(exchange, 200, "{\"found\":false,\"message\":\"No application found for this candidate.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Student Allocation Response (/api/student/allocation-response)
    // Student accepts or rejects the allocated branch.
    // =============================================================
    static class StudentAllocationResponseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String appId = params.getOrDefault("appId", "");
                String decision = params.getOrDefault("action", ""); // "Accept" or "Reject"
                String remarks = params.getOrDefault("remarks", "");

                if (appId.isEmpty() || decision.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Application ID and action are required!\"}");
                    return;
                }

                boolean success = admissionSystem.respondToAllocation(appId, decision, remarks);
                if (success) {
                    if ("Accept".equalsIgnoreCase(decision)) {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Congratulations! You have accepted the allocated branch. Your admission is CONFIRMED.\"}");
                    } else {
                        sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Allocation rejected. Your application has been returned to the Admission Committee for review and reallocation.\"}");
                    }
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to process allocation decision. Application not found.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admin Login (/api/admin-login)
    // =============================================================
    static class AdminLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String email = params.getOrDefault("email", "");
                String password = params.getOrDefault("password", "");

                Admin admin = admissionSystem.authenticateAdmin(email, password);
                if (admin != null) {
                    String json = String.format(
                        "{\"success\":true,\"id\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"message\":\"Admin authentication successful!\"}",
                        escapeJson(admin.getId()),
                        escapeJson(admin.getName()),
                        escapeJson(admin.getEmail()),
                        escapeJson(admin.getRoleDescription())
                    );
                    sendJsonResponse(exchange, 200, json);
                } else {
                    sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid Admin credentials! Access Denied.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admin Get All Applications (/api/admin/applications)
    // =============================================================
    static class AdminApplicationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                ArrayList<Application> allApps = admissionSystem.getAllApplications();

                StringBuilder sb = new StringBuilder();
                sb.append("[");
                for (int i = 0; i < allApps.size(); i++) {
                    Application a = allApps.get(i);
                    Student s = admissionSystem.getStudentByEmail(a.getStudentEmail());
                    String phone = s != null ? s.getPhoneNumber() : "";

                    sb.append(formatApplicationJson(a, phone));
                    if (i < allApps.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append("]");

                sendJsonResponse(exchange, 200, sb.toString());
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admin Action - Approve / Reject (/api/admin/action)
    // =============================================================
    static class AdminActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String appId = params.getOrDefault("appId", "");
                String action = params.getOrDefault("action", "");
                String remarks = params.getOrDefault("remarks", "");

                boolean success = admissionSystem.updateApplicationStatus(appId, action, remarks);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Application status updated to " + escapeJson(action) + "!\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Application ID not found.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admin Document Verification (/api/admin/verify-documents)
    // =============================================================
    static class AdminVerifyDocumentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String appId = params.getOrDefault("appId", "");
                String status = params.getOrDefault("status", "Documents Verified");
                String remarks = params.getOrDefault("remarks", "");

                boolean success = admissionSystem.verifyApplicationDocuments(appId, status, remarks);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Document verification updated to " + escapeJson(status) + "!\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Application ID not found.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HTTP HANDLER: Admin Branch Allocation (/api/admin/allocate-branch)
    // =============================================================
    static class AdminAllocateBranchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);

                String appId = params.getOrDefault("appId", "");
                String branch = params.getOrDefault("branch", "");
                String remarks = params.getOrDefault("remarks", "");

                if (appId.isEmpty() || branch.isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Application ID and branch must be provided!\"}");
                    return;
                }

                boolean success = admissionSystem.allocateBranch(appId, branch, remarks);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Branch '" + escapeJson(branch) + "' allocated successfully! Decision sent to candidate.\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"success\":false,\"message\":\"Application ID not found.\"}");
                }
            } else {
                sendJsonResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed\"}");
            }
        }
    }

    // =============================================================
    // HELPER: Format Application Object as JSON String
    // =============================================================
    private static String formatApplicationJson(Application app, String phone) {
        StringBuilder prefsJson = new StringBuilder();
        prefsJson.append("[");
        List<String> prefs = app.getBranchPreferences();
        for (int i = 0; i < prefs.size(); i++) {
            prefsJson.append("\"").append(escapeJson(prefs.get(i))).append("\"");
            if (i < prefs.size() - 1) prefsJson.append(",");
        }
        prefsJson.append("]");

        StringBuilder docsJson = new StringBuilder();
        docsJson.append("{");
        Map<String, String> docs = app.getUploadedDocuments();
        int docIndex = 0;
        for (Map.Entry<String, String> entry : docs.entrySet()) {
            if (docIndex > 0) docsJson.append(",");
            docsJson.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                    .append(escapeJson(entry.getValue())).append("\"");
            docIndex++;
        }
        docsJson.append("}");

        return String.format(
            "{\"found\":true,\"appId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"," +
            "\"ssc\":%.2f,\"hsc\":%.2f,\"cet\":%.2f,\"course\":\"%s\"," +
            "\"category\":\"%s\",\"status\":\"%s\",\"remarks\":\"%s\"," +
            "\"preferences\":%s,\"allocatedBranch\":\"%s\",\"allocationStatus\":\"%s\"," +
            "\"verificationStatus\":\"%s\",\"documents\":%s,\"allocationRemarks\":\"%s\"}",
            escapeJson(app.getApplicationId()),
            escapeJson(app.getStudentName()),
            escapeJson(app.getStudentEmail()),
            escapeJson(phone),
            app.getSscMarks(),
            app.getHscMarks(),
            app.getCetScore(),
            escapeJson(app.getCourseChoice()),
            escapeJson(app.getCategory()),
            escapeJson(app.getStatus()),
            escapeJson(app.getRemarks()),
            prefsJson.toString(),
            escapeJson(app.getAllocatedBranch()),
            escapeJson(app.getAllocationStatus()),
            escapeJson(app.getVerificationStatus()),
            docsJson.toString(),
            escapeJson(app.getAllocationRemarks())
        );
    }

    // =============================================================
    // UTILITY METHODS FOR HTTP PARSING AND RESPONSES
    // =============================================================

    private static Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            String[] pairs = body.toString().split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length >= 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.name());
                    params.put(key, value);
                } else if (keyValue.length == 1) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.name());
                    params.put(key, "");
                }
            }
        }
        return params;
    }

    private static Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            } else if (keyValue.length == 1) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                params.put(key, "");
            }
        }
        return params;
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
