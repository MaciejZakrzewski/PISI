import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Scaler {
    private static String DELIMITER = " ";

    public static void main(String[] args) {
        List<String> argumentsList = new ArrayList<>();
        if (Arrays.asList(args).contains("-a")) {
            List<String> fileNames = new ArrayList<>();
            for (int i = argumentsList.indexOf("-a") + 1; i < argumentsList.size(); i++) {
                fileNames.add(argumentsList.get(i));
            }

            try {
                saveMinAndMax(fileNames);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
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

        List<double[]> transposedMatrix = transposeMatrix(input);




    }

    private static List<double[]> transposeMatrix(List<List<Double>> matrix) {
        int m = matrix.size();
        int n = matrix.get(0).size();

        double[][] transposedMatrix = new double[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j ++) {
                transposedMatrix[i][j] = matrix.get(j).get(i);
            }
        }

        return new ArrayList<>(Arrays.asList(transposedMatrix));
    }

    private static 
}
