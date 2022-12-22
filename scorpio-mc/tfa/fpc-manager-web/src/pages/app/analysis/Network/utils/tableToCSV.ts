/**
 * 导出为CSV文件
 */

export function tableToCSV(title: string[], data: string[][], namePrefix?: string) {
  if (title === undefined || title?.length === 0) {
    return undefined;
  }
  let str = '';
  str = title.reduce((pre: string, cur: string) => {
    return pre === '' ? '' + cur : pre + ',' + cur;
  }, '');
  str += '\n';
  if (data[0] !== undefined && data[0]?.length > 0) {
    for (let i = 0; i < data.length; i++) {
      for (let j = 0; j < data[i].length; j++) {
        str += data[i][j] + ',';
      }
      str += '\n';
    }
  }
  //encodeURIComponent解决中文乱码
  const uri = 'data:text/csv;charset=utf-8,\ufeff' + encodeURIComponent(str);
  //通过创建a标签实现
  const link = document.createElement('a');
  link.href = uri;
  //对下载的文件命名
  link.download = `${namePrefix || '访问关系图'}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  return;
}
