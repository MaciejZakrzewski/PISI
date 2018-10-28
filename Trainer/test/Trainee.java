import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trainee {
  private static String DELIMITER = " ";

  public static void main(String[] args) {
    String descriptionFileName = "";

    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-d")) {
        descriptionFileName = args[i + 1];
      }
    }

    try {
      List<double[]> parameters = readDescription(descriptionFileName);

      BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

      String in;

      double[] variables;

      while ((in = inputReader.readLine()) != null) {
        variables = Arrays.stream(in.split(DELIMITER)).mapToDouble(Double::parseDouble).toArray();

        System.out.println(getResult(parameters, variables));
      }

      inputReader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static double getResult(List<double[]> parameters, double[] variables) {
    double result = 0.0;

    for (double[] doubles : parameters) {
      double minRes = 1.0;

      for (int i = 0; i < doubles.length - 1; i++) {
        if (minRes != 0.0) {
          for (int j = 0; j < variables.length; j++) {
            if (doubles[i] == j + 1) {
              minRes *= variables[j];
            }
          }
        }
      }

      minRes *= doubles[doubles.length - 1];

      result += minRes;
    }
    return result;
  }

  public static List<double[]> readDescription(String descriptionFileName) throws IOException {
    File file = new File(descriptionFileName);

    BufferedReader br = new BufferedReader(new FileReader(file));

    String st;

    br.readLine();

    List<double[]> parameters = new ArrayList<>();

    while ((st = br.readLine()) != null) {
      double[] params = Arrays.stream(st.split(DELIMITER)).mapToDouble(Double::parseDouble).toArray();

      parameters.add(params);
    }

    return parameters;
  }
}
