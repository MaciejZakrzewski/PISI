import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Validator {
    private static final String DELIMITER = " ";
    private static final int K_FOLD = 2;

    public static void main(String[] args) {
        String setFileName = null;
        String outputDirectory = null;
        String inputValidationSet = null;
        String evaluationFileName = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-g":
                    setFileName = args[i + 1];
                    break;
                case "-d":
                    outputDirectory = args[i + 1];
                    break;
                case "-e":
                    inputValidationSet = args[i + 1];
                    break;
                case "-v":
                    evaluationFileName = args[i + 1];
            }
        }

        if (setFileName != null && outputDirectory != null) {
            try {
                List<List<Double>> set = readSet(setFileName);

                generateSubsetsAndSaveToFile(outputDirectory, set);

                System.out.println(K_FOLD);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (inputValidationSet != null) {
            try {
                List<List<Double>> validationSet = readSet(inputValidationSet);

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                String line;

                List<Double> outputList = new ArrayList<>();

                while ((line = br.readLine()) != null) {
                    outputList.add(Double.parseDouble(line));
                }

                evaluate(validationSet, outputList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (evaluationFileName != null) {
            try {
                Map<Integer, List<Double>> evaluation = readEvaluation(evaluationFileName);

                Integer hyper = getHyperparameter(evaluation);

                System.out.println(hyper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Integer getHyperparameter(Map<Integer, List<Double>> evaluation) {
        Map<Integer, Double> result = new HashMap<>();

        evaluation.forEach((x, y) -> {
            Double firstRes = y.get(0);
            Double secondRes = y.get(1);

            Double res = Math.sqrt(firstRes * firstRes + secondRes * secondRes);

            result.put(x, res);
        });

        return result.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList()).get(0).getKey();
    }

    public static void generateSubsetsAndSaveToFile(String outputDirectory, List<List<Double>> set) throws IOException {
        int help = 0;
        int chunkSize = set.size() / K_FOLD;
        int counter = 1;
        for (int i = 0; i < K_FOLD; i++) {
            List<List<Double>> setCopy = new ArrayList<>(set);
            List<List<Double>> validation = set.subList(help, help + chunkSize);

            for (int j = help; j < help + chunkSize; j++) {
                setCopy.remove(help);
            }

            saveValidationToFile(validation, setCopy, outputDirectory, counter);
            counter++;
            help += chunkSize;
        }
    }

    public static Map<String, List<List<List<Double>>>> generateSubsets(List<List<Double>> set) {
        int help = 0;
        int chunkSize = set.size() / 2;
        Map<String, List<List<List<Double>>>> result = new HashMap<>();
        List<List<List<Double>>> validations = new ArrayList<>();
        List<List<List<Double>>> trainings = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<List<Double>> setCopy = new ArrayList<>(set);
            List<List<Double>> validation = set.subList(help, help + chunkSize);

            for (int j = help; j < help + chunkSize; j++) {
                setCopy.remove(help);
            }

            validations.add(validation);
            trainings.add(setCopy);
            help += chunkSize;
        }

        result.put("trainings", trainings);
        result.put("validations", validations);

        return result;
    }

    private static void evaluate(List<List<Double>> validationSet, List<Double> output) {
        System.out.println(getEvaluationResult(validationSet, output));
    }

    public static double getEvaluationResult(List<List<Double>> validationSet, List<Double> output) {
        double result = 0;
        for (int i = 0; i < output.size(); i++) {
            double toAdd = Math.pow((validationSet.get(i).get(validationSet.get(i).size() - 1) - output.get(i)), 2);
            result += toAdd;
        }
        return result / output.size();
    }

    private static void saveValidationToFile(List<List<Double>> validationSet, List<List<Double>> trainingSet, String directory, int numberOfSet) throws IOException {
        Path path = Paths.get(directory);

        if (!Files.exists(path)) {
            Files.createDirectory(Paths.get(directory));
        }

        String validationSetPath = directory + "/validation_set" + numberOfSet + ".txt";
        String trainingSetPath = directory + "/training_set" + numberOfSet + ".txt";

        generateFileAndSave(validationSet, validationSetPath);
        generateFileAndSave(trainingSet, trainingSetPath);
    }

    private static void generateFileAndSave(List<List<Double>> set, String path) throws IOException {
        StringBuilder result = new StringBuilder();
        for (List<Double> x : set) {
            for (int i = 0; i < x.size(); i++) {
                result.append(x.get(i));
                if (i != x.size() - 1) {
                    result.append(DELIMITER);
                } else {
                    result.append("\n");
                }
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
        bw.write(result.toString());
        bw.close();
    }

    public static List<List<Double>> readSet(String setFileName) throws IOException {
        List<List<Double>> set = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(new File(setFileName)));

        String line;
        while ((line = br.readLine()) != null) {
            set.add(Arrays.stream(line.split(DELIMITER)).mapToDouble(Double::parseDouble).boxed().collect(Collectors.toList()));
        }

        br.close();

        return set;
    }

    private static Map<Integer, List<Double>> readEvaluation(String evaluationFileName) throws IOException {
        Map<Integer, List<Double>> result = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(new File(evaluationFileName)));

        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(DELIMITER);
            result.put(Integer.valueOf(split[0]), new ArrayList<Double>() {{
                add(Double.valueOf(split[1]));
                add(Double.valueOf(split[2]));
            }});
        }

        return result;
    }
}
