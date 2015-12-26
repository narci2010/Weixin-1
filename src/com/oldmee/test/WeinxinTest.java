package com.oldmee.test;

import net.sf.json.JSONObject;

import com.oldmee.po.AccessToken;
import com.oldmee.util.WeixinUtil;

public class WeinxinTest {
	public static void main(String[] args) throws Exception {
		AccessToken token = WeixinUtil.getAccessToken();
		System.out.println("票据："+token.getToken());
		System.out.println("有效时间："+token.getExpiresIn());
		
//		String path = "D:/1.jpg";
//		String mediaId = WeixinUtil.upload(path,token.getToken(),"image");
//		System.out.println(mediaId);
		
		String menu = JSONObject.fromObject(WeixinUtil.initMenu()).toString();
		int result = WeixinUtil.createMenu(token.getToken(), menu);
		if(result == 0) {
			System.out.println("创建菜单成功");
		} else {
			System.out.println("错误码："+result);
		}
	}
}
