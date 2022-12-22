import { Button, Form, Input, message, Select, TreeSelect } from 'antd';
import { useContext, useEffect, useState } from 'react';
import { connect } from 'dva';
import { SegmentAnalysisSearchMapping } from '../../typings';
import ContentBox from './components/ContentBox';
import { queryNetWorkTree } from '@/pages/app/Network/service';
import type { INetworkTreeItem } from '@/pages/app/Network/typing';
import { SearchDataContext } from '../..';

const FormItem = Form.Item;
const { Option } = Select;
const InputGroup = Input.Group;

interface Props {
  onSubmit?: any;
}

export interface ISearchBoxInfo {
  networkIds: string[];
  segmentAnalysisSearchType: string;
  content: string;
}

function SearchBox(props: Props) {
  const { onSubmit } = props;
  const [form] = Form.useForm();

  const [searchType, setSearchType] = useState<string>();

  // const { queryLoading, networkDataOpt } = useContext<any>(SearchDataContext);

  const [networkDataSet, setNetworkDataSet] = useState<INetworkTreeItem[]>([]);
  const [networkTreeLoading, setNetworkTreeLoading] = useState(true);

  const submitForm = (e: ISearchBoxInfo) => {
    const submitInfo = {
      ...e,
      // networkIds:
      //   e.networkIds[0] === 'ALL'
      //     ? networkDataSet.map((item) => `${item.key}^${item.type}`)
      //     : e.networkIds,
    };
    onSubmit(submitInfo);
  };

  useEffect(() => {
    queryNetWorkTree()
      .then((result) => {
        setNetworkDataSet(
          result.map((item) => ({
            ...item,
            value: `${item.value}^${item.type}`,
          })),
        );
        setNetworkTreeLoading(false);
      })
      .catch((err) => {
        message.error(err);
        setNetworkTreeLoading(false);
      });
  }, []);

  return (
    <div>
      <Form
        layout="vertical"
        initialValues={{}}
        name="widget"
        form={form}
        style={{ width: 900 }}
        // onValuesChange={(changedValues, allValues) => {}}
        onFinish={submitForm}
      >
        <InputGroup compact>
          <FormItem name="networkIds" rules={[{ required: true, message: '请选择网络' }]}>
            <TreeSelect
              allowClear
              treeDefaultExpandAll
              treeData={networkDataSet}
              treeCheckable={true}
              loading={networkTreeLoading}
              placeholder={'请选择网络'}
              showCheckedStrategy={'SHOW_PARENT'}
              style={{ width: 360 }}
            />
          </FormItem>
          <FormItem
            name="segmentAnalysisSearchType"
            rules={[{ required: true, message: '请选择搜索维度' }]}
          >
            <Select
              placeholder="请选择搜索维度"
              onChange={(e: string) => {
                setSearchType(e);
                form.setFieldsValue({ content: undefined });
              }}
              style={{ width: 160 }}
            >
              {Object.values(SegmentAnalysisSearchMapping).map((item) => {
                return (
                  <Option key={item.name} value={item.name}>
                    {item.title}
                  </Option>
                );
              })}
            </Select>
          </FormItem>
          <ContentBox
            beforeSelections={form.getFieldsValue(['networkIds', 'segmentAnalysisSearchType'])}
            searchType={searchType}
            style={{ width: 300, maxWidth: 300 }}
          />
          <FormItem noStyle>
            <Button type="primary" style={{ width: 80 }} htmlType="submit">
              搜索
            </Button>
          </FormItem>
        </InputGroup>
      </Form>
    </div>
  );
}
export default connect()(SearchBox);
