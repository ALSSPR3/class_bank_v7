package com.tenco.bank.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller // IoC의 대상(싱글톤 패턴 으로 관리됨)
@RequestMapping("/user") // 대문 처리
@RequiredArgsConstructor
public class UserController {

	@Autowired // 노란색 경고는 사용할 필요 없음 - 가독성을 위해서 선언해도 됨
	private final UserService userService;
	private final HttpSession session;

	/**
	 * 회원 가입 페이지 요청 주소 설계 : http://localhost:8080/user/sign-up
	 * 
	 * @return signUp.jsp
	 */
	@GetMapping("/sign-up")
	public String signUpPage() {
		return "user/signUp";
	}

	/**
	 * 회원 가입 로직 처리 요청
	 * 
	 * @param dto 주소 설계 : http://localhost:8080/user/sign-up
	 * @return
	 */
	@PostMapping("/sign-up")
	public String signUpProc(SignUpDTO dto) {
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		}

		if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}

		if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_FULLNAME, HttpStatus.BAD_REQUEST);
		}

		// 서비스 객체로 전달
		userService.createUser(dto);

		// TODO - 추후 수정
		return "redirect:/user/sign-in";
	}

	/**
	 * 로그인 페이지 요청 주소 설계 : http://localhost:8080/user/sign-ip
	 * 
	 * @return
	 */
	@GetMapping("/sign-in")
	public String signInPage() {
		// prefix: /WEB-INF/view/
		// return: user/signIn
		// suffix: .jsp
		return "user/signIn";
	}

	/**
	 * 로그인 요청 처리 주소 설계 : http://localhost:8080/user/sign-ip
	 * 
	 * @return
	 */
	@PostMapping("/sign-in")
	public String signInProc(SignInDTO dto) {

		// 1. 인증 검사 x
		// 2. 유효성 검사
		if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
		} else if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
		}
		// 서비스 호출
		User principal = userService.readUser(dto);

		// 세션 메모리에 등록 처리
		session.setAttribute(Define.PRINCIPAL, principal);

		// 새로운 페이지로 이동 처리
		// TODO - 계좌 목록 페이지 이동처리 예정
		return "redirect:/account/list";
	}

	/**
	 * 로그아웃 요청 처리
	 * 
	 * @return
	 */
	@GetMapping("/logout")
	public String logout() {
		session.invalidate(); // 로그아웃 됨
		return "redirect:/user/sign-in";
	}

	@GetMapping("/kakao")
	@ResponseBody
	public ResponseEntity<?> kakaoSingIn(@RequestParam("code") String code) {

		System.out.println("Code : " + code);
		
		URI uri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com/oauth/token").path("/" + code).build()
				.toUri();

		RestTemplate restTemplate1 = new RestTemplate();

		// 1. 헤더 구성
		HttpHeaders headers = new HttpHeaders();
		// 'Content-type': 'application/json; charset=UTF-8'
		headers.add("Content-type", "application/json; charset=UTF-8");

		// 2. 바디 구성
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		// 3. 헤더와 바디 결합 --> HttpEntitiy Object
		HttpEntity<MultiValueMap<String, String>> requestEntitiy = new HttpEntity<>(params, headers);

		// 4. RestTemplate 을 활용해서 HTTP 통신 요청
		ResponseEntity<String> responseEntity = restTemplate1.exchange(uri, HttpMethod.POST, requestEntitiy,
				String.class);

		System.out.println("response Header : " + responseEntity.getHeaders());
		System.out.println("response Body : " + responseEntity.getBody());

		return null;
	}
}
