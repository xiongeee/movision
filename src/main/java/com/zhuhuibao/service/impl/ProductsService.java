package com.zhuhuibao.service.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhuhuibao.utils.search.CollectionUtil;
import com.zhuhuibao.utils.search.FormatUtil;
import com.zhuhuibao.utils.search.JSONUtil;
import com.zhuhuibao.utils.search.Pagination;
import com.zhuhuibao.utils.search.StringUtil;
import com.zhuhuibao.pojo.Product;

import com.zhuhuibao.pojo.ProductSearchSpec;
import com.zhuhuibao.service.IProductsService;
import com.zhuhuibao.service.IWordService;
import com.zhuhuibao.service.Searcher;
import com.zhuhuibao.service.exception.ServiceException;

@Service
public class ProductsService implements IProductsService {

	@Autowired
	private IWordService wordService;

	@Override
	public Map<String, Object> search(ProductSearchSpec spec)
			throws ServiceException {
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("spec", spec);

		int fcateid = spec.getFcateid();
		if(fcateid>0){
			Searcher.wrapEqualQuery(query, "fcateid", fcateid);
		}
		int scateid = spec.getScateid();
		if(scateid>0){
			Searcher.wrapEqualQuery(query, "scateid", scateid);
		}
		int brandid = spec.getBrandid();
		if(brandid>0){
			Searcher.wrapEqualQuery(query, "brandid", brandid);
		}
		int member_companyIdentify = spec.getMember_companyIdentify();
		if(member_companyIdentify>0){
			Searcher.wrapEqualQuery(query, "member_companyIdentify", member_companyIdentify);
		}

		spec.setQ(StringUtil.emptyToNull(spec.getQ()));
		if (spec.getQ() != null) {
			String q = spec.getQ();
			result.put("q", q);
			List<String> words = wordService.segWords(q);
			if (!words.isEmpty()) {
				String formatQ = StringUtil.join(words, " ");
				query.put("_s", CollectionUtil.arrayAsMap("type", "phrase",
						"value", formatQ));
			}
		}

		List<Map<String, Object>> sortFields = new ArrayList<Map<String, Object>>(1);
		Map<String, Object> sortField = new HashMap<String, Object>(3);
		if (StringUtil.isNotEmpty(spec.getSort())) {
			String sort = spec.getSort();
			result.put("sort", spec.getSort());
			String sortorder = "true";
			if(StringUtil.isNotEmpty(spec.getSortorder())){
				sortorder = spec.getSortorder();
				result.put("sortorder", spec.getSortorder());
			}
			if (sort.equals("price1")) {
				sortField.put("field", sort);
				sortField.put("type", "FLOAT");
				sortField.put("reverse",
						FormatUtil.parseBoolean(sortorder));
			}else if(sort.equals("publishTime1")){
				sortField.put("field", sort);
				sortField.put("type", "LONG");
				sortField.put("reverse",
						FormatUtil.parseBoolean(sortorder));
			}else {
				sortField.put("field", "id");
				sortField.put("type", "INT");
				sortField.put("reverse",FormatUtil.parseBoolean(true));
			}
		}else {
			sortField.put("field", "id");
			sortField.put("type", "INT");
			sortField.put("reverse",FormatUtil.parseBoolean(true));
		}
		
		sortFields.add(sortField);

		Map<?, ?> psAsMap = (Map<?, ?>) Searcher.request(
				"search",
				CollectionUtil.arrayAsMap("table", "product", "query",
						JSONUtil.toJSONString(query), "sort",
						JSONUtil.toJSONString(sortFields), "offset",
						spec.getOffset(), "limit", spec.getLimit()));
		List<?> list = (List<?>) psAsMap.get("items");
		Pagination<Product> ps = null;
		if (list.isEmpty()) {
			ps = Pagination.getEmptyInstance();
		} else {
			List<Product> products = new ArrayList<Product>(list.size());
			for (Object item : list) {
				Map<?, ?> itemAsMap = (Map<?, ?>) item;
				Product product = new Product();
				{
					product.setId(FormatUtil.parseLong(itemAsMap.get("id")));
					product.setName(FormatUtil.parseString(itemAsMap
							.get("name")));
					product.setImgUrl(FormatUtil.parseString(itemAsMap
							.get("imgUrl")));
					product.setPublishTime(FormatUtil.parseString(itemAsMap
							.get("publishTime")));
					product.setPrice(FormatUtil.parseDouble(itemAsMap
							.get("price")));
				}
				products.add(product);
			}
			ps = new Pagination<Product>(products,
					FormatUtil.parseInteger(psAsMap.get("total")),
					FormatUtil.parseInteger(psAsMap.get("offset")),
					FormatUtil.parseInteger(psAsMap.get("limit")));
		}
		result.put("ps", ps);
		return result;
	}

}
