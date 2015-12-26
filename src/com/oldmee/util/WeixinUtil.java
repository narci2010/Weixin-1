package com.oldmee.util;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.oldmee.menu.Button;
import com.oldmee.menu.ClickButton;
import com.oldmee.menu.Menu;
import com.oldmee.menu.ViewButton;
import com.oldmee.po.AccessToken;


public class WeixinUtil {
	private static final String APPID = "wx30b3651ab7693b8f";
	private static final String APPSECRET = "d4624c36b6795d1d99dcf0547af5443d";
	private static final String ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
	private static final String UPLOAD_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=ACCESS_TOKEN&type=TYPE";
	private static final String CREATE_MENU_URL = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=ACCESS_TOKEN";
	
	/**
	 * doGet请求
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 * @throws IOException
	 */
	public static JSONObject doGetStr(String url) throws Exception, IOException {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		JSONObject jsonObject = null;
		HttpResponse response = httpClient.execute(httpGet);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String result = EntityUtils.toString(entity, "UTF-8");
			jsonObject = JSONObject.fromObject(result);
		}
		return jsonObject;
	}

	/**
	 * doPost请求
	 * 
	 * @param url
	 * @param outStr
	 * @return
	 * @throws Exception
	 */
	public static JSONObject doPostStr(String url, String outStr)
			throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		JSONObject jsonObject = null;
		httpPost.setEntity(new StringEntity(outStr, "UTF-8"));
		HttpResponse response = httpClient.execute(httpPost);
		String result = EntityUtils.toString(response.getEntity(), "UTF-8");
		jsonObject = JSONObject.fromObject(result);
		return jsonObject;
	}
	
	/**
	 * 文件上传
	 * @param filePath
	 * @param accessToken
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws KeyManagementException
	 */
	public static String upload(String filePath, String accessToken,String type) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			throw new IOException("文件不存在");
		}

		String url = UPLOAD_URL.replace("ACCESS_TOKEN", accessToken).replace("TYPE",type);
		
		URL urlObj = new URL(url);
		//连接
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

		con.setRequestMethod("POST"); 
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false); 

		//设置请求头信息
		con.setRequestProperty("Connection", "Keep-Alive");
		con.setRequestProperty("Charset", "UTF-8");

		//设置边界
		String BOUNDARY = "----------" + System.currentTimeMillis();
		con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

		StringBuilder sb = new StringBuilder();
		sb.append("--");
		sb.append(BOUNDARY);
		sb.append("\r\n");
		sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"\r\n");
		sb.append("Content-Type:application/octet-stream\r\n\r\n");

		byte[] head = sb.toString().getBytes("utf-8");

		//获得输出流
		OutputStream out = new DataOutputStream(con.getOutputStream());
		//输出表头
		out.write(head);

		//文件正文部分
		//把文件已流文件的方式 推入到url中
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		int bytes = 0;
		byte[] bufferOut = new byte[1024];
		while ((bytes = in.read(bufferOut)) != -1) {
			out.write(bufferOut, 0, bytes);
		}
		in.close();

		//结尾部分
		byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");//定义最后数据分隔线

		out.write(foot);

		out.flush();
		out.close();

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		String result = null;
		try {
			//定义BufferedReader输入流来读取URL的响应
			reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			if (result == null) {
				result = buffer.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		JSONObject jsonObj = JSONObject.fromObject(result);
		System.out.println(jsonObj);
		String typeName = "media_id";
		if(!"image".equals(type)){
			typeName = type + "_media_id";
		}
		String mediaId = jsonObj.getString(typeName);
		return mediaId;
	}
	
	/**
	 * 获取access_token
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public static AccessToken getAccessToken() throws Exception, Exception {
		AccessToken token = new AccessToken();
		String url = ACCESS_TOKEN_URL.replace("APPID",APPID).replace("APPSECRET",APPSECRET);
		JSONObject jsonObject = doGetStr(url);
		
		if(jsonObject != null) {
			token.setToken(jsonObject.getString("access_token"));
			token.setExpiresIn(jsonObject.getInt("expires_in"));
		}
		return token;
	}
	
	/**
	 * 组装菜单
	 * @return
	 */
	public static Menu initMenu() {
		Menu menu = new Menu();
		//主菜单1
		Button button11 = new Button();
		button11.setName("我");
		//主菜单1的五个子菜单
		ClickButton button12 = new ClickButton();
		button12.setName("免费通知提醒");
		button12.setType("click");
		button12.setKey("12");
		
		ClickButton button13 = new ClickButton();
		button13.setName("一卡通余额查询");
		button13.setType("click");
		button13.setKey("13");
		
		ClickButton button14 = new ClickButton();
		button14.setName("信用卡账单查询");
		button14.setType("click");
		button14.setKey("14");
		
		ClickButton button15 = new ClickButton();
		button15.setName("我要办卡/贷款");
		button15.setType("click");
		button15.setKey("15");
		
		ClickButton button16 = new ClickButton();
		button16.setName("我要汇款/结汇");
		button16.setType("click");
		button16.setKey("16");
		
		button11.setSub_button(new Button[]{button12,button13,button14,button15,button16});
		
//		button11.setType("click");
//		button11.setKey("11");
		
		
		//第二个主菜单 
		Button button = new Button();
		button.setName("发现");
		
		//第二个主菜单的四个子菜单
		ClickButton button31 = new ClickButton();
		button31.setName("朝朝盈");
		button31.setType("scancode_push");
		button31.setKey("31");
		
		ClickButton button32 = new ClickButton();
		button32.setName("为小招点赞");
		button32.setType("location_select");
		button32.setKey("32");
		
		ClickButton button33 = new ClickButton();
		button33.setName("本地特惠");
		button33.setType("location_select");
		button33.setKey("33");
		
		ClickButton button34 = new ClickButton();
		button34.setName("周边网点");
		button34.setType("location_select");
		button34.setKey("34");
		
		
		button.setSub_button(new Button[]{button31,button32,button33,button34});
		
		
		//第三个主菜单
		ViewButton button21 = new ViewButton();
		button21.setName("无卡取款");
		button21.setType("view");
		button21.setUrl("http://www.baidu.com");
		
		menu.setButton(new Button[]{button11,button,button21});
//		menu.setButton(new Button[]{button21});
		return menu;
	}
	
	public static int createMenu(String token,String menu) throws Exception {
		int result = 0;
		String url = CREATE_MENU_URL.replace("ACCESS_TOKEN",token);
		JSONObject jsonObject = doPostStr(url,menu);
		if(jsonObject != null) {
			result = jsonObject.getInt("errcode");
		}
		return result;
	}
}
