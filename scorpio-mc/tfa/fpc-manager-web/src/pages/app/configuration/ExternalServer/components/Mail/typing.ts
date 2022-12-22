export interface IMailType {
  /** id */
  id: string;
  /** 名称 */
  name: string;
  /** 收件人 */
  receiver: string;
  /** 主题 */
  mailTitle: string;
  /** 抄送 */
  cc: string;
  /** 密送 */
  bcc: string;
}
