import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class Regressor {
    private static final String DELIMITER = " ";
    private static final String TR_SET = "trainings";
    private static final String VAL_SET = "validations";

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

                int rowCounter = 0;
                for (List<Double> setRow : set) {
                    for (int i = 0; i < setRow.size(); i++) {
                        setRow.set(i, Scaler.scaleTarget(setRow.get(i), minAndMax.get(i).get(0), minAndMax.get(i).get(1)));
                    }

                    set.set(rowCounter, setRow);
                    rowCounter++;
                }

                Map<String, List<List<List<Double>>>> trAndVal = Validator.generateSubsets(set);

                Map<Integer, List<Double>> hyperParameterMap = new HashMap<>();

                for (int i = 1; i <= 8; i++) {
                    List<List<Double>> generatedPolynomial = Converter.generatePolynomial(polynomialDimension, i);

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

                    List<List<Double>> output = Converter.convertInputForRegressor(preparedPol, trAndVal.get(TR_SET).get(0));

                    List<List<Double>> linearPolynomial = mapGeneratedPolynomialToLinearPolynomial(generatedPolynomial);

                    output = populateWithExpectedValues(output, trAndVal.get(TR_SET).get(0));

                    Trainer.trainForRegressor(output, 1000, linearPolynomial);

                    List<Double> trainingSetOutputs = new ArrayList<>();

                    output.forEach(x -> trainingSetOutputs.add(Trainee.getResult(linearPolynomial, x)));

                    List<List<Double>> preparedValAfterTrain = Converter.convertInputForRegressor(preparedPol, trAndVal.get(VAL_SET).get(0));

                    List<Double> validationSetOutputs = new ArrayList<>();

                    preparedValAfterTrain.forEach(x -> validationSetOutputs.add(Trainee.getResult(linearPolynomial, x)));

                    double trainingSetEvaluation = Validator.getEvaluationResult(output, trainingSetOutputs);

                    double validationSetEvaluation = Validator.getEvaluationResult(preparedValAfterTrain, validationSetOutputs);

                    List<Double> finalResult = new ArrayList<>();
                    finalResult.add(trainingSetEvaluation);
                    finalResult.add(validationSetEvaluation);

                    hyperParameterMap.put(i, finalResult);
                }

                int hyper = Validator.getHyperparameter(hyperParameterMap);

                System.out.println(hyper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
