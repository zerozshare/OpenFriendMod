/*
 * OpenFriend — Copyright (c) 2026 ZSHARE. Licensed under the MIT License.
 */
package jp.zpw.openfriend.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public final class CoreBinary {
    private static final String RESOURCE_ROOT = "openfriend/";

    private CoreBinary() {}

    public enum OS { WINDOWS, MAC, LINUX, UNKNOWN }
    public enum Arch { AMD64, ARM64, UNKNOWN }

    public static OS currentOS() {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (name.contains("win")) return OS.WINDOWS;
        if (name.contains("mac") || name.contains("darwin")) return OS.MAC;
        if (name.contains("nux") || name.contains("nix") || name.contains("bsd")) return OS.LINUX;
        return OS.UNKNOWN;
    }

    public static Arch currentArch() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.contains("aarch64") || arch.contains("arm64")) return Arch.ARM64;
        if (arch.contains("amd64") || arch.contains("x86_64") || arch.contains("x64")) return Arch.AMD64;
        return Arch.UNKNOWN;
    }

    public static String expectedBinaryName() {
        return expectedBinaryName(currentOS(), currentArch());
    }

    public static String expectedBinaryName(OS os, Arch arch) {
        String osTag;
        switch (os) {
            case WINDOWS: osTag = "windows"; break;
            case MAC:     osTag = "darwin";  break;
            case LINUX:   osTag = "linux";   break;
            default: throw new IllegalStateException("unsupported OS");
        }
        String archTag;
        switch (arch) {
            case AMD64: archTag = "amd64"; break;
            case ARM64: archTag = "arm64"; break;
            default: throw new IllegalStateException("unsupported arch");
        }
        String ext = (os == OS.WINDOWS) ? ".exe" : "";
        return "openfriend-" + osTag + "-" + archTag + ext;
    }

    public static boolean extractBundled(ClassLoader cl, String binaryName, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        try (InputStream in = cl.getResourceAsStream(RESOURCE_ROOT + binaryName)) {
            if (in == null) return false;
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        makeExecutable(target);
        return true;
    }

    public static void makeExecutable(Path path) {
        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
            );
            Files.setPosixFilePermissions(path, perms);
        } catch (UnsupportedOperationException ignored) {
            java.io.File f = path.toFile();
            f.setExecutable(true, false);
            f.setReadable(true, false);
        } catch (IOException ignored) {}
    }

    public static String missingBinaryMessage(Path target) {
        return "OpenFriend Core binary is missing at " + target
            + " — expected " + expectedBinaryName()
            + ". Build the jar with the binaries staged in helper/src/main/resources/openfriend/.";
    }
}
