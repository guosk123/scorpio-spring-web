const dhcp = [
  'timestamp',
  'messageType',
  'clientIpAddress',
  'clientMacAddress',
  'serverIpAddress',
  'serverMacAddress',
  'totalBytes',
  'sendBytes',
  'receiveBytes',
  'totalPackets',
  'sendPackets',
  'receivePackets',
] as const;

export type DHCPFields = typeof dhcp[number];
type DhcpFields = {
  [key in DHCPFields]: key;
};

export const EDHCPFields: DhcpFields = dhcp.reduce((res, item) => {
  res[item] = item;
  return res;
}, Object.create(null));
