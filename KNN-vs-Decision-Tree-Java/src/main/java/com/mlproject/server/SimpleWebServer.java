package com.mlproject.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.mlproject.models.UserRecord;
import com.mlproject.utils.DataLoader;
import com.mlproject.utils.PreProcessor;
import com.mlproject.utils.Evaluator;
import com.mlproject.algorithms.KNNClassifier;
import com.mlproject.algorithms.DecisionTreeClassifier;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SimpleWebServer {

    private static final int    PORT         = 8080;
    private static final String FRONTEND_DIR =
        "C:\\Users\\aliyo\\Desktop\\kouacademy\\prolab2-2.projem\\frontend";

    private static List<UserRecord> datasetCache = null;

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/",             new StaticFileHandler());
        server.createContext("/api/evaluate", new MLHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Sunucu calisiyor -> http://localhost:" + PORT);
    }

    
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            File file = new File(FRONTEND_DIR + path);
            if (!file.exists()) {
                sendText(t, 404, "404 Not Found");
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            t.getResponseHeaders().add("Content-Type", contentType(path));
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
        }

        private String contentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css"))  return "text/css; charset=UTF-8";
            if (path.endsWith(".js"))   return "application/javascript; charset=UTF-8";
            return "text/plain";
        }
    }

    
    static class MLHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Content-Type", "application/json");

            try {
                int    k     = 5;
                int    depth = 10;
                String model = "both";

                String query = t.getRequestURI().getQuery();
                if (query != null) {
                    for (String param : query.split("&")) {
                        String[] kv = param.split("=", 2);
                        if (kv.length < 2) continue;
                        switch (kv[0]) {
                            case "k"     -> k     = Integer.parseInt(kv[1]);
                            case "depth" -> depth = Integer.parseInt(kv[1]);
                            case "model" -> model = kv[1];
                        }
                    }
                }

                if (datasetCache == null) {
                    System.out.println("Excel okunuyor...");
                    datasetCache = new DataLoader().loadData();
                    new PreProcessor().processAll(datasetCache);
                }

                Evaluator evaluator = new Evaluator();
                evaluator.splitData(datasetCache);

                String json;
                if ("knn".equals(model)) {
                    System.out.println("KNN basliyor (k=" + k + ")...");
                    Evaluator.EvaluationResult res = evaluator.evaluateModel("KNN", new KNNClassifier(k));
                    json = buildJson(res, null);
                } else if ("dt".equals(model)) {
                    System.out.println("DT basliyor (depth=" + depth + ")...");
                    Evaluator.EvaluationResult res = evaluator.evaluateModel("DT", new DecisionTreeClassifier(depth));
                    json = buildJson(null, res);
                } else {
                    System.out.println("KNN basliyor (k=" + k + ")...");
                    Evaluator.EvaluationResult resKNN = evaluator.evaluateModel("KNN", new KNNClassifier(k));
                    System.out.println("DT basliyor (depth=" + depth + ")...");
                    Evaluator.EvaluationResult resDT  = evaluator.evaluateModel("DT", new DecisionTreeClassifier(depth));
                    json = buildJson(resKNN, resDT);
                }

                sendJson(t, 200, json);

            } catch (Throwable e) {
                e.printStackTrace();
                String msg = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
                sendJson(t, 500, "{\"error\":\"" + msg.replace("\"", "'") + "\"}");
            }
        }

        
        private String buildJson(Evaluator.EvaluationResult knn, Evaluator.EvaluationResult dt) {
            return "{\"knn\":" + resultJson(knn) + ",\"dt\":" + resultJson(dt) + "}";
        }

        
        private String resultJson(Evaluator.EvaluationResult r) {
            if (r == null) {
                return "{\"accuracy\":0,\"time\":0,\"memoryKB\":0,\"categories\":[],\"matrix\":[]}";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format(java.util.Locale.US,
                "{\"accuracy\":%.2f,\"time\":%d,\"memoryKB\":%d,",
                r.accuracy, r.executionTimeMs, r.memoryUsedKB));

            
            sb.append("\"categories\":[");
            for (int i = 0; i < r.categories.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(r.categories.get(i).replace("\"", "'")).append("\"");
            }
            sb.append("],");

            
            sb.append("\"matrix\":[");
            for (int i = 0; i < r.categories.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("[");
                String actualCat = r.categories.get(i);
                Map<String, Integer> row = r.confusionMatrix.getOrDefault(actualCat, Collections.emptyMap());
                for (int j = 0; j < r.categories.size(); j++) {
                    if (j > 0) sb.append(",");
                    sb.append(row.getOrDefault(r.categories.get(j), 0));
                }
                sb.append("]");
            }
            sb.append("]}");

            return sb.toString();
        }
    }

    
    private static void sendJson(HttpExchange t, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
    }

    private static void sendText(HttpExchange t, int code, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = t.getResponseBody()) { os.write(bytes); }
    }
}
