import type { IFilter } from "@/components/FieldFilter/typings";
import type { IFlowAnalysisData } from "../../typings";
import type { TFlowAnalysisType } from "../fieldsManager";

export interface IActionLinkParams {
  /** 类型 */
  type: TFlowAnalysisType;
  /** 表格中每一行的记录数据 */
  record: IFlowAnalysisData;
  /** 已经存在的Filter条件 */
  filter: IFilter[];
  /** 网络ID */
  // networkId: string;
  /** 业务ID */
  // serviceId?: string;
}