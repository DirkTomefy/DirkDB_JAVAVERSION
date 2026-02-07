package sqlTsinjo.storage;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sqlTsinjo.config.TombstoneConfig;

public class FileReplicator {

    private final String primaryPath;
    private final String secondaryPath;
    private final ScheduledExecutorService scheduler;
    private final TombstoneConfig tombstoneConfig;
    private final int intervalSeconds;

    public FileReplicator(String primaryPath, String secondaryPath) {
        this(primaryPath, secondaryPath, new TombstoneConfig(), 2);
    }

    public FileReplicator(String primaryPath, String secondaryPath, TombstoneConfig tombstoneConfig, int intervalSeconds) {
        this.primaryPath = primaryPath;
        this.secondaryPath = secondaryPath;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.tombstoneConfig = tombstoneConfig;
        this.intervalSeconds = intervalSeconds;
    }

    public void startReplication(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(this::replicateChanges, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
        System.out.println("Réplication démarrée toutes les " + intervalSeconds + " secondes");
    }

    private void replicateChanges() {
        try {
            Path primaryDir = Paths.get(primaryPath);
            Path secondaryDir = Paths.get(secondaryPath);

            if (!Files.exists(secondaryDir)) {
                Files.createDirectories(secondaryDir);
            }

            // Parcourir tous les fichiers (incluant tombstones)
            Files.walk(primaryDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Path relative = primaryDir.relativize(file);
                        Path target = secondaryDir.resolve(relative);
                        
                        // Créer les répertoires parents si besoin
                        Files.createDirectories(target.getParent());
                        
                        // Copier seulement si le fichier est plus récent
                        if (!Files.exists(target) || 
                            Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(target)) > 0) {
                            Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Répliqué: " + relative);
                        }
                    } catch (IOException e) {
                        System.err.println("Erreur réplication de " + file + ": " + e.getMessage());
                    }
                });

            // GC / archivage tombstones sur la cible
            TombstoneManager.gcTombstones(secondaryDir, tombstoneConfig, intervalSeconds);
        } catch (IOException e) {
            System.err.println("Erreur lors de la réplication: " + e.getMessage());
        }
    }

    public void stop() {
        scheduler.shutdown();
    }

    // Test
    public static void main(String[] args) throws InterruptedException {
        // Exemple : répliquer de ./databases vers ./databases_backup
        FileReplicator replicator = new FileReplicator("databases", "databases_backup");
        replicator.startReplication(5); // toutes les 5 secondes
        
        Thread.sleep(60000); // tourne pendant 1 minute
        replicator.stop();
    }
}
