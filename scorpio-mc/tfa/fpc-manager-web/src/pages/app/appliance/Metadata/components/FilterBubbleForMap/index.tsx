import type { IFilterCondition } from '@/components/FieldFilter/typings';
import { EFieldType, EFilterOperatorTypes } from '@/components/FieldFilter/typings';
import { snakeCase } from '@/utils/utils';
import { Button, Dropdown, Menu } from 'antd';
import type { ColumnTitle } from 'antd/lib/table/interface';
import type { SetStateAction } from 'react';
import type { IMetadataLog } from '../../typings';
import { EMetadataProtocol } from '../../typings';

interface Props {
  data: Record<string, string>;
  onFilterChange: (value: SetStateAction<IFilterCondition>) => void;
  dataIndex: any;
  protocol: string;
  title: ColumnTitle<IMetadataLog>;
}

export default function FilterBubbleForMap(props: Props) {
  const { data, onFilterChange: setFilterCondition, dataIndex, protocol, title } = props;
  const menuItemRender = () => {
    return Object.keys(data).map((key) => {
      return (
        <Menu.Item
          key={key}
          onClick={() => {
            setFilterCondition((prev) => {
              return [
                ...prev,
                {
                  field: `${snakeCase(dataIndex?.toString() || '')}.${key}`,
                  type: EFieldType.Map,
                  operator: EFilterOperatorTypes.EQ,
                  operand: data[key],
                },
              ];
            });
          }}
        >
          {key}={data[key]}
        </Menu.Item>
      );
    });
  };
  return (
    <Dropdown
      trigger={['click']}
      overlay={
        <Menu style={{ maxHeight: '200px', overflowY: 'auto', maxWidth: '200px' }}>
          {menuItemRender()}
        </Menu>
      }
    >
      <Button size="small" type="text">
        {protocol === EMetadataProtocol.LDAP ? title : `${dataIndex}内容`}
      </Button>
    </Dropdown>
  );
}
