package com.machloop.fpc.manager.asset.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月2日, fpc-manager
 */
public interface AssetInformationService {

  Page<Map<String, Object>> queryAssetInformation(AssetInformationQueryVO queryVO,
      String sortProperty, String sortDirection, Pageable page);

  List<Map<String, Object>> aggregateAssetInformationList(AssetInformationQueryVO queryVO,
      List<Map<String, Object>> assetInformationList);

  void exportAssetInformations(ServletOutputStream outputStream, AssetInformationQueryVO queryVO,
      String fileType, String sortProperty, String sortDirection) throws IOException;

  List<Map<String, Object>> queryAssetInformation(AssetInformationQueryVO queryVO,
      String sortProperty, String sortDirection, int count, Pageable page);

  long countAssetInformation(AssetInformationQueryVO queryVO);
}
