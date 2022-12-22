export enum ACCESS_FUNCTION_TYPE {
  'CHECKBOX' = 'checkBox',
  'DEFAULT' = 'default',
}

export interface IAccessRouteItem {
  path: string;
  title?: string | undefined;
  access?: string | undefined;
  routes?: IAccessRouteItem[] | undefined;
  accessFunction?: { title: string; path: string; type?: ACCESS_FUNCTION_TYPE }[];
  // 忽略显示
  accessMenuIgnore?: boolean;
  // 默认标签
  defTab?: boolean;
  key?: string;
  type?: ACCESS_FUNCTION_TYPE;
  children?: IAccessRouteItem[] | undefined;
}

export interface IMenuAccessItem {
  resource: string;
  perm: boolean | number;
}
