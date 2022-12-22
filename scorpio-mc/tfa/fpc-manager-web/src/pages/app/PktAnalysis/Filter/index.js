import { CheckOutlined } from '@ant-design/icons';
import { AutoComplete, Button, Input, message } from 'antd';
import { connect } from 'dva';
import { Bind, Debounce } from 'lodash-decorators';
import React, { PureComponent } from 'react';
import styles from './index.less';

const InputGroup = Input.Group;

@connect(({ loading: { effects }, pktAnalysisModel: { filter } }) => ({
  filter,
  completeLoading: effects['pktAnalysisModel/filterComplete'],
  checkLoading: effects['pktAnalysisModel/filterCheck'],
}))
class Filter extends PureComponent {
  state = {
    fieldList: [], // 过滤条件自动补充返回值
    filterCheckPass: true, // 过滤条件判断是否正确
  };

  componentWillUnmount() {
    // 卸载时清空条件
    const { dispatch } = this.props;
    dispatch({
      type: 'pktAnalysisModel/changeFilter',
      payload: {
        filter: '',
      },
    });
  }

  handleFilter = () => {
    const { filterCheckPass } = this.state;
    const { onFilter, filter } = this.props;

    if (!filterCheckPass) {
      message.warning('过滤条件没有校验通过');
      return;
    }

    if (onFilter) {
      onFilter(filter);
    }
  };

  handleKeyup = (e) => {
    if (e.keyCode === 13) {
      this.handleSearch(e.target.value);
    }
  };

  handleClear = () => {
    this.setState({
      filterCheckPass: true,
      fieldList: [],
    });

    const { onFilterChange } = this.props;
    if (onFilterChange) {
      onFilterChange('');
    }

    const { onFilter } = this.props;
    if (onFilter) {
      onFilter('');
    }
  };

  handleChange = (value) => {
    const { onFilterChange } = this.props;
    if (onFilterChange) {
      onFilterChange(value);
    }

    if (!value) {
      this.setState({ filterCheckPass: true });
    }
    this.handleCheckFilter(value);
  };

  @Bind()
  @Debounce(300)
  handleCheckFilter(filter) {
    const { dispatch, sourceType, taskId } = this.props;
    dispatch({
      type: 'pktAnalysisModel/filterCheck',
      payload: {
        sourceType,
        taskId,
        req: 'check',
        filter,
      },
    }).then((flag) => {
      this.setState({ filterCheckPass: flag });
    });
  }

  @Bind()
  @Debounce(300)
  handleSearch(value) {
    if (!value) {
      this.setState({ fieldList: [] });
    }
    const { dispatch, sourceType, taskId } = this.props;
    dispatch({
      type: 'pktAnalysisModel/filterComplete',
      payload: {
        sourceType,
        taskId,
        req: 'complete',
        field: value,
      },
    }).then((field) => {
      this.setState({ fieldList: field });
    });
  }

  render() {
    const { filterCheckPass, fieldList } = this.state;
    const { filter, style } = this.props;
    // 没有过滤条件时，样式置空
    let clazzName = '';
    if (filter) {
      clazzName = filterCheckPass ? styles.right : styles.error;
    }

    return (
      <div style={style} className={styles.filterWrap}>
        <InputGroup style={{ display: 'flex', flexWrap: 'nowrap' }} compact>
          <AutoComplete
            style={{ flex: 1, minWidth: 200 }}
            className={clazzName}
            value={filter}
            defaultActiveFirstOption
            options={fieldList.map((item) => ({ value: item.f }))}
            onSearch={this.handleSearch}
            onChange={this.handleChange}
            placeholder="输入过滤条件"
          >
            <Input onKeyUp={this.handleKeyup} />
          </AutoComplete>
          <div style={{ whiteSpace: 'nowrap' }}>
            <Button
              disabled={!filterCheckPass}
              icon={<CheckOutlined />}
              type="primary"
              onClick={this.handleFilter}
            >
              应用
            </Button>
            <Button onClick={this.handleClear}>清除</Button>
          </div>
        </InputGroup>
      </div>
    );
  }
}

export default Filter;
