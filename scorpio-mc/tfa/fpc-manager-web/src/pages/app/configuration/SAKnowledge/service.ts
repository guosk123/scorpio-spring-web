import ajax from '@/utils/frame/ajax';
import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
import type {
  AppCategoryItem,
  ApplicationItem,
  AppSubCategoryItem,
  ECustomSAApiType,
} from './typings';

// eslint-disable-next-line import/prefer-default-export
export async function querySaKnowledgeInfo() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/knowledge-infos`);
}

/**
 * 查询所有的应用字典
 */
export async function queryAllApplicaiton() {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/rules`);
}

/**
 * 自定义分类、子分类、应用详情
 */
export async function queryCustomSADetail({ id, type }: { id: string; type: ECustomSAApiType }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/${type}/${id}`);
}

interface IParams {
  data: ApplicationItem | AppSubCategoryItem | AppCategoryItem;
  type: ECustomSAApiType;
}
/**
 * 新建自定义分类、子分类、应用
 */
export async function createCustomSA({ data, type }: IParams) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/${type}`, {
    type: 'POST',
    data: {
      ...data,
    },
  });
}

/**
 * 编辑自定义分类、子分类、应用
 */
export async function updateCustomSA({ data, type }: IParams) {
  const { id, ...restData } = data;
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/${type}/${id}`, {
    type: 'POST',
    data: {
      _method: 'PUT',
      ...restData,
    },
  });
}

/**
 * 删除自定义分类、子分类、应用
 */
export async function deleteCustomSA({ id, type }: { id: string; type: ECustomSAApiType }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/${type}/${id}`, {
    type: 'POST',
    data: {
      _method: 'DELETE',
    },
  });
}

/**
 * 导入自定义分类、子分类、应用
 */
export async function importCustomSA(formData: { file: any; type: ECustomSAApiType }) {
  return ajax(`${API_VERSION_PRODUCT_V1}/appliance/sa/as-import`, {
    type: 'POST',
    processData: false, //  告诉jquery不要处理发送的数据
    contentType: false, // 告诉jquery不要设置content-Type请求头
    data: formData,
  });
}
