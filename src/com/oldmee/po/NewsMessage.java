package com.oldmee.po;

import java.util.List;

public class NewsMessage extends BaseMessage {
	private int ArticleCount;
	private List<News> Articles;
	public List<News> getArticles() {
		return Articles;
	}
	public void setArticles(List<News> articles) {
		Articles = articles;
	}
	public void setArticleCount(int articleCount) {
		ArticleCount = articleCount;
	}
	public int getArticleCount() {
		return ArticleCount;
	}
	
}
