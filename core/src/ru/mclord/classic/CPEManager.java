package ru.mclord.classic;

import java.util.HashSet;
import java.util.Set;

public class CPEManager implements Manager {
    private static final CPEManager INSTANCE = new CPEManager();

    private final Set<CPE> supportedExtensions = new HashSet<>();

    private CPEManager() {
    }

    public static CPEManager getInstance() {
        return INSTANCE;
    }

    public boolean isExtensionSupported(String name, short version) {
        // since equals() and hashCode() methods are
        // marked final, we can freely use this approach
        return supportedExtensions.contains(new CPE(name, version));
    }

    public Set<CPE> getSupportedExtensions() {
        if (checkStage()) {
            System.err.println("[Warning] Detected an attempt to collect all " +
                    "supported CPE during pre-initialization. At this stage " +
                    "the game cannot provide accurate data");
        }

        return new HashSet<>(supportedExtensions);
    }

    /* package-private */ Set<CPE> getSupportedExtensionsFast() {
        return supportedExtensions;
    }

    public void registerExtension(CPE extension) {
        if (!checkStage()) {
            throw new IllegalStateException(
                    "Cannot register extensions during current game stage");
        }
        if (supportedExtensions.contains(extension)) {
            throw new IllegalArgumentException(
                    "Extension " + extension + " is already registered");
        }

        supportedExtensions.add(extension);
    }

    @Override
    public boolean checkStage() {
        McLordClassic.GameStage stage = McLordClassic.game().stage;

        return stage == McLordClassic.GameStage.PRE_INITIALIZATION;
    }
}
