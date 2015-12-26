package com.oldmee.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;

import com.oldmee.po.TextMessage;
import com.oldmee.util.CheckUtil;
import com.oldmee.util.MessageUtil;

public class WeixinServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String signature = req.getParameter("signature");
		String timestamp = req.getParameter("timestamp");
		String nonce = req.getParameter("nonce");
		String echostr = req.getParameter("echostr");

		PrintWriter out = resp.getWriter();
		if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();
		try {
			Map<String, String> map = MessageUtil.xmlToMap(req);
			String toUserName = map.get("ToUserName");
			String fromUserName = map.get("FromUserName");
			String msgType = map.get("MsgType");
			String content = map.get("Content");

			String message = null;
			if (MessageUtil.MESSAGE_TEXT.equals(msgType)) {
				// if("1".equals(content)) {
				// message = MessageUtil.initImageMessage(toUserName,
				// fromUserName);
				// }else if("2".equals(content)) {
				message = MessageUtil.initNewsMessage(toUserName, fromUserName);
				// }else if("?".equals(content) || "？".equals(content)) {
				//					
				// }
				//				
			}
			// else if(MessageUtil.MESSAGE_EVENT.equals(msgType)) {
			String eventType = map.get("Event");
			String eventKey = map.get("EventKey");
			if (MessageUtil.MESSAGE_SUBSCRIBE.equals(eventType)) {
				message = MessageUtil.initText(toUserName, fromUserName,
						MessageUtil.menuText());
			} else if (MessageUtil.MESSAGE_CLICK.equals(eventType)) {
				if (eventKey.equals("12")) {
					message = MessageUtil.initText(toUserName, fromUserName,
							"即刻下载手机银行，享受更多更全的账户变动通知和理财日历提醒服务，还可享受转账汇款0费用！");
				} else if (eventKey.equals("13")) {
					message = MessageUtil
							.initText(
									toUserName,
									fromUserName,
									"欢迎使用一卡通余额查询服务，请点击以下链接登录手机银行网页版，您自动进入“账户查询”菜单即可进行查询:"+"\n"+"输入序号查看相关问题："+"\n"+"1.一卡通转账汇款"+"\n"+"2.一卡通生活缴费"+"\n"+"3.一卡通理财产品购买");
				} else {
					message = MessageUtil.initText(toUserName, fromUserName,
					"返回信息随意");
				}
			}
			// }
			System.out.println(message);
			out.print(message);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			out.close();
		}

	}
}
