export enum EIpLabelCatagory {
  domain = '1',
  company = '2',
  location = '3',
  topic = '4',
  target = '5',
  target_importance = '6',
  asset_property = '7',
  influence = '8',
}

export interface IIpLabel {
  id: string;
  name: string;
  category: EIpLabelCatagory;
  ipAddress: string;
  description?: string;
}

export const IpLabelCategoryText: Record<EIpLabelCatagory, string> = {
  [EIpLabelCatagory.domain]: '行业标签',
  [EIpLabelCatagory.company]: '单位标签',
  [EIpLabelCatagory.location]: '地区标签',
  [EIpLabelCatagory.topic]: '专题标签',
  [EIpLabelCatagory.target_importance]: '重保目标重要性等级',
  [EIpLabelCatagory.influence]: '影响范围',
  [EIpLabelCatagory.target]: '重保目标标签',
  [EIpLabelCatagory.asset_property]: '资产属性标签',
};
