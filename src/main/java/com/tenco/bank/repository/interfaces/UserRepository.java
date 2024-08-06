package com.tenco.bank.repository.interfaces;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tenco.bank.repository.model.User;

// MyBatis 설정 확인

// UserRepository 인터페이스와 user.xml 파일을 매칭 시킨다.
@Mapper // 반드시 선언을 해야 동작한다.
public interface UserRepository {
	public int insert(User user);

	public int updateById(User user);

	public int deleteById(Integer Id);

	public User findById(Integer Id);

	public List<User> findAll();

}
