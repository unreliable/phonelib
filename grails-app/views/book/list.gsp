
<%@ page import="phonelibv2.Book"%>
<!doctype html>

<html>
<head>
<meta name="layout" content="main">
<g:set var="entityName"
	value="${message(code: 'book.label', default: 'Book')}" />
<title><g:message code="default.list.label" args="[entityName]" /></title>

<script type="text/javascript" src="/phonelibV2/js/douban_api.js"></script>
<style type="text/css">
.category {
	font-size: 14px;
	line-height: 40px;
	list-style-type: none;
	background: #EEEEEE;
	font-size: 14px;
	height: 38px;
	line-height: 40px;
	margin-left: 6px;
	margin-top: 1px;
	text-indent: 36px;
	width: 160px;
}
</style>
</head>
<body>




	<div>
		<div class="span3">
			<ul>
				<li class="nav-header"><h3>图书类别</h3></li>

				<!-- 分类加载-->
				<li class="categpry"><g:link url="/phonelibV2/book/list">全部</g:link>
				</li>
				<g:each in="${categoryInstanceList}" status="i"
					var="categoryInstance">
					<div>
						<li class="category"><g:link controller="book"
								action="category" id="${categoryInstance.id}">
								${categoryInstance.cname}
							</g:link></li>

					</div>
				</g:each>
			</ul>
		</div>

		<!-- end book category -->

		<div class="span9">

			<div>
		
	<shiro:hasRole name="ROLE_ADMIN">
				<g:link url="/phonelibV2/bgindex.gsp">管理员入口</g:link><br/>
	</shiro:hasRole>

			
 
			搜索你想要的书
				
				<g:form class="form-search" action="search">
				<!-- 
					<g:textField name="bookName" value="书名、ISBN"/>
					<g:submitButton type="submit" name="搜索" class="btn" />
				 -->
				<div class="input-group input-group-lg">
 				 
  			<input type="text" name="bookName"  class="form-control" placeholder="书名、ISBN">
  			<g:submitButton type="submit" name="搜索" class="btn" />
				</div>
				</g:form>
				<!-- book list -->
				<h1>图书列表</h1>


			</div>

			<div id="list-book" class="content scaffold-list" role="main">
				<g:if test="${flash.message}">
					<div class="message" role="status">
						${flash.message}
					</div>
				</g:if>


				<ul class="thumbnails">

					<g:each in="${bookInstanceList}" status="i" var="bookInstance">

						<li style="margin: 10px 7px 5px 7px;"><a class="thumbnail"
							data-toggle="modal" data-target="#myModal${bookInstance.isbn13}"
							href="javascript:">
								<div id=${bookInstance.isbn13}.img></div>
								<div class="caption" id=${bookInstance.isbn13}.title></div>

						</a>
							<div class="modal hide fade" id="myModal${bookInstance.isbn13}"
								tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
								aria-hidden="true">
								<div class="modal-dialog">
									<div class="modal-content">
										<div class="modal-header">
											<button type="button" class="close" data-dismiss="modal"
												aria-hidden="true">&times;</button>
											<h4 class="modal-title" id="myModalLabel">
												<div id="${bookInstance.isbn13}.title.dialog"></div>
											</h4>
										</div>
										<div class="modal-body">
											<div class="row-fluid">
												<div class="span3">
													<div id=${bookInstance.isbn13}.img.dialog></div>
												</div>
												<div class="span9">
													拥有者:<g:each in="${(bookInstance.own.user)}" var="userInstance">
																<g:link action = "searchByUser" id ="${userInstance.id }">${ userInstance.username}
																</g:link>
												 	    	</g:each>
													<br/>
													分类：${fieldValue(bean: bookInstance, field: "category.cname")}</br>
													isbn:${bookInstance.isbn13}
													<div id=${bookInstance.isbn13}.author.dialog></div>
													<div id=${bookInstance.isbn13}.publisher.dialog></div>
													<div id=${bookInstance.isbn13}.pubdate.dialog></div>
												</div>
											</div>
											<div class="span12">
												<h5 style="color: #888888;">--内容简介--</h5>
												<div id=${bookInstance.isbn13}.summary></div>
											</div>
										</div>
										<div class="modal-footer">
											<button type="button" class="btn btn-default"
												data-dismiss="modal">关闭</button>
											<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">借书 <span class="caret"></span></button>
										        <ul class="dropdown-menu pull-right">
										          <li><a href="#">选择拥有者</a></li>
										          <g:each in="${(bookInstance.own.user)}" var="userInstance">
														<li><g:link controller="borrow" action = "borrowBookAdd" params ="[userId:userInstance.id,bookId:bookInstance.id]">${ userInstance.username}
														</g:link></li>
												  </g:each>
										        </ul>
										</div>
									</div>
									<!-- /.modal-content -->
								</div>
								<!-- /.modal-dialog -->
							</div>
							<!-- /.modal --></li>

						<script type="text/javascript">
					DOUBAN.apikey = 
						DOUBAN.getISBNBook({
						    isbn:${bookInstance.isbn13},
						    callback:function(re){
						        //alert(re.title);
						        var Title=re.title;
						        var bookTitle=re.title;
						        var len=Title.length;
						        if(len>6){
						        	var bookTitle = Title.slice(0, 5);
							        }
						       document.getElementById(re.isbn13+'.title').innerHTML=bookTitle; 
						       document.getElementById(re.isbn13+'.img').innerHTML="<img style=\"height:160px; width:120px;\" src="+re.images.medium+">";
						       document.getElementById(re.isbn13+'.title.dialog').innerHTML=Title;
						       document.getElementById(re.isbn13+'.img.dialog').innerHTML="<img style=\"height:160px; width:120px;\" src="+re.images.medium+">";
						       document.getElementById(re.isbn13+'.author.dialog').innerHTML='作者：'+re.author;
						       document.getElementById(re.isbn13+'.publisher.dialog').innerHTML='出版社：'+re.publisher;
						       document.getElementById(re.isbn13+'.pubdate.dialog').innerHTML='出版时间：'+re.pubdate;
						       document.getElementById(re.isbn13+'.summary').innerHTML=re.summary;
						    }
						})
					</script>

					</g:each>
				</ul>
				<!-- end booklist -->
				<div class="pagination">
					<g:paginate total="${bookInstanceTotal}" />
				</div>
			</div>
		</div>


	</div>
</body>
</html>
