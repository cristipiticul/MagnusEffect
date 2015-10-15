package ro.licj.magnus.util;
//
//import ro.licj.magnus.shaders.ShaderProgram;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
///**
// * Source:
// * http://goharsha.com/lwjgl-tutorial-series/the-first-triangle/
// */
//public class FileUtil {
//  public static String readFromFile(String name)
//  {
//    StringBuilder source = new StringBuilder();
//    try
//    {
//      BufferedReader reader = new BufferedReader(
//          new InputStreamReader(
//              ShaderProgram.class
//                  .getClassLoader()
//                  .getResourceAsStream(name)));
//
//      String line;
//      while ((line = reader.readLine()) != null)
//      {
//        source.append(line).append("\n");
//      }
//
//      reader.close();
//    }
//    catch (Exception e)
//    {
//      System.err.println("Error loading source code: " + name);
//      e.printStackTrace();
//    }
//
//    return source.toString();
//  }
//}
