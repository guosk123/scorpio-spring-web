import XLSX from 'xlsx';

export type XLSXBookType = 'csv' | 'xlsx';

/**
 * 数组导出excel或 csv
 * @data 数据
 * @fileName 文件名
 * @bookType 文件类型
 *
 * ```js
 * exportXlsx([['name','age'], ['zhangsan','18']], '用户表', 'csv');
 * ```
 */
const exportXlsx = (data: string[][], fileName: string, bookType: XLSXBookType = 'xlsx') => {
  // 将数组转化为标签页
  const ws = XLSX.utils.aoa_to_sheet(data);
  // 创建工作薄
  const wb = XLSX.utils.book_new();
  // 将标签页插入到工作薄里
  XLSX.utils.book_append_sheet(wb, ws, 'sheet');
  // 将工作薄导出为excel文件
  XLSX.writeFile(wb, `${fileName}.${bookType}`, {
    bookType,
  });
};

export { exportXlsx };
