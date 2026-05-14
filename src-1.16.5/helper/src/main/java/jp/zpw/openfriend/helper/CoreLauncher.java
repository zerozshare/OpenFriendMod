/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CoreLauncher {

    private final Path dataDir;
    private final ClassLoader resourceLoader;

    public CoreLauncher(Path dataDir, ClassLoader resourceLoader) {
        this.dataDir = dataDir;
        this.resourceLoader = resourceLoader;
    }

    public Path dataDir() { return dataDir; }

    public Path resolveBinaryPath() {
        return dataDir.resolve("bin").resolve(CoreBinary.expectedBinaryName());
    }

    public Path ensureBinary() throws IOException {
        Files.createDirectories(dataDir);
        Path target = resolveBinaryPath();
        if (!CoreBinary.extractBundled(resourceLoader, CoreBinary.expectedBinaryName(), target)) {
            if (Files.isExecutable(target)) return target;
            throw new IOException(CoreBinary.missingBinaryMessage(target));
        }
        return target;
    }

    public Process spawnIpc() throws IOException {
        Path bin = ensureBinary();
        List<String> command = new ArrayList<>();
        command.add(bin.toAbsolutePath().toString());
        command.add("--ipc-stdio");
        command.add("--watch-parent");
        command.add("--no-update");
        command.add("--data-dir");
        command.add(dataDir.toAbsolutePath().toString());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dataDir.toFile());
        pb.redirectErrorStream(false);
        pb.environment().put("LC_ALL", "C");
        return pb.start();
    }

    public static Path defaultDataDir(String modId) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", ".");
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                return Paths.get(appData, modId);
            }
            return Paths.get(home, "AppData", "Roaming", modId);
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return Paths.get(home, "Library", "Application Support", modId);
        }
        String xdgData = System.getenv("XDG_DATA_HOME");
        if (xdgData != null && !xdgData.isEmpty()) {
            return Paths.get(xdgData, modId);
        }
        return Paths.get(home, ".local", "share", modId);
    }
}
