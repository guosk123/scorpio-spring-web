/**
 * ===========
 *  外部存储服务器配置
 * ===========
 */

 import ajax from '@/utils/frame/ajax';
 import { API_VERSION_PRODUCT_V1 } from '@/common/applicationConfig';
 import type { IExternalStorage } from './typings';
 import type { IAjaxResponseFactory } from '@/common/typings';
 
 /**
  * 查询外部存储服务器列表（不分页）
  */
 export async function queryExternalStorageList(): Promise<
   IAjaxResponseFactory<IExternalStorage[]>
 > {
   return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-storages`);
 }
 
 /**
  * 查询外部存储服务器详情
  */
 export async function queryExternalStorageDetail(
   id: string,
 ): Promise<IAjaxResponseFactory<IExternalStorage>> {
   return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-storages/${id}`);
 }
 
 /** 新增外部存储服务器 */
 export async function createExternalStorage(
   params: IExternalStorage,
 ): Promise<IAjaxResponseFactory<any>> {
   return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-storages`, {
     type: 'POST',
     data: {
       ...params,
     },
   });
 }
 
 /** 编辑外部存储服务器 */
 export async function updateExternalStorage({
   id,
   ...restParams
 }: IExternalStorage): Promise<IAjaxResponseFactory<any>> {
   return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-storages/${id}`, {
     type: 'POST',
     data: {
       _method: 'PUT',
       ...restParams,
     },
   });
 }
 
 /** 删除外部存储服务器 */
 export async function deleteExternalStorage(id: string): Promise<IAjaxResponseFactory<any>> {
   return ajax(`${API_VERSION_PRODUCT_V1}/appliance/external-storages/${id}`, {
     type: 'POST',
     data: {
       _method: 'DELETE',
     },
   });
 }
 