import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

                Collections.shuffle(set);
                List<List<List<Double>>> dividedSet = chunk(set, set.size() / K_FOLD);

                int counter = 1;
                for (List<List<Double>> x : dividedSet) {
                    List<List<List<Double>>> trainingSet = dividedSet.stream().filter(y -> !y.equals(x)).collect(Collectors.toList());

                    saveValidationToFile(x, trainingSet, outputDirectory, counter);

                    counter++;
                }

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

                Map<Integer, Double> result = new HashMap<>();

                evaluation.forEach((x, y) -> {
                    Double firstRes = y.get(0);
                    Double secondRes = y.get(1);

                    Double res = Math.sqrt(firstRes * firstRes + secondRes * secondRes);

                    result.put(x, res);
                });

                Integer hyper = result.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toList()).get(0).getKey();

                System.out.println(hyper);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private static void evaluate(List<List<Double>> validationSet, List<Double> output) {
//        BigDecimal result = new BigDecimal(0);
//        for (int i = 0; i < output.size(); i++) {
//            BigDecimal expected = new BigDecimal(validationSet.get(i).get(validationSet.get(i).size() - 1));
//            BigDecimal actual = new BigDecimal(output.get(i));
//            actual = actual.subtract(expected);
//            actual = actual.pow(2);
//            result = result.add(actual);
//        }
//        result = result.divide(new BigDecimal(output.size()));
//        System.out.println(result);
//    }

    private static void evaluate(List<List<Double>> validationSet, List<Double> output) {
        double result = 0;
        for (int i = 0; i < output.size(); i++) {
            double toAdd = Math.pow((validationSet.get(i).get(validationSet.get(i).size() - 1) - output.get(i)), 2);
            result += toAdd;
        }
        System.out.println(result / output.size());
    }

    private static <T> Collection<List<T>> partition(List<T> list, int size) {
        final AtomicInteger counter = new AtomicInteger(0);

        return list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
                .values();
    }

    private static <T> List<List<T>> chunk(List<T> input, int chunkSize) {
        int inputSize = input.size();
        int chunkCount = (int) Math.ceil(inputSize / (double) chunkSize);

        Map<Integer, List<T>> map = new HashMap<>(chunkCount);
        List<List<T>> chunks = new ArrayList<>(chunkCount);

        for (int i = 0; i < inputSize; i++) {

            map.computeIfAbsent(i / chunkSize, (ignore) -> {

                List<T> chunk = new ArrayList<>();
                chunks.add(chunk);
                return chunk;

            }).add(input.get(i));
        }

        return chunks;
    }

    private static void saveValidationToFile(List<List<Double>> validationSet, List<List<List<Double>>> trainingSet, String directory, int numberOfSet) throws IOException {
        List<List<Double>> joinedTrainingSet = new ArrayList<>();

        trainingSet.forEach(joinedTrainingSet::addAll);

        Path path = Paths.get(directory);

        if (!Files.exists(path)) {
            Files.createDirectory(Paths.get(directory));
        }

        String validationSetPath = directory + "/validation_set" + numberOfSet + ".txt";
        String trainingSetPath = directory + "/training_set" + numberOfSet + ".txt";

        generateFileAndSave(validationSet, validationSetPath);
        generateFileAndSave(joinedTrainingSet, trainingSetPath);
    }

    private static void generateFileAndSave(List<List<Double>> set, String path) throws IOException {
        StringBuilder result = new StringBuilder();
        for (List<Double> x : set) {
            result.append(String.join(DELIMITER, x.stream().map(String::valueOf).collect(Collectors.toList()))).append(System.lineSeparator());
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
        bw.write(result.toString());
        bw.close();
    }

    private static List<List<Double>> readSet(String setFileName) throws IOException {
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
