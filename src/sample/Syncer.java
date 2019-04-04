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

    private Log logger;

    public static final String seperatorLine = "\n===========================\n";

    public void setLogger(Log logger){
        this.logger = logger;
    }


    public static Syncer getInstance() {
        return ourInstance;
    }

    private Syncer() {
    }

    public boolean isSyncing() {
        return isSyncing;
    }


    public double getCurrentProccess() {
        double current = (double) (bytesProcessed) / (double) totalBytesToProcess;
        if(Double.isNaN(current))
            current = 0;
        return current;
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
            logger.log(e.getMessage());
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

        logger.log(seperatorLine + "Copying phase - Searching for files to copy.");

        Files.walkFileTree(rootPath, new FileVisitor<Path>() {


            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String relativeRootString = dir.toString().substring(rootString.length());
                if (relativeRootString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                logger.log(seperatorLine);
                logger.log("Evaluating Dir: " + relativeRootString);
                File currentDir = dir.toFile();
                File targetDir = new File(targetString + relativeRootString);

                long currFolderSize = FileUtils.sizeOfDirectory(dir.toFile());


                if (targetDir.exists()) {
                    logger.log("Target Dir exists.");
                } else {
                    logger.log("Target Dir doesn't exist.");
                    logger.log("Copying " + targetDir.getPath().toString());
                    FileUtils.copyDirectory(currentDir, targetDir);
                    bytesProcessed += currFolderSize;
                    logger.log("Finished Copying.");
                    logger.log(seperatorLine);
                    return FileVisitResult.SKIP_SUBTREE;
                }




                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String relativeFileString = file.toString().substring(rootString.length());
                logger.log("\n");
                logger.log("Evaluating File: " + relativeFileString);
                if (relativeFileString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }

                long fileSize = FileUtils.sizeOf(file.toFile());
                bytesProcessed += fileSize;

                File currFile = file.toFile();
                File targetFile = new File(targetString + relativeFileString);
                if (targetFile.exists()) {
                    logger.log("File exists.");
                    logger.log("\n");
                } else {
                    logger.log("File doesn't exist.");
                    logger.log("Copying " + targetFile.getPath().toString());
                    FileUtils.copyFile(currFile, targetFile);
                    logger.log("Finished Copying " + targetFile.getPath().toString());
                    logger.log("\n");
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
         logger.log(seperatorLine + " Checking to delete");

        Files.walkFileTree(targetDir.toPath(), new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String relativeTargetString = dir.toString().substring(targetString.length());
                if (relativeTargetString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }
                logger.log(relativeTargetString);
                File currentDir = dir.toFile();
                File rootDir = new File(rootString + relativeTargetString);

                if (rootDir.exists()) {
                    logger.log("Root Dir exists.");
                } else {

                    logger.log("Root Dir doesn't exist.");
                    FileUtils.deleteDirectory(currentDir);
                    return FileVisitResult.SKIP_SUBTREE;
                }
                logger.log(seperatorLine);
                //System.out.println("=========================");
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativeFileString = file.toString().substring(targetString.length());
                logger.log(relativeFileString);
                if (relativeFileString.isEmpty()) {
                    return FileVisitResult.CONTINUE;
                }

                File currFile = file.toFile();
                File rootFile = new File(rootString + relativeFileString);
                if (rootFile.exists()) {
                    //System.out.println("File exists");
                    logger.log("File exists.");
                } else {
                    logger.log("File doesn't exist.");
                    //System.out.println("File not exists");
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
