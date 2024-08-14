<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- header.jsp -->
<%@include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of contnet.jsp(xxx.jsp) -->

<div class="col-sm-8">
	<h2>회원 가입</h2>
	<h5>Bank App에 오신걸 환영합니다.</h5>

	<form action="/user/sign-up" method="post" enctype="multipart/form-data">
		<div class="form-group">
			<label for="username">username:</label> <input type="text" class="form-control" placeholder="Enter username" id="username" name="username" value="야스오1">
		</div>
		<div class="form-group">
			<label for="pwd">Password:</label> <input type="password" class="form-control" placeholder="Enter password" id="pwd" name="password" value="asd123">
		</div>
		<div class="form-group">
			<label for="fullname">fullname:</label> <input type="text" class="form-control" placeholder="Enter fullname" id="fullname" name="fullname" value="fullname">
		</div>
		<div class="custom-file">
			<input type="file" class="custom-file-input" id="mFile" name="mFile"> <label class="custom-file-label" for="mFile">파일 선택</label>
		</div>
		<div class="d-flex justify-content-end">
			<button type="submit" class="btn btn-primary mt-md-4">회원가입</button>
		</div>
		<div>
			<a href="https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=85e512a7442b5f870c18abd789366108&redirect_uri=http://localhost:8080/user/kakao">
				<img alt="" src="/images/kakao_login_medium.png">
			</a>
		</div>
	</form>

	<script>
		// Add the following code if you want the name of the file appear on select
		$(".custom-file-input").on(
				"change",
				function() {
					let fileName = $(this).val().split("\\").pop();
					$(this).siblings(".custom-file-label").addClass("selected")
							.html(fileName);
				});
	</script>

</div>
</div>
</div>

<!-- end of contnet.jsp(xxx.jsp) -->
<!-- footer.jsp -->
<%@include file="/WEB-INF/view/layout/footer.jsp"%>
