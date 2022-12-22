export interface ISsoPlatformSearchParams {
  name: string;
}

export interface ISsoPlatform {
  id?: string;
  name: string;
  platformId: string;
  appToken: string;
  description?: string;
  createTime: string;
}

export interface ISsoUserSearchParams {
  platformName: string;
}

export interface ISsoUser {
  id?: string;
  ssoPlatformId: string;
  platformUserId: string;
  systemUserId: string;
  description?: string;
  createTime: string;
}

export interface ISystemUser {
  id: string;
  name: string;
  fullname: string;
  locked: '0' | '1';
  lockedText: string;
  userRoles: IUserRole[];
  userType: string;
}

export enum EUSERTYPE {
  SIMPLEUSER = '0',
  RESTUSER = '1',
  SINGLESIGNUSER = '2',
}

interface IUserRole {
  id: string;
  description: string;
  nameEn: string;
  nameZh: string;
}
