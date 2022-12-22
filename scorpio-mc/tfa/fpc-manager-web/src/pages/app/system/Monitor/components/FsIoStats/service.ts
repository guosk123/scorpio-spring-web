import { API_VERSION_PRODUCT_V1 } from "@/common/applicationConfig";
import type { IAjaxResponseFactory, ITimeParams } from "@/common/typings";
import ajax from "@/utils/frame/ajax";
import { stringify } from "qs";
import type { IFsIoMetric } from "./typings";

export async function queryFsIoHistogram(params: ITimeParams & {
  /** 分区名，不填时展示所有分区的 IO 统计 */
  partitionName?: string
}): Promise<IAjaxResponseFactory<IFsIoMetric[]>> {
  return ajax(`${API_VERSION_PRODUCT_V1}/system/io-metrics/as-histogram?${stringify(params)}`);
}