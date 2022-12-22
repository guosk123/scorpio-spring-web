
## 一个完整的搜索

```
# `ipv4_responder` 为 `'10.0.0.1'`
# 并且 `ip_protocol` 的值为 `TCP` 或 `UDP`
# 并且 `port_initiator` 大于 `80`
# 并且 `port_initiator` 小于 `100`

ipv4_responder = '10.0.0.1' AND ip_protocol in ('TCP', 'UDP') AND port_initiator > 80 AND port_initiator < 100
```

## 语法说明

```
# 搜索字段
[[| search] <field-name> <operate> <field-value>] [<logical-connector> <field-name> <operate> <field-value>]]
```


## 参数说明

|         参数          |    名称    | 描述                                                         |
| :-------------------: | :--------: | ------------------------------------------------------------ |
|    `<field-name>`     |   字段名   | 允许输入大小字母、数字、下划线[`_`]、英文的点[`.`]<br />例如：`start_time`、`cup.usage` |
|      `<operate>`      |   操作符   | `=`、`!=`、`>`、`>=`、`<`、`<=`、`IN`、`NOT IN`                              |
|    `<field-value>`    |   字段值   | 允许输入大小字母、数字、下划线[`_`]、英文的点[`.`]、冒号[`:`]、正斜杠[`/`]、通配符[`*`]、通配符[`?`]。<br />允许内容被单引号[`''`]或双引号[`""`]包裹。<br />含有通配符时，将会进行模糊查询。例如：`12`、`"1.2"`、`"中国"`、`"a_b"`、`"a?b*"` |
| `<logical-connector>` | 逻辑关系符 | `and`、`AND`、`or`、`OR`、`&&`、`||`                         |



## Demo

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
