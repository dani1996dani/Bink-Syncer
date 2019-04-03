package sample;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class Syncer {
    private static Syncer ourInstance = new Syncer();
    boolean isSyncing;
    private long bytesProcessed = 0;
    private long totalBytesToProcess = 0;


    public static Syncer getInstance() {
        return ourInstance;
    }

    private Syncer() {
    }

    public boolean isSyncing() {
        return isSyncing;
    }


    public double getCurrentProccess() {
        return (double) (bytesProcessed) / (double) totalBytesToProcess;
    }

    public void sync(String rootString, String targetString, boolean keepTargetFiles) {

        isSyncing = true;

        File rootDir = new File(rootString);
        Path rootPath = rootDir.toPath();


        File targetDir = new File(targetString);


        try {
            if (!keepTargetFiles) {
                deleteUnknownFilesInTarget(rootString, targetString, targetDir);
            }

            totalBytesToProcess = FileUtils.sizeOfDirectory(rootDir);

            copyFilesToTarget(rootPath, rootString, targetString);
        } catch (IOException e) {

        } finally {
            resetSyncerState();
        }


    }

    private void resetSyncerState() {
        isSyncing = false;
        totalBytesToProcess = 0;
        bytesProcessed = 0;
    }


    private void copyFilesToTarget(Path rootPath, String rootString, String targetString) throws IOException {
        Files.walkFileTree(rootPath, new FileVisitor<Path>() {


            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String relativeRootString = dir.toString().substring(rootString.length());
                if (relativeRootString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                System.out.println(relativeRootString);
                File currentDir = dir.toFile();
                File targetDir = new File(targetString + relativeRootString);

                long currFolderSize = FileUtils.sizeOfDirectory(dir.toFile());

                if (targetDir.exists()) {
                    System.out.println("exists");
                } else {
                    System.out.println("not exists");
                    FileUtils.copyDirectory(currentDir, targetDir);
                    bytesProcessed += currFolderSize;
                    return FileVisitResult.SKIP_SUBTREE;
                }
                System.out.println("=========================");


                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String relativeFileString = file.toString().substring(rootString.length());
                System.out.println(relativeFileString);
                if (relativeFileString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }

                long fileSize = FileUtils.sizeOf(file.toFile());
                bytesProcessed += fileSize;

                File currFile = file.toFile();
                File targetFile = new File(targetString + relativeFileString);
                if (targetFile.exists()) {
                    System.out.println("File exists");
                } else {
                    System.out.println("File not exists");
                    FileUtils.copyFile(currFile, targetFile);
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }


    private void deleteUnknownFilesInTarget(String rootString, String targetString, File targetDir) throws IOException {
        System.out.println(" ================> Checking to delete");

        Files.walkFileTree(targetDir.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String relativeTargetString = dir.toString().substring(targetString.length());
                if (relativeTargetString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                System.out.println(relativeTargetString);
                File currentDir = dir.toFile();
                File rootDir = new File(rootString + relativeTargetString);

                if (rootDir.exists()) {
                    System.out.println("exists");
                } else {
                    System.out.println("not exists");
                    FileUtils.deleteDirectory(currentDir);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                System.out.println("=========================");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativeFileString = file.toString().substring(targetString.length());
                System.out.println(relativeFileString);
                if (relativeFileString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }

                File currFile = file.toFile();
                File rootFile = new File(rootString + relativeFileString);
                if (rootFile.exists()) {
                    System.out.println("File exists");
                } else {
                    System.out.println("File not exists");
                    currFile.delete();
                    return FileVisitResult.SKIP_SUBTREE;
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

    }
}
