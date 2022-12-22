
## 一个完整的搜索

```
# `ipv4_responder` 为 `'10.0.0.1'`
# 并且 `ip_protocol` 的值为 `TCP` 或 `UDP`
# 并且 `port_initiator` 大于 `80`
# 并且 `port_initiator` 小于 `100`
# 并且 `start_time` 的值在7天前到现在之间
# 以 `start_time` 倒序排序
# 每页返回30条数据
# 查询超时时间上限为30s
# 设置每分片查询结果上限 10000000，达到该数量时查询执行将提前终止

ipv4_responder = '10.0.0.1' AND ip_protocol in ('TCP', 'UDP') AND port_initiator > 80 AND port_initiator < 100
| gentimes start_time start=now-7d end=now
| sort -start_time
| head 30
| timeout 30s
| terminate_after 10000000
```

- 🥳 如果你有一条搜索语句，可以直接复制粘贴使用。例如上述一个完整的搜索。
- 🧐 如果需要手动书写，可以使用 `#` 和 `@` 进行快速提示。

## 语法说明

```
# 搜索字段
[[| search] <field-name> <operate> <field-value>] [<logical-connector> <field-name> <operate> <field-value>]]

# 限制时间
[| gentimes <time-field> start <time-value> [end <time-value>]]

# 排序,+为正序，-为倒序
[| sort <sort-operate> <sort-field> [, <sort-operate> <sort-field>]]

# 每页多少条数据
[| head <size-value>]

# 查询超时时间，也可通过【高级】/ 【搜索设置】进行设置
[| timeout <timeout-value> ]

# 每分片查询结果上限
[| terminate_after <terminate-after-value>]

```


## 参数说明

|           参数            |            名称            | 描述                                                         |
| :-----------------------: | :------------------------: | ------------------------------------------------------------ |
|      `<field-name>`       |           字段名           | 允许输入大小字母、数字、下划线[`_`]、英文的点[`.`]<br />例如：`start_time`、`cup.usage` |
|        `<operate>`        |           操作符           | `=`、`!=`、`>`、`>=`、`<`、`<=`、`IN`、`NOT IN`                              |
|      `<field-value>`      |           字段值           | 允许输入大小字母、数字、下划线[`_`]、英文的点[`.`]、冒号[`:`]、正斜杠[`/`]、通配符[`*`]、通配符[`?`]。<br />允许内容被单引号[`''`]或双引号[`""`]包裹。<br />含有通配符时，将会进行模糊查询。例如：`12`、`"1.2"`、`"中国"`、`"a_b"`、`"a?b*"` |
|   `<logical-connector>`   |         逻辑关系符         | `and`、`AND`、`or`、`OR`、`&&`、`||`                         |
|      `<time-field>`       |         时间字段名         | 同`<field-name>`                                             |
|      `<time-value>`       |         时间内容值         | <a>时间范围</a>                                              |
|     `<sort-operate>`      |          排序符号          | `+` 正序<br />`-` 倒序                                       |
|      `<sort-field>`       |         排序字段名         | 同`<field-name>`                                             |
|      `<size-value>`       |       返回多少条数据       | 数字。例如：30                                               |
|     `<timeout-value>`     |          超时时间          | 数字 + 时间单位。时间单位可以为秒或毫秒。<br />例如，`30s`、 `10ms` |
| `<terminate-after-value>` | 每分片最大查询结果数量上限 | 数字，推荐设置区间为`[1000, 10000000]`。<br />不限制上限时，语句中不要设置 `terminate_after` 条件即可。 |



## Demo

### 时间条件

```
| gentimes start_time start=2020-07-13T00:00:00+08 end=2020-07-13T23:59:59+08

// end时间可以省略，下面2个查询条件是等价的
| gentimes start_time start=now-2d
| gentimes start_time start=now-2d end=now

| gentimes start_time start=1594569600000 end=1594624363506
```

### 字段条件

⚠️ 开头的 `| search` 可省略

#### ① 查询一个字段

```
| search a=1
等价于
a=1
```

#### ② 使用逻辑关系表达式查询多个字段

```
| search a=1 and b>4
a=1 && (b=1 AND (c="2" OR c='3')) OR d!='2'
| search a=1 and b in ('2','3','4')
| search a=1 or b in ('2','3','4')
```

#### ③ 模糊查询

⚠️ 为了保证搜索性能，请避免使用 * 或开头模式 ?

支持两个通配符运算符： 

- `?`，它与任何单个字符匹配
- `*`，可以匹配零个或多个字符，包括一个空字符



例1，匹配 `kiy`、` kity` 或  `kimchy`

```
| search a="ki*y"
```



例2，匹配 `C1K0-KD345`、` C2K5-DFG65`、 `C4K8-UI365`

```
# 搜索以C开头，第一个字符必须为C，第二字符随意，第三个字符必须是K
| search a="C?K*"
```

#### ④ 查询范围

```
| search a>1 and a<10
| search a>1 and a<=10
| search a>=1 and a<=10
```



#### ⑤ 字段命中多个值

```
| search a in (2,5,6)
等价于
| search a=2 and a=5 and a=6
```

#### ⑥ 占位符

目前只支持 `<标准端口>` 一个占位符，查询时会自动转换成系统中配置的标准端口。

```
| search a NOT IN (<标准端口>)
```

### 限制返回条数

```
# 返回前100条数据
| head 100
```

### 排序

```
# create_time倒序，state正序
| sort -create_time, +state
```



## 时间范围

`| gentimes <time-field> start=<time-value> [end=<time-value>]`

时间的内容值可以分为**相对时间**和**绝对时间**：

- 相对时间

  - `now` 当前时间

  - `now-<int>(y | M | w | d | H | h | m | s)`

    | 单位       | 说明      |
    | ---------- | --------- |
    | `y`        | `Year`    |
    | `M`        | `Months`  |
    | `w`        | `Weeks`   |
    | `d`        | `Days`    |
    | `h` or `H` | `Hours`   |
    | `m`        | `Minutes` |
    | `s`        | `Seconds` |

    例如：`now-7d`，7天前

- 绝对时间

  - `2017-04-01T12:34:56+08`
  - `2017-04-01T12:34:56+0800`
  - `2017-04-01T12:34:56+08:00`
  - 时间戳（毫秒）

#### 使用Demo

- `| gentimes time-field start=2020-07-13T00:00:00+08 end=2020-07-13T23:59:59+08`
- `| gentimes start=now-7d end=now`
- `| gentimes start=1594569600000 end=1594624363506`
