package jp.hazuki.yuzubrowser.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class EnvironmentUtils {
    private EnvironmentUtils() {
        throw new UnsupportedOperationException();
    }

    //thanks for http://inujirushi123.blog.fc2.com/blog-entry-93.html
    public static Set<String> getExternalStoragesFromSystemFile() {
        HashSet<String> list = new HashSet<>();
        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(new File("/system/etc/vold.fstab")));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {
                    String path = line.split(" ")[2];
                    list.add(path);
                }
            }
        } catch (FileNotFoundException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return list;
    }
}
