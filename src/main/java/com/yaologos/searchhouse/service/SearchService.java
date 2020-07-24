package com.yaologos.searchhouse.service;

import java.util.List;

public interface SearchService {
    /**
     *
     * @param username
     * @return
     */
    boolean index(String username);

    /**
     *
     * @param username
     * @return
     */
    boolean remove(String username);

    /**
     *
     * @param keyword
     * @return
     */
    List<String> query(String keyword);
}
