public class Trainer {
    private static String DELIMITER = " ";

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

        System.out.println(trainSetFileName);
        System.out.println(dataInFileName);
        System.out.println(dataOutFileName);
    }
}
