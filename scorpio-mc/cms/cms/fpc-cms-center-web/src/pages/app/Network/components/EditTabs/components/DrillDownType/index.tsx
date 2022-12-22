import storage from '@/utils/frame/storage';
import { Switch } from 'antd';
import { useEffect } from 'react';

export default function DrillDownType() {
  useEffect(() => {
    if (storage.get('jumpToNew') === null) {
      storage.put('jumpToNew', false);
    }
    return () => {};
  }, []);

  // 目前不提供给用户选择
  return (
    <Switch
      style={{ display: 'none' }}
      onClick={(value) => {
        storage.put('jumpToNew', !value);
      }}
      checkedChildren="本标签页内下钻"
      unCheckedChildren="新建标签页下钻"
      defaultChecked={
        storage.get('jumpToNew') === null ? true : storage.get('jumpToNew') !== 'true'
      }
      // style={{ display: 'none' }}
    />
  );
}
