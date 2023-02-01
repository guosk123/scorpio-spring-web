# SDK接入介绍

## SDK包结构

```
├── doc
│   └── IOSP_SDK_JAVA_DEVELOPER'S_GUIDE.pdf
├── libs
│   ├── iosp-sdk-1.0.0.jar
│   ├── iosp-sdk-1.0.0-sources.jar
│   └── LINUX64
│       ├── libsdk_jni.so
│       └── libsdk.so
└── samplecode
    └── IospSDKTest.java

```
## 环境要求
Centos 7  
JDK 1.8

需要将so文件libsdk.so及libsdk_jni.so放到系统 /lib64 目录下

## SDK日志
通过传入JVM参数"iosp.sdk.logpath"可以指定SDK日志目录，默认不生成日志

# 数据结构
## FullObject
### 类描述  
对象完整结果，包含对象的元数据信息及二进制内容

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| int | createTime | 生成时间 |
| int | ingestTime | 写入IOSP时间 |
| int | objectSize | 对象大小（byte） |
| String | objectId | 对象ID |
| String | objectName | 对象名称 |
| String | zone | 虚拟域 |
| String | site | 写入位置 |
| String | label | 标签 |
| ByteBuffer | content | 对象内容 |

## Metadata
### 类描述
对象元数据结果，仅包含元数据信息

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| int | createTime | 生成时间 |
| int | ingestTime | 写入IOSP时间 |
| int | objectSize | 对象大小（byte） |
| String | objectId | 对象ID |
| String | objectName | 对象名称 |
| String | zone | 虚拟域 |
| String | site | 写入位置 |
| String | label | 标签 |

## WriteFullObject
### 类描述
写入对象的完整信息

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| int | createTime | 生成时间 |
| String | objectName | 对象名称 |
| String | zone | 虚拟域 |
| String | site | 写入位置 |
| String | label | 标签 |
| ByteBuffer | content | 对象内容 |

## CursorFullObjectResult
### 类描述
游标查询完整对象结果集

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| FullObject[] | objects | 对象结果数组 |
| String | datanodeMsg | 数据节点状态消息（JSON格式） |
| int | cursor | 游标 |

## CursorMetadataResult
### 类描述
游标查询对象元数据结果集

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| Metadata[] | metadatas | 对象结果数组 |
| String | datanodeMsg | 数据节点状态消息（JSON格式） |
| int | cursor | 游标 |

## SubscribeObjectResult
### 类描述
订阅消费对象结果集

### 成员变量
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| FullObject[] | objects | 对象结果数组 |
| int | hasNext | 是否可以继续消费（0：继续；1：消费已完成） |

## SubscribeMetadataResult
### 类描述
订阅消费对象元数据结果集

### 成员变量
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| Metadata[] | metadatas | 对象元数据结果数组 |
| int | hasNext | 是否可以继续消费（0：继续；1：消费已完成） |

## AnalysResult
### 类描述
批量查询结果分析

### 成员描述
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| String | datanodeMsg | 数据节点状态消息（JSON格式） |
| String | analysMsg | 分析结果（JSON格式） |

## SearchCondition
### 类描述
批量查询条件

### 成员描述
| 类型 | 变量名 | 描述 | 必填 | 备注 |
| ------ | ------ | ------ | ------ |
| int | startTime | 查询开始时间，毫秒级时间戳（1654012800000） | 是 | 对象生成时间 |
| int | endTime | 查询结束时间，毫秒级时间戳 | 是 | 对象生成时间 |
| int | timeout | 查询超时时间（单位：秒） | 否 | |
| String | zone | 虚拟域的key值 | 是 | 只能选择一个虚拟域 |
| String | site | 写入位置 | 否 | 支持选择多个位置，多个位置使用半角逗号分隔。例如：001,002 |
| String | label | 标签 | 否 | 支持选择多个标签，多个标签使用 and, or 以及括号（不支持括号嵌套），关键词之间需要使用空格隔开，最大支持500字符、32个条件。例如：蓝色 and ( 高尔夫 or 速腾 ) |

## 备注
### datanodeMsg JSON内容说明
| 字段类型 | 字段名称 | 字段含义 | 备注 |
| ------ | ------ | ------ | ------ |
| int | took | 耗时 |  |
| object | datanodes | 数据节点统计 |  |
| int | total | 涉及节点数 | 此字段为datanodes对象中的元素 |
| int | successful | 成功节点数 | 此字段为datanodes对象中的元素 |
| int | skipped | 跳过节点数 | 此字段为datanodes对象中的元素 |
| int | failed | 失败节点数 | 此字段为datanodes对象中的元素 |
| array | error | 错误节点列表 |  |
| string | datanode_id | 节点ID | 此字段为error数组中的元素 |
| int | code | 错误码 | 此字段为error数组中的元素 |
| string | err_msg | 错误信息 | 此字段为error数组中的元素 |

### analysMsg JSON内容说明
| 类型 | 变量名 | 描述 |
| ------ | ------ | ------ |
| long | timestamp | 统计时间戳 |
| String | zone_id | 虚拟域ID |
| long | object_number | 对象数量 |
| long | object_avg_size | 对象大小平均值 |
| long | object_max_size | 对象大小最大值 |
| long | object_min_size | 对象平均大小最小值 |
| long | object_earliest_create_time | 最早对象生成时间 |
| long | object_latest_create_time | 最晚对象生成时间 |
| long | object_earliest_write_time | 最晚对象写入时间 |
| long | object_latest_write_time | 最晚对象写入时间 |
| int | location_number | 对象产生位置数量 |
| int | location_avg_object_number | 平均每个产生位置的对象数量 |
| long | location_avg_size | 平均每个产生位置容量大小 |

# 功能接口
## Highlevel
### 类
IospConnection
### 构造方法
#### IospConnection(String ip, short port, String clientId, String clientToken)  
| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| String | ip | Datanode IP地址 |
| short | port | Datanode 端口号 |
| String | clientId | IOSP Client ID |
| String | clientToken | IOSP Client Token |

#### IospConnection(String ip, String clientId, String clientToken)  
使用默认1100端口连接IOSP Datanode  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| String | ip | Datanode IP地址 |
| String | clientId | IOSP Client ID |
| String | clientToken | IOSP Client Token |

#### IospConnection(String ip, short port, String clientId, String clientToken,boolean useDirectByteBuffer)
可以通过useDirectByteBuffer指定结果返回方式，若该字段为true，则查询及批量查询获取的对象结果使用DirectByteBuffer方式  

**！！！注意：使用DirectByteBuffer需限制每次调用后立即对结果进行处理，并且禁止保留该ByteBuffer，当下一次接口调用时会清理上一次结果数据，当数据清理后再次使用DirectByteBuffer获取数据会导致未知结果**

### 接口
#### 1、连接  
方法：connect()  
说明：与IOSP Datanode建立连接  
参数：无  
返回值：无  
异常：IOException  


#### 2、断开连接  
方法：close()  
说明：与IOSP Datanode断开连接  
参数：无  
返回值：无  
异常：IOException


#### 3、批量查询对象全部信息接口
方法：search(SearchCondition searchCondition, Sort sortType,
      int searchNum, int cursor)  
说明：根据条件批量查询完整对象结果，指定单次查询数量，并可以指定上次查询游标继续获取结果集  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件 |
| Sort | sortType | 排序方式 |
| int | searchNum | 查询数量（最大1000） |
| int | cursor | 查询游标（使用之前查询返回的游标继续查询） |

返回值：CursorFullObjectResult  
异常：IOException


#### 4、批量查询对象元数据接口
方法：searchMetadata(SearchCondition searchCondition, Sort sortType,
      int searchNum, int cursor)  

说明：根据条件批量查询完整对象结果，指定单次查询数量，并可以指定上次查询游标继续获取结果集  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件 |
| Sort | sortType | 排序方式 |
| int | searchNum | 查询数量（最大1000） |
| int | cursor | 查询游标（使用之前查询返回的游标继续查询） |

返回值：CursorMetadataResult  
异常：IOException


#### 5、批量查询对象全部信息落盘接口
方法：searchAndWriteDisk(SearchCondition searchCondition, String savePath,
      int dirFileNum)  

说明：根据条件批量查询完整对象结果，并将结果写入磁盘。执行成功后会在指定目录生成批量查询结果内容文件，文件会按照配置的目录最大文件数在根目录下创建目录并限制每个目录下的文件数量，并在指定根目录下创建对象元数据信息CSV文件  

参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件 |
| String | savePath | 保存路径 |
| int | dirFileNum | 单个目录下最大文件数 |

返回值：无  
异常：IOException


#### 6、批量查询分析接口
方法：searchAnalys(SearchCondition searchCondition)  
说明：指定查询条件，查询符合条件的统计数据。  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件 |

返回值：AnalysResult  
异常：IOException


#### 7、通过ID读取对象元数据接口
方法：read(String objectId)  
说明：通过对象ID读取指定对象的全部信息  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| String | objectId | 对象ID |

返回值：FullObject  
异常：IOException


#### 8、通过ID读取对象元数据接口
方法：readMetadata(String objectId)  
说明：通过对象ID读取指定对象的元数据信息  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| String | objectId | 对象ID |

返回值：Metadata  
异常：IOException


#### 9、写入单对象接口
方法：write(WriteFullObject writeFullObject)  
说明：向IOSP中写入一个对象  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| WriteFullObject | writeFullObject | 待写入对象 |

返回值：String 写入对象ID  
异常：IOException


#### 10、通过ID修改标签接口
方法：modifyLabel(String objectId, String label)  
说明：通过对象ID修改指定对象的标签。  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| String | objectId | 待修改对象ID |
| String | label | 新的标签 |

返回值：无  
异常：IOException


#### 11、创建订阅任务接口
方法：createSubscribeTask(SearchCondition searchCondition, SubscribeType type, long taskId)  
说明：通过指定条件，创建任务，将对象元数据或全部信息汇总，提供订阅功能。  
**同一个连接最多只能创建一个订阅任务！！！**  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件，历史数据只支持订阅过去24H内的数据 |
| SubscribeType | type | 订阅数据的类型，0表示只订阅元数据，1表示订阅元数据+内容 |
| long | taskId | 订阅任务ID，0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重启任务 |

返回值：long 任务ID  
异常：IOException


#### 12、消费订阅对象接口
方法： consumerObject(long taskId)  
说明：消费已创建的订阅任务内包含的对象，根据对象生成时间排序，每次调用返回增量数据。单次最多返回100个对象！  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| long | taskId | 订阅任务的任务ID |

返回值：SubscribeObjectResult  
异常：IOException


#### 13、消费订阅对象元数据接口
方法： consumerMetadata(long taskId)  
说明：消费已创建的订阅任务内包含的对象元数据，根据对象生成时间排序，每次调用返回增量数据。单次最多返回20000条对象元数据信息！  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| long | taskId | 订阅任务的任务ID |

返回值：SubscribeMetadataResult  
异常：IOException

**！！！注意：消费订阅接口在订阅任务未结束前，并且没有新的数据时，会一直阻塞等待，直到有新的数据，才会返回结果。**


#### 14、销毁订阅任务接口
方法： destorySubscribeTask()  
说明：销毁当前连接下创建的订阅任务。  
参数：无  
返回值：无  
异常：IOException


#### 15、订阅对象接口
方法： subscribe(SearchCondition searchCondition, long taskId, ObjectListener listener)  
说明：订阅对象，自动消费（根据对象生成时间排序，增量消费，单次最多消费100个对象），**自动销毁**。  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件，历史数据只支持订阅过去24H内的数据 |
| long | taskId | 订阅任务ID，0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重启任务 |
| ObjectListener | listener | 对象消费监听器。创建监听器，并实现该类的consume方法，将监听器传给接口 |

返回值：SubscribeObjectTask  

| 方法名 | 描述 |
| ------ | ------ |
| start | 启动订阅 |
| shutdown | 销毁订阅 |
| isfinished | 订阅结束状态 |
| setExecutor | 调用方可传入自定义线程池 |
| setExceptionHandler | 调用方可传入自定义异常处理 |

异常：IOException


#### 16、订阅对象元数据接口
方法： subscribeMetadata(SearchCondition searchCondition, long taskId, MetadataListener listener)  
说明：订阅对象元数据，自动消费（根据对象生成时间排序，增量消费，单次最多消费20000条对象元数据），**自动销毁**。  
参数：  

| 类型 | 参数名 | 描述 |
| ------ | ------ | ------ |
| SearchCondition | searchCondition | 查询条件，历史数据只支持订阅过去24H内的数据 |
| long | taskId | 订阅任务ID，0表示新建一个订阅任务，非0则填写已有订阅任务ID，表示重启任务 |
| MetadataListener | listener | 对象元数据消费监听器。创建监听器，并实现该类的consume方法，将监听器传给接口 |

返回值：SubscribeMetadataTask  

| 方法名 | 描述 |
| ------ | ------ |
| start | 启动订阅 |
| shutdown | 销毁订阅 |
| isfinished | 订阅结束状态 |
| setExecutor | 调用方可传入自定义线程池 |
| setExceptionHandler | 调用方可传入自定义异常处理 |

异常：IOException