package com.zhuhuibao.mybatis.product.entity;

import java.io.Serializable;

/**
 * 参数价格混合结构类
 * @author penglong
 *
 */
public class ParamPrice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fname;
	
	private String fvalue;
	
	private String sname;
	    
    private String svalue;
    
    private String price;

    private Double repository;
    
    private String imgUrl;

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getFvalue() {
		return fvalue;
	}

	public void setFvalue(String fvalue) {
		this.fvalue = fvalue;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public String getSvalue() {
		return svalue;
	}

	public void setSvalue(String svalue) {
		this.svalue = svalue;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Double getRepository() {
		return repository;
	}

	public void setRepository(Double repository) {
		this.repository = repository;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
}
