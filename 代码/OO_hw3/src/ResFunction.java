import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResFunction {
    private final String initDef0;
    private final String initDef1;
    private final String resDef;
    private int paramCount = 0;
    private final ArrayList<String> factors = new ArrayList<>();
    private final String[] coef = new String[2];
    private String param1;
    private String param2;
    private String expr;
    private final ArrayList<String> formatExpr = new ArrayList<>();

    public ResFunction(HashMap<Character, String> res) {
        //remove block char
        initDef0 = res.get('0');
        initDef1 = res.get('1');
        resDef = res.get('n');
        createFormatExpr();
    }

    private void createFormatExpr() {
        //init def -> (expr)
        parseInitDef();
        //res def
        parseResDef();
        //cal res to expr
        for (int i = 2; i <= 5; i++) {
            calFormatExpr(i);
        }
    }

    private void parseInitDef() {
        Pattern pattern = Pattern.compile("=(.*)");
        Matcher matcher0 = pattern.matcher(initDef0);
        Matcher matcher1 = pattern.matcher(initDef1);
        if (matcher0.find()) {
            formatExpr.add("(" + matcher0.group(1) + ")");
        } else {
            throw new RuntimeException("initDef0 should have char'='");
        }
        if (matcher1.find()) {
            formatExpr.add("(" + matcher1.group(1) + ")");
        } else {
            throw new RuntimeException("initDef1 should have char'='");
        }
    }

    private void parseResDef() {
        String left = resDef.split("=")[0];
        String right = resDef.split("=")[1];
        //param
        parseLeft(left);
        //coef , factor and leftover
        String[] s = parseRight(right);
        if (paramCount == 1) {
            factors.add("(" + s[0] + ")");
            factors.add("(" + s[1] + ")");
        } else {
            factors.add("(" + getReplaceFactor(s[0])[0] + ")");
            factors.add("(" + getReplaceFactor(s[0])[1] + ")");
            factors.add("(" + getReplaceFactor(s[1])[0] + ")");
            factors.add("(" + getReplaceFactor(s[1])[1] + ")");
        }
    }

    private void parseLeft(String left) {
        Pattern headerPattern = Pattern.compile(
            "^f\\{n}\\(([xy])(?:,([xy]))?\\)$"
        );
        Matcher headerMatcher = headerPattern.matcher(left);
        if (headerMatcher.find()) {
            param1 = headerMatcher.group(1);
            param2 = headerMatcher.group(2);
            paramCount = (param2 != null) ? 2 : 1;
        } else {
            throw new RuntimeException("resDef should be matched");
        }
    }

    private String[] parseRight(String right) {
        int beginIndex = 0;
        int endIndex = 0;
        int parseIndex = 1;
        String[] s = new String[2];
        while (beginIndex < right.length()) {
            switch (parseIndex) {
                case 1: //get coef
                    endIndex = right.indexOf('*', beginIndex);
                    coef[0] = right.substring(beginIndex, endIndex);
                    parseIndex = 2;
                    break;
                case 2: //get s1
                    beginIndex = right.indexOf("}", endIndex) + 2;
                    s[0] = PreProcessor.parseBracketedContent(right, beginIndex);
                    parseIndex = 3;
                    beginIndex += s[0].length() + 1;//skip ')'
                    break;
                case 3: //get coef
                    endIndex = right.indexOf('*', beginIndex);
                    coef[1] = right.substring(beginIndex, endIndex);
                    parseIndex = 4;
                    break;
                case 4: //get s2
                    beginIndex = right.indexOf("}", endIndex) + 2;
                    s[1] = PreProcessor.parseBracketedContent(right, beginIndex);
                    parseIndex = 5;
                    beginIndex += s[1].length() + 1;//skip ')'
                    break;
                case 5: // leftover,if possible
                    expr = right.substring(beginIndex);
                    beginIndex = right.length();
                    break;
                default:
            }
        }
        return s;
    }

    private void calFormatExpr(int index) {
        String s1 = formatExpr.get(index - 1);
        String s2 = formatExpr.get(index - 2);
        if (paramCount == 1) {
            // only 2 factor here
            s1 = s1.replace(param1, factors.get(0));
            s2 = s2.replace(param1, factors.get(1));
        } else {
            // 4 factor : f_n-1_1,f_n-1_2,f_n-2_1,f_n-2_2
            // use unique char to avoid repeated replace
            s1 = s1.replace(param1, "#");
            s1 = s1.replace(param2, factors.get(1));
            s1 = s1.replace("#", factors.get(0));
            s2 = s2.replace(param1, "#");
            s2 = s2.replace(param2, factors.get(3));
            s2 = s2.replace("#", factors.get(2));
        }
        if (expr != null) {
            formatExpr.add("(" + coef[0] + "*" + s1 + coef[1] + "*" + s2 + expr + ")");
        } else {
            formatExpr.add("(" + coef[0] + "*" + s1 + coef[1] + "*" + s2 + ")");
        }
    }

    public String replace(int index, String factor) {
        String format = formatExpr.get(index);
        if (paramCount == 1) {
            return format.replaceAll(param1, "(" + factor + ")");
        } else {
            String result = format.replace(param1, "#");
            String[] factors = getReplaceFactor(factor);
            result = result.replaceAll(param2, "(" + factors[1] + ")");
            return result.replaceAll("#", "(" + factors[0] + ")");
        }
    }

    private String[] getReplaceFactor(String factor) {
        String[] result = new String[2];
        int depth = 0;
        int index = 0;
        // search for ',' that make all "()" paired
        for (int i = 0; i < factor.length(); i++) {
            if (factor.charAt(i) == '(') {
                depth++;
            }
            if (factor.charAt(i) == ')') {
                depth--;
            }
            if (factor.charAt(i) == ',' && depth == 0) {
                index = i;
                break;
            }
        }
        result[0] = factor.substring(0, index);
        result[1] = factor.substring(index + 1);
        return result;
    }
}
