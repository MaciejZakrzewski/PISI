import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Converter {
    private static String DELIMITER = " ";

    public static void main(String[] args) {
        Integer dimensions = null;
        Integer degree = null;
        String descriptionFileName = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-n":
                    dimensions = Integer.valueOf(args[i + 1]);
                    break;
                case "-k":
                    degree = Integer.valueOf(args[i + 1]);
                    break;
                case "-d":
                    descriptionFileName = args[i + 1];
                    break;
            }
        }

        if (dimensions != null && degree != null) {
            System.out.println(dimensions + " " + degree);

            List<List<Double>> finalList = new ArrayList<>();

            List<Double> firstRow = new ArrayList<>();
            for (int i = 0; i < degree; i++) {
                firstRow.add(Double.valueOf(dimensions));
            }

            finalList.add(firstRow);

            while (!checkIfLastRow(finalList.get(finalList.size() - 1))) {
                List<Double> result = new ArrayList<>(finalList.get(finalList.size() - 1));
                int index = 0;
                for (int i = 0; i < degree; i++) {
                    if (result.get(i) == 0.0) {
                        index = i - 1;
                        break;
                    } else {
                        index = i;
                    }
                }
                result.set(index, result.get(index) - 1);

                for (int i = index; i < degree; i++) {
                    result.set(i, result.get(index));
                }

                finalList.add(result);

            }

            populateWithParameters(finalList);

            for (List<Double> aFinalList : finalList) {
                for (int j = 0; j < aFinalList.size(); j++) {
                    if (j != aFinalList.size() - 1) {
                        System.out.print(aFinalList.get(j).intValue() + " ");
                    } else {
                        System.out.print(aFinalList.get(j));
                    }
                }
                System.out.println();
            }

        } else if (descriptionFileName != null) {
            try {
                List<double[]> description = Trainee.readDescription(descriptionFileName);

                List<List<Double>> parameters = new ArrayList<>();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                String in;

                while ((in = bufferedReader.readLine()) != null) {
                    List<Double> inputParams = Arrays.stream(in.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
                    parameters.add(inputParams);
                }

                description = description.stream().filter(x -> x[x.length - 1] != 0.0).collect(Collectors.toList());

                List<List<Double>> output = new ArrayList<>();

                for (List<Double> params : parameters) {
                    List<Double> result = new ArrayList<>();

                    for (double[] descArray : description) {
                        double value = 1.0;

                        for (int i = 0; i < descArray.length - 1; i++) {
                            value *= params.get(i);
                        }
                        result.add(value);
                    }
                    output.add(result);
                }

                displayConvertedInput(output);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static double roundToFirstDecimalPlace(double value) {
        int scale = (int) Math.pow(10, 1);
        return (double) Math.round(value * scale) / scale;
    }

    private static boolean checkIfLastRow(List<Double> row) {
        return row.stream().noneMatch(aRow -> aRow != 0.0);
    }

    private static void populateWithParameters(List<List<Double>> result) {
        for (List<Double> aResult : result) {
            aResult.add(roundToFirstDecimalPlace(ThreadLocalRandom.current().nextDouble(-1, 1)));
        }
    }

    private static void displayConvertedInput(List<List<Double>> converted) {
        for (List<Double> outList : converted) {
            StringBuilder toDisplay = new StringBuilder();
            for (Double out : outList) {
                toDisplay.append(out).append(DELIMITER);
            }
            System.out.println(toDisplay);
        }
    }
}
