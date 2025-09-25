# 任务描述  
* 基于hw1迭代开发
* 新增支持嵌套多层括号
* 新增三角函数因子
* 新增自定义递推函数因子
* 输入模式修改:本次作业的输入数据包含若干行：
第一行为一个整数 n(0≤n≤1),表示自定义递推函数定义的个数.
第 2 到第 3n+1 行,每行一个字符串,每三行表示一组自定义递推函数的定义.
第 3n+2 行,一行字符串,表示待展开表达式.
* 输出要求修改:
  展开所有括号 &rarr; 不含有自定义递推函数,且只包含必要的括号

### 新增概念
1.三角函数:
* __一般形式__ 类似于幂函数,由`sin(<factor>)`或`cos(<factor>)`加可能的指数组成;
* __省略形式__ 当指数为 1 的时候,可以采用省略形式,省略指数符号^和指数部分,如:`sin(x)`;
* "三角函数( tri_factor )"仅包含`sin`和`cos`.
  
2.自定义递推函数:
* 自定义递推函数的定义形如
```
f{n}(x, y) = 递推表达式
f{0}(x, y) = 函数表达式
f{1}(x, y) = 函数表达式
```
三者顺序任意,以换行分隔,定义中默认 n > 1 
* 仅以 `f` 为递推函数的函数名;
* 仅以`n 0 1`作为递推函数的序号;
* 仅以`x y`作为递推函数的形参,形参数为1或2
* 递推表达式是一个关于形参的表达式,保证其中 `f{n-1}`和`f{n-2}` __各被且只被调用 1 次__,并且调用前需要 __和一个常数因子相乘__
* 保证没有其他函数的调用
* 自定义递推函数的调用形式为:`f{<constant_factor>}(<factor>, <factor>)`.其中`constant_factor`为调用时序号,0 ≤ 序号 ≤ 5,`factor`为实参,可以是任意因子
* 关于空白字符,保证`sin` 和 `cos` ,`f{序号}`关键字中不包含空白字符

### 更新形式化表述
| 中文名词         | 英文                | 符号表述         |
|------------------|--------------------|------------------|
自变量|Variable|&lt;variable&gt;
三角函数|Trigonometric Function	|&lt;trig_function&gt;
函数调用|Function Call|&lt;func_call&gt;
换行|Newline|&lt;newline&gt;
自定义递推函数定义|Recursive Definition|&lt;recursive_def&gt;
定义列表|Definition List|&lt;definition_list&gt;
初始定义|Initial Definition|&lt;initial_def&gt;
初始序号|Initial Index|&lt;initial_idx&gt;
递推定义|Recursive Definition|&lt;recursive_def_part&gt;
序号|Index|&lt;index&gt;
形参自变量|Formal Parameter|&lt;formal_param&gt;
自定义递推函数调用|Recursive Call|&lt;recursive_call&gt;
自定义递推函数调用n-1|Recursive Call n-1|&lt;recursive_call_n1&gt;
自定义递推函数调用n-2|Recursive Call n-2|&lt;recursive_call_n2&gt;
递推表达式|Recursive Expression|&lt;recursive_expr&gt;
函数表达式|Function Expression|&lt;func_expr&gt;

原始规则相关:
* &lt;variable&gt; ::= 'x'
* &lt;variable_factor&gt; ::= &lt;power_function&gt; | &lt;trig_function&gt;     | &lt;func_call&gt; 
* &lt;trig_function&gt; ::= 'sin' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;factor&gt; &lt;blank_item&gt; ')' [&lt;blank_item&gt; &lt;exponent&gt;]
  | 'cos' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;factor&gt; &lt;blank_item&gt; ')' [&lt;blank_item&gt; &lt;exponent&gt;]
* &lt;func_call&gt; ::= &lt;recursive_call&gt; 
* &lt;newline&gt; ::= '\n'
  
自定义递推函数相关:
* &lt;recursive_def&gt; ::= &lt;definition_list&gt;
* &lt;definition_list&gt; ::= 
  &lt;initial_def&gt; &lt;newline&gt; &lt;initial_def&gt; &lt;newline&gt; &lt;recursive_def_part&gt;
| &lt;initial_def&gt; &lt;newline&gt; &lt;recursive_def_part&gt; &lt;newline&gt; &lt;initial_def&gt;
| &lt;recursive_def_part&gt; &lt;newline&gt; &lt;initial_def&gt; &lt;newline&gt; &lt;initial_def&gt;
* &lt;initial_def&gt; ::= 'f' '{' &lt;initial_idx&gt; '}' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;] ')' &lt;blank_item&gt; '=' &lt;blank_item&gt; &lt;func_expr&gt;
* &lt;initial_idx&gt; ::= '0' | '1'
* &lt;recursive_def_part&gt; ::= 'f{n}' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;] ')' &lt;blank_item&gt; '=' &lt;blank_item&gt; &lt;recursive_expr&gt;
* &lt;index&gt; ::= '0' | '1' | '2' | '3' | '4' | '5'
* &lt;formal_param&gt; ::= 'x' | 'y'
* &lt;recursive_call&gt; ::= 'f{' &lt;index&gt; '}' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;factor&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;factor&gt; &lt;blank_item&gt;] ')'
* &lt;recursive_call_n1&gt; ::= 'f{n-1}' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;factor_no_call&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;factor_no_call&gt; &lt;blank_item&gt;] ')'
* &lt;recursive_call_n2&gt; ::= 'f{n-2}' &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;factor_no_call&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;factor_no_call&gt; &lt;blank_item&gt;] ')'
* &lt;recursive_expr&gt; ::= &lt;constant_factor&gt; &lt;blank_item&gt; '\*' &lt;blank_item&gt; &lt;recursive_call_n1&gt; &lt;blank_item&gt;&lt;add_sub&gt; &lt;blank_item&gt; &lt;constant_factor&gt; &lt;blank_item&gt; '*' &lt;blank_item&gt; &lt;recursive_call_n2&gt;[&lt;blank_item&gt; '+' &lt;blank_item&gt; &lt;func_expr&gt;]
* &lt;func_expr&gt; ::= &lt;expr_no_call&gt;
  
注:
* 变量因子,幂函数被更新
* 三角函数,函数调用,换行被增加
* 保证`自定义递归函数调用n-1`和`自定义递归函数调用n-2`中的因子,不会出现`函数调用`;保证`函数表达式`中,不会出现`函数调用`.也就是说, 在`自定义递推函数定义`中,__不会出现__`函数调用`   
* `函数调用`时,实参允许出现`函数调用`
* 函数形参不重复出现,函数定义式中所有变量均在形参中定义
* 定义时函数参数个数与顺序应一致

### 括号保留的正确性判定
* 三角函数调用时必要的一层括号：`sin()` 与 `cos()`.
* 三角函数对应的嵌套因子为不带指数的表达式因子时,该表达式因子两侧必要的一层括号：`sin((x+x))` 与 `cos((x*x))`.(注意是"不带指数"的表达式因子,如果是`sin((x+1)^2)`,这并不符合必要括号的定义,你必须将其展开为`sin((x^2+2*x+1))`这种类似的形式才是合法的)
* 同样,例如 `sin(1)` 与 `sin((1))` 均为展开形式,但 `sin(((1)))` 不是,因为后者除了函数调用和三角嵌套表达式因子的一层括号外,还包括了表达式内嵌套表达式的括号
  
### 其他规则
* 最后一行输入的待展开表达式的有效长度至多为 200 个字符,自定义递推函数定义时,每个定义的有效长度至多为 75 个字符
* 互测最终输入表达式的有效长度至多为 50 个字符,递推定义的有效长度至多 50 个字符，初始定义的有效长度至 30 个字符,引入Cost规则
  
# 思路分析
Step1: `preProcesser`
* 沿用正则表达式,但将其独立出来
* 提前进行一次表达式分割,将自定义递推函数替换掉

Step2: `Lexer`and`Parser`
* 关于三角函数的分割,为`Token.Type`新增`SIN`和`COS`即可
* 新增`TrigFactor`类,为`Factor`的一种,管理属性为:
  * trig 正弦或余弦
  * factor 其内部的因子,可能是 __任意__ 一种因子
  * exp 指数,默认为1
  * reverse 正负号
* `TrigFactor`的解析:
  * 当`ParseFactor`遇到的`token`类型为`SIN`或`COS`时
  * 为确保文法正确,三角函数一定有一组可略过的括号
  * 嵌套内部调用`ParseFactor`
  * 鉴于可能出现指数的位置较多(表达式因子,幂函数,三角函数),抽象出`ParseExponent`方法,返回`int`类型的`exp`
  
Step3:`Operator`
* 需要进行重构,将`Polynomial`类改为拥有`HashSet<Monomial>`属性
  * 编写`add`,`multiply`,`power`方法
  * 重写相关方法
  * 方法实质应为循环调用其内部的`mono`,将计算交给`Monomial`类
* `Monomial`类的属性由其形式决定:
  * $mono = tx^a\prod sin^i(poly_k)\prod cos^j(poly_l)$
  * 这意味着`Monomial`将有以下属性:
    * `BigInteger coef` : 系数 $t$
    * `Integer exp`  : 指数 $a$
    * `HashMap<Polynomial, Integer> sinArray` : $[key , value] = [poly_k , i]$ 
    * `HashMap<Polynomial, Integer> cosArray` : $[key , value] = [poly_l , j]$ 
* 加法设计:
  * `mono`能够相加的充要条件为它们的 `sinArray` 和 `cosArray` 完全相同且 `exp` 相等  
  * 若可加,则系数相加即可，否则为两个不同项
  * `poly`相加的实质是加数的每一个 `mono` 对加数的 `mono` 是否可加进行判断
* 乘法设计:
  * 乘法是无条件的
  * `poly`相乘应拆解为若干相乘结果相加
  * `mono`的相乘需要注意合并三角函数
* 乘方设计:
  * 相当于若干次乘法,这样可以最大程度保证正确性
* 运算设计：
  * 重构`equals`方法,编写`comparable`方法,以判断是否可加
  * 编写`getComparableMono`方法,作为层次间的链接

Step4:`RecursiveFunction`
第一行输入判断是否存在自定义递推函数,考量递推定义和初始定义:
* 递推定义: $f_{\{n\}(x,y)}=a*f_{\{n-1\}(factor_{t1},factor_{t2})}+b*f_{\{n-2\}(factor_{s1},factor_{s2})}+expr$
* 初始定义: $f_{\{0/1\}(x,y)}=expr_{0/1}$  
* 所有信息由以下部分构成:a,b,expr,四个factor(单参数为两个)
发现其实$f_{\{0-5\}}$都能够改写为$expr$,所以可以事先构造好一个`ArrayList<String>`作为被替换的式子  
所以要做的事情有:
* 区分出定义列表的三个表达式
* 解析原始定义,得到0和1的表达式
* 解析递归定义,得到参数数量,系数和因子的信息
* 建立替换方法,需要着重考虑嵌套调用的情况,考量因子的正确切分

Step5:`CorrectCheck`
最终输出必须符合括号的正确性判断
本次的策略是保住正确分,因而:
* 凡是出现三角函数,直接 __双层括号__
* 除了合并同类项以外,不进行任何其他合并
* 允许合理的连续加减存在
* `StringBuilder`的原则,为任意一步添加完成后均得到合法输出

Debug1:
本次出现的正确性问题为,由`equals`方法的误写导致的三角函数合并出错

Debug2:
进行基础的优化:
* hw1已给出的优化(hw2重构后没加):
  * 指数为0时不需要输出x^0;
  * 指数为1时不需要输出^1;
  * 系数为1或-1时不需要输出1;
  * 除非输出为空,否则不输出值为0的项;
* 优化计算过程,__0项不计入下一步计算__
* 不再允许连续加减号,各项符号改为前置
* sin(0)和cos(0)的输出修改为 1 和 0 ,但不改变其他的输出
* 修正了错误单词: `PreProcesser` &rarr; `PreProcessor`

# 互测
测试点:
```
//test1:
input:
1
f{0}(x,y)=--x
f{1}(x,y)=-+(-y)
f{n}(x,y)=-3*f{n-1}(y,x)+4*f{n-2}(x,y)
f{2}((1*f{0}(f{1}(0,2),0)),f{2}(1,0))+1
output:
3
```
```
//test2:
input:
0
sin(((x+x)))
output:
sin((2*x))
```
```
//test3:
input:
1
f{0}(x)=cos(x)^2
f{1}(x)=sin(x)^2
f{n}(x)=1*f{n-1}(x)+1*f{n-2}(x)
sin(f{4}(cos(x)))*f{2}(cos((x^3+21)))
output:
sin((2+sin(cos(x))^2))
```
```
//test5
input:
1
f{n}(x,y)=2*f{n-1}((x+y),sin((x*y)))+6*f{n-2}(x,y)
f{0}(x,y)=(x-1)^2-sin(cos(y))
f{1}(x,y)=x^3+y*cos((x+ -9))
-f{2}(0,(x-x)^0)
output:
-8+6*sin(cos(1))
```
```
//test6:
input:
1
f{0}(y,x)=-+(-y)
f{1}(y,x)=0
f{n}(y,x)=0*f{n-1}(y,x)+1*f{n-2}((-x),y)
f{2}(-5,3)
output:
-3
```
```
//test7
input:
0
-x^+02 + sin((x * (-x)))^+03
output:
-x^2+sin((-x^2))^3
```
```
//test8
input:
0
cos((x + sin((x - +03))))^+01
output:
cos((x+sin((x-3))))
```
```
//test9
input:
0
cos(0)
output:
1
```
```
//test10
input:
0
-(x * -01)^+03 + sin((-x))^+02
output:
x-2
```