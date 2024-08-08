package com.tenco.bank.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;

import jakarta.servlet.http.HttpSession;

@Service
public class AccountService {

	private final AccountRepository accountRepository;
	private final HistoryRepository historyRepository;

	public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
		this.accountRepository = accountRepository;
		this.historyRepository = historyRepository;
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
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}
		if (result == 0) {
			throw new DataDeliveryException(Define.FAIL_TO_CREATE_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 계좌 목록 조회
	 * 
	 * @param id
	 */
	public List<Account> readAccounListById(Integer userId) {
		List<Account> accountListEntitiy = null;
		try {
			accountListEntitiy = accountRepository.findByUserId(userId);
		} catch (DataAccessException e) {
			throw new DataDeliveryException(Define.INVALID_INPUT, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			throw new RedirectException(Define.UNKNOWN, HttpStatus.SERVICE_UNAVAILABLE);
		}

		return accountListEntitiy;
	}

	// 한번에 모든 기능을 생각하는것은 어려움
	// 1. 계좌 존재 여부 확인 -- select
	// 2. 본인 계좌 여부 확인 -- 객체 상태값에서 비교
	// 3. 계좌 비밀번호 확인 -- 객체 상태값에서 일치 여부 확인
	// 4. 잔액 여부 확인 -- 객체 상태값에서 확인
	// 5. 출금 처리 -- update
	// 6. 거래 내역 등록 -- insert(history)
	// 7. 트랜잭션 처리
	@Transactional
	public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
		// 1.
		Account accountEntitiy = accountRepository.findByNumber(dto.getWAccountNumber());
		if (accountEntitiy == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		// 2.
		accountEntitiy.checkOwner(principalId);
		// 3.
		accountEntitiy.checkPassword(dto.getWAccountPassword());
		// 4.
		accountEntitiy.checkBalance(dto.getAmount());
		// 5.
		// accountEntitiy 객체의 잔액을 변경하고 업데이트 처리
		accountEntitiy.withdraw(dto.getAmount());
		// update 처리
		accountRepository.updateById(accountEntitiy);
		// 6. - 거래 내역 등록
		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(accountEntitiy.getBalance());
		history.setDBalance(null);
		history.setWAccountId(accountEntitiy.getId());
		history.setDAccountId(null);

		int rowResultCount = historyRepository.insert(history);
		if(rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
