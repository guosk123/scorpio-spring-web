import type { Request, Response } from 'express';

const natConfig = {
  natAction: '0',
  id: 1,
};

const updateConfig = (req: Request, res: Response) => {
  const data = req.body;
  console.log(data);
  natConfig.natAction = data.natAction;
  res.end();
};

export default {
  'GET /api/webapi/fpc-v1/appliance/nat-config': natConfig,
  'PUT /api/webapi/fpc-v1/appliance/nat-config': updateConfig,
};
