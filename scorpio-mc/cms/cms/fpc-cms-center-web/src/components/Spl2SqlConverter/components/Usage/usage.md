## 一个完整的搜索

```
# `ipv4_responder` 为 `'10.0.0.1'`
# 并且 `ip_protocol` 的值为 `TCP` 或 `UDP`
# 并且 `port_initiator` 大于 `80`
# 并且 `port_initiator` 小于 `100`
# 时间范围限制为：2天前至现在

ipv4_responder<IPv4> = '10.0.0.1' AND ip_protocol in ('tcp', 'udp') AND port_initiator > 80 AND port_initiator < 100
| gentimes start_time start=now-2d end=now
```

## 语法说明

```JSON
# 搜索字段
[[| search] <field-name> <operate> <field-value>] [<logical-connector> <field-name> <operate> <field-value>]]

# 限制时间
[| gentimes <time-field> start <time-value> [end <time-value>]]
```

## 参数说明

|         参数          |    名称    | 描述                                                         |
| :-------------------: | :--------: | :----------------------------------------------------------- |
|    `<field-name>`     |   字段名   | 允许输入大小字母、数字、下划线`_`、英文的点`.`<br />例如：`start_time`、`cup.usage` |
|      `<operate>`      |   操作符   | `=`、`!=`、`>`、`>=`、`<`、`<=`、`IN`、`NOT IN`、`LIKE`、`NOT LIKE`<br />注意：不区分大小写 |
|    `<field-value>`    |   字段值   | 无特殊限制，允许内容被单引号`''`或双引号`""`包裹，但是引号内不允许出现引号。<br />例如：`12`、`"1.2"`、`"中国"`、`"a_b"` |
| `<logical-connector>` | 逻辑关系符 | `AND`、`OR`、`&&` <br />注意：不区分大小写              |
|    `<time-field>`     | 时间字段名 | 同`<field-name>`                                             |
|    `<time-value>`     | 时间内容值 | 查看下述的时间范围<br/>绝对时间值请用单引号或双引号包裹                                        |



## Demo

### 字段条件

⚠️ 开头的 `| search` 可省略

#### 操作符 `=`

```json
# 含义：字段a 等于 1.2.3.4
| search a = 1.2.3.4
# 等价于
a = 1.2.3.4

```

#### 操作符 `!=`

```json
# 含义：字段b 不等于 1.2.3.4
b != 1.2.3.4
```

#### 操作符 `>`

```json
# 含义：字段c 大于 100
c > 100
```

#### 操作符 `>=`

```json
# 含义：字段c 大于等于 100
c >= 100
```

#### 操作符 `<`

```json
# 含义：字段d 小于 100
d < 200
```

#### 操作符 `<=`

```json
# 含义：字段d 小于等于 <200
c <= 200
```

#### 操作符 `IN`

可用于搜索多个值

```json
# 含义：字段name=张三 或者 name=李四
name IN ("张三", "李四")

# 等价于
name = "张三" OR name = "李四"
```

#### 操作符 `NOT IN`

可用于排除多个值

```json
# 含义：字段name!=张三 并且 name!=李四
name NOT IN ("张三", "李四")

# 等价于
name != "张三" AND name != "李四"
```

#### 操作符 `LIKE`

可用于模糊查询，条件可以分为四种匹配模式

① `％` 表示零个或任意多个字符

```json
# 以"山"开头的省份，例如：山东、山西
province LIKE "山%"

# 以"东"结尾的省份，例如：山东、广东
province LIKE "%东"

# 包含"马"名字，例如：马云、马化腾、司马光
name LIKE "%马%"
```

② `_` 任意单个字符、匹配单个任意字符

```json
# 以 "C" 开头，然后是一个任意字符，然后是 "r"，然后是任意字符，然后是 "er"：
name LIKE "C_r_er"
```

#### 操作符 `NOT LIKE`

可用于排除字段。用法同操作符 `LIKE`



#### 使用逻辑关系表达式查询多个字段

```
a=1 AND b>4
a=1 && (b=1 AND (c="2" OR c='3')) OR d!='2'
a=1 AND b IN ('2','3','4') AND c LIKE "%a_b%"
a=1 OR b IN ('2','3','4')
```

## 时间范围

针对时间格式做处理一些调整，这里的时间格式和`Splunk`中标准的时间格式不同。

#### splunk标准格式

`Splunk` 中的时间格式为：`| gentimes start=<timestamp> [end=<timestamp>] [increment=<increment>]` [Gentimes文档](https://docs.splunk.com/Documentation/Splunk/8.0.5/SearchReference/Gentimes)

其中 `timestamp` 的格式为：`MM/DD/YYYY[:HH:MM:SS] | <int>`

---

#### 修改后的时间内容值

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

  - `2017-10-14T12:34:56+08`
  - `2017-10-14T12:34:56+0800`
  - `2017-10-14T12:34:56+08:00`
  - `2020-10-14 12:34:56`  没有时区时以当前时区为准
  
  - 时间戳（毫秒）

#### 使用Demo

- `| gentimes <time-field> start="2020-07-13T00:00:00+08" end="2020-07-13T23:59:59+08"`
- `| gentimes <time-field> start=now-7d end=now`
- `| gentimes <time-field> start=1594569600000 end=1594624363506`
