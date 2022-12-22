import { ExclamationCircleOutlined, SettingTwoTone } from '@ant-design/icons';
import { Button, Checkbox, Dropdown, Menu, Modal, Space, Switch } from 'antd';
import { useEffect, useState } from 'react';
import type { ISearchBoxInfo } from '../../../typings';
import { categoryMap, IShowCategory } from '../../../typings';

export const showIpImageOptions = Object.values(IShowCategory).map((option) => ({
  value: option,
  label: categoryMap[option],
}));

const { confirm } = Modal;

interface IWindowsSettings {
  windowIds: IShowCategory[];
  changeWindowIds: any;
  // changeSettings: (layoutIds: IShowCategory[]) => void;
  searchInfo: ISearchBoxInfo;
  isEdit: boolean;
  configurePositions: any;
  refreshConfig: () => void;
}

export default function ConfigImage({
  // changeSettings,
  windowIds,
  changeWindowIds,
  searchInfo,
  isEdit,
  configurePositions,
  refreshConfig,
}: IWindowsSettings) {
  // 存最开始的配置
  const [windowsCache, setWindowsCache] = useState<IShowCategory[]>(windowIds);

  const changeWindowsCache = (checkedValues: any[]) => {
    changeWindowIds(checkedValues);
  };

  const menuSettings = (
    <Menu selectable={true}>
      <Menu.Item key="all">
        {/* <Checkbox checked={checkAll} onChange={}>
          全选
        </Checkbox> */}
      </Menu.Item>

      <Checkbox.Group
        style={{
          display: 'flex',
          flexDirection: 'column',
          width: '100%',
          padding: '5px 12px',
          minHeight: 100,
          overflowY: 'auto',
        }}
        options={showIpImageOptions}
        value={windowIds}
        onChange={changeWindowsCache}
      />
    </Menu>
  );

  const showConfirm = () => {
    confirm({
      title: '你想保存这些配置吗?',
      icon: <ExclamationCircleOutlined />,
      // content: '你想保存这些配置吗？',
      onOk() {
        console.log('ok');
        // setWindowsCache(windowIds);
        refreshConfig();
      },
      onCancel() {
        console.log('Cancel');
      },
    });
  };

  const handleEdit = (checked: boolean) => {
    // if (!checked) {
    //   changeWindowIds(windowsCache);
    // }
    configurePositions(checked);
  };

  return (
    <>
      {searchInfo.IpAddress ? (
        <Space>
          {isEdit && (
            <Dropdown trigger={['click']} overlay={menuSettings}>
              <Button type="primary" icon={<SettingTwoTone width={10} />} />
            </Dropdown>
          )}
          {isEdit && (
            <Button type="primary" onClick={showConfirm}>
              保存配置
            </Button>
          )}
          <Switch
            defaultChecked={isEdit}
            checkedChildren="编辑模式"
            unCheckedChildren="非编辑模式"
            onChange={handleEdit}
          />
        </Space>
      ) : null}
    </>
  );
}
