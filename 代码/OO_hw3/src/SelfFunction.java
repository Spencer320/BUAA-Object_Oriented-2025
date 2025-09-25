import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelfFunction {
    private final String selfDef;
    private int paramCount = 0;
    private String param1;
    private String param2;
    private String formatExpr;
    private String name;

    public SelfFunction(String selfDef) {
        this.selfDef = selfDef;
        parseSelfDef();
    }

    public String getName() {
        return name;
    }

    private void parseSelfDef() {
        String left = selfDef.split("=")[0];
        String right = selfDef.split("=")[1];
        // parse left
        Pattern headerPattern = Pattern.compile(
            "^([gh])\\(([xy])(?:,([xy]))?\\)$"
        );
        Matcher headerMatcher = headerPattern.matcher(left);
        if (headerMatcher.find()) {
            name = headerMatcher.group(1);
            param1 = headerMatcher.group(2);
            param2 = headerMatcher.group(3);
            paramCount = (param2 != null) ? 2 : 1;
        } else {
            throw new RuntimeException("selfDef should be matched");
        }
        // parse right
        formatExpr = "(" + right + ")";
    }

    public String replace(String factor) {
        if (paramCount == 1) {
            return formatExpr.replaceAll(param1, "(" + factor + ")");
        } else {
            String result = formatExpr.replace(param1, "#");
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
