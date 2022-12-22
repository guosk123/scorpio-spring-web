/**
 * 连接状态：正常
 */
 export const CONNECT_STATUS_OK = '0';
 export const CONNECT_STATUS_OK_COLOR = '#52c41a';

 /**
 * 连接状态：异常
 */
export const CONNECT_STATUS_FAILED = '1';
export const CONNECT_STATUS_FAILED_COLOR = '#f5222d';

/**
 * 任务下发状态：下发成功
 */
 export const TASK_ASSIGNMENT_STATE_SUCCESS = '0';
 /**
  * 任务下发状态：正在下发
  */
 export const TASK_ASSIGNMENT_STATE_RUNNING = '1';
 
 /**
  * 任务下发状态：下发失败
  */
 export const TASK_ASSIGNMENT_STATE_FAILED = '2';
 /**
  * 任务下发状态：等待下发
  */
 export const TASK_ASSIGNMENT_STATE_WAITING = '3';

 /**
 * 任务下发状态：取消下发
 */
export const TASK_ASSIGNMENT_STATE_CANCEL = '4';

/**
 * 任务下发状态：停止下发
 */
export const TASK_ASSIGNMENT_STATE_STOP = '5';

/**
 * 任务执行状态：未启动
 */
 export const TASK_SEEK_STATUS_NOT_START = 0;
 /**
  * 任务执行状态：执行中
  */
 export const TASK_SEEK_STATUS_RUNNING = 1;
 /**
  * 任务执行状态：已完成
  */
 export const TASK_SEEK_STATUS_FINISHED = 2;

export const TASK_SEEK_STATUS_LIST = [
  {
    label: '未启动',
    key: TASK_SEEK_STATUS_NOT_START,
  },
  {
    label: '执行中',
    key: TASK_SEEK_STATUS_RUNNING,
  },
  {
    label: '已完成',
    key: TASK_SEEK_STATUS_FINISHED,
  },
];


/**
 * 查询任务执行状态：进行中
 */
 export const TASK_EXECUTION_STATE_RUNNING = '0';

 /**
  * 查询任务执行状态：已停止
  */
 export const TASK_EXECUTION_STATE_STOPPED = '1';
 
 /**
  * 查询任务执行状态：已完成
  */
 export const TASK_EXECUTION_STATE_FINISHED = '2';

 /**
 * 落盘文件中的数据仅包含查询条件中的部分数据
 */
export const PCAP_IS_WRITE_ALL_DATA_FALSE = 0;
