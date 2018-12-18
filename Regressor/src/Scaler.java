import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Scaler {
    private static String DELIMITER = " ";

    public static void main(String[] args) {
        List<String> argumentsList = Arrays.asList(args);
        if (argumentsList.contains("-a")) {
            List<String> fileNames = new ArrayList<>();
            for (int i = argumentsList.indexOf("-a") + 1; i < argumentsList.size(); i++) {
                fileNames.add(argumentsList.get(i));
            }

            try {
                saveMinAndMax(fileNames);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (argumentsList.contains("-s")) {
            String dataFileName = argumentsList.get(argumentsList.indexOf("-s") + 1);

            try {
                scale(dataFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (argumentsList.contains("-u")) {
            String dataFileName = argumentsList.get(argumentsList.indexOf("-u") + 1);

            try {
                unScale(dataFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void scale(String dataFileName) throws IOException {
        List<List<Double>> data = readDataForScaling(dataFileName);
        List<List<Double>> inputList = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String in;

        while ((in = br.readLine()) != null) {
            inputList.add(Arrays.stream(in.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList()));
        }

        for (List<Double> anInputList : inputList) {
            StringBuilder result = new StringBuilder();
            for (int j = 0; j < anInputList.size(); j++) {
                result.append(scaleTarget(anInputList.get(j), data.get(j).get(0), data.get(j).get(1))).append(DELIMITER);
            }
            System.out.println(result);
        }
    }

    private static void unScale(String dataFileName) throws IOException {
        List<List<Double>> data = readDataForScaling(dataFileName);
        List<List<Double>> inputList = new ArrayList<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String in;

        while ((in = br.readLine()) != null) {
            inputList.add(Arrays.stream(in.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList()));
        }

        for (List<Double> anInputList : inputList) {
            StringBuilder result = new StringBuilder();
            for (int j = 0; j < anInputList.size(); j++) {
                result.append(unscaleTarget(anInputList.get(j), data.get(j).get(0), data.get(j).get(1))).append(DELIMITER);
            }
            System.out.println(result);
        }
    }

    private static List<List<Double>> readDataForScaling(String dataFileName) throws IOException {
        List<List<Double>> data = new ArrayList<>();

        File file = new File(dataFileName);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;

        while ((st = br.readLine()) != null) {
            List<Double> params = Arrays.stream(st.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
            data.add(params);
        }

        br.close();

        return data;
    }

    private static void saveMinAndMax(List<String> fileNames) throws IOException {
        List<List<Double>> input = new ArrayList<>();

        for (String fileName : fileNames) {
            File file = new File(fileName);

            BufferedReader br = new BufferedReader(new FileReader(file));

            String st;

            while ((st = br.readLine()) != null) {
                List<Double> params = Arrays.stream(st.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
                input.add(params);
            }

            br.close();
        }

        List<List<Double>> transposedMatrix = transposeMatrix(input);

        saveMinAndMaxToStdOutput(transposedMatrix);
    }

    public static List<List<Double>> transposeMatrix(List<List<Double>> matrix) {
        int m = matrix.size();
        int n = matrix.get(0).size();

        double[][] transposedMatrix = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j ++) {
                transposedMatrix[i][j] = matrix.get(j).get(i);
            }
        }

        List<double[]> beforeReturn = new ArrayList<>(Arrays.asList(transposedMatrix));

        List<List<Double>> toReturn = new ArrayList<>();

        for (double[] column : beforeReturn) {
            toReturn.add(DoubleStream.of(column).boxed().collect(Collectors.toList()));
        }

        return toReturn;
    }

    private static void saveMinAndMaxToStdOutput(List<List<Double>> transposedMatrix) {
        for (List<Double> column : transposedMatrix) {
            System.out.println(Collections.min(column) + DELIMITER + Collections.max(column));
        }
    }

    public static List<List<Double>> getMinAndMax(List<List<Double>> transposedMatrix) {
        List<List<Double>> toReturn = new ArrayList<>();

        for (List<Double> column: transposedMatrix) {
            List<Double> minAndMax = new ArrayList<>();
            minAndMax.add(Collections.min(column));
            minAndMax.add(Collections.max(column));

            toReturn.add(minAndMax);
        }

        return toReturn;
    }

    public static Double scaleTarget(Double target, Double colMin, Double colMax) {
        return ((target - colMin) / (colMax - colMin)) * 2.0 - 1.0;
    }

    public static Double unscaleTarget(Double target, Double colMin, Double colMax) {
        return ((target + 1.0) / (2.0)) * (colMax - colMin) + colMin;
    }
}
