import type { CellTitleOption } from '@/components/TopologyGraph/typing';
import type { Node } from '@antv/x6';
import { Form, Input, Select, Tabs } from 'antd';
import { useEffect, useState } from 'react';

const { TabPane } = Tabs;

interface Props {
  node: Node;
  titleOption?: CellTitleOption;
}

const NodeConfig: React.FC<Props> = (props) => {
  const { node, titleOption } = props;
  const [nodeTitle, setNodeTitle] = useState(node.data.title);

  useEffect(() => {
    setNodeTitle(node.data.title);
  }, [node]);

  useEffect(() => {
    node.setData({ title: nodeTitle });
  }, [node, nodeTitle]);

  return (
    <>
      <Tabs>
        <TabPane tab={'节点属性设置'}>
          <Form layout={'vertical'}>
            <Form.Item label="节点名称">
              {!titleOption || titleOption.inputType === 'input' || node.data.iconName !== 'app' ? (
                <Input
                  value={nodeTitle}
                  onChange={(e) => {
                    setNodeTitle(e.target.value);
                  }}
                />
              ) : (
                <Select
                  allowClear={true}
                  value={
                    titleOption?.selectItem?.find((option) => option.label === node.data?.title)
                      ?.value
                  }
                  showSearch={true}
                  filterOption={(input, option) =>
                    (option?.label as string).toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                  onChange={(value, option) => {
                    if (value) {
                      const { label } = option as { label: string };
                      node.setData({ value, title: label });
                    } else {
                      node.setData({ title: '节点' });
                    }
                  }}
                  options={titleOption?.selectItem?.map((option) => {
                    return {
                      label: option.label,
                      value: option.value,
                    };
                  })}
                />
              )}
            </Form.Item>
          </Form>
        </TabPane>
      </Tabs>
    </>
  );
};

export default NodeConfig;
