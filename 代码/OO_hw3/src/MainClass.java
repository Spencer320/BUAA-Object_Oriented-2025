import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int n = scanner.nextInt();
        scanner.nextLine();
        ArrayList<String> self = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String nextLine = scanner.nextLine();
            self.add(PreProcessor.simplify(nextLine));
        }
        int m = scanner.nextInt();
        scanner.nextLine();
        HashMap<Character, String> res = new HashMap<>();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < 3; j++) {
                String nextLine = scanner.nextLine();
                //s[2] is the index
                res.put(nextLine.charAt(2), PreProcessor.simplify(nextLine));
            }
        }

        String input = scanner.nextLine();
        String preInput = PreProcessor.preProcess(n, self, m, res, input);
        Lexer lexer = new Lexer(preInput);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Polynomial polynomial = expr.calExpr();
        System.out.println(polynomial.toString());
    }
}
