// import { Request, Response } from 'umi';
// import { Random } from 'mockjs';

// interface MitreAttack {
//   id: string;
//   name: string;
//   parentId: string;
// }

// interface ClassType {
//   name: string;
//   level: number;
//   id: string;
// }

// export interface ISuricataRule {
//   // suricata 规则id， 由用户填写
//   sid: number;
//   // 规则描述
//   msg: string;
//   // 规则正文
//   content: string;
//   // 规则分类
//   classtypeId?: string;
//   // 战术策略
//   mitreTacticId?: string;
//   // 技术分类
//   mitreTechniqueId?: string;
//   // cve编号
//   cve?: string;
//   // cnnvd编号
//   cnnvd?: string;
//   // 优先级
//   priority: number;
//   // 严重级别
//   signatueSeverity: number;
//   // 受害方
//   target: string;
//   protocol?: string;
//   srcIp: string;
//   srcPort: string;
//   destIp: string;
//   destPort: string;
//   direction: string;
//   state?: number;
//   operatorId: string;
// }

// const mitreAttacks: MitreAttack[] = [];

// for (let i = 0; i < 12; i++) {
//   const rootAttack = {
//     id: Random.id(),
//     name: Random.name(),
//     parentId: '',
//   };

//   mitreAttacks.push(rootAttack);
// }

// for (let i = 0; i < 5; i++) {
//   const attack: MitreAttack = {
//     id: Random.id(),
//     name: Random.name(),
//     parentId: mitreAttacks[0].id,
//   };

//   mitreAttacks.push(attack);
// }

// const classTypes: ClassType[] = [];

// for (let i = 0; i < 4; i++) {
//   classTypes.push({
//     id: Random.id(),
//     name: Random.word(),
//     level: Random.integer(0, 10),
//   });
// }

// const rules: ISuricataRule[] = [];

// for (let i = 0; i < 5; i++) {
//   const rule: ISuricataRule = {
//     sid: Random.increment(),
//     protocol: Random.string(),
//     srcIp: Random.ip(),
//     srcPort: Random.string(undefined, 1, 5),
//     destIp: Random.ip(),
//     destPort: Random.string(undefined, 1, 5),
//     msg: Random.sentence(),
//     content: Random.sentence(),
//     mitreTacticId: Random.id(),
//     mitreTechniqueId: Random.id(),
//     classtypeId: Random.id(),
//     cve: Random.id(),
//     cnnvd: Random.id(),
//     priority: Random.integer(1, 255),
//     signatueSeverity: Random.integer(0, 3),
//     target: Random.boolean() ? 'src' : 'dest',
//     direction: Random.boolean() ? 'srcToDest' : 'twoWay',
//     state: Random.integer(0, 1),
//     operatorId: Random.id(),
//   };

//   rules.push(rule);
// }

// const getClassTypes = (req: Request, res: Response) => {
//   return res.json(classTypes);
// };

// const getMitreAttacks = (req: Request, res: Response) => {
//   return res.json(mitreAttacks);
// };

// const createClassType = (req: Request, res: Response) => {
//   classTypes.push({ ...req.body, id: Random.id() });
//   return res.end();
// };

// const getRules = (req: Request, res: Response) => {
//   return res.json({
//     content: rules,
//     totalElements: rules.length,
//     number: 0,
//     size: 20,
//   });
// };

// const updateClasstype = (req: Request, res: Response) => {
//   console.log(req.body, req.path);
//   res.end();
// };

// const createRule = (req: Request, res: Response) => {
//   console.log(req.body);
//   res.end();
// };

// const batchRules = (req: Request, res: Response) => {
//   console.log(req.body);
//   res.end();
// };

// const getRuleDetail = (req: Request, res: Response) => {
//   return res.json(rules[0]);
// };

// const updateRule = (req: Request, res: Response) => {
//   return res.end();
// };

// const topTargetHost = {};
// for (let i = 0; i < 10; i++) {
//   topTargetHost[Random.ip()] = Random.integer(0, 1000);
// }

// const topOriginIp = {};
// for (let i = 0; i < 10; i++) {
//   topOriginIp[Random.ip()] = Random.integer(0, 1000);
// }

// const topAlarmId = {};
// for (let i = 0; i < 10; i++) {
//   topAlarmId[Random.id()] = Random.integer(0, 1000);
// }

// const classificationProportion = {};
// for (let i = 0; i < 4; i++) {
//   classificationProportion[Random.word()] = Random.integer(0, 1000);
// }

// const mitreTacticProportion = {};
// for (let i = 0; i < 4; i++) {
//   mitreTacticProportion[Random.id()] = Random.integer(0, 1000);
// }

// const alarmTrend = [];
// const now = new Date().getTime();
// for (let i = 0; i < 60; i++) {
//   alarmTrend.push([now + i * 60 * 1000, Random.integer(0, 1000)]);
// }

// const dashboardData = {
//   alarmTotalCount: Random.integer(),
//   topTargetHost,
//   topOriginIp,
//   classificationProportion,
//   topAlarmId,
//   mitreTacticProportion,
//   alarmTrend,
// };

// const getDashboardData = (req: Request, res: Response) => {
//   console.log(req.params);
//   return res.json(dashboardData);
// };

// const suricataStatdata = [
//   { key: '0', count: 195727 },
//   { key: '1', count: 3886 },
//   { key: '32', count: 2374 },
//   { key: '19', count: 2340 },
//   { key: '11', count: 1479 },
//   { key: '23', count: 972 },
//   { key: '15', count: 905 },
//   { key: '33', count: 787 },
//   { key: '9', count: 760 },
//   { key: '16', count: 676 },
// ];

// const getSuricataStat = (req: Request, res: Response) => {
//   return res.json(suricataStatdata);
// };

// const alertMessage = {
//   content: [
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.131.23',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 133,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311822400414',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.24.140',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.131.23',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.147.231',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 67,
//       countryIdInitiator: 235,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 68,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311353754800',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.17.196',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.147.231',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '192.168.24.140',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 67,
//       countryIdInitiator: 255,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 68,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311822400414',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.131.23',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.24.140',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.128.52',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 255,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311219575154',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.38.235',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.128.52',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.124.130',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 133,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311486850764',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.25.44',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.124.130',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.135.200',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 133,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311822400533',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.27.24',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.135.200',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.143.244',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 133,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311219572467',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.0.52',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.143.244',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '192.168.5.110',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 67,
//       countryIdInitiator: 255,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 68,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311486853170',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.116.248',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.5.110',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.143.183',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 235,
//       sid: 197001011,
//       cityIdResponder: 91,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311353754749',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 11,
//       srcIp: '192.168.31.46',
//       countryIdResponder: 91,
//       url: '',
//       target: '114.114.143.183',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.139.48',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 133,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311419747465',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.9.204',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.139.48',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '192.168.2.67',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 67,
//       countryIdInitiator: 235,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 68,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311755290480',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.130.62',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.2.67',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.125.142',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 255,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311755290466',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.30.254',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.125.142',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'DHCP',
//       mitreTacticId: '',
//       destIp: '114.114.124.186',
//       cityIdInitiator: 212,
//       mitreTechniqueId: '',
//       srcPort: 68,
//       countryIdInitiator: 1,
//       sid: 197001011,
//       cityIdResponder: 0,
//       protocol: 'UDP',
//       classtypeId: '',
//       cve: '',
//       destPort: 67,
//       l7Protocol: 'dhcp',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311486850743',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.32.183',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.124.186',
//       provinceIdInitiator: 19,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.37.152',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41634,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.134.17',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.37.152',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '114.114.117.20',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 55768,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '192.168.26.134',
//       countryIdResponder: 0,
//       url: '',
//       target: '114.114.117.20',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.29.133',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41580,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.116.164',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.29.133',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.20.192',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 58,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41634,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '7091529311889506482',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.121.88',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.20.192',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.22.121',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 3,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41634,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 3,
//       srcIp: '114.114.150.50',
//       countryIdResponder: 3,
//       url: '',
//       target: '192.168.22.121',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.37.152',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41634,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.134.17',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.37.152',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//     {
//       msg: 'RAT',
//       mitreTacticId: '',
//       destIp: '192.168.29.133',
//       cityIdInitiator: 0,
//       mitreTechniqueId: '',
//       srcPort: 443,
//       countryIdInitiator: 133,
//       sid: 1101010101,
//       cityIdResponder: 0,
//       protocol: 'TCP',
//       classtypeId: '',
//       cve: '',
//       destPort: 41580,
//       l7Protocol: '',
//       signatureSeverity: 2,
//       networkId: 'bIyMZYABXPHw2vRMbHg2',
//       flowId: '0',
//       timestamp: '2022-04-28T05:55:34Z',
//       provinceIdResponder: 0,
//       srcIp: '114.114.116.164',
//       countryIdResponder: 0,
//       url: '',
//       target: '192.168.29.133',
//       provinceIdInitiator: 0,
//       cnnvd: '',
//       domain: '',
//     },
//   ],
//   sort: [
//     { direction: 'DESC', property: 'timestamp', ascending: false },
//     { direction: 'ASC', property: 'sid', ascending: true },
//   ],
//   totalPages: 10997,
//   totalElements: 219940,
//   size: 20,
//   number: 0,
// };

// const getAlertMessage = (req: Request, res: Response) => {
//   return res.json(alertMessage);
// };

// export default {
//   // 'GET /api/webapi/fpc-v1/security/suricata/mitre-attacks': getMitreAttacks,
//   // 'GET /api/webapi/fpc-v1/security/suricata/rule-classtypes': getClassTypes,
//   // 'POST /api/webapi/fpc-v1/security/suricata/rule-classtypes': createClassType,
//   // 'GET /api/webapi/fpc-v1/security/suricata/rules': getRules,
//   // 'POST /api/webapi/fpc-v1/security/suricata/rule-classtypes/:id': updateClasstype,
//   // 'POST /api/webapi/fpc-v1/security/suricata/rules': createRule,
//   // 'POST /api/webapi/fpc-v1/security/suricata/rules/batch': batchRules,
//   // 'GET /api/webapi/fpc-v1/security/suricata/rules/:sid': getRuleDetail,
//   // 'POST /api/webapi/fpc-v1/security/suricata/rules/:sid': updateRule,
//   // 'GET /api/webapi/fpc-v1/security/dashboard': getDashboardData,
//   'GET /api/webapi/fpc-v1/suricata/alert-messages/statistics': getSuricataStat,
//   'GET /api/webapi/fpc-v1/suricata/alert-messages': getAlertMessage,
// };
