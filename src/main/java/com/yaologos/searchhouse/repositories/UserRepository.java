package com.yaologos.searchhouse.repositories;

import com.yaologos.searchhouse.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * simple-elasticsearch-demo
 * Description:
 * User: 张普
 * Date: 2020-07-24
 * Time: 11:16
 */
public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByName(String name);
}