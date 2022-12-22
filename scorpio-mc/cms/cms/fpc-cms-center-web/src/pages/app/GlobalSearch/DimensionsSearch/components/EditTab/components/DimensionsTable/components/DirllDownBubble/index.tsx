import type { ISearchType } from '@/pages/app/GlobalSearch/DimensionsSearch/typing';
import { Dropdown, Menu } from 'antd';
import type { CSSProperties } from 'react';

export interface DirllDownBoxType {
  key: string;
  content: string;
}

interface Props {
  content?: string;
  dataIndex?: string;
  style?: CSSProperties | undefined;
  children?: string;
  onClick?: any;
  dirllDownTypes?: ISearchType[];
}

export default function DirllDownBubble(props: Props) {
  const { content, dataIndex, style, children, onClick, dirllDownTypes } = props;

  return (
    <Dropdown
      trigger={['click']}
      overlay={
        <Menu
          style={style}
          onClick={({ key }) => {
            onClick({ key, content } as DirllDownBoxType);
          }}
        >
          <Menu.ItemGroup key={`${dataIndex}_bubble`} title="下钻">
            {dirllDownTypes?.map((item: any) => {
              return <Menu.Item key={item.name}>{item.title}</Menu.Item>;
            })}
          </Menu.ItemGroup>
        </Menu>
      }
    >
      <div style={{ cursor: 'pointer' }}>{children}</div>
    </Dropdown>
  );
}
