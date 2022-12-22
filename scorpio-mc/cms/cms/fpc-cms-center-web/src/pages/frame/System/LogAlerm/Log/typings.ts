export interface ISystemLog {
  id: string;
  level: string;
  source: string;
  nodeId: string;
  ariseTime: string;
  category: string;
  component: string;
  content: string;
}

export interface optionsType {
  label: string;
  key: string;
}