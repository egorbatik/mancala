package com.egorbatik.mancala.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.egorbatik.mancala.model.Board;

@Repository
@Component
public interface BoardRepository extends CrudRepository<Board, Long> {

}
