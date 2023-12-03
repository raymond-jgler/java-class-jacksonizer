package com.aggregated;

import java.io.*;
import java.util.List;

public class FileUtils {
  private static String BASE_MODULE_PATH = "src\\main\\java\\";

  public static File makeFolderOrFile(List<Object> input) {
//        if (!BASE_MODULE_PATH.equalsIgnoreCase(String.valueOf(input.get(0)))) {
//            BASE_MODULE_PATH = String.valueOf(input.get(0));
//        }
    return makeFolderOrFile(String.valueOf(input.get(0)));
  }

  public static File makeFolderOrFile(String packageName) {
    if (packageName.contains(BASE_MODULE_PATH)) {
      return new File(packageName);
    }
    return new File(BASE_MODULE_PATH + StringArsenal.current().with(packageName).resolveReplaces(".**", "", ".", "//").getInternal());
  }

  public static void writeContentToFile(String fileName, String content, boolean isAppend) {
    try (InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(fileName)) {
      if (inputStream != null) {
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        String fileContent = new String(buffer);

        // Try-with-resources to automatically close the FileWriter
        try (FileWriter writer = new FileWriter(fileName, isAppend)) {
          // Write content to the file
          writer.write(fileContent + "\n\n");
          System.out.println("Content written to " + fileName);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        System.out.println("InputStream is null. Unable to read content.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static boolean contentExists(String fileName, String content) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains(content) || content.contains(line)) {
          return true;
        }
      }
    }
    return false;
  }
}


