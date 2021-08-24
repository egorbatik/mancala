package com.egorbatik.mancala.service;

import java.security.InvalidParameterException;
import java.util.Optional;

import com.egorbatik.mancala.model.Board;
import com.egorbatik.mancala.model.PlayerType;

public interface BoardService {

	Board retriveNewBoard();

	Board retrieveBoard(final Long id);
	
	Board getBoard(final Optional<Long> id);

	PlayerType resolvePlayer(Optional<String> player) throws InvalidParameterException;

	Board applyGame(final Long boardId,final PlayerType player,final Integer house) throws InvalidParameterException;

}