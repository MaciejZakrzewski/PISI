import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Converter {
    private static String DELIMITER = " ";
    private static final Random random = new Random();

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

            List<List<Double>> finalList = generatePolynomial(dimensions, degree);

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
                List<List<Integer>> description = readDescriptionForConversion(descriptionFileName);

                List<List<Double>> parameters = new ArrayList<>();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                String in;

                while ((in = bufferedReader.readLine()) != null) {
                    List<Double> inputParams = Arrays.stream(in.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
                    parameters.add(inputParams);
                }

                description.remove(description.size() - 1);

                List<List<BigDecimal>> output = convertInput(description, parameters);

                displayConvertedInput(output);

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<List<BigDecimal>> convertInput(List<List<Integer>> description, List<List<Double>> parameters) {
        List<List<BigDecimal>> output = new ArrayList<>();

        for (List<Double> params : parameters) {
            List<BigDecimal> result = new ArrayList<>();
            for (List<Integer> descArray : description) {
                BigDecimal value = BigDecimal.ONE;
                for (Integer aDescArray : descArray) {
                    if (aDescArray != 0) {
                        value = value.multiply(BigDecimal.valueOf(params.get(aDescArray - 1)));
                    }
                }
                result.add(value);

            }

            output.add(result);
        }
        return output;
    }

    public static List<List<Double>> convertInputForRegressor(List<List<Integer>> description, List<List<Double>> parameters) {
        List<List<Double>> output = new ArrayList<>();

        for (List<Double> params : parameters) {
            List<Double> result = new ArrayList<>();
            for (List<Integer> descArray : description) {
                Double value = 1d;
                for (Integer aDescArray : descArray) {
                    if (aDescArray != 0) {
                        value *= params.get(aDescArray - 1);
                    }
                }
                result.add(value);
            }
            output.add(result);
        }
        return output;
    }

    public static List<List<Double>> generatePolynomial(Integer dimensions, Integer degree) {
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
        return finalList;
    }

    private static boolean checkIfLastRow(List<Double> row) {
        return row.stream().noneMatch(aRow -> aRow != 0.0);
    }

    private static void populateWithParameters(List<List<Double>> result) {
        for (List<Double> aResult : result) {
            aResult.add(random.nextDouble() * 2.0 - 1.0);
        }
    }

    private static void displayConvertedInput(List<List<BigDecimal>> converted) {
        for (List<BigDecimal> outList : converted) {
            StringBuilder toDisplay = new StringBuilder();
            for (BigDecimal out : outList) {
                toDisplay.append(out).append(DELIMITER);
            }
            System.out.println(toDisplay);
        }
    }

    private static List<List<Integer>> readDescriptionForConversion(String descriptionFileName) throws IOException {
        File file = new File(descriptionFileName);

        BufferedReader br = new BufferedReader(new FileReader(file));

        String st;

        br.readLine();

        List<List<Integer>> parameters = new ArrayList<>();

        while ((st = br.readLine()) != null) {
            List<Integer> params = new ArrayList<>();
            String[] split = st.split(DELIMITER);

            for (int i = 0; i < split.length - 1; i++) {
                if (Double.valueOf(split[split.length - 1]) != 0.0) {
                    params.add(Integer.valueOf(split[i]));
                }
            }

            parameters.add(params);
        }

        br.close();

        return parameters;
    }
}
