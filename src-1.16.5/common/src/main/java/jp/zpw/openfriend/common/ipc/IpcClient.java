/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.common.ipc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class IpcClient {

    private final Supplier<Process> processFactory;
    private final Consumer<String> stderrSink;

    private final AtomicLong nextId = new AtomicLong(1);
    private final Map<Long, CompletableFuture<JsonObject>> pending = new ConcurrentHashMap<>();
    private final List<IpcListener> listeners = new ArrayList<>();
    private final Object listenersLock = new Object();

    private volatile Process process;
    private volatile BufferedWriter stdin;
    private final Object writeLock = new Object();
    private volatile boolean running;
    private volatile Thread readerThread;
    private volatile Thread stderrThread;

    public IpcClient(Supplier<Process> processFactory, Consumer<String> stderrSink) {
        this.processFactory = processFactory;
        this.stderrSink = stderrSink == null ? msg -> {} : stderrSink;
    }

    public synchronized void start() throws IOException {
        if (running) return;
        Process p;
        try {
            p = processFactory.get();
        } catch (Exception e) {
            throw new IOException("failed to spawn Core process", e);
        }
        if (p == null) throw new IOException("processFactory returned null");
        this.process = p;
        this.stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream(), StandardCharsets.UTF_8));
        this.running = true;
        this.readerThread = new Thread(this::runReader, "openfriend-ipc-reader");
        this.readerThread.setDaemon(true);
        this.readerThread.start();
        this.stderrThread = new Thread(this::runStderr, "openfriend-ipc-stderr");
        this.stderrThread.setDaemon(true);
        this.stderrThread.start();
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            CompletableFuture<JsonObject> f = requestAsync("quit", null);
            try { f.get(500, TimeUnit.MILLISECONDS); } catch (Exception ignored) {}
        } catch (Exception ignored) {}
        try {
            if (process != null && process.isAlive()) {
                if (!process.waitFor(1, TimeUnit.SECONDS)) process.destroy();
                if (!process.waitFor(1, TimeUnit.SECONDS)) process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (CompletableFuture<JsonObject> f : pending.values()) {
            f.completeExceptionally(new IpcException("ipc shutdown"));
        }
        pending.clear();
    }

    public boolean isRunning() {
        Process p = process;
        return running && p != null && p.isAlive();
    }

    public CompletableFuture<JsonObject> requestAsync(String method, JsonObject params) {
        if (!isRunning()) {
            CompletableFuture<JsonObject> bad = new CompletableFuture<>();
            bad.completeExceptionally(new IpcException("ipc not running"));
            return bad;
        }
        long id = nextId.getAndIncrement();
        CompletableFuture<JsonObject> fut = new CompletableFuture<>();
        pending.put(id, fut);

        JsonObject req = new JsonObject();
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("id", id);
        req.addProperty("method", method);
        if (params != null) req.add("params", params);

        try {
            writeLine(req.toString());
        } catch (IOException e) {
            pending.remove(id);
            fut.completeExceptionally(new IpcException("write failed", e));
        }
        return fut;
    }

    public JsonObject request(String method, JsonObject params, long timeoutMs) throws IpcException {
        try {
            return requestAsync(method, params).get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new IpcException("ipc timeout after " + timeoutMs + "ms: " + method);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IpcException("ipc interrupted: " + method);
        } catch (ExecutionException e) {
            Throwable c = e.getCause();
            if (c instanceof IpcException) throw (IpcException) c;
            throw new IpcException("ipc failure: " + method, c);
        }
    }

    public void notify(String method, JsonObject params) throws IpcException {
        if (!isRunning()) throw new IpcException("ipc not running");
        JsonObject req = new JsonObject();
        req.addProperty("jsonrpc", "2.0");
        req.addProperty("method", method);
        if (params != null) req.add("params", params);
        try {
            writeLine(req.toString());
        } catch (IOException e) {
            throw new IpcException("write failed", e);
        }
    }

    public void addListener(IpcListener l) {
        synchronized (listenersLock) {
            if (!listeners.contains(l)) listeners.add(l);
        }
    }

    public void removeListener(IpcListener l) {
        synchronized (listenersLock) {
            listeners.remove(l);
        }
    }

    private void writeLine(String json) throws IOException {
        synchronized (writeLock) {
            BufferedWriter w = stdin;
            if (w == null) throw new IOException("stdin closed");
            w.write(json);
            w.newLine();
            w.flush();
        }
    }

    private void runReader() {
        Process p = process;
        if (p == null) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = br.readLine()) != null) {
                handleLine(line);
            }
        } catch (IOException ignored) {
        } finally {
            running = false;
            for (CompletableFuture<JsonObject> f : pending.values()) {
                f.completeExceptionally(new IpcException("ipc reader ended"));
            }
            pending.clear();
        }
    }

    private void runStderr() {
        Process p = process;
        if (p == null) return;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                stderrSink.accept(line);
            }
        } catch (IOException ignored) {}
    }

    private void handleLine(String line) {
        if (line == null || line.isEmpty()) return;
        JsonElement parsed;
        try {
            parsed = new JsonParser().parse(line);
        } catch (Exception e) {
            stderrSink.accept("ipc: bad json: " + line);
            return;
        }
        if (!parsed.isJsonObject()) return;
        JsonObject obj = parsed.getAsJsonObject();

        if (obj.has("id") && obj.get("id").isJsonPrimitive() && obj.get("id").getAsJsonPrimitive().isNumber()) {
            long id = obj.get("id").getAsLong();
            CompletableFuture<JsonObject> fut = pending.remove(id);
            if (fut == null) return;
            if (obj.has("error") && !obj.get("error").isJsonNull()) {
                JsonObject err = obj.getAsJsonObject("error");
                int code = err.has("code") ? err.get("code").getAsInt() : 0;
                String msg = err.has("message") ? err.get("message").getAsString() : "unknown";
                if (err.has("data") && !err.get("data").isJsonNull()) {
                    JsonElement d = err.get("data");
                    String dataStr = d.isJsonPrimitive() ? d.getAsString() : d.toString();
                    if (!dataStr.isEmpty()) msg = msg + ": " + dataStr;
                }
                fut.completeExceptionally(new IpcException(msg, code));
                return;
            }
            JsonObject result = obj.has("result") && obj.get("result").isJsonObject()
                    ? obj.getAsJsonObject("result")
                    : new JsonObject();
            fut.complete(result);
            return;
        }

        if (obj.has("method")) {
            String method = obj.get("method").getAsString();
            JsonElement p = obj.get("params");
            JsonObject params = (p != null && p.isJsonObject()) ? p.getAsJsonObject() : new JsonObject();
            List<IpcListener> snapshot;
            synchronized (listenersLock) {
                snapshot = new ArrayList<>(listeners);
            }
            for (IpcListener l : snapshot) {
                try {
                    l.onNotification(method, params);
                } catch (Throwable t) {
                    stderrSink.accept("ipc: listener threw: " + t);
                }
            }
        }
    }

    public static JsonObject params(Object... kv) {
        JsonObject o = new JsonObject();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            Object k = kv[i];
            Object v = kv[i + 1];
            if (!(k instanceof String)) continue;
            String key = (String) k;
            if (v == null) {
                o.add(key, JsonNull.INSTANCE);
            } else if (v instanceof Number) {
                o.add(key, new JsonPrimitive((Number) v));
            } else if (v instanceof Boolean) {
                o.add(key, new JsonPrimitive((Boolean) v));
            } else if (v instanceof JsonElement) {
                o.add(key, (JsonElement) v);
            } else {
                o.add(key, new JsonPrimitive(String.valueOf(v)));
            }
        }
        return o;
    }
}
