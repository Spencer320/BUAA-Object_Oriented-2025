# 任务描述
本次作业的新增内容有：
* 新增双轿厢电梯改造
* 相关的约束更新

### 双轿厢改造
* 尽快就地开始改造,$T_{reset}=1s$后,两部轿厢闪现到初始位置
* 输入格式:`[时间戳]UPDATE-A电梯ID-B电梯ID-目标楼层`
* 输出格式:
  * 电梯系统接收到改造请求:`[时间戳]UPDATE-ACCEPT-A电梯ID-B电梯ID-目标楼层`(由官方包自动输出)
  * 双轿厢系统开始改造:`[时间戳]UPDATE-BEGIN-A电梯ID-B电梯ID`
  * 双轿厢系统改造完成:`[时间戳]UPDATE-END-A电梯ID-B电梯ID`
* 正确性约束:
  * 接收到改造指令的两部电梯必须在两次移动楼层操作内将所有乘客放出,并在两部电梯都停下后输出`UPDATE-BEGIN`就地开始改造动作
  * `UPDATE-BEGIN`时,电梯轿厢内没有人,电梯门关闭
  * 改造完成后,A电梯井道停用;B电梯井道中,B电梯位于初始位置的下一层,A电梯位于初始位置的上一层
  * 改造完成后,位于上方的轿厢(即A轿厢)运行范围为`目标楼层-F7`,位于下方的轿厢(B轿厢)运行范围为`B4-目标楼层`;它们不能同时出现在同一楼层
  * 电梯改造后速度提升到0.2s/层
  * 双轿厢电梯允许不受RECEIVE约束而离开目标楼层一层
* 数据限制:
  * 一部电梯至多改造一次
  * 双轿厢电梯将不再收到改造或临时调度请求
  * 目标楼层限制:`B2-F5`

# 思路分析
不妨做这样的等价转化:双轿厢电梯的改造实质,是更改了两部电梯的运行楼层,加快它们的运行速度,并要求它们不能够同时出现在目标楼层.需要解决的任务转化为:
* 双轿厢电梯的运行逻辑:
  * 实质是是电梯类型的转换
  * 运行动作完全相同
  * 调整逻辑：
    * 运行速度
    * 运行层数
    * 对目标楼层互斥锁,建立`Floor`锁实现
* 因运行加速调整打分系统
  * 速度更快的双轿厢电梯距离打分减半
  * 由于可能的跨楼层,认为使用双轿厢电梯但是一次无法到达的乘客在目标楼层离开并发回一个`ElevatorRequest`,不指定他的下一部电梯
  * 做出这样的假设,离开电梯后能立刻进入某电梯,但为载重低的单轿厢电梯,便于打分.
  * 出发楼层不在运行范围内的轿厢,无法接受对应请求
  * 重排打分系统:
    * $score=(base_score+distance_score)*speed_punish*weight_punish+door_punish$
    `base_score`是`fromFloor`与`toFloor`距离;`distance_score`与原来一致;`speed_punish`当单轿厢电梯为2,双轿厢为1;`weight_punish`与原来一致;`door_punish`仅双轿厢电梯跨楼层时,获得下电梯惩罚
  * 数值设计:
    * 双轿厢电梯距离一层:1分
    * 下电梯惩罚:5分  
* 请求处理:
  * mainDispatcher通过waitingPassenger向电梯发出类型转换请求
  * 生成Floor锁,由两轿厢共同持有
  * 通过类型标签,dispatchStragety可以区分不同电梯
  * 电梯新增`BottomFloor`和`TopFloor`的属性
  * 新建`DoubleCarElevatorManager`,线程上,负责等待1s以完成;类上,创建Floor资源并被共享
  * 电梯关于update的状态有四个,未开始,准备,进行和已结束
    * 未开始,已结束:终结状态
    * 准备:过渡状态,清空电梯内乘客
    * 进行:过渡状态,清空候乘表
* 结束条件修改:
  * 原条件: _elevator 不产出请求当且仅当所有的 sche 请求均消耗完全,即 inputThread 已经结束,且 requestTable 中不存在 sche 请求,且没有waitingRequest的 isSche 为真,这一检查涉及: requestTable 的 isInputEnd() , requestTable 的元素类型检查 和 各电梯的 isSche_
  * 现在,多了一种产出请求的情况,即双轿厢电梯在目标楼层乘客离开,所以,新增的检查为,是否有这样的双轿厢电梯,它内部有无法直达的乘客.
  * 同时,电梯是否正常运行需要封装,即处于Sche和Update都不是正常运行
* 严格按照状态模式建模:状态切换仅允许由动作完成,外界只能输入请求信号

# 互测
(B组):

回归测试:0/6

测试点:尝试攻击开门时更新,1/6
```
[2.0]602-PRI-6-FROM-F1-TO-F5
[2.0]601-PRI-6-FROM-F1-TO-F5
[2.0]603-PRI-6-FROM-F1-TO-F5
[2.0]604-PRI-6-FROM-F1-TO-F5
[2.0]605-PRI-6-FROM-F1-TO-F5
[2.3]UPDATE-1-2-B1
[2.3]UPDATE-4-5-B1
[2.3]UPDATE-3-6-B1
```

测试点:尝试攻击更新未完成时移动,2/6
```
[3.0]1-PRI-17-FROM-F2-TO-F4
[3.0]2-PRI-17-FROM-F2-TO-F4
[3.0]3-PRI-17-FROM-F2-TO-F4
[3.0]4-PRI-17-FROM-F2-TO-F4
[3.0]5-PRI-17-FROM-F2-TO-F4
[3.0]6-PRI-17-FROM-F2-TO-F4
[3.0]7-PRI-17-FROM-F2-TO-F4
[3.0]8-PRI-17-FROM-F2-TO-F4
[3.0]9-PRI-17-FROM-F2-TO-F4
[3.0]10-PRI-17-FROM-F2-TO-F4
[15.0]UPDATE-1-6-F4
[15.0]UPDATE-2-5-F4
[15.0]UPDATE-3-4-F4
[23.0]11-PRI-17-FROM-F6-TO-F1
[23.0]12-PRI-17-FROM-F6-TO-F1
[23.0]13-PRI-17-FROM-F6-TO-F1
[23.0]14-PRI-17-FROM-F6-TO-F1
[23.0]15-PRI-17-FROM-F6-TO-F1
[23.0]16-PRI-17-FROM-F6-TO-F1
[23.0]17-PRI-17-FROM-F6-TO-F1
[23.0]18-PRI-17-FROM-F6-TO-F1
[23.0]19-PRI-17-FROM-F6-TO-F1
[23.0]20-PRI-17-FROM-F6-TO-F1
```

测试点:卡SCHE与UPDATE间隔8s,2/6
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
[10.2]UPDATE-4-3-F3
[10.2]UPDATE-2-5-B2
[12.3]362-PRI-89-FROM-B4-TO-F7
[12.3]517-PRI-54-FROM-B4-TO-F7
[12.3]129-PRI-38-FROM-B4-TO-F7
[12.3]800-PRI-3-FROM-B4-TO-F7
[12.3]46-PRI-96-FROM-B4-TO-F7
[12.3]685-PRI-16-FROM-B4-TO-F7
[12.3]254-PRI-74-FROM-B4-TO-F7
[12.3]371-PRI-60-FROM-B4-TO-F7
[12.3]988-PRI-42-FROM-B4-TO-F7
[12.3]133-PRI-22-FROM-B4-TO-F7
[12.3]745-PRI-81-FROM-B4-TO-F7
[12.3]510-PRI-5-FROM-B4-TO-F7
[12.3]89-PRI-39-FROM-B4-TO-F7
[12.3]606-PRI-90-FROM-B4-TO-F7
[12.3]317-PRI-12-FROM-B4-TO-F7
[12.3]960-PRI-70-FROM-B4-TO-F7
[12.3]220-PRI-48-FROM-B4-TO-F7
[12.3]788-PRI-33-FROM-B4-TO-F7
[12.3]55-PRI-59-FROM-B4-TO-F7
[12.3]431-PRI-97-FROM-B4-TO-F7
[12.3]652-PRI-20-FROM-B4-TO-F7
[12.4]UPDATE-1-6-F1
```

测试点:意义不大,但是1/6
```
[49.8]953-PRI-67-FROM-F3-TO-F7
[49.8]721-PRI-25-FROM-F3-TO-F7
[49.8]315-PRI-83-FROM-F3-TO-F7
[49.8]104-PRI-11-FROM-F3-TO-F7
[49.8]864-PRI-7-FROM-F3-TO-F7
[49.8]UPDATE-1-6-F4
[49.8]452-PRI-50-FROM-F3-TO-F7
[49.8]632-PRI-93-FROM-F3-TO-F7
[49.8]278-PRI-2-FROM-F3-TO-F7
[49.8]989-PRI-65-FROM-F3-TO-F7
[49.8]537-PRI-34-FROM-F3-TO-F7
[49.8]111-PRI-76-FROM-F3-TO-F7
[49.8]868-PRI-18-FROM-F3-TO-F7
[49.8]SCHE-2-0.5-F4
[49.8]43-PRI-99-FROM-F3-TO-F7
[49.8]776-PRI-41-FROM-F3-TO-F7
[49.8]659-PRI-8-FROM-F3-TO-F7
[49.8]902-PRI-57-FROM-F3-TO-F7
[49.8]384-PRI-15-FROM-F3-TO-F7
[49.8]267-PRI-72-FROM-F3-TO-F7
[49.8]590-PRI-63-FROM-F3-TO-F7
[49.8]18-PRI-29-FROM-F3-TO-F7
[49.8]445-PRI-87-FROM-F3-TO-F7
[49.8]320-PRI-6-FROM-F3-TO-F7
[49.8]583-PRI-55-FROM-F3-TO-F7
[49.8]907-PRI-47-FROM-F3-TO-F7
[49.8]251-PRI-31-FROM-F3-TO-F7
[49.8]689-PRI-21-FROM-F3-TO-F7
[49.8]96-PRI-68-FROM-F3-TO-F7
[49.8]774-PRI-10-FROM-F3-TO-F7
[49.8]362-PRI-89-FROM-F3-TO-F7
[49.8]517-PRI-54-FROM-F3-TO-F7
[49.8]129-PRI-38-FROM-F3-TO-F7
[49.8]800-PRI-3-FROM-F3-TO-F7
[49.8]46-PRI-96-FROM-F3-TO-F7
[49.8]685-PRI-16-FROM-F3-TO-F7
[49.8]254-PRI-74-FROM-F3-TO-F7
[49.8]371-PRI-60-FROM-F3-TO-F7
[49.8]988-PRI-42-FROM-F3-TO-F7
[49.8]133-PRI-22-FROM-F3-TO-F7
[49.8]745-PRI-81-FROM-F3-TO-F7
[49.8]510-PRI-5-FROM-F3-TO-F7
[49.8]89-PRI-39-FROM-F3-TO-F7
[49.8]606-PRI-90-FROM-F3-TO-F7
[49.8]317-PRI-12-FROM-F3-TO-F7
[49.8]960-PRI-70-FROM-F3-TO-F7
[49.8]220-PRI-48-FROM-F3-TO-F7
[49.8]788-PRI-33-FROM-F3-TO-F7
[49.8]55-PRI-59-FROM-F3-TO-F7
[49.8]431-PRI-97-FROM-F3-TO-F7
[49.8]652-PRI-20-FROM-F3-TO-F7
[49.8]UPDATE-3-5-F4
[49.8]899-PRI-44-FROM-F3-TO-F7
[49.8]341-PRI-13-FROM-F3-TO-F7
[49.8]763-PRI-8-FROM-F3-TO-F7
[49.8]588-PRI-91-FROM-F3-TO-F7
[49.8]312-PRI-64-FROM-F3-TO-F7
[49.8]857-PRI-19-FROM-F3-TO-F7
[49.8]700-PRI-52-FROM-F3-TO-F7
[49.8]265-PRI-75-FROM-F3-TO-F7
[49.8]918-PRI-45-FROM-F3-TO-F7
[49.8]387-PRI-24-FROM-F3-TO-F7
[49.8]402-PRI-100-FROM-B4-TO-F7
```