package com.tenco.bank.service;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.SaveDTO;
import com.tenco.bank.dto.TransferDTO;
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
		if (rowResultCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 1. 계좌 존재 여부 확인 -- select
	// 2. 입금 처리
	// 3. history에 저장
	// 4. 트랜젝션 처리
	@Transactional
	public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
		Account accountEntitiy = accountRepository.findByNumber(dto.getDAccountNumber());
		if (accountEntitiy == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		accountEntitiy.checkOwner(principalId);
		accountEntitiy.deposit(dto.getAmount());

		accountRepository.updateById(accountEntitiy);

		History history = new History();
		history.setAmount(dto.getAmount());
		history.setWBalance(null);
		history.setDBalance(accountEntitiy.getBalance());
		history.setWAccountId(null);
		history.setDAccountId(accountEntitiy.getId());

		int rowCount = historyRepository.insert(history);
		if (rowCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// 이체 기능 만들기
	// 1. 출금 계좌 존재 여부 확인 -- select
	// 2. 입금 계좌 존재 여부 확인 -- select
	// 3. 출금 계좌 소유자 확인 -- 객체 상태값과 세션 id 비교하기
	// 4. 출금 계좌 비밀번호 확인 -- 객체 상태값과 dto 비교
	// 5. 출금 계좌 잔액 확인 -- 객체 상태값과 dto 비교
	// 6. 입금 계좌 객체 상태값 변경 처리 (거래금액 증가)
	// 7. 입급 계좌 update 처리
	// 8. 출금 계좌 객체 상태값 변경 처리 (잔액 - 거래금액)
	// 9. 출금 계좌 update 처리
	// 10. 거래 내역 등록 처리 (history)
	// 11. 트랜젝션 처리
	@Transactional
	public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
		Account wAccountEntitiy = accountRepository.findByNumber(dto.getWAccountNumber());
		Account dAccountEntitiy = accountRepository.findByNumber(dto.getDAccountNumber());
		if (wAccountEntitiy == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}
		if (dAccountEntitiy == null) {
			throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
		}

		wAccountEntitiy.checkOwner(principalId);
		wAccountEntitiy.checkPassword(dto.getWAccountPassword());
		wAccountEntitiy.checkBalance(dto.getAmount());

		dAccountEntitiy.deposit(dto.getAmount());
		wAccountEntitiy.withdraw(dto.getAmount());

		int resultRowCountWithdraw = accountRepository.updateById(dAccountEntitiy);
		int resultRowCountDeposit = accountRepository.updateById(wAccountEntitiy);

		if (resultRowCountWithdraw != 1 && resultRowCountDeposit != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		History history = History.builder().amount(dto.getAmount()).wAccountId(wAccountEntitiy.getId())
				.dAccountId(dAccountEntitiy.getId()).wBalance(wAccountEntitiy.getBalance())
				.dBalance(dAccountEntitiy.getBalance()).build();
		int rowCount = historyRepository.insert(history);
		if (rowCount != 1) {
			throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
