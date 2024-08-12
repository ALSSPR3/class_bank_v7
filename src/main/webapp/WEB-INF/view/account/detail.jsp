<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp -->
<%@include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of contnet.jsp(xxx.jsp) -->

<div class="col-sm-8">
	<h2>계좌 상세 보기</h2>
	<h5>Bank App에 오신걸 환영합니다</h5>

	<div class="bg-light p-md-5 p-75">
		<div class="user--box">
			${principal.username}님 계좌 <br> 계좌 번호 : ${account.number}<br> 
			잔액 : <fmt:formatNumber value="${account.balance}"/>원
		</div>
		<br>
		<div>
			<a href="/account/detail/${account.id}?type=all" class="btn btn-outline-primary">전체</a>&nbsp <a href="/account/detail/${account.id}?type=deposit" class="btn btn-outline-primary">입금</a>&nbsp
			<a href="/account/detail/${account.id}?type=withdrawal" class="btn btn-outline-primary">출금</a>&nbsp
		</div>
		<br>
		<c:choose>
			<c:when test="${not empty historyList}">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>날짜</th>
							<th>보낸이</th>
							<th>받은이</th>
							<th>입출금 금액</th>
							<th>계좌 잔액</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="historyList" items="${historyList}">
							<tr>
								<td><fmt:formatDate value="${historyList.createdAt}" type="both"/></td>
								<td>${historyList.sender}</td>
								<td>${historyList.receiver}</td>
								<td><fmt:formatNumber value="${historyList.amount}"/>원</td>
								<td><fmt:formatNumber value="${historyList.balance}"/>원</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</c:when>
			<c:otherwise>
				<p>입 출금 내역이 없습니다.</p>
			</c:otherwise>
		</c:choose>
	</div>


</div>
</div>
</div>

<!-- end of contnet.jsp(xxx.jsp) -->
<!-- footer.jsp -->
<%@include file="/WEB-INF/view/layout/footer.jsp"%>
