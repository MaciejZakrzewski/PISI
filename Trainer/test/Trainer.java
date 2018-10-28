import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trainer {
  private static String DELIMITER = " ";
  private static double ALPHA = 0.01;

  public static void main(String[] args) {
    String trainSetFileName = "";
    String dataInFileName = "";
    String dataOutFileName = "";

    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-t":
          trainSetFileName = args[i + 1];
          break;
        case "-i":
          dataInFileName = args[i + 1];
          break;
        case "-o":
          dataOutFileName = args[i + 1];
          break;
      }
    }

    List<double[]> trainList;
    int maxNumOfIterations;
    List<double[]> parameters = new ArrayList<>();
    int n, k;

    try {
      trainList = readTrainingFile(trainSetFileName);

      maxNumOfIterations = readNumberOfIterations(dataInFileName);

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

      String in;


      in = bufferedReader.readLine();

      int[] nKParams = Arrays.stream(in.split(DELIMITER)).mapToInt(Integer::parseInt).toArray();

      n = nKParams[0];
      k = nKParams[1];

      while ((in = bufferedReader.readLine()) != null) {
        double[] inputParams = Arrays.stream(in.split(DELIMITER)).mapToDouble(Double::parseDouble).toArray();

        parameters.add(inputParams);
      }

      int actualNumOfIterations = 0;

      List<Double> listToApply = new ArrayList<>();
      for (int i = 0; i < maxNumOfIterations; i++) {

        double averageOfAverages = countLastParamAverage(trainList, parameters);

        listToApply.add(averageOfAverages);
        for (int j = 1; j < trainList.get(0).length; j++) {
          double average = countAverage(trainList, parameters, j);
          listToApply.add(average);
          averageOfAverages += average;
        }

        averageOfAverages /= trainList.get(0).length;

        double[] lastParam = parameters.get(parameters.size() - 1);
        lastParam[lastParam.length - 1] -= ALPHA * listToApply.get(0);
        parameters.set(parameters.size() - 1, lastParam);

        int counter = 0;
        for (int j = 0; j < parameters.size() - 1; j++) {
          double[] fullParameters = parameters.get(j);
          if (fullParameters[0] != 0.0 && isVariableFirstDegree(fullParameters)) {
            fullParameters[fullParameters.length - 1] -= ALPHA * listToApply.get(listToApply.size() - 1 - counter);
            parameters.set(j, fullParameters);
            counter++;
          }
        }

        listToApply.clear();

        actualNumOfIterations++;

        if (Math.abs(averageOfAverages) < 0.000001) {
          break;
        }
      }

      writeOutput(parameters, n, k, dataOutFileName, actualNumOfIterations);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void writeOutput(List<double[]> params, int n, int k, String outputFileName, int numOfIterations) {
    try (PrintWriter out = new PrintWriter(outputFileName)) {
      out.println("iterations=" + numOfIterations);
      out.println();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    System.out.println(String.format("%s %s", n, k));
    for (double[] paramsArray : params) {
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < paramsArray.length - 1; i++) {
        result.append(Double.valueOf(paramsArray[i]).intValue()).append(" ");
      }
      result.append(paramsArray[paramsArray.length - 1]);
      System.out.println(result);
    }
  }

  private static List<double[]> readTrainingFile(String trainingFileName) throws IOException {
    File file = new File(trainingFileName);

    BufferedReader br = new BufferedReader(new FileReader(file));

    String st;

    List<double[]> parameters = new ArrayList<>();

    while ((st = br.readLine()) != null) {
      double[] params = Arrays.stream(st.split(DELIMITER)).mapToDouble(Double::parseDouble).toArray();

      parameters.add(params);
    }

    br.close();

    return parameters;
  }

  private static double[] readTrainingVariables(double[] input) {
    return Arrays.copyOf(input, input.length - 1);
  }

  private static boolean isVariableFirstDegree(double[] input) {
    for (int i = 1; i < input.length - 1; i++) {
      if (input[i] != 0.0) {
        return false;
      }
    }

    return true;
  }

  private static double countLastParamAverage(List<double[]> trainList, List<double[]> parameters) {
    double average = 0.0;

    for (double[] aTrainList : trainList) {
      average += Trainee.getResult(parameters, readTrainingVariables(aTrainList)) - aTrainList[aTrainList.length - 1];
    }

    return average / trainList.size();
  }

  private static double countAverage(List<double[]> trainList, List<double[]> parameters, int x) {
    double average = 0.0;

    for (double[] aTrainList : trainList) {
      average += ((Trainee.getResult(parameters, readTrainingVariables(aTrainList)) - aTrainList[aTrainList.length - 1]) * aTrainList[x - 1]);
    }

    return average / trainList.size();
  }

  private static int readNumberOfIterations(String dataInFileName) throws IOException {
    File file = new File(dataInFileName);

    BufferedReader br = new BufferedReader(new FileReader(file));

    String iterations = br.readLine();

    br.close();

    return Integer.valueOf(iterations.substring(iterations.indexOf("=") + 1));
  }


}
