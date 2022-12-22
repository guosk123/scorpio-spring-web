import type { CellTitleOption } from '@/components/TopologyGraph/typing';
import type { Edge } from '@antv/x6';
import { Form, Input, Select, Tabs } from 'antd';
import type { DefaultOptionType } from 'antd/lib/select';

const { TabPane } = Tabs;
const { Option } = Select;

interface Props {
  edge: Edge;
  titleOption?: CellTitleOption;
}

const EdgeConfig: React.FC<Props> = (props) => {
  const { edge, titleOption } = props;

  const setEdgeLabel = (name: string) => {
    // console.log(node);
    edge.setLabels([name]);
  };

  return (
    <>
      <Tabs>
        <TabPane tab={'边属性设置'}>
          <Form layout={'vertical'}>
            <Form.Item label="选择网络">
              {!titleOption || titleOption.inputType === 'input' ? (
                <Input
                  onChange={(e) => {
                    setEdgeLabel(e.target.value);
                  }}
                />
              ) : (
                <Select
                  allowClear={true}
                  onChange={(value, option) => {
                    if (value) {
                      const title = (option as DefaultOptionType).title as string;
                      edge.setData({ value });
                      setEdgeLabel(title);
                    } else {
                      edge.setData({});
                      setEdgeLabel('');
                    }
                  }}
                  showSearch={true}
                  filterOption={(input, option) =>
                    (option?.title as string).toLowerCase().indexOf(input.toLowerCase()) >= 0
                  }
                >
                  {titleOption.selectItem?.map((option) => {
                    return (
                      <Option key={option.value} value={option.value} title={option.label}>
                        {option.label}
                      </Option>
                    );
                  })}
                </Select>
              )}
            </Form.Item>
          </Form>
        </TabPane>
      </Tabs>
    </>
  );
};

export default EdgeConfig;
