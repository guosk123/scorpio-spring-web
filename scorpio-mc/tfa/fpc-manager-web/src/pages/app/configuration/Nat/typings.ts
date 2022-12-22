export enum NATAction {
  'open' = '1',
  'close' = '0',
}

export interface NATConfig {
  id: 1;
  natAction: NATAction;
}
