package com.g2.web.controller.mgr.game;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.web.Servlets;

import com.g2.entity.Server;
import com.g2.entity.User;
import com.g2.entity.game.PayPoint;
import com.g2.service.account.AccountService;
import com.g2.service.game.DataBasicService;
import com.g2.service.game.DataPayPointService;
import com.g2.service.server.ServerService;
import com.g2.web.controller.mgr.BaseController;
import com.google.common.collect.Maps;

/**
 * 付费点管理的controller
 *
 */
@Controller("dataPayPointController")
@RequestMapping(value="/manage/game/dataPayPoint")
public class DataPayPointController extends BaseController{

	private static final Logger logger = LoggerFactory.getLogger(DataPayPointController.class);
	
	private static final String PAGE_SIZE = "15";
	
	SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd" ); 
	Calendar calendar = new GregorianCalendar(); 

	private static Map<String, String> sortTypes = Maps.newLinkedHashMap();

	static {
		sortTypes.put("buyCount", "购买次数");
		sortTypes.put("amount", "总金额");
	}
	
	public static Map<String, String> getSortTypes() {
		return sortTypes;
	}

	public static void setSortTypes(Map<String, String> sortTypes) {
		DataPayPointController.sortTypes = sortTypes;
	}

	@Override
	@InitBinder
	protected void initBinder(ServletRequestDataBinder binder){
		binder.registerCustomEditor(Date.class,"createDate",new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
		binder.registerCustomEditor(Date.class,"upDate",new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
	}
	
	@Autowired
	private AccountService accountService;

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private DataPayPointService dataPayPointService;
	
	/**
	 * @throws Exception 
	 */
	@RequestMapping(value = "index", method = RequestMethod.GET)
	public String index(@RequestParam(value = "page", defaultValue = "1") int pageNumber,
			@RequestParam(value = "page.size", defaultValue = PAGE_SIZE) int pageSize,
			@RequestParam(value = "sortType", defaultValue = "auto")String sortType, Model model,
			ServletRequest request) throws Exception{
		logger.debug("付费点");
		Map<String, Object> searchParams = Servlets.getParametersStartingWith(request, "search_");
		Long userId = dataPayPointService.getCurrentUserId();
		User user = accountService.getUser(userId);

		List<PayPoint> payPoints = new ArrayList<>();
		PayPoint p1 = new PayPoint();
		p1.setAmount("6000");
		p1.setBuyCount("20");
		p1.setBuyItem("钻石＊60");
		p1.setPrice("6");
		PayPoint p2 = new PayPoint();
		p2.setAmount("2000");
		p2.setBuyCount("30");
		p2.setBuyItem("钻石＊600");
		p2.setPrice("60");
		payPoints.add(p1);
		payPoints.add(p2);
		
		PageRequest pageRequest = buildPageRequest(pageNumber, pageSize, sortType);
		PageImpl<PayPoint> ps = new PageImpl<PayPoint>(payPoints, pageRequest, Long.valueOf("10"));
		
		model.addAttribute("sortType", sortType);
		model.addAttribute("sortTypes", sortTypes);
		
		model.addAttribute("user", user);
		model.addAttribute("dateFrom", dataPayPointService.thirtyDayAgoFrom());
		model.addAttribute("dateTo", dataPayPointService.nowDate());
		model.addAttribute("servers", serverService.findAll());
		model.addAttribute("payPoints", ps);
		
		// 将搜索条件编码成字符串，用于排序，分页的URL
		model.addAttribute("searchParams", Servlets.encodeParameterStringWithPrefix(searchParams, "search_"));
		return "/game/datapaypoint/index";
	}
	
	/**
	 * 服务器获取时间
	 */
	@RequestMapping(value="/getDate")
	@ResponseBody
	public Map<String, String> getDate(){
		Map<String,String> dateMap = new HashMap<String, String>();
		SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd" ); 
		Calendar calendar = new GregorianCalendar(); 
		String nowDate = sdf.format(new Date());
		
	    calendar.setTime(new Date()); 
	    calendar.add(Calendar.DATE,-1);
	    String yesterday = sdf.format(calendar.getTime());
	    
	    calendar.setTime(new Date()); 
	    calendar.add(Calendar.DATE,-7);
	    String sevenDayAgo = sdf.format(calendar.getTime()); 
	    
	    calendar.setTime(new Date()); 
	    calendar.add(Calendar.DATE,-30);
	    String thirtyDayAgo = sdf.format(calendar.getTime()); 
		
	    dateMap.put("nowDate",nowDate);
	    dateMap.put("yesterday",yesterday);
	    dateMap.put("sevenDayAgo",sevenDayAgo);
	    dateMap.put("thirtyDayAgo",thirtyDayAgo);
		return dateMap;
	}
	
	private PageRequest buildPageRequest(int pageNumber, int pagzSize, String sortType) {
		Sort sort = null;
		if ("buyCount".equals(sortType)) {
			sort = new Sort(Direction.DESC, "buyCount");
		} else if ("amount".equals(sortType)) {
			sort = new Sort(Direction.DESC, "amount");
		}
		return new PageRequest(pageNumber - 1, pagzSize, sort);
	}
	
}