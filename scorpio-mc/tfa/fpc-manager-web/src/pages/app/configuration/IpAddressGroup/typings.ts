export interface IpAddressGroup {
  id: string;
  name: string;
  description?: string;
  ipAddress?: string;
}

export type IpAddressGroupMap = Record<string, IpAddressGroup>;
