/** 分页封装器 */
export interface IPageFactory<T> {
  /** 当前页数 */
  number: number;
  /** 每页记录数 */
  size: number;
  /** 总页数 */
  totalPages: number;
  /** 总条数 */
  totalElements: number;
  /** 内容 */
  content: T[];
}

/** ajax 封装好的返回值 */
export interface IAjaxResponseFactory<T> {
  status?: number;
  success: boolean;
  result: T;
}

export interface IProTableData<T> {
  success: boolean;
  data: T;
  page: number;
  total: number;
}

export interface IPageParms {
  page?: number;
  pageSize?: number;
  sortProperty?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface IEnumValue {
  value: string;
  text: string;
}
