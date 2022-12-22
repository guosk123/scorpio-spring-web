interface IBaseInfo {
  /**
   * 名称英文
   */
  name: string;
  /**
   * 名称中文
   */
  nameText: string;
  /**
   * 描述英文
   */
  description?: string;
  /**
   * 描述中文
   */
  descriptionText?: string;

  // 自定义拥有的属性
  id?: string;
  isCustom?: boolean;
}

/**
 * 应用大类
 */
export interface AppCategoryItem extends IBaseInfo {
  categoryId: string;
  subCategoryList?: AppSubCategoryItem[];

  // 自定义
  subCategory?: string;
}

/**
 * 应用子分类
 */
export interface AppSubCategoryItem extends IBaseInfo {
  subCategoryId: string;
  categoryId: string;
  applicationList?: ApplicationItem[];

  /**
   * 【自定义属性】应用IDs
   */
  applicationIds?: string;
}

/**
 * 应用
 */
export interface ApplicationItem extends IBaseInfo {
  applicationId: string;
  categoryId: string;
  subCategoryId: string;

  rule?: string;
  l7ProtocolId: string;
}

/**
 * 自定义分类
 */
export interface ICustomCategory {
  categoryId: string;
  createTime: string;
  description: string;
  id: string;
  name: string;
  subCategoryIds: string;
}

/**
 * 自定义子分类
 */
export interface ICustomSubCategory {
  categoryId: string;
  subCategoryId: string;
  createTime: string;
  description: string;
  id: string;
  name: string;
  applicationIds: string;
}

/**
 * 所有的应用字典返回值
 */
export interface AppCategoryResponseData {
  categoryList: AppCategoryItem[];
  customCategoryList: ICustomCategory[];
  subCategoryList: AppSubCategoryItem[];
  customSubCategoryList: ICustomSubCategory[];
  applicationList: ApplicationItem[];
  customApplicationList: ApplicationItem[];
}

export interface IApplicationInfo {
  categoryId: string | number;
  application_category_id: string | number;
  categorynName: string;
  application_category_name: string;
  categorynDescription: string;
  application_category_description: string;
  subCategoryId: string | number;
  application_subcategory_id: string | number;
  subCategorynName: string;
  application_subcategory_name: string;
  subCategorynDescription: string;
  application_subcategory_description: string;
  applicationId: string | number;
  application_id: string | number;
  applicationName: string;
  application_name: string;
  applicationDescription: string;
  application_description: string;
  maliciousApplicationId: string;
  malicious_application_id: string | number;
  maliciousApplicationName: string | number;
  malicious_application_name: string;
  maliciousApplicationDescription: string;
  malicious_application_description: string;
}

export type IApplicationMap = Record<string, ApplicationItem>;

/**
 * SA知识库版本信息
 */
export interface ISaKnowledgeInfo {
  version?: string;
  releaseDate?: string;
  uploadDate?: string;
}

/**
 * 自定义分类、子分类、应用
 */
export enum ECustomSAApiType {
  'CATEGORY' = 'custom-categorys',
  'SUB_CATEGORY' = 'custom-subcategorys',
  'APPLICATION' = 'custom-applications',
}
