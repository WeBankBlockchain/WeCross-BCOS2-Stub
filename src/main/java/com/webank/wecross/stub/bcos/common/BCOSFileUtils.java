package com.webank.wecross.stub.bcos.common;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.*;

public class BCOSFileUtils {
    private static final String SEPARATOR = "/";
    private static final String SUFFIX = ".zip";
    private static final int BUFFER_SIZE = 500000;

    public static String zipDir(String dirPath) throws IOException {
        File fileToZip = new File(dirPath);
        String name = fileToZip.getName();
        FileOutputStream fos = new FileOutputStream(name + SUFFIX);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        zipFile(fileToZip, name, zipOut);
        zipOut.close();
        fos.close();

        return name + ".zip";
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut)
            throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith(SEPARATOR)) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + SEPARATOR));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : Objects.requireNonNull(children)) {
                zipFile(childFile, fileName + SEPARATOR + childFile.getName(), zipOut);
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[BUFFER_SIZE];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

    public static ArrayList<String> unZip(String unZipFile, String desPath) throws IOException {
        ArrayList<String> innterFiles = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(unZipFile)) {
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                innterFiles.add(entry.getName());
                if (entry.isDirectory()) {
                    new File(desPath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(desPath + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                try (BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {

                    int count;
                    byte[] data = new byte[BUFFER_SIZE];
                    while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
                        bos.write(data, 0, count);
                    }
                    bos.flush();
                }
                bis.close();
            }
            return innterFiles;
        }
    }

    public static void deleteDir(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : Objects.requireNonNull(files)) {
                deleteDir(f);
            }
        }
        dir.delete();
    }
}
