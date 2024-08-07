package com.tenco.bank.service;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.model.User;

import jakarta.servlet.http.HttpSession;

@Service
public class AccountService {

	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	/**
	 * 계좌 생성 기능
	 * 
	 * @param dto
	 * @param userId
	 */
	// 트랜 잭션 처리
	@Transactional
	public void createAccount(SaveDTO dto, Integer principalId) {
		int result = 0;
		try {
			result = accountRepository.insert(dto.toAccount(principalId));
		} catch (DataAccessException e) {
			throw new DataDeliveryException("잘못된 요청 입니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException("알 수 없는 오류 발생", HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result == 0) {
			throw new DataDeliveryException("정상 처리되지 않았습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
