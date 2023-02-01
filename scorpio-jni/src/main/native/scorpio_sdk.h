/***************************************************************************
The software is copyrighted by Beijing Machloop Technology Co., Ltd.
本软件版权为北京马赫谷科技有限公司所有
***************************************************************************/

#ifndef __MACHLAKE_SDK_H__
#define __MACHLAKE_SDK_H__

#define MAX_ASYNC_TASK_FD                   16

#define METADATA_OBJECT_NAME_LEN            128
#define METADATA_OBJECT_ID_LEN              (64 + 1)
#define METADATA_ZONE_LEN                   (32 + 1)
#define METADATA_LABEL_LEN                  (16 * 33)
#define METADATA_SITE_ID_LEN                (32 + 1)
#define METADATA_SEARCH_SITE_ID_LEN         (33 * 128)

#define MAX_SYNC_SEARCH_NUM                 1000
#define MIN_SYNC_SEARCH_NUM                 (-1000)

#define METADATA_FILE_LINE_MAX_LEN          2048
#define METADATA_FILE_PATH_MAX_LEN          256
#define OBJECT_FILE_PATH_MAX_LEN            256
#define OBJECT_DIR_PATH_MAX_LEN             128

#define SDK_SEARCH_SORT_NO_METHOD           0           //批量查询排序方式：不排序
#define SDK_SEARCH_SORT_ASC                 1           //批量查询排序方式：按对象创建时间顺序排列
#define SDK_SEARCH_SORT_DESC                2           //批量查询排序方式：按对象创建时间倒序排列

#define SDK_OBJECT_SIZE_MAX                 (64 * 1000 * 1000)

//订阅任务类型
enum machlake_sub_type{
    SUB_TYPE_META = 0,    //只订阅元数据
    SUB_TYPE_OBJECT,      //订阅元数据+data
    SUB_TYPE_MAX,
};

//订阅任务状态
enum machlake_sub_task_state{
    SUB_STATE_NORMAL = 0,    //正常状态
    SUB_STATE_DONE,          //订阅已结束
    SUB_STATE_MULTI_CONN,    //多个连接使用同一个任务ID
    SUB_STATE_MAX_TASK,      //达到最大任务数
    SUB_STATE_MAX,
};

//传输模式，目前只支持TCP，后面考虑支持SSL
enum machlake_trans_mode{
    TRANS_MODE_TCP = 0,
    TRANS_MODE_NONE,
};

/* SEARCH NODE ERR CODE */
typedef enum sdk_search_err_tag{
    SDK_SEARCH_ERR_BEGIN = 0,
    SDK_SEARCH_ERR,
    SDK_SEARCH_IDX_TIMEOUT,
    SDK_SEARCH_DAT_TIMEOUT,
    SDK_SEARCH_ERR_MAX,
}sdk_search_err;

enum machlake_err_no{
    MACHLAKE_OK = 0,
    MACHLAKE_PARAM_ERR = 1,                             //参数错误
    MACHLAKE_FD_CREATE_ERR,                             //创建标识符失败
    MACHLAKE_SERVER_CONNECT_ERR,                        //与SERVER连接异常
    MACHLAKE_QUERY_ID_ERR,                              //
    MACHLAKE_QUERY_BUFFER_NOT_ENOUGH,                   //5
    MACHLAKE_SERVER_AVP_TYPE_ERR,                       //server返回异常类型响应
    MACHLAKE_TLV_BUFFER_ERR,                            //消息拼装或拆解异常
    MACHLAKE_MALLOC_BUFFER_ERR,                         //申请内存失败
    MACHLAKE_NO_MATCH_DATA,                             //没有匹配的数据
    MACHLAKE_SERVER_MATCH_DATA_ERR,                     //SERVER查找数据错误10
    MACHLAKE_INIT_ASYNC_TASK_ERR,                       //初始化异步任务失败
    MACHLAKE_AIO_TASK_LIST_FULL,                        //异步任务队列满员
    MACHLAKE_CLIENT_AUTH_ERR,                           //客户端认证错误
    MACHLAKE_AUTH_TIME_WRONG,                           //认证时间有误
    MACHLAKE_AUTH_TOKEN_WRONG,                          //认证Token有误15
    MACHLAKE_AUTH_ID_WRONG,                             //认证ID有误
    MACHLAKE_AUTH_CONN_MAX,                             //SDK连接数超出最大值
    MACHLAKE_SUB_MULTI_CONN,                            //订阅任务多个连接使用同一个task_id
    MACHLAKE_SUB_MAX_NUM,                               //达到最大任务数19
    MACHLAKE_SUB_MULTI_LAKE_ONE_TASK,                   //未删除plake上面旧的task
    MACHLAKE_FUNC_USE_ERR,                              //函数使用错误
    MACHLAKE_MAX_ERR_NO,
};

/* 对象元数据结构体 */
typedef struct metadata_tag {
    uint32_t    create_time;                            //创建时间
    uint32_t    ingest_time;                            //写入时间
    uint32_t    object_size;                            //对象规格
    char        object_id[METADATA_OBJECT_ID_LEN];      //对象ID
    char        object_name[METADATA_OBJECT_NAME_LEN];  //对象名称
    char        zone[METADATA_ZONE_LEN];                //虚拟域
    char        site[METADATA_SITE_ID_LEN];             //位置
    char        label[METADATA_LABEL_LEN];              //标签
    char*       object;                                 //对象内容
}metadata;

typedef struct metadata_v1_tag {
    uint32_t    create_time;                            //创建时间
    uint32_t    ingest_time;                            //写入时间
    uint64_t    object_size;                            //对象规格
    char        object_id[METADATA_OBJECT_ID_LEN];      //对象ID
    char        object_name[METADATA_OBJECT_NAME_LEN];  //对象名称
    char        zone[METADATA_ZONE_LEN];                //虚拟域
    char        site[METADATA_SITE_ID_LEN];             //位置
    char        label[METADATA_LABEL_LEN];              //标签
    char*       object;                                 //对象内容
}metadata_v1;

/* 查询条件结构体 */
typedef struct metadata_search_tag {
    uint32_t    start_time;                             //开始时间
    uint32_t    end_time;                               //结束时间
    uint32_t    time_out;                               //超时时间
    char        zone[METADATA_ZONE_LEN];                //虚拟域
    char        site[METADATA_SEARCH_SITE_ID_LEN];      //位置
    char        label[METADATA_LABEL_LEN];              //标签
}metadata_search;

/* 初始化接口 */
/***********************************************************************
 *函 数 名: iosp_global_init
 *功能描述: 全局初始化接口
 *输入参数: pcLogPath-----SDK日志路径
 *输出参数: 无
 *返 回 值: 0-------------连接成功
            其他----------失败
***********************************************************************/
extern int iosp_global_init(char *log_path);

/***********************************************************************
 *函 数 名: iosp_sync_init
 *功能描述: ucMode--------传输方式
            pcServerIp----DATANODE的IP
            usPort--------DATANODE的PORT
            pcClientId----客户端ID
            pcToken-------客户端认证所使用的TOKEN
 *输出参数: 无
 *返 回 值: 地址----------同步任务结构指针
            NULL----------失败
***********************************************************************/
extern void *iosp_sync_init(unsigned char ucMode, char *s_ip, unsigned short s_port, char *c_id, char *c_token);

/***********************************************************************
 *函 数 名: iosp_aio_init
 *功能描述: 创建异步任务接口
 *输入参数: pcServerIp----DATANODE的IP
            usPort--------DATANODE的PORT
            pcClientId----客户端ID
            pcToken-------客户端认证所使用的TOKEN
 *输出参数: 无
 *返 回 值: 大于等于0-----异步任务结构标识符
            小于0的值-----失败
***********************************************************************/
extern int iosp_aio_init(char *s_ip, unsigned short s_port, char *c_id, char *c_token);

/* 释放接口 */
/***********************************************************************
 *函 数 名: iosp_sync_destory
 *功能描述: 释放同步任务接口
 *输入参数: pplake--------初始化成功的同步任务结构指针的地址
 *输出参数: 无
 *返 回 值: 无
***********************************************************************/
extern void  iosp_sync_destory(void **pplake);

/***********************************************************************
 *函 数 名: iosp_sync_result_release
 *功能描述: 释放上次同步查询任务的结果
 *输入参数: plake---------初始化成功的同步任务结构指针
 *输出参数: 无
 *返 回 值: 无
***********************************************************************/
extern void iosp_sync_result_release(void *plake);

/***********************************************************************
 *函 数 名: iosp_aio_release
 *功能描述: 释放异步任务接口
 *输入参数: task_fd-------初始化成功的异步任务结构标识符
 *输出参数: 无
 *返 回 值: 无
***********************************************************************/
extern void iosp_aio_release(int task_fd);

/* 同步操作接口 */
/***********************************************************************
 *函 数 名: iosp_read_metadata
 *功能描述: 通过对象ID读取指定对象的元数据
 *输入参数: plake---------初始化成功的同步任务结构指针
            pcObjectId----对象ID
            pbuffer------读取对象元数据使用的缓冲区指针
 *输出参数: pbuffer
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_read_metadata(void *plake, char *object_id, void **buffer);

/***********************************************************************
 *函 数 名: iosp_read_object
 *功能描述: 通过对象ID读取指定对象的全部信息
 *输入参数: plake---------初始化成功的同步任务结构指针
            pcObjectId----对象ID
            pbuffer------读取对象元数据使用的缓冲区指针
 *输出参数: pbuffer
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_read_object(void *plake, char *object_id, void **buffer);

/***********************************************************************
 *函 数 名: iosp_write_object
 *功能描述: 通过对象ID读取指定对象的全部信息
 *输入参数: plake---------初始化成功的同步任务结构指针
            meta_p---对象元数据及正文内容的指针
            pcBuffer------返回对象ID缓冲区指针
            iBufferLen----返回对象ID缓冲区大小
 *输出参数: pbuffer
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_write_object(void *plake, metadata *meta, char *buffer, int buf_len);

int iosp_healthy_check_write_object(void *plake, metadata *meta, char *buffer, int buf_len);
int iosp_healthy_check_read_object(void *plake, char *object_id, void **buffer);

/***********************************************************************
 *函 数 名: iosp_modify_label
 *功能描述: 通过对象ID读取指定对象的全部信息
 *输入参数: plake---------初始化成功的同步任务结构指针
            pcID----------对象ID
            pcLabel-------新标签
 *输出参数: 无
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_modify_label(void *plake, char *object_id, char *label);

/***********************************************************************
 *函 数 名: iosp_search_analys
 *功能描述: 指定查询条件，查询符合条件的统计数据
 *输入参数: plake-------------初始化成功的同步任务结构指针
            meta_search-批量查询条件结构指针
            ppcAnalysResult---分析结果
            ppcDnMsg----------查询时数据节点状态消息
 *输出参数: ppcAnalysResult
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_search_analys(void *plake, metadata_search *meta_search, char **ana_res, char **dn_msg);

/***********************************************************************
 *函 数 名: iosp_search_metadata
 *功能描述: 指定查询条件，批量查询对象元数据
 *输入参数: plake-------------初始化成功的同步任务结构指针
            meta_search-批量查询条件结构指针
            ucSortType--------查询模式
            piSearchNum-------查询数量
            pulCursor---------查询游标
            ppmeta_p-----查询结果缓冲区指针
            ppcDnMsg----------查询时数据节点状态消息
 *输出参数: piSearchNum
            pulCursor
            ppmeta_p
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_search_metadata(void *plake, metadata_search *meta_search, unsigned char sort_type, int *search_num_p, 
        uint32_t *cursor_p, metadata ***meta_array, char **dn_msg);

/***********************************************************************
 *函 数 名: iosp_search_object
 *功能描述: 指定查询条件，批量查询对象元数据
 *输入参数: plake-------------初始化成功的同步任务结构指针
            meta_search-批量查询条件结构指针
            ucSortType--------查询模式
            piSearchNum-------查询数量
            pulCursor---------查询游标
            ppmeta_p-----查询结果缓冲区指针
            ppcDnMsg----------查询时数据节点状态消息
 *输出参数: piSearchNum
            pulCursor
            ppmeta_p
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_search_object(void *plake, metadata_search *meta_search, unsigned char sort_type, int *search_num_p,
        uint32_t *cursor_p, metadata ***meta_array, char **dn_msg);

/***********************************************************************
 *函 数 名: iosp_search_object_write_disk
 *功能描述: 指定查询条件，批量查询对象全部信息并存在指定路径
 *输入参数: plake-------------初始化成功的同步任务结构指针
            meta_search-批量查询条件结构指针
            pcSavePath--------存放路径
            iDirFileNum-------单个文件夹中的对象数量
 *输出参数: 无
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_search_object_write_disk(void *plake, metadata_search *meta_search, char *save_path, int dir_file_num);

/* 异步操作接口 */
/***********************************************************************
 *函 数 名: iosp_aio_read_metadata
 *功能描述: 通过对象ID读取指定对象元数据的异步任务接口
 *输入参数: task_fd-----------初始化成功的异步任务结构标识符
            pcObjectId--------对象ID
            buffer-----------内存缓冲区，回调时的入参pOutBuffer
            pfRdCallBack------回调函数
 *回调参数: pstRecvMeta-------读取结果缓冲区指针
            pOutBuffer--------用户传入的缓冲区指针 
            iRet--------------任务执行结果，0表示成功，小于0表示失败
 *输出参数: buffer
 *返 回 值: 0-------------添加读取元数据任务成功，读取结果需要在回调函数中判断
            小于0的值-----添加读取元数据任务失败
***********************************************************************/
extern int iosp_aio_read_metadata(int task_fd, char *object_id, void *buffer,
    void (*cb_func_rd)(metadata *meta_p, void *out_buf, int ret));

/***********************************************************************
 *函 数 名: iosp_aio_read_object
 *功能描述: 通过对象ID读取指定对象全部信息的异步任务接口
 *输入参数: task_fd-----------初始化成功的异步任务结构标识符
            pcObjectId--------对象ID
            buffer-----------内存缓冲区，回调时的入参pOutBuffer
            pfRdCallBack------回调函数
 *回调参数: pstRecvMeta-------读取结果缓冲区指针
            pOutBuffer--------用户传入的缓冲区指针 
            iRet--------------任务执行结果，0表示成功，小于0表示失败
 *输出参数: buffer
 *返 回 值: 0-------------添加读取对象任务成功，读取结果需要在回调函数中判断
            小于0的值-----添加读取对象任务失败
***********************************************************************/
extern int iosp_aio_read_object(int task_fd, char *object_id, void *buffer,
    void (*cb_func_rd)(metadata *meta_p, void *out_buf, int ret));

/***********************************************************************
 *函 数 名: iosp_aio_write_object
 *功能描述: 向IOSP中写入一个对象的异步任务接口
 *输入参数: task_fd-----------初始化成功的异步任务结构标识符
            meta_p-------对象元数据及内容指针
            buffer-----------内存缓冲区，回调时的入参pOutBuffer
            pfWrCallBack------回调函数
 *回调参数: pstRecvMeta-------读取结果缓冲区指针
            pOutBuffer--------用户传入的缓冲区指针 
            iRet--------------任务执行结果，0表示成功，小于0表示失败
 *输出参数: buffer
 *返 回 值: 0-------------添加写入对象任务成功，写入结果需要在回调函数中判断
            小于0的值-----添加写入对象任务失败
***********************************************************************/
extern int iosp_aio_write_object(int task_fd, metadata * meta_p, char *buffer,
    int (*cb_func_wr)(char *recv_buf, void *out_buf, int ret));

/***********************************************************************
 *函 数 名: iosp_aio_block_wait
 *功能描述: 阻塞等待异步任务直到其完成全部事件
 *输入参数: task_fd-------初始化成功的异步任务结构标识符
 *输出参数: 无
 *返 回 值: 0-------------成功
            小于0的值-----失败
***********************************************************************/
extern int iosp_aio_block_wait(int task_fd);

/* 错误输出接口 */
/***********************************************************************
 *函 数 名: iosp_get_error_code
 *功能描述: 在调用返回值为指针类型的接口报错时，可调用此函数获取错误码
 *输入参数: 无
 *输出参数: 无
 *返 回 值: 0-------------成功
            小于0的值-----错误码
***********************************************************************/
extern int iosp_get_error_code(void);

/***********************************************************************
 *函 数 名: iosp_get_error_code
 *功能描述: 对于int类型的接口函数返回值为小于0的值时，调用此接口可以获取
            对应错误码的描述信息
 *输入参数: iErrCode------错误码
 *输出参数: 无
 *返 回 值: 字符串--------错误码对应的描述信息
***********************************************************************/
extern const char* iosp_get_error(int err_code);
extern const char* iosp_get_error_from_lake(void *plake);

/* 测试落盘接口 */
extern int iosp_result_save_proc(char *s_ip, unsigned short s_port, char *c_id, char *c_token, 
    char *save_path, char *zone, char *site, uint32_t stime, uint32_t etime, int pthread_num);

extern uint64_t iosp_subscribe_task_create(void *plake, metadata_search *meta_search, unsigned char type, uint64_t task_id);
extern int iosp_subscribe_consume(void *plake, int *search_num_p, uint64_t task_id, metadata ***meta_array);
extern int iosp_subscribe_destroy(void *plake);
extern int iosp_subscribe_destroy_for_taskid(void *plake, uint64_t task_id);

/* ----------sdk v1 interface---------- */
extern int  iosp_global_init_v1(char *log_path);
extern void *iosp_sync_init_v1(unsigned char mode, char *s_ip, unsigned short s_port, char *c_id, char *c_token);
extern int  iosp_aio_init_v1(char *s_ip, unsigned short s_port, char *c_id, char *c_token);
extern void iosp_sync_destory_v1(void **pplake);
extern void iosp_sync_result_release_v1(void *plake);
extern void iosp_aio_release_v1(int task_fd);
extern int  iosp_aio_block_wait_v1(int task_fd);

extern int iosp_get_error_code_v1(void);
extern const char* iosp_get_error_v1(int err_code);

extern int iosp_read_metadata_v1(void *plake, char *object_id, void **buffer);
extern uint64_t iosp_read_object_content_v1(void *plake, char *object_id, char *buffer, uint64_t buf_len, uint64_t pos);
extern int iosp_read_object_simple_v1(void *plake, char *object_id, void **buffer);
extern uint64_t iosp_read_object_v1(void *plake, char *object_id, char *buffer, uint64_t buf_len, uint64_t pos);
extern int iosp_write_object_v1(void *plake, metadata_v1 * meta_v1, char *buffer, int len);
extern int iosp_write_file_v1(void *plake, metadata_v1 * meta_v1, char *file_name, char *buffer, int len);
extern int iosp_write_mem_v1(void *plake, metadata_v1 * meta_v1, char *buffer, uint32_t buf_len, char *string, uint32_t str_len);

extern int iosp_search_metadata_v1(void *plake, metadata_search *meta_search, unsigned char sort_type, int *search_num_p,
        uint32_t *cursor_p, metadata_v1 ***meta_v1_array, char **dn_msg);
extern int iosp_search_object_v1(void *plake, metadata_search *meta_search, unsigned char sort_type, int *search_num_p,
        uint32_t *cursor_p, metadata_v1 ***meta_v1_array, char **dn_msg);
extern int iosp_search_object_write_disk_v1(void *plake, metadata_search *meta_search, char *save_path, int dir_file_num);
extern int iosp_search_analys_v1(void *plake, metadata_search *meta_search, char **ana_res, char **dn_msg);
extern int iosp_modify_label_v1(void *plake, char *object_id, char *label);

extern int iosp_aio_write_object_v1(int task_fd, metadata_v1 *meta_v1, char *buffer,
    int (*cb_func_wr)(char *msg_recv, void *out, int ret));
extern int iosp_aio_read_metadata_v1(int task_fd, char * object_id, void *buffer,
    void (*cb_func_rd_v1)(metadata_v1 *meta_v1_p, void *out_buf, int ret));
extern int iosp_aio_read_object_v1(int task_id, char * object_id, void *buffer,
    void (*cb_func_rd_v1)(metadata_v1 *meta_v1_p, void *out_buf, int ret));

extern uint64_t iosp_subscribe_task_create_v1(void *plake, metadata_search *meta_search, unsigned char type, uint64_t task_id);
extern int iosp_subscribe_destroy_v1(void *plake);
extern int iosp_subscribe_destroy_for_taskid_v1(void *plake, uint64_t task_id);
extern int iosp_subscribe_consume_v1(void *plake, int *search_num_p, uint64_t task_id, metadata_v1 ***meta_v1_array);

#endif
