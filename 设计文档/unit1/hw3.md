# 任务描述  
* 基于hw2迭代开发
* 新增自定义普通函数因子
* 新增求导算子
* 输入模式修改:本次作业的输入数据包含若干行：
第一行为一个整数 n(0≤n≤2),表示自定义普通函数定义的个数.
第 2 到第 n+1 行,每行一个字符串,每行表示一个自定义普通函数的定义.
第 n+2 行为一个整数 m(0≤m≤1),一行字符串,表示自定义递推函数定义的个数.
第 n+3 到第 n+2+3m 行,每行一个字符串,每三行表示一组自定义递推函数的定义.
第 n+3+3m 行,一行字符串,表示待展开表达式.
* 不含有自定义递推函数,且只包含必要的括号 &rarr; 不含有自定义函数和求导算子,且只包含必要的括号

### 新增概念 与 形式化表述
1.更新形式化描述
| 中文名词         | 英文                | 符号表述         |
|------------------|--------------------|------------------|
自定义普通函数定义|Self Function Definition|&lt;self_func_def&gt;
自定义普通函数名|Self Function Name |&lt;self_func_name&gt;
自定义递推普通调用|Self Function Call|&lt;self_func_call&gt;
求导算子|Derivation|&lt;derivation&gt;
求导因子|Derivative Factor|&lt;diff_factor&gt;

2.自定义普通函数
* &lt;self_func_def&gt; ::= &lt;self_func_name&gt; &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;[',' &lt;blank_item&gt; &lt;formal_param&gt; &lt;blank_item&gt;] ')' &lt;blank_item&gt; '=' &lt;blank_item&gt; &lt;func_expr&gt;
* &lt;self_func_name&gt; ::= 'g' | 'h'

3.求导算子
* &lt;derivation&gt; ::= 'dx'
* &lt;derived_factor&gt; ::= &lt;derivation&gt; &lt;blank_item&gt; '(' &lt;blank_item&gt; &lt;expr&gt; &lt;blank_item&gt; ')'
  
### 其他规则
* 自定义递推函数中允许调用自定义普通函数
* 自定义普通函数的函数名被确定为'g'或'h'
* 函数表达式中允许调用自定义普通函数;但不会出现递归调用;不允许先调用再声明函数,也不允许调用未声明的函数
* 求导算子不会出现在自定义函数的函数表达式和递推表达式中

# 思路分析
Step1: `preProcessor`
* 沿用hw2
* 提前进行分割,将自定义普通函数也替换掉,替换顺序如下:
  * 先替换可能的自定义递推函数
  * 再替换可能的第二个出现的自定义普通函数
  * 最后替换可能的第一个出现的自定义普通函数
  * 这样做的原因是,自定义递推函数可能调用自定义普通函数,第二个自定义普通函数可能调用第一个
  
Step2: `Lexer`and`Parser`
* 关于求导算子的分割,为`Token.Type`新增`DIFF`即可
* 新增`DiffFactor`类,为`Factor`的一种,
  * 管理属性 : expr 其内部的表达式 ; reverse 正负号
  * 解析方法 : 
    * 当`ParseFactor`遇到的`token`类型为`DIFF`时
    * 为确保文法正确,求导因子一定有一组可略过的括号
    * 嵌套内部调用`ParseExpr`

Step3:`Operator`
* 沿用 hw1 的 `cal...` 和 hw2 的 `Mono/Poly` 框架
* 求导运算交给`Mono/Poly`轴处理
  * 对`poly`求导返回的是其 `monos` 求导后求和
  * $mono = tx^a\prod sin^i(poly_k)\prod cos^j(poly_l)$
  * $$\begin{aligned} mono' &= t \cdot a x^{a-1} \prod_{k} \sin^{i_k}(poly_k) \prod_{l} \cos^{j_l}(poly_l) \newline &\quad + \sum_{k} \Bigg[ t x^a \cdot i_k \sin^{i_k-1}(poly_k) \cos(poly_k) poly'_k \cdot \prod_{m \neq k} \sin^{i_m}(poly_m) \prod_{l} \cos^{j_l}(poly_l) \Bigg] \newline &\quad - \sum_{l} \Bigg[ t x^a \cdot j_l \cos^{j_l-1}(poly_l) \sin(poly_l) poly'_l \cdot \prod_{k} \sin^{i_k}(poly_k) \prod_{n \neq l} \cos^{j_n}(poly_n) \Bigg] \end{aligned}$$
  * 对`mono`求导返回的是一个名为`diffMono`的`poly`,且`monos.size() == 1 + sinArray.size() + cosArray.size()`(合并前)
  * 故求导逻辑写为:
    * 循环 `monos` 求导,相加结果
    * 对`mono`求导,要循环创建求导出的 `mono` ,按类似相加逻辑并入 `diffMono`
    * 首项$t \cdot a x^{a-1} \prod_{k} \sin^{i_k}(poly_k) \prod_{l} \cos^{j_l}(poly_l)$:具有特殊性, __深克隆__ 后直接调整系数和指数得到,将其加入为`diffMono`首项
    * 创建其他 `mono` 即先 __深克隆__ ,移去该乘子,再与该乘子求导的结果(一个`Poly`)相乘
    * 相乘的方法为,先乘上同样的部分,然后视作该`clone`与`trigArray`导出的`poly`相乘
    * 对乘子求导的方法并不单独列出
* `DiffFactor`对`calFactor`的重写,即返回`this.expr.calFactor().derive()`,这也是 __唯一__ 可能出现导数的位置

Step4:`SelfFunction`
* 模仿`ResFunction`的`InitDef`即可
* 多一个`name`属性,表示函数名
* 对`PrePrecessor`进行重构,让主类看起来更清爽

# 互测
```
// input
2
g(x)=x+1
h(y,x)=g(x)+g(y)-2
0
g(h(g(x),g(x)))
// output
+2*x+3
```

```
// input
1
g(x)=sin(x)
1
f{n}(x,y)=0*f{n-1}(x,x)+1*f{n-2}(0,0)
f{1}(x,y)=g(g((x*y+4*y)))
f{0}(x,y)=g((x-y))
f{1}(0,f{0}(g(1),(sin(1)+1)))
// output
sin((sin((4*sin((-1))))))
```
