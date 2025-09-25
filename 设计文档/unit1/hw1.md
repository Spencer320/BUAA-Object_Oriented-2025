# 任务描述
* 读入一个包含加、减、乘、乘方以及括号（其中括号的深度 __至多为 1 层__ ）的 __单变量__ 表达式，输出 __恒等变形展开所有括号后的表达式__
### 形式化表述
| 中文名词         | 英文                | 符号表述         |
|------------------|--------------------|------------------|
| 表达式           | Expression         | &lt;expr&gt;         |
| 项               | Term               | &lt;term&gt;         |
| 因子             | Factor             | &lt;factor&gt;       |
| 变量因子         | Variable Factor    | &lt;variable_factor&gt; |
| 常数因子         | Constant Factor    | &lt;constant_factor&gt; |
| 表达式因子       | Expression Factor  | &lt;expr_factor&gt;  |
| 幂函数           | Power Function     | &lt;power_function&gt; |
| 指数             | Exponent           | &lt;exponent&gt;     |
| 带符号的整数     | Signed Integer     | &lt;signed_integer&gt; |
| 允许前导零的整数 | Leading Zero Integer | &lt;leading_zero_integer&gt; |
| 空白项           | Blank Item         | &lt;blank_item&gt;   |
| 空白字符         | Blank Character    | &lt;blank_char&gt;   |
| 加减             | Addition/Subtraction | &lt;add_sub&gt;      |

* &lt;expr&gt; ::= &lt;blank_item&gt; [&lt;add_sub&gt; &lt;blank_item&gt;] &lt;term&gt; &lt;blank_item&gt;           
 | &lt;expr&gt; &lt;add_sub&gt; &lt;blank_item&gt; &lt;term&gt; &lt;blank_item&gt;
* &lt;term&gt; ::= [&lt;add_sub&gt; &lt;blank_item&gt;] &lt;factor&gt;
           | &lt;term&gt; &lt;blank_item&gt; '*' &lt;blank_item&gt; &lt;factor&gt;
* &lt;factor&gt; ::= &lt;variable_factor&gt; 
             | &lt;constant_factor&gt;
             | &lt;expr_factor&gt; 
* &lt;variable_factor&gt; ::= &lt;power_function&gt; 
* &lt;constant_factor&gt; ::= &lt;signed_integer&gt; 
* &lt;expr_factor&gt; ::= '(' &lt;expr&gt; ')' [&lt;blank_item&gt; &lt;exponent&gt;]
* &lt;power_function&gt; ::= 'x' [&lt;blank_item&gt; &lt;exponent&gt;]
* &lt;exponent&gt; ::= '^' &lt;blank_item&gt; ['+'] &lt;leading_zero_integer&gt;
* &lt;signed_integer&gt; ::= [&lt;add_sub&gt;] &lt;leading_zero_integer&gt;
* &lt;leading_zero_integer&gt; ::= ('0' | '1' | '2' | ... | '9') {'0' | '1' | '2' | ... | '9'}
* &lt;blank_item&gt; ::= {&lt;blank_char&gt;}
* &lt;blank_char&gt; ::= ' ' | '\t'
* &lt;add_sub&gt; ::= '+' | '-'

### 数据限制
* 输入表达式中至多包含1层括号
* 指数一定不是负数，且最大不超过 8
* 默认 0 ^ 0 = 1
* 有效长度（去掉所有空白符后字符总数）至多为 200 个字符（互测为 50 个字符）
* 整数的范围并不一定在int或long范围内
* 互测中，引入 Cost 规则

# 思路分析
Step1:`Regular`
预处理:
* 删去空白符,`replaceAll("[ \t]", "")`;
* 删去先导零,`replaceAll("(?<=\\D|^)(0+)(?=\\d)", "")`;
* 删去冗余加减,即紧邻的'+' '-'(两或三个化为一个).
```  
public String replaceAll(String regex, String replacement)
``` 
这样处理后,字符串具有如下特点:
* 无空白符
* 无冗余加减(但仍可能有*+等情况)
* 无先导零,即数字一定有数学含义
  
Step2:`Lexer`
按形式化表述进行下降解析的需求:
* 表达式类 &rarr; Expr
* 项类 &rarr; Term
* 因子类 &rarr; Factor &larr; Number & Variable & SubExpr
* 表达式因子:实质是子表达式加括号和可能的指数 优先级由语法树结构体现
* 常数因子:可以单独用 Number 来存
* 变量因子:可以单独用 Variable 来存

故而,Token类型有: `+ - * ^ ( ) Var Num `,
其中,`Var`包含一个指数，`Num`为无符号,无并应当使用`BigInteger`;
于是,能得到一个`Token`的`ArrayList`.

所有`factor`都有符号,用一个`reverse`符号标识.
关于`factor`符号的处理,需要考虑`-`可能出现的位置:
* 两项之间,此时在`parseExpr`被发现,规定当发现时,下一项的`reverse`反转
* 任何因子左侧,此时在`parseFactor`被发现,规定当发现时,下一因子的`reverse`反转
* expr的reverse应始终为false(因为reverse被exprFactor占去)
* term所有的reverse默认跟随expr
* factor仅首个reverse跟随term,其他factor默认为false
* `reverse`作为参数在`parse`间传递

Step3:`Parse`
语法树(优先级):
Expr = Term [+ -] Term ... [+ -] Term 
Term =  Factor * Factor ... * Factor
Factor = SubExpr | Number | Variable^Exponent
SubExpr = (Expr)^Exponent
...(一旦遇到括号,即递归调用parseExpr)
至此,读入工作完成

Step4:`Operator` 
统一采用 __多项式__ 计算,用`TreeMap<Integer,BigInteger>`存储`[exp,coeff]`.
Operator | Operating Class |
---|---
|ADD/SUB|Term|
| MUL |Factor|
|POWER|Expr|

常数因子和变量因子也视为多项式;
乘方运算简单视为指数个相同多项式相乘.

Step5:`Polynomial`
使用多项式运算完成计算与输出.
需要实现的计算有:加法,乘法,乘方.
输出的形式为:
Output ::= coeff\*x\^exp+...+coeff\*x\^exp
为确保输出最短,需要:
指数为0时不需要输出x^0;
指数为1时不需要输出^1;
系数为1或-1时不需要输出1;
除非输出为空,否则不输出值为0的项;
除非必须,否则首项不选择带'-'的项;

# 互测
(C组)找到的测试点:
`x^0*0`
`11*x`
`input:-5*x^8+x*-6*x+x*(+8)^2-x+1-7` `output:-5*x^8-6*x^2+63*x-6`
`-(+4-5)^2`
`input:(99*x^1)^3*(99*x^2)^2*(9999*x^3)^1*(999*x^8)^2` `output:94899411188817087501*x^26`
`(0)`