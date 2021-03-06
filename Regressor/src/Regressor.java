import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Regressor {
    private static final String DELIMITER = " ";
    private static final String TR_SET = "trainings";
    private static final String VAL_SET = "validations";
    private static final int MAX_NUM_OF_ITER = 10000;

    public static void main(String[] args) {
        String setFileName = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t":
                    setFileName = args[i + 1];
                    break;
            }
        }

        if (setFileName != null) {
            try {

                List<List<Double>> set = Validator.readSet(setFileName);

                int polynomialDimension = set.get(0).size() - 1;

                List<List<Double>> minAndMax = Scaler.getMinAndMax(Scaler.transposeMatrix(set));

                scaleSet(set, minAndMax);

                Map<String, List<List<List<Double>>>> trAndVal = Validator.generateSubsets(set);

                Map<Integer, List<Double>> hyperParameterMap = new HashMap<>();

                Map<Integer, List<List<Integer>>> preparedIntegerPolynomialsMap = new HashMap<>();

                for (int i = 1; i <= 8; i++) {
                    List<List<Double>> generatedPolynomial = Converter.generatePolynomial(polynomialDimension, i);

                    List<List<Integer>> preparedPol = generateIntegerPolynomial(generatedPolynomial);

                    List<List<Double>> output;
                    List<List<Double>> linearPolynomial = mapGeneratedPolynomialToLinearPolynomial(generatedPolynomial);
                    preparedIntegerPolynomialsMap.put(i, preparedPol);
                    if (i != 1) {
                        output = Converter.convertInputForRegressor(preparedPol, trAndVal.get(TR_SET).get(0));
                        output = populateWithExpectedValues(output, trAndVal.get(TR_SET).get(0));
                    } else {
                        output = trAndVal.get(TR_SET).get(0);
                    }

                    List<double[]> convertedOutList = convertListOfLists(output);

                    List<double[]> convertedLin = convertListOfLists(linearPolynomial);

                    int actualNumOfIterations = Trainer.train(convertedOutList, MAX_NUM_OF_ITER, convertedLin);

                    output = reconvert(convertedOutList);
                    linearPolynomial = reconvert(convertedLin);

                    List<Double> trainingSetOutputs = getTrainingSetOutputs(output, linearPolynomial);

                    List<List<Double>> preparedValAfterTrain = Converter.convertInputForRegressor(preparedPol, trAndVal.get(VAL_SET).get(0));

                    List<Double> validationSetOutputs = new ArrayList<>();

                    for (List<Double> aprep : preparedValAfterTrain) {
                        validationSetOutputs.add(Trainee.getResult(linearPolynomial, aprep));
                    }

                    double trainingSetEvaluation = Validator.getEvaluationResult(output, trainingSetOutputs);

                    preparedValAfterTrain = populateWithExpectedValues(preparedValAfterTrain, trAndVal.get(VAL_SET).get(0));

                    double validationSetEvaluation = Validator.getEvaluationResult(preparedValAfterTrain, validationSetOutputs);

                    List<Double> finalResult = new ArrayList<>();
                    finalResult.add(trainingSetEvaluation);
                    finalResult.add(validationSetEvaluation);

                    hyperParameterMap.put(i, finalResult);
                }

                int hyper = Validator.getHyperparameter(hyperParameterMap);

                List<double[]> trained = trainRegressor(set, hyper);

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

                String in;

                List<List<Double>> inputs = new ArrayList<>();

                while ((in = bufferedReader.readLine()) != null) {
                    List<Double> inputParams = Arrays.stream(in.split(DELIMITER)).map(Double::valueOf).collect(Collectors.toList());
                    inputs.add(inputParams);
                }

                scaleSet(inputs, minAndMax);

                List<List<Double>> preparedTests;

                if (hyper != 1) {
                    preparedTests = Converter.convertInputForRegressor(preparedIntegerPolynomialsMap.get(hyper), inputs);
                } else {
                    preparedTests = inputs;
                }

                List<double[]> convertedTests = convertListOfLists(preparedTests);

                List<Double> finalResults = new ArrayList<>();

                convertedTests.forEach(x -> finalResults.add(Trainee.getResult(trained, x)));

                rescale(finalResults, minAndMax.get(minAndMax.size() - 1));

                finalResults.forEach(System.out::println);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<double[]> convertListOfLists(List<List<Double>> toConvert) {
        List<double[]> result = new ArrayList<>();
        for (List<Double> aConv : toConvert) {
            double[] toPut = new double[aConv.size()];
            for (int i = 0; i < aConv.size(); i++) {
                toPut[i] = aConv.get(i);
            }
            result.add(toPut);
        }

        return result;
    }

    public static List<List<Double>> reconvert(List<double[]> toConvert) {
        List<List<Double>> result = new ArrayList<>();
        for (double[] aConv : toConvert) {
            List<Double> toPut = new ArrayList<>();
            for (int i = 0; i < aConv.length; i++) {
                toPut.add(aConv[i]);
            }
            result.add(toPut);
        }
        return result;
    }

    public static List<Double> getTrainingSetOutputs(List<List<Double>> output, List<List<Double>> linearPolynomial) {
        List<Double> trainingSetOutputs = new ArrayList<>();

        output.forEach(x -> {
            List<Double> tmp = new ArrayList<>(x);

            tmp.remove(tmp.size() - 1);

            trainingSetOutputs.add(Trainee.getResult(linearPolynomial, tmp));
        });

        return trainingSetOutputs;
    }

    public static void rescale(List<Double> results, List<Double> minAndMax) {
        for (int i = 0; i < results.size(); i++) {
            results.set(i, Scaler.unscaleTarget(results.get(i), minAndMax.get(0), minAndMax.get(1)));
        }
    }

    public static void scaleSet(List<List<Double>> set, List<List<Double>> minAndMax) {
        int rowCounter = 0;
        for (List<Double> setRow : set) {
            for (int i = 0; i < setRow.size(); i++) {
                setRow.set(i, Scaler.scaleTarget(setRow.get(i), minAndMax.get(i).get(0), minAndMax.get(i).get(1)));
            }

            set.set(rowCounter, setRow);
            rowCounter++;
        }
    }

    public static List<List<Integer>> generateIntegerPolynomial(List<List<Double>> generatedPolynomial) {
        List<List<Double>> generatedPolynomialCopy = new ArrayList<>();

        generatedPolynomial.forEach(x -> {
            List<Double> helper = new ArrayList<>(x);

            generatedPolynomialCopy.add(helper);
        });

        generatedPolynomialCopy.remove(generatedPolynomialCopy.size() - 1);

        generatedPolynomialCopy.forEach(x -> x.remove(x.size() - 1));
        List<List<Integer>> preparedPol = new ArrayList<>();

        generatedPolynomialCopy.forEach(x -> {
            List<Integer> helper = new ArrayList<>();

            x.forEach(y -> helper.add(y.intValue()));

            preparedPol.add(helper);
        });
        return preparedPol;
    }

    private static List<double[]> trainRegressor(List<List<Double>> set, int hyperParameter) {
        List<List<Double>> generatedPoly = Converter.generatePolynomial(set.get(0).size() - 1, hyperParameter);

        List<List<Integer>> preparedPol = generateIntegerPolynomial(generatedPoly);

        List<List<Double>> output;
        List<List<Double>> linearPolynomial = mapGeneratedPolynomialToLinearPolynomial(generatedPoly);
        if (hyperParameter != 1) {
            output = Converter.convertInputForRegressor(preparedPol, set);
            output = populateWithExpectedValues(output, set);
        } else {
            output = set;
        }


        List<double[]> linearPolyTest = convertListOfLists(linearPolynomial);

        List<double[]> testOut = convertListOfLists(output);

        int numOfIters = Trainer.train(testOut, MAX_NUM_OF_ITER, linearPolyTest);

        return linearPolyTest;
    }

    private static List<List<Double>> populateWithExpectedValues(List<List<Double>> output, List<List<Double>> trainingSet) {
        for (int i = 0; i < output.size(); i++) {
            output.get(i).add(trainingSet.get(i).get(trainingSet.get(i).size() - 1));
        }

        return output;
    }

    private static List<List<Double>> mapGeneratedPolynomialToLinearPolynomial(List<List<Double>> polynomial) {
        List<List<Double>> result = new ArrayList<>();

        for (int i = 0; i < polynomial.size(); i++) {
            List<Double> toPut = new ArrayList<>();
            toPut.add((double) (polynomial.size() - 1 - i));
            toPut.add(polynomial.get(i).get(polynomial.get(i).size() - 1));

            result.add(toPut);
        }

        return result;
    }

}
