import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessor {

    public static String preProcess(int numberOfSelf, ArrayList<String> self,
        int numberOfRes, HashMap<Character, String> res, String input) {
        String result = PreProcessor.simplify(input);
        if (numberOfRes == 1) {
            result = PreProcessor.replaceRes(result, new ResFunction(res));
        }
        if (numberOfSelf == 2) {
            result = PreProcessor.replaceSelf(result, new SelfFunction(self.get(1)));
            result = PreProcessor.replaceSelf(result, new SelfFunction(self.get(0)));
        } else if (numberOfSelf == 1) {
            result = PreProcessor.replaceSelf(result, new SelfFunction(self.get(0)));
        }
        return result;
    }

    public static String simplify(String expr) {
        String expression = expr;
        expression = expression.replaceAll("[ \t]", "");//remove block char
        expression = expression.replaceAll("\\++", "+");//++...+ -> +
        expression = expression.replaceAll("\\+-", "-");//+- -> -
        expression = expression.replaceAll("-\\+", "-");//-+ -> -
        expression = expression.replaceAll("--", "+");//-- -> +
        expression = expression.replaceAll("\\+-", "-");//--- -> -
        expression = expression.replaceAll("\\+\\+", "+");//--+ , +-- -> +
        expression = expression.replaceAll("(?<=\\D|^)(0+)(?=\\d)", "");//remove leading-zero
        return expression;
    }

    public static String replaceRes(String expr, ResFunction resFun) {
        String expression = expr;
        Pattern pattern = Pattern.compile("f\\{([0-5])\\}\\(");
        while (true) {
            Matcher matcher = pattern.matcher(expression);
            if (! matcher.find()) {
                break;
            }
            do {
                int num = Integer.parseInt(matcher.group(1));
                int start = matcher.start();
                int paramStart = matcher.end(); // the char next to '('
                String factor = PreProcessor.parseBracketedContent(expression, paramStart);
                int paramEnd = paramStart + factor.length();
                String left = expression.substring(0, start);
                String right = expression.substring(paramEnd + 1);
                expression = left + resFun.replace(num, factor) + right;
                break;
            } while (matcher.find());
        }
        return expression;
    }

    public static String replaceSelf(String expr, SelfFunction selfFun) {
        String expression = expr;
        Pattern pattern = Pattern.compile(selfFun.getName() + "\\(");
        while (true) {
            Matcher matcher = pattern.matcher(expression);
            if (! matcher.find()) {
                break;
            }
            do {
                int start = matcher.start();
                int paramStart = matcher.end(); // the char next to '('
                String factor = PreProcessor.parseBracketedContent(expression, paramStart);
                int paramEnd = paramStart + factor.length(); // the char ')'
                String left = expression.substring(0, start);
                String right = expression.substring(paramEnd + 1);
                expression = left + selfFun.replace(factor) + right;
                break;
            } while (matcher.find());
        }
        return expression;
    }

    public static String parseBracketedContent(String string, int begin) {
        /*
        begin is the char next to '('
        substring does not contain ')'
        pos is the char next to ')' at last
        */
        int depth = 1;
        int pos = begin;
        while (depth > 0 && pos < string.length()) {
            char c = string.charAt(pos);
            if (c == '(') {
                depth++;
            }
            if (c == ')') {
                depth--;
            }
            pos++;
        }
        return string.substring(begin, pos - 1);
    }

}
