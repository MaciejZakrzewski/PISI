import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Trainer {
    private static String DELIMITER = " ";
    private static double ALPHA = 0.1;

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

            int actualNumOfIterations = train(trainList, maxNumOfIterations, parameters);

            writeOutput(parameters, n, k, dataOutFileName, actualNumOfIterations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int train(List<double[]> trainList, int maxNumOfIterations, List<double[]> parameters) {
        int actualNumOfIterations = 0;

        List<Double> listToApply = new ArrayList<>();
        for (int i = 0; i < maxNumOfIterations; i++) {

            double averageOfAverages = countLastParamAverage(trainList, parameters);

            listToApply.add(averageOfAverages);
            for (int j = 1; j < trainList.get(0).length; j++) {
                double average = countAverage(trainList, parameters, j);
                listToApply.add(average);
            }

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

            if (Math.abs(averageOfAverages) < 0.0000000000000001) {
                break;
            }
        }
        return actualNumOfIterations;
    }

    private static void writeOutput(List<double[]> params, int n, int k, String outputFileName, int numOfIterations) {
        try (PrintWriter out = new PrintWriter(outputFileName)) {
            out.println("iterations=" + numOfIterations);

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

        while ((st = br.readLine()) != null && !st.isEmpty()) {
            String[] splits = st.split(DELIMITER);
            List<Double> toAdd = new ArrayList<>();
            for (String split : splits) {
                if (split != null && !split.isEmpty()) {
                    toAdd.add(Double.valueOf(split));
                }
            }

            double[] target = toAdd.stream().mapToDouble(Double::doubleValue).toArray();

            parameters.add(target);
        }

        br.close();

        return parameters;
    }

    private static double[] readTrainingVariables(double[] input) {
        return Arrays.copyOf(input, input.length - 1);
    }

    private static boolean isVariableFirstDegree(double[] input) {
        return (int) IntStream.range(0, input.length - 1).filter(i -> input[i] != 0.0).count() == 1;
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
