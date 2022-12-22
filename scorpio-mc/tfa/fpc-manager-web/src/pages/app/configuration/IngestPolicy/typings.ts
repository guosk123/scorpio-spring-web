/**
 * 捕获过滤
 */
export interface IIngestPolicy {
  id: string;
  name: string;
  /**
   * 默认规则
   */
  defaultAction: '0' | '1';
  /**
   * 报文去重
   */
  deduplication: '0' | '1';
  /**
   * BPF过滤条件
   */
  exceptBpf?: string;
  /**
   * 流过滤条件
   */
  exceptTuple?: string;
  
  description?: string;

  /**
   * 策略使用次数
   */
  referenceCount: number;
}

/**
 * 流过滤规则
 */
export interface IExceptTuple {
  sourceIp: string;
  sourcePort: string;
  destIp: string;
  destPort: string;
  protocol: string;
  vlanId: string;
}
