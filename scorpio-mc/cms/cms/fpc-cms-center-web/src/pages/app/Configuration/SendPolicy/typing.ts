export interface ISendPolicy {
  id: string;
  name: string;
  externalReceiverId: string;
  state: '0' | '1';
  sendRuleId: string;
  networkId: string;
  packetAnalysisTaskId: string;
  quote: string,
  quoteText: string,
}
