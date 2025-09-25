# 任务描述
本次作业的新增内容有：
* 取消"乘客请求指定电梯"约束
* 新增"临时调度"请求;新增"RECEIVE"约束
* 修改乘客请求格式;修改"OUT"指令输出格式;修改测试数据约束;

### 临时调度
* 尽快到达某一楼层,然后$T_{stop}=1s$,关门后恢复调度许可
* 输入:`[时间戳]SCHE-电梯ID-临时运行速度-目标楼层`
* 输出:
  * 接受临时调度请求(官方包自动输出):`[时间戳]SCHE-ACCEPT-电梯ID-临时运行速度-目标楼层`
  * 开始临时调度:`[时间戳]SCHE-BEGIN-电梯ID`
  * 临时调度完成:`[时间戳]SCHE-END-电梯ID`
* 正确性约束:
  * 临时调度存在楼层限制: B2-F5
  * 接收到临时调度指令的电梯必须在两次移动楼层操作内开始临时调度动作(即`SCHE-ACCEPT`后至多两个`ARRIVE`)
  * `SCHE-BEGIN-电梯ID`时,电梯门关闭,且静止于某一楼层
  * `SCHE-BEGIN-电梯ID`后,电梯门保持关闭,以指定速度前往目标楼层,过程中不开关门,但轿厢内允许有乘客
  * 到达后开门一定时间,再按顺序输出`CLOSE`和`SCHE-END`
  * 到达目标楼层电梯开门后,轿厢只下不上(所有人必须离开)
  * `SCHE-END-电梯ID`时,电梯门应关闭且门内无人
  * 在输出`SCHE-BEGIN-电梯ID` 和输出`SCHE-END-电梯ID`之间,电梯不得参与电梯调度
  * `SCHE-END-电梯ID`后,电梯位于临时调度请求指定的目标楼层,运行速度恢复到默认值

### RECEIVE限制
程序需要在乘客进入电梯和电梯移动前输出RECEIVE来说明乘客请求分配情况.
* 乘客只有在电梯外才可以输出RECEIVE
* 任何时刻任何一个乘客请求都至多会被分配给一部电梯,乘客只能进入RECEIVE输出规定的电梯
* 电梯内没有乘客时,只有RECEIVE到请求或开始SCHE时可以开始移动
* REVEIVE被取消当且仅当`[时间戳]SCHE-BEGIN-电梯ID`输出后,或乘客离开电梯后,

### 输出改动:
* 乘客离开电梯:
  * 乘客已到达目标楼层:`[时间戳]OUT-S-乘客ID-所在层-电梯ID`
  * 否则:`[时间戳]OUT-F-乘客ID-所在层-电梯ID`
* 电梯开始临时调度:`[时间戳]SCHE-BEGIN-电梯ID`
* 电梯临时调度完成:`[时间戳]SCHE-END-电梯ID`
* 电梯接收分配:`[时间戳]RECEIVE-乘客ID-电梯ID`

# 思路分析
---
线程设计:取消掉了内部类线程.现在有八个线程,交互如下.
* inputThread &rarr; requsetTable &rarr; MainDispatcher &rarr;
  * if instance of PersonRequest : waitingPassenger
  * elif instance of ScheRequest : elevator
* OUT-F : elevator &rarr; currentPassenger &rarr; requestTable
* SCHE-BEGIN : elevator &rarr; waitingPassenger &rarr; requestTable
---
分配思路:
* 分配顺序:先分配优先级高的请求,实现方法是把RequestTable的容器该为优先级容器
* 分配方法:引入打分系统,得分低者得
  * 距离分: 评估接到乘客的运行楼层期望
    * 同方向且电梯在后:电梯楼层与乘客楼层差
    * 同方向但电梯在前:电梯两边界间距离
    * 反方向相向而行:电梯按电梯方向到边界的距离
    * 反方向相背而行:乘客按电梯方向到边界的距离
  * 载重减分:评估电梯的请求总数
    * 一倍以内,惩罚系数为1
    * 一倍以上,二倍以内,惩罚系数为2
    * 两倍以上,惩罚系数为10
  * 延迟系统:获取请求后延迟分配,防止大量临检导致分配失误
    * 临检数量为0或1时,无延迟
    * 临检数量为2或3时,延迟为sleep(400)
    * 临检数量为4或5时,延迟为sleep(2000),注意分五次完成,间隙看一眼有无schedule请求
    * 临检数量为6时,忙等待直至某电梯临检结束(sleep(1000)后看一眼)
* 分配输出:当具体分配交予后输出RECEIVE
* 返还分配:以ElevatorRequest的格式返还,它内含一个Passenger.三种Request中,ScheRequest优先级最高,剩下两个按priority排序.
---
电梯逻辑:
电梯正常运行逻辑不改变.
临检时,电梯直奔目标楼层,在目标楼层放人
* 新增状态:SchedulingState
* 新增动作:scheduleBegin scheduleEnd
* 新增输出:...
---
线程结束:
严格遵守 `Producer-Consumer`模型,容器结束当且仅当生产者不再产出,线程结束当且仅当容器结束且消费完全
* elevator 结束逻辑不改变,当且仅当 waitingPassenger 结束,且无请求
* waitingPassenger 结束当且仅当 mainDispatcher 结束
* mainDispatcher 结束当且仅当 requestTable 结束且为空
* requestTable 结束当且仅当 inputThread 和 elevator 均不产出请求
* inputThread 不产出请求由投喂数据控制
* elevator 不产出请求当且仅当所有的 sche 请求均消耗完全,即 inputThread 已经结束,且 requestTable 中不存在 sche 请求,且没有waitingRequest的 isSche 为真,这一检查涉及: requestTable 的 isInputEnd() , requestTable 的元素类型检查 和 各电梯的 isSche
* 理论上,这个检查应该由六部 elevator 共同给出,但是 elevator 实际上也并不知道它是否还会接受 sche ,所以这一步检查交给 mainDispatcher 的 checkElevatorScheEnd 方法
* 可能因为checkElevatorScheEnd没有得到执行而无法退出(即条件达成了但是 mainDispatcher 在等待),所以每个Sche完成执行一次判断

# 互测
(A组):

测试点:尝试攻击临时调度总耗时,1/7
```
[1.5]226-PRI-48-FROM-F3-TO-F7
[1.5]311-PRI-63-FROM-F3-TO-F6
[1.5]774-PRI-73-FROM-F1-TO-F7
[2.1]SCHE-4-0.2-F4
[2.1]SCHE-5-0.2-F5
[2.1]SCHE-6-0.4-F1
[2.1]SCHE-3-0.4-B1
[2.1]SCHE-2-0.3-F4
[4.3]370-PRI-82-FROM-F7-TO-B4
[4.3]611-PRI-92-FROM-B3-TO-F7
[4.3]499-PRI-40-FROM-F2-TO-F1
[4.4]SCHE-1-0.5-B2
```

测试点:尝试攻击总耗时,0/7
```
[49.8]SCHE-4-0.5-F4
[49.8]953-PRI-67-FROM-B4-TO-F7
[49.8]721-PRI-25-FROM-B4-TO-F7
[49.8]315-PRI-83-FROM-B4-TO-F7
[49.8]104-PRI-11-FROM-B4-TO-F7
[49.8]864-PRI-7-FROM-B4-TO-F7
[49.8]SCHE-1-0.5-F4
[49.8]452-PRI-50-FROM-B4-TO-F7
[49.8]632-PRI-93-FROM-B4-TO-F7
[49.8]278-PRI-2-FROM-B4-TO-F7
[49.8]989-PRI-65-FROM-B4-TO-F7
[49.8]537-PRI-34-FROM-B4-TO-F7
[49.8]111-PRI-76-FROM-B4-TO-F7
[49.8]868-PRI-18-FROM-B4-TO-F7
[49.8]SCHE-2-0.5-F4
[49.8]43-PRI-99-FROM-B4-TO-F7
[49.8]776-PRI-41-FROM-B4-TO-F7
[49.8]659-PRI-8-FROM-B4-TO-F7
[49.8]902-PRI-57-FROM-B4-TO-F7
[49.8]384-PRI-15-FROM-B4-TO-F7
[49.8]267-PRI-72-FROM-B4-TO-F7
[49.8]590-PRI-63-FROM-B4-TO-F7
[49.8]18-PRI-29-FROM-B4-TO-F7
[49.8]445-PRI-87-FROM-B4-TO-F7
[49.8]320-PRI-6-FROM-B4-TO-F7
[49.8]583-PRI-55-FROM-B4-TO-F7
[49.8]907-PRI-47-FROM-B4-TO-F7
[49.8]251-PRI-31-FROM-B4-TO-F7
[49.8]689-PRI-21-FROM-B4-TO-F7
[49.8]96-PRI-68-FROM-B4-TO-F7
[49.8]SCHE-6-0.5-F4
[49.8]774-PRI-10-FROM-B4-TO-F7
[49.8]362-PRI-89-FROM-B4-TO-F7
[49.8]517-PRI-54-FROM-B4-TO-F7
[49.8]129-PRI-38-FROM-B4-TO-F7
[49.8]800-PRI-3-FROM-B4-TO-F7
[49.8]46-PRI-96-FROM-B4-TO-F7
[49.8]685-PRI-16-FROM-B4-TO-F7
[49.8]254-PRI-74-FROM-B4-TO-F7
[49.8]371-PRI-60-FROM-B4-TO-F7
[49.8]988-PRI-42-FROM-B4-TO-F7
[49.8]133-PRI-22-FROM-B4-TO-F7
[49.8]745-PRI-81-FROM-B4-TO-F7
[49.8]510-PRI-5-FROM-B4-TO-F7
[49.8]89-PRI-39-FROM-B4-TO-F7
[49.8]606-PRI-90-FROM-B4-TO-F7
[49.8]317-PRI-12-FROM-B4-TO-F7
[49.8]960-PRI-70-FROM-B4-TO-F7
[49.8]220-PRI-48-FROM-B4-TO-F7
[49.8]788-PRI-33-FROM-B4-TO-F7
[49.8]55-PRI-59-FROM-B4-TO-F7
[49.8]431-PRI-97-FROM-B4-TO-F7
[49.8]652-PRI-20-FROM-B4-TO-F7
[49.8]SCHE-3-0.5-F4
[49.8]SCHE-5-0.5-F4
[49.8]899-PRI-44-FROM-B4-TO-F7
[49.8]341-PRI-13-FROM-B4-TO-F7
[49.8]763-PRI-8-FROM-B4-TO-F7
[49.8]588-PRI-91-FROM-B4-TO-F7
[49.8]312-PRI-64-FROM-B4-TO-F7
[49.8]857-PRI-19-FROM-B4-TO-F7
[49.8]700-PRI-52-FROM-B4-TO-F7
[49.8]265-PRI-75-FROM-B4-TO-F7
[49.8]918-PRI-45-FROM-B4-TO-F7
[49.8]387-PRI-24-FROM-B4-TO-F7
[49.8]402-PRI-100-FROM-B4-TO-F7
```
