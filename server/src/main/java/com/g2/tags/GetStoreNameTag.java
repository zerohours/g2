package com.g2.tags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.g2.entity.Stores;
import com.g2.service.store.StoreService;

/**
 * 
 * @author pengqiuyuan
 * 
 */
public class GetStoreNameTag extends TagSupport {

	/**
	 * 
	 */
	@Autowired
	private StoreService storeService;
	
	private static final long serialVersionUID = 1L;

	private String id;

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int doStartTag() throws JspTagException {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspTagException {
		try {
			WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());//获取SPRING的上下文
			StoreService storeService = (StoreService) ctx.getBean("storeService");
			Stores stores =  storeService.findById(Long.parseLong(id));
			if(stores!=null){
				pageContext.getOut().write(stores.getName());
			}else if(id.equals("0")){
				pageContext.getOut().write("<b>超级管理员</b>");
			}else{
				pageContext.getOut().write("<b>项目已不存在</b>");

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

}
