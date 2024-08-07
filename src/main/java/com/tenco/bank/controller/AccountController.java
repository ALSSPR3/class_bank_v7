package com.tenco.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedExeception;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;

import jakarta.servlet.http.HttpSession;

@Controller // IoC 대상 (싱글톤으로 관리)
@RequestMapping("/account")
public class AccountController {

	private final HttpSession session;
	private final AccountService accountService;

	public AccountController(HttpSession session, AccountService accountService) {
		this.session = session;
		this.accountService = accountService;
	}

	/**
	 * 계좌 생성 페이지 요청 주소 설계: http://localhost:8080/account/save
	 * 
	 * @return
	 */
	@GetMapping("/save")
	public String savePage() {

		// 1. 인증 검사가 필요(account 전체가 필요함)
		User principal = (User) session.getAttribute("principal");
		if (principal == null) {
			throw new UnAuthorizedExeception("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		return "account/save";
	}

	/**
	 * 계좌 생성 요청 처리
	 * 
	 * @param dto
	 * @return
	 */
	@PostMapping("/save")
	public String saveProc(SaveDTO dto) {
		// 1. form 데이터 추출(파싱 전략)
		// 2. 인증 검사
		// 3. 유효성 검사
		// 4. 서비스 호출
		User principal = (User) session.getAttribute("principal");
		if(principal == null) {
			throw new UnAuthorizedExeception("인증된 사용자가 아닙니다.", HttpStatus.UNAUTHORIZED);
		}
		if(dto.getNumber() == null || dto.getNumber().isEmpty()) {
			throw new DataDeliveryException("계좌 번호를 입력하세요.", HttpStatus.BAD_REQUEST);
		} else if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new DataDeliveryException("계좌 비밀 번호를 입력하세요.", HttpStatus.BAD_REQUEST);
		} else if(dto.getBalance() == null || dto.getBalance() <= 0) {
			throw new DataDeliveryException("계좌 잔액을 입력하세요.", HttpStatus.BAD_REQUEST);
		}
		
		accountService.createAccount(dto, principal.getId());
		
		return "redirect:/index";
	}

	@GetMapping("/list")
	public String listPage() {
		return "account/list";
	}

}
