package com.tenco.bank.handler.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UnAuthorizedExeception extends RuntimeException {

	private HttpStatus status;
	
	// throw UnAuthorizedExecption
	public UnAuthorizedExeception(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}
	
}
