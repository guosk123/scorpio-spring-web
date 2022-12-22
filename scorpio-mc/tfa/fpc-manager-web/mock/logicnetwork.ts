// import { Request, Response } from 'express';

// const logicSubnetsList = [
//   {
//     id: '001',
//     name: '测试子网001',
//     networkId: 'JE4lPngBsNThA3XBZjPq',
//     networkName: '',
//     type: 'ip',
//     bandwidth: 1000,
//     configuration: '',
//   },
//   {
//     id: '002',
//     name: '测试子网002',
//     networkId: 'KU4lPngBsNThA3XB9zNb',
//     networkName: '',
//     type: 'mac',
//     bandwidth: 1000,
//     configuration: '01:23:45:67:89:ab,01:23:45:67:89:ac',
//   },
//   {
//     id: '003',
//     name: '测试子网003',
//     networkId: 'KU4lPngBsNThA3XB9zNb',
//     networkName: '',
//     type: 'gre',
//     bandwidth: 1000,
//     configuration: '{"greKey":"12,14"}',
//   },
// ];

// const getLogicSubnets = (req: Request, res: Response) => {
//   res.json(logicSubnetsList);
// };

// const getLogicSubnetsDetail = (req: Request, res: Response) => {
//   res.json(logicSubnetsList.find((item) => item.id === req.params.id) || {});
// };

// export default {
//   'GET /api/webapi/fpc-v1/appliance/logical-subnets': getLogicSubnets,
//   'GET /api/webapi/fpc-v1/appliance/logical-subnets/:id': getLogicSubnetsDetail,
// };
