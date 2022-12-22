export enum EUserStatus {
  /** 正常 */
  UnLocked = '0',
  /** 锁定 */
  Locked = '1',
}

export interface IUser {
  appKey: string;
  appToken: string;
  createTime: string;
  description: string;
  email: string;
  fullname: string;
  id: string;
  locked: EUserStatus;
  lockedText: string;
  name: string;
  userRoles: IUserRole[];
  latestLoginTime: string;
}

export interface IUserRole {
  id: string;
  /** 英文名称 */
  nameEn: string;
  /** 中文名称 */
  nameZh: string;
  description: string;
}
