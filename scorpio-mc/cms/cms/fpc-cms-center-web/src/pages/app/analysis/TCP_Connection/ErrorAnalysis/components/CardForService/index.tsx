import { EServiceType } from '@/pages/app/analysis/typings';
import { Card, Divider, Radio, Switch } from 'antd';
import type { ReactNode } from 'react';
import { Fragment } from 'react';

interface Props {
  children: ReactNode;
  changeService: any;
  changeRate?: any;
}

export default function CardForService(props: Props) {
  const { children, changeService, changeRate } = props;
  return (
    <Card
      size="small"
      extra={
        <Fragment>
          <Switch
            checkedChildren="数量"
            unCheckedChildren="比率"
            onChange={(checked) => {
              if (checked) {
                changeRate('count');
              } else {
                changeRate('rate');
              }
            }}
            defaultChecked
          />
          <Divider type="vertical" />
          <Radio.Group
            size="small"
            defaultValue={EServiceType.TOTALSERVICE}
            onChange={(e) => {
              changeService(e.target.value);
            }}
          >
            <Radio.Button value={EServiceType.TOTALSERVICE}>总体</Radio.Button>;
            <Radio.Button value={EServiceType.INTRANETSERVICE}>内网服务</Radio.Button>
            <Radio.Button value={EServiceType.INTERNETSERVICE}>外网服务</Radio.Button>
          </Radio.Group>
        </Fragment>
      }
    >
      {children}
    </Card>
  );
}
