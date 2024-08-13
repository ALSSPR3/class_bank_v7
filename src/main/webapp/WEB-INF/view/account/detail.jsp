<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp  -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of content.jsp(xxx.jsp)   -->
<div class="col-sm-8">
	<h2>계좌 상세 보기(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>

	<div class="bg-light p-md-5">
		<div class="user--box">
			${principal.username}님 계좌<br> 계좌번호 : ${account.number}<br> 잔액 : ${account.formatKoreanWon(account.balance)}
		</div>
		<br>
		<c:choose>
		<c:when test="${not empty historyList}">
			<div>
				<a href="/account/detail/${account.id}?type=all" class="btn btn-outline-primary">전체</a>&nbsp; <a href="/account/detail/${account.id}?type=deposit"
					class="btn btn-outline-primary">입금</a>&nbsp; <a href="/account/detail/${account.id}?type=withdrawal" class="btn btn-outline-primary">출금</a>&nbsp;
			</div>
			<br>
			<div>
				<table class="table table-striped">
					<thead>
						<tr>
							<th>날짜</th>
							<th>보낸이</th>
							<th>받은이</th>
							<th>입출금 금액</th>
							<th>계좌잔액</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="historyAccount" items="${historyList}">
							<tr>
								<td>${historyAccount.timestampToString(historyAccount.createdAt)}</td>
								<td>${historyAccount.sender}</td>
								<td>${historyAccount.receiver}</td>
								<td>${historyAccount.formatKoreanWon(historyAccount.amount)}</td>
								<td>${historyAccount.formatKoreanWon(historyAccount.balance)}</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
				<br>
				<!-- Pagination -->
				<div class="d-flex justify-content-center">
					<ul class="pagination">
						<!-- Previous Page Link -->
						<li class="page-item  <c:if test='${currentPage == 1}'>disabled</c:if>"><a class="page-link" href="?type=${type}&page=${currentPage - 1}&size=${size}">&#12298;</a></li>

						<!-- Page Numbers -->
						<!-- [Previous]  1 2 3 4 5 6 7 8   [Next] -->
						<c:forEach begin="1" end="${totalPages}" var="page">
							<li class="page-item  <c:if test='${page == currentPage}'>active </c:if>"><a class="page-link" href="?type=${type}&page=${page}&size=${size}">${page}</a></li>
						</c:forEach>

						<!-- Next Page Link  -->
						<li class="page-item <c:if test='${currentPage == totalPages}'>disabled</c:if>"><a class="page-link" href="?type=${type}&page=${currentPage + 1}&size=${size}">&#10219;</a>
						</li>
					</ul>

				</div>
			</div>
			</c:when>
			<c:otherwise>
				<h3>거래 내역이 없습니다.</h3>
			</c:otherwise>
		</c:choose>
	</div>

</div>
<!-- end of col-sm-8  -->
</div>
</div>
<!-- end of content.jsp(xxx.jsp)   -->

<!-- footer.jsp  -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>