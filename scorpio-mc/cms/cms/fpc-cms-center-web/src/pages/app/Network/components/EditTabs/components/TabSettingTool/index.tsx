import storage from '@/utils/frame/storage';
import { SettingTwoTone } from '@ant-design/icons';
import { Button, Checkbox, Divider, Dropdown, Menu } from 'antd';
import type { CheckboxValueType } from 'antd/lib/checkbox/Group';
import type { CSSProperties } from 'react';
import { useMemo, useState } from 'react';

const MenuItem = Menu.Item;
const CheckboxGroup = Checkbox.Group;

interface ISelectItem {
  title: string;
  key: string;
  selected: boolean;
  disabled?: boolean;
  isNewTab?: boolean;
}

interface Props {
  tabs: any;
  tabsKey: string;
  tabsSettingState: any;
  style?: CSSProperties;
}

export default function TabSettingTool(props: Props) {
  const { tabs, tabsKey, tabsSettingState, style } = props;

  // 同步到上层时使用
  const [, setTabsSetting] = tabsSettingState;
  // tabs tool使用
  const [tabsToolSetting, setTabsToolSetting] = useState(JSON.parse(storage.get(tabsKey) || '[]'));
  const [visible, setVisible] = useState<boolean>(false);
  const [maxItemWidth, setMaxItemWidth] = useState(150);

  const selectObj = useMemo(() => {
    const res: ISelectItem[] = [];
    const safeTabKeysFlag =
      tabsToolSetting.length && !tabsToolSetting.some((item: any) => !item.length);
    Object.keys(tabs).forEach((key) => {
      const tab = tabs[key];

      // 适应长标题
      const textWidth = 30 + tab.title.length * 16;
      if (textWidth > maxItemWidth) {
        setMaxItemWidth(textWidth);
      }

      res.push({
        title: tab.title,
        key,
        selected: safeTabKeysFlag ? tabsToolSetting.includes(key) : tab.defShow || false,
        disabled: tab.disabled,
      });
    });
    return res;
  }, [maxItemWidth, tabsToolSetting, tabs]);

  // 是否为半选中状态
  const indeterminate = useMemo(() => {
    return !selectObj
      .filter((ele) => !ele.disabled)
      .every((item) => {
        return item.selected === selectObj.filter((ele) => !ele.disabled)[0].selected;
      });
  }, [selectObj]);

  const ColumnSettingMenu = useMemo(() => {
    const onChangeGroup = (e: CheckboxValueType[]) => {
      setTabsSetting(e);
      setTabsToolSetting(e);
      storage.put(tabsKey, JSON.stringify(e));
    };

    const isSelectedAll = selectObj.every((item) => item.selected);

    const onCheckAllChange = () => {
      if (indeterminate || !isSelectedAll) {
        const tmpItems = selectObj.map((item) => item.key);
        setTabsSetting(tmpItems);
        setTabsToolSetting(tmpItems);
        storage.put(tabsKey, JSON.stringify(tmpItems));
      } else if (isSelectedAll) {
        const tmpItems = selectObj
          .filter((item) => item.disabled || item.isNewTab)
          .map((ele) => ele.key);
        setTabsSetting(tmpItems);
        setTabsToolSetting(tmpItems);
        storage.put(tabsKey, JSON.stringify(tmpItems));
      }
    };

    return (
      <Menu>
        <MenuItem>
          <Checkbox
            style={{ width: '100%' }}
            value={'all'}
            key={'all'}
            onChange={onCheckAllChange}
            indeterminate={indeterminate}
            checked={selectObj.every((item) => item.selected === true)}
          >
            {`全部${isSelectedAll ? '取消' : '选中'}`}
          </Checkbox>
        </MenuItem>
        <Menu.Item key={'next-divider'}>
          <Divider style={{ margin: '1px' }} />
        </Menu.Item>
        <CheckboxGroup
          onChange={onChangeGroup}
          style={{ width: '100%' }}
          value={selectObj.filter((item) => item.selected).map((item) => item.key)}
          defaultValue={selectObj.filter((item) => item.selected).map((item) => item.key)}
        >
          {selectObj.length &&
            selectObj.map((item) => {
              return (
                <MenuItem key={item.key}>
                  <Checkbox
                    style={{ width: '100%' }}
                    value={item.key}
                    key={item.key}
                    disabled={item.disabled === undefined ? false : item.disabled}
                  >
                    {item.title}
                  </Checkbox>
                </MenuItem>
              );
            })}
        </CheckboxGroup>
      </Menu>
    );
  }, [selectObj, indeterminate, setTabsSetting, tabsKey]);

  return (
    <Dropdown
      overlay={ColumnSettingMenu}
      trigger={['click']}
      visible={visible}
      overlayStyle={{ width: maxItemWidth, maxHeight: 400, overflowX: 'hidden', overflowY: 'auto' }}
      onVisibleChange={(nextVisible: boolean) => setVisible(nextVisible)}
    >
      <Button style={style} type="primary" icon={<SettingTwoTone />} />
    </Dropdown>
  );
}
